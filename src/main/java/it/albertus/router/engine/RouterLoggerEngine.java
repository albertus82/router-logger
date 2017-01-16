package it.albertus.router.engine;

import java.io.IOException;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import it.albertus.jface.JFaceMessages;
import it.albertus.router.email.ThresholdsEmailSender;
import it.albertus.router.mqtt.RouterLoggerMqttClient;
import it.albertus.router.reader.Reader;
import it.albertus.router.resources.Messages;
import it.albertus.router.server.WebServer;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.router.util.LoggerFactory;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.Writer;
import it.albertus.util.ConfigurationException;
import it.albertus.util.Console;
import it.albertus.util.StringUtils;
import it.albertus.util.SystemConsole;
import it.albertus.util.Version;

public abstract class RouterLoggerEngine {

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerEngine.class);

	public static class Defaults {
		public static final int ITERATIONS = 0;
		public static final boolean CLOSE_WHEN_FINISHED = false;
		public static final long INTERVAL_FAST_IN_MILLIS = 1000L;
		public static final long INTERVAL_NORMAL_IN_MILLIS = 5000L;
		public static final long HYSTERESIS_IN_MILLIS = 10000L;
		public static final int RETRIES = 3;
		public static final long RETRY_INTERVAL_IN_MILLIS = 30000L;
		public static final boolean CONSOLE_SHOW_CONFIGURATION = false;
		public static final boolean THRESHOLDS_EMAIL = false;
		public static final boolean LOG_CONNECTED = false;
		public static final boolean WAIT_DISCONNECTED = false;
		public static final boolean WAIT_DISCONNECTED_INTERVAL_THRESHOLD = true;
		public static final long WAIT_DISCONNECTED_INTERVAL_THRESHOLD_IN_MILLIS = INTERVAL_FAST_IN_MILLIS;
		public static final Class<? extends Writer> WRITER_CLASS = CsvWriter.class;

		protected Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected static final int FIRST_ITERATION = 1;

	private static final String CFG_KEY_LOGGER_INTERVAL_NORMAL_MS = "logger.interval.normal.ms";

	private static final String MSG_KEY_ERR_CONFIGURATION_INVALID = "err.configuration.invalid";

	protected final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	protected final WebServer httpServer = WebServer.getInstance();
	protected final RouterLoggerMqttClient mqttClient = RouterLoggerMqttClient.getInstance();
	protected final Console out = SystemConsole.getInstance();

	private Reader reader;
	private Writer writer;

	protected Thread pollingThread;
	private volatile boolean interruptible = false;
	protected volatile boolean exit = false;
	private Thread shutdownHook;

	private RouterData currentData;
	private ThresholdsReached currentThresholdsReached = new ThresholdsReached(Collections.<Threshold, String> emptyMap(), new Date());
	private RouterLoggerStatus currentStatus = new RouterLoggerStatus(Status.STARTING);
	private RouterLoggerStatus previousStatus = null;
	private long waitTimeInMillis = configuration.getLong(CFG_KEY_LOGGER_INTERVAL_NORMAL_MS, Defaults.INTERVAL_NORMAL_IN_MILLIS);

	private volatile int iteration = FIRST_ITERATION;
	private boolean connected = false;
	private boolean loggedIn = false;

	public RouterLoggerEngine() {
		// Inizializzazione dell'HttpServer...
		httpServer.init(this);
	}

	public RouterLoggerStatus getCurrentStatus() {
		return currentStatus;
	}

	public RouterLoggerStatus getPreviousStatus() {
		return previousStatus;
	}

	protected boolean setStatus(final Status newStatus) {
		final boolean update = !currentStatus.getStatus().equals(newStatus);
		if (update) {
			previousStatus = currentStatus;
			currentStatus = new RouterLoggerStatus(newStatus);
			mqttClient.publishStatus(currentStatus);
		}
		return update;
	}

	public long getWaitTimeInMillis() {
		return waitTimeInMillis;
	}

	protected Reader createReader() {
		final String configurationKey = "reader.class.name";
		final String readerClassName = getReaderClassName(configuration.getString(configurationKey));

		final Reader rdr;
		try {
			rdr = (Reader) Class.forName(readerClassName).newInstance();
		}
		catch (final Exception e) {
			throw new ConfigurationException(JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_INVALID, configurationKey) + ' ' + JFaceMessages.get("err.configuration.review", configuration.getFileName()), e, configurationKey);
		}
		catch (final LinkageError le) {
			throw new ConfigurationException(JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_INVALID, configurationKey) + ' ' + JFaceMessages.get("err.configuration.review", configuration.getFileName()), le, configurationKey);
		}
		return rdr;
	}

	protected Writer createWriter() {
		final String configurationKey = "writer.class.name";
		final String writerClassName = getWriterClassName(configuration.getString(configurationKey, RouterLoggerEngine.Defaults.WRITER_CLASS.getName()));

		final Writer wrt;
		try {
			wrt = (Writer) Class.forName(writerClassName).newInstance();
		}
		catch (final Exception e) {
			throw new ConfigurationException(JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_INVALID, configurationKey) + ' ' + JFaceMessages.get("err.configuration.review", configuration.getFileName()), e, configurationKey);
		}
		catch (final LinkageError le) {
			throw new ConfigurationException(JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_INVALID, configurationKey) + ' ' + JFaceMessages.get("err.configuration.review", configuration.getFileName()), le, configurationKey);
		}
		return wrt;
	}

	public static String getReaderClassName(final String configuredClassName) {
		String readerClassName;
		try {
			readerClassName = StringUtils.trimToEmpty(configuredClassName);
			Class.forName(readerClassName, false, RouterLoggerEngine.class.getClassLoader());
		}
		catch (final Exception e) {
			if (logger.isDebugEnabled()) {
				logger.log(e, Destination.CONSOLE, Destination.FILE);
			}
			readerClassName = Reader.class.getPackage().getName() + '.' + configuredClassName;
		}
		catch (final LinkageError le) {
			if (logger.isDebugEnabled()) {
				logger.log(le, Destination.CONSOLE, Destination.FILE);
			}
			readerClassName = Reader.class.getPackage().getName() + '.' + configuredClassName;
		}
		return readerClassName;
	}

	public static String getWriterClassName(final String configuredClassName) {
		String writerClassName;
		try {
			writerClassName = StringUtils.trimToEmpty(configuredClassName);
			Class.forName(writerClassName, false, RouterLoggerEngine.class.getClassLoader());
		}
		catch (final Exception e) {
			if (logger.isDebugEnabled()) {
				logger.log(e, Destination.CONSOLE, Destination.FILE);
			}
			writerClassName = Writer.class.getPackage().getName() + '.' + configuredClassName;
		}
		catch (final LinkageError le) {
			if (logger.isDebugEnabled()) {
				logger.log(le, Destination.CONSOLE, Destination.FILE);
			}
			writerClassName = Writer.class.getPackage().getName() + '.' + configuredClassName;
		}
		return writerClassName;
	}

	protected void beforeConnect() {
		printWelcome();
		setStatus(Status.STARTING);
		httpServer.start();
		initReaderAndWriter();
		printDeviceModel();
	}

	/** Registers a shutdown hook (which can detect CTRL+C press). */
	protected void addShutdownHook() {
		// Gestione chiusura console (CTRL+C)...
		shutdownHook = new Thread("shutdownHook") {
			@Override
			public void run() {
				if (reader != null) {
					setStatus(Status.DISCONNECTING);
					try {
						reader.disconnect();
						setStatus(Status.DISCONNECTED);
					}
					catch (final Exception e) {
						if (logger.isDebugEnabled()) {
							logger.log(e, Destination.CONSOLE, Destination.FILE);
						}
					}
				}
				release();
				stopNetworkServices();
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	/** Unregisters the shutdown hook. */
	protected void removeShutdownHook() {
		if (shutdownHook != null) {
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownHook);
			}
			catch (final Exception e) {
				if (logger.isDebugEnabled()) {
					logger.log(e, Destination.CONSOLE, Destination.FILE);
				}
			}
		}
	}

	protected void printGoodbye() {
		out.println(Messages.get("msg.bye"), true);
	}

	protected void outerLoop() {
		for (int index = 0; !exit; index++) {
			final int retries = configuration.getInt("logger.retry.count", Defaults.RETRIES);
			if (retries > 0 && index > retries) {
				break;
			}
			// Gestione riconnessione in caso di errore...
			if (index > 0) {
				setStatus(Status.RECONNECTING);
				final long retryIntervalInMillis = configuration.getLong("logger.retry.interval.ms", Defaults.RETRY_INTERVAL_IN_MILLIS);
				final StringBuilder message = new StringBuilder(Messages.get("msg.wait.reconnection"));
				if (retries > 0) {
					message.append(' ').append(Messages.get("msg.wait.reconnection.retry", index, retries));
				}
				message.append(' ').append(Messages.get("msg.wait.reconnection.time", retryIntervalInMillis));
				logger.log(message.toString(), Destination.CONSOLE);
				try {
					interruptible = true;
					Thread.sleep(retryIntervalInMillis);
				}
				catch (final InterruptedException ie) {
					// Se si chiude il programma mentre e' in attesa di riconnessione...
					if (logger.isDebugEnabled()) {
						logger.log(ie, Destination.CONSOLE, Destination.FILE);
					}
					exit = true;
					Thread.currentThread().interrupt();
					continue;
				}
				finally {
					interruptible = false;
				}
			}

			/* Avvio della procedura... */
			try {
				setStatus(Status.CONNECTING);
				interruptible = true;
				connected = reader.connect();
			}
			catch (RuntimeException re) {
				/* Configurazione non valida */
				logger.log(re);
				exit = true;
				setStatus(Status.ERROR);
				continue;
			}
			finally {
				interruptible = false;
			}

			// Log in...
			if (connected && !exit) {
				setStatus(Status.AUTHENTICATING);
				try {
					interruptible = true;
					loggedIn = reader.login(configuration.getString("router.username"), configuration.getCharArray("router.password"));
				}
				catch (Exception e) {
					logger.log(e);
				}
				finally {
					interruptible = false;
				}

				// Loop...
				if (loggedIn && !exit) {
					setStatus(Status.OK);
					if (configuration.getBoolean("reader.log.connected", Defaults.LOG_CONNECTED)) {
						logger.log(Messages.get("msg.reader.connected", reader.getDeviceModel()), Destination.FILE, Destination.EMAIL);
					}
					index = 0;
					try {
						innerLoop();
						exit = true; // Se non si sono verificati errori.
					}
					catch (final InterruptedException ie) {
						if (logger.isDebugEnabled()) {
							logger.log(ie, Destination.CONSOLE, Destination.FILE);
						}
						logger.log(Messages.get("msg.loop.interrupted"), Destination.CONSOLE);
						Thread.currentThread().interrupt();
					}
					catch (final IOException ioe) {
						if (!exit) {
							logger.log(ioe);
						}
					}
					catch (final Exception e) {
						logger.log(e);
					}
					finally {
						// In ogni caso si esegue la disconnessione dal server...
						try {
							reader.logout();
						}
						catch (Exception e) {
							if (!exit) {
								logger.log(e);
							}
						}
						reader.disconnect();
					}
				}
				else {
					if (loggedIn) { // Evidentemente exit == true!
						try {
							reader.logout();
						}
						catch (Exception e) {
							logger.log(e);
						}
					}
					// In caso di autenticazione fallita, si esce subito per evitare il blocco dell'account.
					else {
						exit = true;
						setStatus(Status.ERROR);
					}
					reader.disconnect();
				}
			}
			else {
				if (connected) { // Evidentemente exit == true!
					reader.disconnect();
				}
				else {
					setStatus(Status.ERROR);
				}
			}
		}

		release();
		if (!Status.ERROR.equals(currentStatus.getStatus())) {
			setStatus(Status.DISCONNECTED);
		}

		if (configuration.getBoolean("logger.close.when.finished", Defaults.CLOSE_WHEN_FINISHED) && iteration >= configuration.getInt("logger.iterations", Defaults.ITERATIONS)) {
			close();
		}
	}

	/** Prints a welcome message. */
	protected void printWelcome() {
		out.println(" ____             _            _");
		out.println("|  _ \\ ___  _   _| |_ ___ _ __| |    ___   __ _  __ _  ___ _ __");
		out.println("| |_) / _ \\| | | | __/ _ \\ '__| |   / _ \\ / _` |/ _` |/ _ \\ '__|");
		out.println("|  _ < (_) | |_| | ||  __/ |  | |__| (_) | (_| | (_| |  __/ |");
		out.println("|_| \\_\\___/ \\__,_|\\__\\___|_|  |_____\\___/ \\__, |\\__, |\\___|_|");
		out.println("                                          |___/ |___/");
		final Version version = Version.getInstance();
		out.println(Messages.get("msg.welcome", Messages.get("msg.application.name"), Messages.get("msg.version", version.getNumber(), version.getDate()), Messages.get("msg.website")));
		out.println();
		out.println(Messages.get("msg.startup.date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())));

		if (!configuration.getThresholds().isEmpty()) {
			out.println(Messages.get("msg.thresholds", configuration.getThresholds()));
		}
		if (configuration.getBoolean("console.show.configuration", Defaults.CONSOLE_SHOW_CONFIGURATION)) {
			out.println(Messages.get("msg.settings", configuration));
		}
	}

	/** Prints the device model name, if available. */
	protected void printDeviceModel() {
		if (reader != null && reader.getDeviceModel() != null && !reader.getDeviceModel().trim().isEmpty()) {
			out.println(Messages.get("msg.device.model", reader.getDeviceModel().trim()));
		}
		out.println();
	}

	protected void innerLoop() throws IOException, InterruptedException {
		long hysteresis = 0;

		// Iterazione...
		for (int iterations = configuration.getInt("logger.iterations", Defaults.ITERATIONS); (iterations <= 0 || iteration <= iterations) && !exit; iteration++) {
			final long timeBeforeRead = System.currentTimeMillis();

			// Reconnect if needed...
			if (!connected && !exit) {
				connected = reader.connect();
				if (!connected) { // Retry...
					throw new ConnectException(Messages.get("msg.reconnection.error"));
				}
			}
			if (!loggedIn && !exit) {
				try {
					loggedIn = reader.login(configuration.getString("router.username"), configuration.getCharArray("router.password"));
					if (!loggedIn) {
						break; // Exit immediately!
					}
				}
				catch (final IOException ioe) {
					logger.log(ioe);
					break; // Exit immediately!
				}
				catch (final RuntimeException re) {
					logger.log(re);
					break; // Exit immediately!
				}
			}

			if (exit) {
				break;
			}

			final Map<String, String> info = reader.readInfo();
			final long timeAfterRead = System.currentTimeMillis();
			currentData = new RouterData((int) (timeAfterRead - timeBeforeRead), info);

			writer.saveInfo(currentData);

			/* Impostazione stato di allerta e gestione isteresi... */
			final Map<Threshold, String> thresholdsReached = configuration.getThresholds().getReached(currentData);
			currentThresholdsReached = new ThresholdsReached(thresholdsReached, currentData.getTimestamp());

			boolean importantThresholdReached = false;
			for (final Threshold threshold : thresholdsReached.keySet()) {
				if (!threshold.isExcluded()) {
					importantThresholdReached = true;
					break;
				}
			}

			if (importantThresholdReached || System.currentTimeMillis() - hysteresis < configuration.getLong("logger.hysteresis.ms", Defaults.HYSTERESIS_IN_MILLIS)) {
				setStatus(Status.WARNING);
				if (importantThresholdReached) {
					hysteresis = System.currentTimeMillis();
					if (configuration.getBoolean("thresholds.email", Defaults.THRESHOLDS_EMAIL)) {
						ThresholdsEmailSender.getInstance().send(thresholdsReached, currentData);
					}
				}
			}
			else if (!thresholdsReached.isEmpty()) {
				setStatus(Status.INFO);
			}
			else {
				setStatus(Status.OK);
			}
			// Aggiorna l'interfaccia
			showInfo(currentData, thresholdsReached);

			// Pubblica via MQTT
			mqttClient.publishData(currentData);
			mqttClient.publishThresholds(currentThresholdsReached);

			if (exit) {
				break;
			}

			// All'ultimo giro non deve esserci il tempo di attesa tra le iterazioni.
			if (iterations <= 0 || iteration < iterations) {
				if (Status.WARNING.equals(currentStatus.getStatus())) {
					waitTimeInMillis = configuration.getLong("logger.interval.fast.ms", Defaults.INTERVAL_FAST_IN_MILLIS);
				}
				else {
					waitTimeInMillis = configuration.getLong(CFG_KEY_LOGGER_INTERVAL_NORMAL_MS, Defaults.INTERVAL_NORMAL_IN_MILLIS);
				}

				// Disconnect if requested...
				final boolean disconnectionRequested = configuration.getBoolean("reader.wait.disconnected", Defaults.WAIT_DISCONNECTED) && (!configuration.getBoolean("reader.wait.disconnected.interval.threshold", Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD) || waitTimeInMillis > configuration.getLong("reader.wait.disconnected.interval.threshold.ms", Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD_IN_MILLIS));
				if (disconnectionRequested) {
					reader.logout();
					loggedIn = false;
					reader.disconnect();
					connected = false;
					writer.release();
				}

				// Sottrazione dal tempo di attesa di quello trascorso durante la scrittura dei dati...
				final long adjustedWaitTimeInMillis = waitTimeInMillis - (System.currentTimeMillis() - timeAfterRead);

				if (adjustedWaitTimeInMillis > 0L) {
					if (disconnectionRequested) {
						final String formattedDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(new Date(System.currentTimeMillis() + adjustedWaitTimeInMillis));
						logger.log(Messages.get("msg.reconnection.info", formattedDate), Destination.CONSOLE);
					}
					interruptible = true;
					Thread.sleep(adjustedWaitTimeInMillis);
					interruptible = false;
				}
			}
			iterations = configuration.getInt("logger.iterations", Defaults.ITERATIONS);
		}
	}

	protected abstract void showInfo(RouterData info, Map<Threshold, String> thresholdsReached);

	/**
	 * Libera le risorse eventualmente allocate (file, connessioni a database,
	 * ecc.).
	 */
	protected void release() {
		try {
			reader.release();
		}
		catch (RuntimeException re) {
			if (logger.isDebugEnabled()) {
				logger.log(re, Destination.CONSOLE, Destination.FILE);
			}
		}
		try {
			writer.release();
		}
		catch (RuntimeException re) {
			if (logger.isDebugEnabled()) {
				logger.log(re, Destination.CONSOLE, Destination.FILE);
			}
		}
	}

	protected void initReaderAndWriter() {
		// Inizializzazione del Reader...
		setReader(createReader());

		// Inizializzazione del Writer...
		setWriter(createWriter());
	}

	public boolean canConnect() {
		return (getReader() != null && getWriter() != null && Status.STARTING.equals(getCurrentStatus().getStatus()) || Status.DISCONNECTED.equals(getCurrentStatus().getStatus()) || Status.ERROR.equals(getCurrentStatus().getStatus())) && (configuration.getInt("logger.iterations", Defaults.ITERATIONS) <= 0 || getIteration() <= configuration.getInt("logger.iterations", Defaults.ITERATIONS));
	}

	public boolean canDisconnect() {
		return !(Status.STARTING.equals(getCurrentStatus().getStatus()) || Status.DISCONNECTED.equals(getCurrentStatus().getStatus()) || Status.ERROR.equals(getCurrentStatus().getStatus()) || Status.DISCONNECTING.equals(getCurrentStatus().getStatus()));
	}

	public abstract void connect();

	/** Interrupts polling thread and disconnect. */
	public void disconnect() {
		disconnect(false);
	}

	protected void disconnect(final boolean force) {
		if (canDisconnect() || force) {
			setStatus(Status.DISCONNECTING);
			exit = true;
			if (pollingThread != null && isInterruptible()) {
				try {
					pollingThread.interrupt();
				}
				catch (final SecurityException se) {
					logger.log(se);
				}
			}
		}
		else {
			logger.log(Messages.get("err.operation.not.allowed", getCurrentStatus().getStatus().getDescription()), Destination.CONSOLE);
		}
	}

	public abstract void restart();

	public abstract void close();

	protected void joinPollingThread() {
		if (pollingThread != null) {
			try {
				pollingThread.join();
			}
			catch (final InterruptedException ie) {
				if (logger.isDebugEnabled()) {
					logger.log(ie, Destination.CONSOLE, Destination.FILE);
				}
				Thread.currentThread().interrupt();
			}
			catch (final Exception e) {
				logger.log(e);
			}
		}
	}

	protected void stopNetworkServices() {
		httpServer.stop();
		setStatus(Status.CLOSED);
		mqttClient.disconnect();
	}

	protected int getIteration() {
		return iteration;
	}

	protected void setIteration(int iteration) {
		this.iteration = iteration;
	}

	public RouterData getCurrentData() {
		return currentData;
	}

	public ThresholdsReached getCurrentThresholdsReached() {
		return currentThresholdsReached;
	}

	public Reader getReader() {
		return reader;
	}

	protected void setReader(Reader reader) {
		this.reader = reader;
	}

	public Writer getWriter() {
		return writer;
	}

	protected void setWriter(Writer writer) {
		this.writer = writer;
	}

	protected boolean isInterruptible() {
		return interruptible;
	}

}
