package it.albertus.router;

import it.albertus.router.Threshold.Type;
import it.albertus.router.reader.Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.Writer;
import it.albertus.util.Configuration;
import it.albertus.util.Console;
import it.albertus.util.ExceptionUtils;
import it.albertus.util.StringUtils;
import it.albertus.util.Version;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class RouterLogger {

	private interface Defaults {
		boolean DEBUG = false;
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

	private static final String THRESHOLD_PREFIX = "threshold";
	private static final String THRESHOLD_SUFFIX_KEY = "key";
	private static final String THRESHOLD_SUFFIX_TYPE = "type";
	private static final String THRESHOLD_SUFFIX_VALUE = "value";
	private static final char[] ANIMATION = { '-', '\\', '|', '/' };

	private static final Configuration configuration = RouterLoggerConfiguration.getInstance();
	private static final Console out = Console.getInstance();

	private final Set<Threshold> thresholds = new TreeSet<Threshold>();
	private final Reader reader;
	private final Writer writer;

	public static final void main(final String... args) {
		new RouterLogger().run();
	}

	private RouterLogger() {
		// Valorizzazione delle soglie...
		loadThresholds();

		// Inizializzazione del Reader...
		reader = initReader();

		// Inizializzazione del Writer...
		writer = initWriter();
	}

	private void loadThresholds() {
		final Set<String> thresholdsAdded = new HashSet<String>();
		for (Object objectKey : configuration.getProperties().keySet()) {
			String key = (String) objectKey;
			if (key != null && key.startsWith(THRESHOLD_PREFIX + '.')) {
				if (key.indexOf('.') == key.lastIndexOf('.') || "".equals(key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'))) || (!key.endsWith(THRESHOLD_SUFFIX_KEY) && !key.endsWith(THRESHOLD_SUFFIX_TYPE) && !key.endsWith(THRESHOLD_SUFFIX_VALUE))) {
					throw new IllegalArgumentException("Thresholds misconfigured. Review your " + configuration.getFileName() + " file.");
				}
				final String thresholdName = key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'));
				if (thresholdsAdded.contains(thresholdName)) {
					continue;
				}
				final String thresholdKey = configuration.getString(THRESHOLD_PREFIX + '.' + thresholdName + '.' + THRESHOLD_SUFFIX_KEY);
				final Type thresholdType = Type.getEnum(configuration.getString(THRESHOLD_PREFIX + '.' + thresholdName + '.' + THRESHOLD_SUFFIX_TYPE));
				final String thresholdValue = configuration.getString(THRESHOLD_PREFIX + '.' + thresholdName + '.' + THRESHOLD_SUFFIX_VALUE);
				if (thresholdKey == null || "".equals(thresholdKey.trim()) || thresholdValue == null || thresholdType == null) {
					throw new IllegalArgumentException("Threshold misconfigured: \"" + thresholdName + "\". Review your " + configuration.getFileName() + " file.");
				}
				thresholds.add(new Threshold(thresholdKey.trim(), thresholdType, thresholdValue));
				thresholdsAdded.add(thresholdName);
			}
		}
	}

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
		return writer;
	}

	private void run() {
		welcome();

		boolean exit = false;

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
					printLog(e);
				}

				// Loop...
				if (loggedIn) {
					index = 0;
					try {
						loop();
						exit = true; // Se non si sono verificati errori.
					}
					catch (Exception e) {
						printLog(e);
					}
					finally {
						// In ogni caso, si esegue la disconnessione dal server...
						try {
							reader.logout();
						}
						catch (Exception e) {
							printLog(e);
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
	}

	private void printLog(Throwable throwable) {
		if (configuration.getBoolean("logger.debug", Defaults.DEBUG)) {
			out.print(ExceptionUtils.getStackTrace(throwable), true);
		}
		else {
			out.println(ExceptionUtils.getLogMessage(throwable), true);
		}
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
		if (!thresholds.isEmpty()) {
			out.println("Thresholds: " + thresholds.toString());
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
		for (int iteration = 1, lastLogLength = 0; iteration <= iterations; iteration++) {
			// Chiamata alle implementazioni specifiche...
			final Map<String, String> info = reader.readInfo();
			saveInfo(info);
			// Fine implementazioni specifiche.

			// Scrittura indice dell'iterazione in console...
			final StringBuilder clean = new StringBuilder();
			while (lastLogLength-- > 0) {
				clean.append('\b').append(' ').append('\b');
			}
			final StringBuilder log = new StringBuilder();
			final boolean animate = configuration.getBoolean("console.animation", Defaults.CONSOLE_ANIMATION);
			if (animate) {
				log.append(ANIMATION[iteration & 3]).append(' ');
			}
			log.append(iteration);
			if (iterations != Integer.MAX_VALUE) {
				log.append('/').append(iterations);
			}
			log.append(' ');
			if (animate) {
				log.append(ANIMATION[iteration & 3]).append(' ');
			}
			// Fine scrittura indice.

			// Scrittura informazioni aggiuntive richieste...
			if (info != null && !info.isEmpty()) {
				final StringBuilder infoToShow = new StringBuilder();
				for (String keyToShow : configuration.getString("console.show.keys", "").split(configuration.getString("console.show.keys.separator", Defaults.CONSOLE_SHOW_KEYS_SEPARATOR).trim())) {
					if (keyToShow != null && !"".equals(keyToShow.trim())) {
						keyToShow = keyToShow.trim();
						for (final String key : info.keySet()) {
							if (key != null && key.trim().equals(keyToShow)) {
								if (infoToShow.length() == 0) {
									infoToShow.append('[');
								}
								else {
									infoToShow.append(", ");
								}
								infoToShow.append(keyToShow + ": " + info.get(key));
							}
						}
					}
				}
				if (infoToShow.length() != 0) {
					infoToShow.append("] ");
				}
				log.append(infoToShow);
			}
			// Fine scrittura informazioni aggiuntive.

			lastLogLength = log.length();
			out.print(clean.toString() + log.toString());

			// All'ultimo giro non deve esserci il tempo di attesa tra le iterazioni.
			if (iteration != iterations) {
				final long waitTimeInMillis;
				final boolean thresholdReached = isTresholdReached(info);
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

	private boolean isTresholdReached(final Map<String, String> info) {
		// Gestione delle soglie...
		if (!thresholds.isEmpty() && info != null && !info.isEmpty()) {
			for (final String key : info.keySet()) {
				if (key != null && key.trim().length() != 0) {
					for (final Threshold threshold : thresholds) {
						if (key.trim().equals(threshold.getKey()) && threshold.isReached(info.get(key))) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Libera le risorse eventualmente allocate (file, connessioni a database,
	 * ecc.).
	 */
	private void release() {
		try {
			reader.release();
		}
		catch (Exception e) {}
		try {
			writer.release();
		}
		catch (Exception e) {}
	}

}
