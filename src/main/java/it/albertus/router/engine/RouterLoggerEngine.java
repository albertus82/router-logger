package it.albertus.router.engine;

import it.albertus.router.reader.Reader;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.Writer;
import it.albertus.util.Console;
import it.albertus.util.StringUtils;
import it.albertus.util.Version;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public abstract class RouterLoggerEngine {

	public interface Defaults {
		int ITERATIONS = 0;
		long INTERVAL_FAST_IN_MILLIS = 1000L;
		long INTERVAL_NORMAL_IN_MILLIS = 5000L;
		long HYSTERESIS_IN_MILLIS = 10000L;
		int RETRIES = 3;
		long RETRY_INTERVAL_IN_MILLIS = 30000L;
		boolean CONSOLE_SHOW_CONFIGURATION = false;
		Class<? extends Writer> WRITER_CLASS = CsvWriter.class;
	}

	protected static final int FIRST_ITERATION = 1;

	protected final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	protected final Logger logger = Logger.getInstance();

	protected final Console out = getConsole();

	private Reader reader;
	private Writer writer;

	protected volatile boolean exit = false;
	protected Thread shutdownHook;

	private RouterLoggerStatus currentStatus = RouterLoggerStatus.STARTING;
	private RouterLoggerStatus previousStatus = null;

	private volatile int iteration = FIRST_ITERATION;

	public RouterLoggerStatus getCurrentStatus() {
		return currentStatus;
	}

	public RouterLoggerStatus getPreviousStatus() {
		return previousStatus;
	}

	protected void setStatus(RouterLoggerStatus status) {
		doSetStatus(status);
	}

	private final void doSetStatus(RouterLoggerStatus status) {
		this.previousStatus = this.currentStatus;
		this.currentStatus = status;
	}

	protected Reader createReader() {
		final String configurationKey = "reader.class.name";
		final String readerClassName = getReaderClassName(StringUtils.trimToEmpty(configuration.getString(configurationKey)));

		final Reader reader;
		try {
			reader = (Reader) Class.forName(readerClassName).newInstance();
		}
		catch (final RuntimeException re) {
			throw re;
		}
		catch (final Throwable throwable) {
			throw new ConfigurationException(Resources.get("err.invalid.cfg", configurationKey) + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), throwable, configurationKey);
		}
		reader.init(out);
		return reader;
	}

	protected Writer createWriter() {
		final String configurationKey = "writer.class.name";
		String writerClassName = getWriterClassName(configuration.getString(configurationKey, RouterLoggerEngine.Defaults.WRITER_CLASS.getName()).trim());

		final Writer writer;
		try {
			writer = (Writer) Class.forName(writerClassName).newInstance();
		}
		catch (final RuntimeException re) {
			throw re;
		}
		catch (final Throwable throwable) {
			throw new ConfigurationException(Resources.get("err.invalid.cfg", configurationKey) + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), throwable, configurationKey);
		}
		writer.init(out);
		return writer;
	}

	public static String getReaderClassName(String readerClassName) {
		try {
			Class.forName(readerClassName); // Default package.
		}
		catch (final Throwable throwable) {
			readerClassName = Reader.class.getPackage().getName() + '.' + readerClassName;
		}
		return readerClassName;
	}

	public static String getWriterClassName(String writerClassName) {
		try {
			Class.forName(writerClassName); // Default package.
		}
		catch (final Throwable throwable) {
			writerClassName = Writer.class.getPackage().getName() + '.' + writerClassName;
		}
		return writerClassName;
	}

	/** Stampa il messaggio di benvenuto e registra un listener per CTRL+C. */
	protected void beforeOuterLoop() {
		welcome();

		// Gestione chiusura console (CTRL+C)...
		shutdownHook = new Thread("shutdownHook") {
			@Override
			public void run() {
				reader.disconnect();
				release();
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	/** Rimuove il listener per CTRL+C e stampa il messaggio di commiato. */
	protected void afterOuterLoop() {
		if (shutdownHook != null) {
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownHook);
			}
			catch (Exception e) {}
		}
		out.println(Resources.get("msg.bye"), true);
	}

	protected void outerLoop() {
		for (int index = 0, retries = configuration.getInt("logger.retry.count", Defaults.RETRIES); index <= retries && !exit; index++) {
			// Gestione riconnessione in caso di errore...
			if (index > 0) {
				setStatus(RouterLoggerStatus.RECONNECTING);
				final long retryIntervalInMillis = configuration.getLong("logger.retry.interval.ms", Defaults.RETRY_INTERVAL_IN_MILLIS);
				out.println(Resources.get("msg.wait.reconnection", index, retries, retryIntervalInMillis), true);
				try {
					Thread.sleep(retryIntervalInMillis);
				}
				catch (InterruptedException ie) {
					/* Se si chiude il programma mentre e' in attesa di riconnessione... */
					exit = true;
					continue;
				}
			}

			/* Avvio della procedura... */
			final boolean connected;
			try {
				setStatus(RouterLoggerStatus.CONNECTING);
				connected = reader.connect();
			}
			catch (RuntimeException re) {
				/* Configurazione non valida */
				logger.log(re);
				exit = true;
				setStatus(RouterLoggerStatus.ERROR);
				continue;
			}

			// Log in...
			if (connected && !exit) {
				setStatus(RouterLoggerStatus.AUTHENTICATING);
				boolean loggedIn = false;
				try {
					loggedIn = reader.login(configuration.getString("router.username"), configuration.contains("router.password") ? configuration.getString("router.password").toCharArray() : null); // TODO
				}
				catch (Exception e) {
					logger.log(e);
				}

				// Loop...
				if (loggedIn && !exit) {
					setStatus(RouterLoggerStatus.OK);
					index = 0;
					try {
						innerLoop();
						exit = true; // Se non si sono verificati errori.
					}
					catch (InterruptedException ie) {
						out.println(Resources.get("msg.loop.interrupted"), true);
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
	}

	private void welcome() {
		final Version version = Version.getInstance();
		out.println(Resources.get("msg.welcome", Resources.get("msg.application.name"), Resources.get("msg.version", version.getNumber(), version.getDate()), Resources.get("msg.website")));
		out.println();
		out.println(Resources.get("msg.startup.date", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())));

		if (StringUtils.isNotBlank(reader.getDeviceModel())) {
			out.println(Resources.get("msg.device.model", reader.getDeviceModel().trim()));
		}
		if (!configuration.getThresholds().isEmpty()) {
			out.println(Resources.get("msg.thresholds", configuration.getThresholds()));
		}
		if (configuration.getBoolean("console.show.configuration", Defaults.CONSOLE_SHOW_CONFIGURATION)) {
			out.println(Resources.get("msg.settings", configuration));
		}

		out.println();
	}

	protected void innerLoop() throws IOException, InterruptedException {
		long hysteresis = 0;

		// Iterazione...
		for (int iterations = configuration.getInt("logger.iterations", Defaults.ITERATIONS); (iterations <= 0 || iteration <= iterations) && !exit; iteration++) {
			final long timeBeforeRead = System.currentTimeMillis();
			final RouterData info = reader.readInfo();
			final long timeAfterRead = System.currentTimeMillis();
			info.setResponseTime((int) (timeAfterRead - timeBeforeRead));

			writer.saveInfo(info);

			/* Impostazione stato di allerta e gestione isteresi... */
			final Map<Threshold, String> allThresholdsReached = configuration.getThresholds().getReached(info);
			boolean importantThresholdReached = false;
			for (final Threshold threshold : allThresholdsReached.keySet()) {
				if (!threshold.isExcluded()) {
					importantThresholdReached = true;
					break;
				}
			}

			if (importantThresholdReached || System.currentTimeMillis() - hysteresis < configuration.getLong("logger.hysteresis.ms", Defaults.HYSTERESIS_IN_MILLIS)) {
				doSetStatus(RouterLoggerStatus.WARNING); /* Normalmente chiamare setStatus(...) per garantire l'aggiornamento della GUI */
				if (importantThresholdReached) {
					hysteresis = System.currentTimeMillis();
				}
			}
			else if (!allThresholdsReached.isEmpty()) {
				doSetStatus(RouterLoggerStatus.INFO); /* Normalmente chiamare setStatus(...) per garantire l'aggiornamento della GUI */
			}
			else {
				doSetStatus(RouterLoggerStatus.OK); /* Normalmente chiamare setStatus(...) per garantire l'aggiornamento della GUI */
			}

			showInfo(info, allThresholdsReached); /* Aggiorna l'interfaccia */

			// All'ultimo giro non deve esserci il tempo di attesa tra le iterazioni.
			if (iterations <= 0 || iteration < iterations) {
				long waitTimeInMillis;
				if (RouterLoggerStatus.WARNING.equals(currentStatus)) {
					waitTimeInMillis = configuration.getLong("logger.interval.fast.ms", Defaults.INTERVAL_FAST_IN_MILLIS);
				}
				else {
					waitTimeInMillis = configuration.getLong("logger.interval.normal.ms", Defaults.INTERVAL_NORMAL_IN_MILLIS);
				}

				// Sottrazione dal tempo di attesa di quello trascorso durante la scrittura dei dati...
				waitTimeInMillis = waitTimeInMillis - (System.currentTimeMillis() - timeAfterRead);

				if (waitTimeInMillis > 0L) {
					Thread.sleep(waitTimeInMillis);
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
		// Inizializzazione del Logger...
		logger.init(out);
	}

	protected void initReaderAndWriter() {
		// Inizializzazione del Reader...
		setReader(createReader());

		// Inizializzazione del Writer...
		setWriter(createWriter());
	}

	protected abstract Console getConsole();

	protected int getIteration() {
		return iteration;
	}

	protected void setIteration(int iteration) {
		this.iteration = iteration;
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

}
