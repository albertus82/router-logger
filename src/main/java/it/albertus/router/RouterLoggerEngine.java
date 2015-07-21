package it.albertus.router;

import it.albertus.router.reader.Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.util.Logger;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.Writer;
import it.albertus.util.Console;
import it.albertus.util.StringUtils;
import it.albertus.util.Version;

import java.io.IOException;
import java.util.Map;

public abstract class RouterLoggerEngine {

	protected interface Defaults {
		int ITERATIONS = -1;
		long INTERVAL_FAST_IN_MILLIS = 1000L;
		long INTERVAL_NORMAL_IN_MILLIS = 5000L;
		long HYSTERESIS_IN_MILLIS = 10000L;
		int RETRIES = 3;
		long RETRY_INTERVAL_IN_MILLIS = 30000L;
		boolean WRITER_THREAD = false;
		boolean CONSOLE_ANIMATION = true;
		boolean CONSOLE_SHOW_CONFIGURATION = false;
		String CONSOLE_SHOW_KEYS_SEPARATOR = ",";
		Class<? extends Writer> WRITER_CLASS = CsvWriter.class;
		Class<? extends Reader> READER_CLASS = TpLink8970Reader.class;
	}

	protected static final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	protected static final Logger logger = Logger.getInstance();

	protected final Console out = getConsole();
	protected final Reader reader;
	protected final Writer writer;

	protected volatile boolean exit = false;
	protected volatile boolean close = false;

	private Reader initReader() {
		final String configurationKey = "reader.class.name";
		String readerClassName = configuration.getString(configurationKey, Defaults.READER_CLASS.getName()).trim();

		try {
			Class.forName(readerClassName); // Default package.
		}
		catch (ClassNotFoundException e) {
			readerClassName = Reader.class.getPackage().getName() + '.' + readerClassName;
		}

		final Reader reader;
		try {
			reader = (Reader) Class.forName(readerClassName).newInstance();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException("Invalid \"" + configurationKey + "\" property. Review your " + configuration.getFileName() + " file.", e);
		}
		reader.init(out);
		return reader;
	}

	private Writer initWriter() {
		final String configurationKey = "writer.class.name";
		String writerClassName = configuration.getString(configurationKey, Defaults.WRITER_CLASS.getName()).trim();

		try {
			Class.forName(writerClassName); // Default package.
		}
		catch (ClassNotFoundException e) {
			writerClassName = Writer.class.getPackage().getName() + '.' + writerClassName;
		}

		final Writer writer;
		try {
			writer = (Writer) Class.forName(writerClassName).newInstance();
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Exception e) {
			throw new RuntimeException("Invalid \"" + configurationKey + "\" property. Review your " + configuration.getFileName() + " file.", e);
		}
		writer.init(out);
		return writer;
	}
	
	protected void run() {
		welcome();

		final int retries = configuration.getInt("logger.retry.count", Defaults.RETRIES);

		// Gestione chiusura console (CTRL+C)...
		final Thread hook = new Thread() {
			public void run() {
				reader.disconnect();
				release();
			}
		};
		Runtime.getRuntime().addShutdownHook(hook);

		for (int index = 0; index <= retries && !exit; index++) {
			// Gestione riconnessione in caso di errore...
			if (index > 0) {
				final long retryIntervalInMillis = configuration.getLong("logger.retry.interval.ms", Defaults.RETRY_INTERVAL_IN_MILLIS);
				out.println("Waiting for reconnection " + index + '/' + retries + " (" + retryIntervalInMillis + " ms)...", true);
				try {
					Thread.sleep(retryIntervalInMillis);
				}
				catch (InterruptedException ie) {
					throw new RuntimeException(ie);
				}
			}

			// Avvio della procedura...
			final boolean connected = reader.connect();

			// Log in...
			if (connected) {
				boolean loggedIn = false;
				try {
					loggedIn = reader.login();
				}
				catch (Exception e) {
					logger.log(e);
				}

				// Loop...
				if (loggedIn) {
					index = 0;
					try {
						loop();
						exit = true; // Se non si sono verificati errori.
					}
					catch (Exception e) {
						logger.log(e);
					}
					finally {
						// In ogni caso, si esegue la disconnessione dal
						// server...
						try {
							reader.logout();
						}
						catch (Exception e) {
							logger.log(e);
						}
						reader.disconnect();
					}
				}
				else {
					// In caso di autenticazione fallita, si esce subito per evitare il blocco dell'account.
					exit = true;
					reader.disconnect();
				}
			}
		}

		Runtime.getRuntime().removeShutdownHook(hook);

		release();
		out.println("Bye!", true);
		
		close = true;
	}

	private void welcome() {
		// Preparazione numero di versione (se presente)...
		final Version version = Version.getInstance();
		final StringBuilder versionInfo = new StringBuilder();
		final String versionNumber = version.getNumber();
		if (versionNumber != null && !"".equals(versionNumber.trim())) {
			versionInfo.append('v').append(versionNumber.trim()).append(' ');
		}
		String versionDate = version.getDate();
		if (versionDate != null && !"".equals(versionDate.trim())) {
			versionInfo.append('(').append(versionDate.trim()).append(") ");
		}

		out.println("********** ADSL Modem Router Logger " + versionInfo.toString() + "**********");
		out.println();
		boolean lineBreak = false;
		if (StringUtils.isNotBlank(reader.getDeviceModel())) {
			out.println("Device model: " + reader.getDeviceModel().trim() + '.');
			lineBreak = true;
		}
		if (!configuration.getThresholds().isEmpty()) {
			out.println("Thresholds: " + configuration.getThresholds().toString());
			lineBreak = true;
		}
		if (configuration.getBoolean("console.show.configuration", Defaults.CONSOLE_SHOW_CONFIGURATION)) {
			out.println("Settings: " + configuration.toString());
			lineBreak = true;
		}
		if (lineBreak) {
			out.println();
		}
	}

	private final void loop() throws IOException, InterruptedException {
		// Determinazione numero di iterazioni...
		int iterations = configuration.getInt("logger.iterations", Defaults.ITERATIONS);
		if (iterations <= 0) {
			iterations = Integer.MAX_VALUE;
		}

		long hysteresis = 0;

		// Iterazione...
		for (int iteration = 1, lastLogLength = 0; iteration <= iterations && !exit; iteration++) {
			// Chiamata alle implementazioni specifiche...
			final Map<String, String> info = reader.readInfo();
			saveInfo(info);
			// Fine implementazioni specifiche.

			log(info, iteration, lastLogLength, iterations);

			// All'ultimo giro non deve esserci il tempo di attesa tra le iterazioni.
			if (iteration != iterations) {
				final long waitTimeInMillis;
				final boolean thresholdReached = configuration.getThresholds().isReached(info);
				if (thresholdReached || System.currentTimeMillis() - hysteresis < configuration.getLong("logger.hysteresis.ms", Defaults.HYSTERESIS_IN_MILLIS)) {
					waitTimeInMillis = configuration.getLong("logger.interval.fast.ms", Defaults.INTERVAL_FAST_IN_MILLIS);
					if (thresholdReached) {
						hysteresis = System.currentTimeMillis();
					}
				}
				else {
					waitTimeInMillis = configuration.getLong("logger.interval.normal.ms", Defaults.INTERVAL_NORMAL_IN_MILLIS);
				}
				Thread.sleep(waitTimeInMillis);
			}
		}
	}
	
	protected abstract void log(Map<String, String> info, int iteration, int lastLogLength, int iterations);

	private void saveInfo(final Map<String, String> info) {
		if (configuration.getBoolean("logger.writer.thread", Defaults.WRITER_THREAD)) {
			new Thread() {
				@Override
				public void run() {
					writer.saveInfo(info);
				}
			}.start();
		}
		else {
			writer.saveInfo(info);
		}
	}

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
		
		// Inizializzazione del Reader...
		reader = initReader();

		// Inizializzazione del Writer...
		writer = initWriter();
	}

	protected abstract Console getConsole();

}
