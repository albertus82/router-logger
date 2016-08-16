package it.albertus.router.engine;

import it.albertus.router.email.ThresholdsEmailSender;
import it.albertus.router.mqtt.RouterLoggerMqttClient;
import it.albertus.router.reader.Reader;
import it.albertus.router.resources.Resources;
import it.albertus.router.server.WebServer;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.Writer;
import it.albertus.util.ConfigurationException;
import it.albertus.util.Console;
import it.albertus.util.StringUtils;
import it.albertus.util.SystemConsole;
import it.albertus.util.Version;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public abstract class RouterLoggerEngine {

	public interface Defaults {
		int ITERATIONS = 0;
		boolean CLOSE_WHEN_FINISHED = false;
		long INTERVAL_FAST_IN_MILLIS = 1000L;
		long INTERVAL_NORMAL_IN_MILLIS = 5000L;
		long HYSTERESIS_IN_MILLIS = 10000L;
		int RETRIES = 3;
		long RETRY_INTERVAL_IN_MILLIS = 30000L;
		boolean CONSOLE_SHOW_CONFIGURATION = false;
		boolean THRESHOLDS_EMAIL = false;
		boolean LOG_CONNECTED = false;
		boolean WAIT_DISCONNECTED = false;
		boolean WAIT_DISCONNECTED_INTERVAL_THRESHOLD = true;
		long WAIT_DISCONNECTED_INTERVAL_THRESHOLD_IN_MILLIS = INTERVAL_FAST_IN_MILLIS;
		Class<? extends Writer> WRITER_CLASS = CsvWriter.class;
	}

	protected static final int FIRST_ITERATION = 1;

	protected final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	protected final Logger logger = Logger.getInstance();
	protected final WebServer httpServer = WebServer.getInstance();
	protected final RouterLoggerMqttClient mqttClient = RouterLoggerMqttClient.getInstance();
	protected final Console out = SystemConsole.getInstance();

	private Reader reader;
	private Writer writer;

	protected Thread pollingThread;
	private volatile boolean interruptible = false;
	protected volatile boolean exit = false;
	private Thread shutdownHook;

	private RouterLoggerStatus currentStatus = RouterLoggerStatus.STARTING;
	private RouterLoggerStatus previousStatus = null;
	private RouterData currentData;

	private volatile int iteration = FIRST_ITERATION;
	private boolean connected = false;
	private boolean loggedIn = false;

	public RouterLoggerStatus getCurrentStatus() {
		return currentStatus;
	}

	public RouterLoggerStatus getPreviousStatus() {
		return previousStatus;
	}

	protected void setStatus(RouterLoggerStatus status) {
		doSetStatus(status);
	}

	private final void doSetStatus(final RouterLoggerStatus status) {
		boolean first = this.previousStatus == null;
		this.previousStatus = this.currentStatus;
		this.currentStatus = status;
		if (!this.currentStatus.equals(this.previousStatus) || first) {
			mqttClient.publish(status);
		}
	}

	protected Reader createReader() {
		final String configurationKey = "reader.class.name";
		final String readerClassName = getReaderClassName(configuration.getString(configurationKey));

		final Reader reader;
		try {
			reader = (Reader) Class.forName(readerClassName).newInstance();
		}
		catch (final Throwable throwable) {
			throw new ConfigurationException(Resources.get("err.invalid.cfg", configurationKey) + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), throwable, configurationKey);
		}
		return reader;
	}

	protected Writer createWriter() {
		final String configurationKey = "writer.class.name";
		final String writerClassName = getWriterClassName(configuration.getString(configurationKey, RouterLoggerEngine.Defaults.WRITER_CLASS.getName()));

		final Writer writer;
		try {
			writer = (Writer) Class.forName(writerClassName).newInstance();
		}
		catch (final Throwable throwable) {
			throw new ConfigurationException(Resources.get("err.invalid.cfg", configurationKey) + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), throwable, configurationKey);
		}
		return writer;
	}

	public static String getReaderClassName(String readerClassName) {
		try {
			readerClassName = StringUtils.trimToEmpty(readerClassName);
			Class.forName(readerClassName, false, RouterLoggerEngine.class.getClassLoader());
		}
		catch (final Throwable throwable) {
			readerClassName = Reader.class.getPackage().getName() + '.' + readerClassName;
		}
		return readerClassName;
	}

	public static String getWriterClassName(String writerClassName) {
		try {
			writerClassName = StringUtils.trimToEmpty(writerClassName);
			Class.forName(writerClassName, false, RouterLoggerEngine.class.getClassLoader());
		}
		catch (final Throwable throwable) {
			writerClassName = Writer.class.getPackage().getName() + '.' + writerClassName;
		}
		return writerClassName;
	}

	protected void beforeConnect() {
		printWelcome();
		setStatus(RouterLoggerStatus.STARTING);
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
					setStatus(RouterLoggerStatus.DISCONNECTING);
					try {
						reader.disconnect();
						setStatus(RouterLoggerStatus.DISCONNECTED);
					}
					catch (final Exception e) {/* Ignore */}
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
			catch (Exception e) {/* Ignore */}
		}
	}

	protected void printGoodbye() {
		out.println(Resources.get("msg.bye"), true);
	}

	protected void outerLoop() {
		for (int index = 0; !exit; index++) {
			final int retries = configuration.getInt("logger.retry.count", Defaults.RETRIES);
			if (retries > 0 && index > retries) {
				break;
			}
			// Gestione riconnessione in caso di errore...
			if (index > 0) {
				setStatus(RouterLoggerStatus.RECONNECTING);
				final long retryIntervalInMillis = configuration.getLong("logger.retry.interval.ms", Defaults.RETRY_INTERVAL_IN_MILLIS);
				final StringBuilder message = new StringBuilder(Resources.get("msg.wait.reconnection"));
				if (retries > 0) {
					message.append(' ').append(Resources.get("msg.wait.reconnection.retry", index, retries));
				}
				message.append(' ').append(Resources.get("msg.wait.reconnection.time", retryIntervalInMillis));
				logger.log(message.toString(), Destination.CONSOLE);
				try {
					interruptible = true;
					Thread.sleep(retryIntervalInMillis);
				}
				catch (InterruptedException ie) {
					// Se si chiude il programma mentre e' in attesa di riconnessione...
					exit = true;
					continue;
				}
				finally {
					interruptible = false;
				}
			}

			/* Avvio della procedura... */
			try {
				setStatus(RouterLoggerStatus.CONNECTING);
				interruptible = true;
				connected = reader.connect();
			}
			catch (RuntimeException re) {
				/* Configurazione non valida */
				logger.log(re);
				exit = true;
				setStatus(RouterLoggerStatus.ERROR);
				continue;
			}
			finally {
				interruptible = false;
			}

			// Log in...
			if (connected && !exit) {
				setStatus(RouterLoggerStatus.AUTHENTICATING);
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
					setStatus(RouterLoggerStatus.OK);
					if (configuration.getBoolean("reader.log.connected", Defaults.LOG_CONNECTED)) {
						logger.log(Resources.get("msg.reader.connected", reader.getDeviceModel()), Destination.FILE, Destination.EMAIL);
					}
					index = 0;
					try {
						innerLoop();
						exit = true; // Se non si sono verificati errori.
					}
					catch (InterruptedException ie) {
						logger.log(Resources.get("msg.loop.interrupted"), Destination.CONSOLE);
					}
					catch (IOException ioe) {
						if (!exit) {
							logger.log(ioe);
						}
					}
					catch (Exception e) {
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
						setStatus(RouterLoggerStatus.ERROR);
					}
					reader.disconnect();
				}
			}
			else {
				if (connected) { // Evidentemente exit == true!
					reader.disconnect();
				}
				else {
					setStatus(RouterLoggerStatus.ERROR);
				}
			}
		}

		release();
		if (!RouterLoggerStatus.ERROR.equals(currentStatus)) {
			setStatus(RouterLoggerStatus.DISCONNECTED);
		}

		if (configuration.getBoolean("logger.close.when.finished", Defaults.CLOSE_WHEN_FINISHED) && iteration >= configuration.getInt("logger.iterations", Defaults.ITERATIONS)) {
			close();
		}
	}

	/** Prints a welcome message. */
	protected void printWelcome() {
		final Version version = Version.getInstance();
		out.println(Resources.get("msg.welcome", Resources.get("msg.application.name"), Resources.get("msg.version", version.getNumber(), version.getDate()), Resources.get("msg.website")));
		out.println();
		out.println(Resources.get("msg.startup.date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())));

		if (!configuration.getThresholds().isEmpty()) {
			out.println(Resources.get("msg.thresholds", configuration.getThresholds()));
		}
		if (configuration.getBoolean("console.show.configuration", Defaults.CONSOLE_SHOW_CONFIGURATION)) {
			out.println(Resources.get("msg.settings", configuration));
		}
	}

	/** Prints the device model name, if available. */
	protected void printDeviceModel() {
		if (reader != null && reader.getDeviceModel() != null && !reader.getDeviceModel().trim().isEmpty()) {
			out.println(Resources.get("msg.device.model", reader.getDeviceModel().trim()));
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
					throw new RuntimeException(Resources.get("msg.reconnection.error"));
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
			final Map<Threshold, String> allThresholdsReached = configuration.getThresholds().getReached(currentData);
			boolean importantThresholdReached = false;
			for (final Threshold threshold : allThresholdsReached.keySet()) {
				if (!threshold.isExcluded()) {
					importantThresholdReached = true;
					break;
				}
			}

			if (importantThresholdReached || System.currentTimeMillis() - hysteresis < configuration.getLong("logger.hysteresis.ms", Defaults.HYSTERESIS_IN_MILLIS)) {
				// Normalmente chiamare setStatus(...) per garantire l'aggiornamento della GUI
				doSetStatus(RouterLoggerStatus.WARNING);
				if (importantThresholdReached) {
					hysteresis = System.currentTimeMillis();
					if (configuration.getBoolean("thresholds.email", Defaults.THRESHOLDS_EMAIL)) {
						ThresholdsEmailSender.getInstance().send(allThresholdsReached, currentData);
					}
				}
			}
			else if (!allThresholdsReached.isEmpty()) {
				// Normalmente chiamare setStatus(...) per garantire l'aggiornamento della GUI
				doSetStatus(RouterLoggerStatus.INFO);
			}
			else {
				// Normalmente chiamare setStatus(...) per garantire l'aggiornamento della GUI
				doSetStatus(RouterLoggerStatus.OK);
			}
			// Aggiorna l'interfaccia
			showInfo(currentData, allThresholdsReached);

			// Pubblica via MQTT
			mqttClient.publish(currentData);
			if (importantThresholdReached) {
				mqttClient.publish(allThresholdsReached, currentData.getTimestamp());
			}

			if (exit) {
				break;
			}

			// All'ultimo giro non deve esserci il tempo di attesa tra le iterazioni.
			if (iterations <= 0 || iteration < iterations) {

				final long waitTimeInMillis;
				if (RouterLoggerStatus.WARNING.equals(currentStatus)) {
					waitTimeInMillis = configuration.getLong("logger.interval.fast.ms", Defaults.INTERVAL_FAST_IN_MILLIS);
				}
				else {
					waitTimeInMillis = configuration.getLong("logger.interval.normal.ms", Defaults.INTERVAL_NORMAL_IN_MILLIS);
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
						final String formattedDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Resources.getLanguage().getLocale()).format(new Date(System.currentTimeMillis() + adjustedWaitTimeInMillis));
						logger.log(Resources.get("msg.reconnection.info", formattedDate), Destination.CONSOLE);
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
		catch (Exception e) {}
		try {
			writer.release();
		}
		catch (Exception e) {}
	}

	public RouterLoggerEngine() {
		// Inizializzazione dell'HttpServer...
		httpServer.init(this);
	}

	protected void initReaderAndWriter() {
		// Inizializzazione del Reader...
		setReader(createReader());

		// Inizializzazione del Writer...
		setWriter(createWriter());
	}

	public boolean canConnect() {
		return (getReader() != null && getWriter() != null && RouterLoggerStatus.STARTING.equals(getCurrentStatus()) || RouterLoggerStatus.DISCONNECTED.equals(getCurrentStatus()) || RouterLoggerStatus.ERROR.equals(getCurrentStatus())) && (configuration.getInt("logger.iterations", Defaults.ITERATIONS) <= 0 || getIteration() <= configuration.getInt("logger.iterations", Defaults.ITERATIONS));
	}

	public boolean canDisconnect() {
		return !(RouterLoggerStatus.STARTING.equals(getCurrentStatus()) || RouterLoggerStatus.DISCONNECTED.equals(getCurrentStatus()) || RouterLoggerStatus.ERROR.equals(getCurrentStatus()) || RouterLoggerStatus.DISCONNECTING.equals(getCurrentStatus()));
	}

	public abstract void connect();

	/** Interrupts polling thread and disconnect. */
	public void disconnect() {
		disconnect(false);
	}

	protected void disconnect(final boolean force) {
		if (canDisconnect() || force) {
			setStatus(RouterLoggerStatus.DISCONNECTING);
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
			logger.log(Resources.get("err.operation.not.allowed", getCurrentStatus().getDescription()), Destination.CONSOLE);
		}
	}

	public abstract void restart();

	public abstract void close();

	protected void joinPollingThread() {
		if (pollingThread != null) {
			try {
				pollingThread.join();
			}
			catch (final InterruptedException ie) {}
			catch (final Exception e) {
				logger.log(e);
			}
		}
	}

	protected void stopNetworkServices() {
		httpServer.stop();
		setStatus(RouterLoggerStatus.CLOSED);
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
