package it.albertus.router.logger;

import it.albertus.router.RouterLoggerConfiguration;
import it.albertus.router.Threshold;
import it.albertus.router.Threshold.Type;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.Writer;
import it.albertus.util.ExceptionUtils;
import it.albertus.util.Version;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.net.telnet.TelnetClient;

public abstract class RouterLogger {

	private interface Defaults {
		String ROUTER_ADDRESS = "192.168.1.1";
		int ROUTER_PORT = 23;
		int SOCKET_TIMEOUT_IN_MILLIS = 30000;
		int CONNECTION_TIMEOUT_IN_MILLIS = 20000;
		int ITERATIONS = -1;
		long INTERVAL_FAST_IN_MILLIS = 1000L;
		long INTERVAL_NORMAL_IN_MILLIS = 5000L;
		int RETRIES = 3;
		long RETRY_INTERVAL_IN_MILLIS = 30000L;
		boolean TELNET_SEND_CRLF = true;
		boolean CONSOLE_ANIMATION = true;
		String CONSOLE_SHOW_KEYS_SEPARATOR = ",";
		Class<? extends Writer> WRITER_CLASS = CsvWriter.class;
	}

	private static final String COMMAND_LINE_HELP = "Usage: routerlogger logger.class.Name";

	private static final String THRESHOLD_PREFIX = "threshold";
	private static final String THRESHOLD_SUFFIX_KEY = "key";
	private static final String THRESHOLD_SUFFIX_TYPE = "type";
	private static final String THRESHOLD_SUFFIX_VALUE = "value";
	private static final char[] ANIMATION = { '-', '\\', '|', '/' };
	
	protected static final PrintStream out = System.out;
	
	protected final TelnetClient telnet = new TelnetClient();
	protected final Set<Threshold> thresholds = new TreeSet<Threshold>();
	protected final Writer writer;
	protected final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	public static final void main(final String... args) {
		if (args.length != 1) {
			out.println(COMMAND_LINE_HELP);
		}
		else {
			String className = args[0];
			try {
				Class.forName(className);
			}
			catch (ClassNotFoundException e) {
				className = RouterLogger.class.getPackage().getName() + '.' + className;
			}

			try {
				((RouterLogger) Class.forName(className).newInstance()).run();
			}
			catch (Exception e) {
				out.println(ExceptionUtils.getStackTrace(e));
				out.println(COMMAND_LINE_HELP);
			}
		}
	}

	protected final void run() {
		welcome();

		boolean exit = false;

		final int retries = configuration.getInt("logger.retry.count", Defaults.RETRIES);

		for (int index = 0; index <= retries && !exit; index++) {
			// Gestione riconnessione in caso di errore...
			if (index > 0) {
				final long retryIntervalInMillis = configuration.getLong("logger.retry.interval.ms", Defaults.RETRY_INTERVAL_IN_MILLIS);
				out.println("Waiting for reconnection " + index + '/' + retries + " (" + retryIntervalInMillis + " ms)...");
				try {
					Thread.sleep(retryIntervalInMillis);
				}
				catch (InterruptedException ie) {
					throw new RuntimeException(ie);
				}
			}

			// Avvio della procedura...
			final boolean connected = connect();

			// Log in...
			if (connected) {
				boolean loggedIn = false;
				try {
					loggedIn = login();
				}
				catch (Exception e) {
					out.print(ExceptionUtils.getStackTrace(e));
				}

				// Loop...
				if (loggedIn) {
					index = 0;
					try {
						loop();
						exit = true; // Se non si sono verificati errori.
					}
					catch (Exception e) {
						out.print(ExceptionUtils.getStackTrace(e));
					}
					finally {
						// In ogni caso, si esegue la disconnessione dal server...
						out.println();
						try {
							logout();
						}
						catch (Exception e) {
							out.print(ExceptionUtils.getStackTrace(e));
						}
						disconnect();
					}
				}
				else {
					// In caso di autenticazione fallita, si esce subito per evitare il blocco dell'account.
					exit = true;
					disconnect();
				}
			}
		}
		release();
		out.println("Bye!");
	}

	/**
	 * Estrae le informazioni di interesse dai dati ricevuti dal server telnet,
	 * utilizzando i metodi {@link #writeToTelnet(String)} e
	 * {@link #readFromTelnet(String, boolean)}.
	 * 
	 * @return la mappa contenente le informazioni estratte.
	 * @throws IOException in caso di errore nella lettura dei dati.
	 */
	protected abstract Map<String, String> readInfo() throws IOException;

	/**
	 * Restituisce una stringa contenente marca e modello del router relativo
	 * all'implementazione realizzata.
	 */
	protected String getDeviceModel() {
		return getClass().getSimpleName();
	}

	protected RouterLogger() {
		// Valorizzazione delle soglie...
		loadThresholds();

		// Inizializzazione del Writer...
		writer = initWriter();
	}

	private Writer initWriter() {
		final String configurationKey = "logger.writer.class.name";
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

	/**
	 * Effettua la connessione al server telnet, ma non l'autenticazione.
	 * 
	 * @return <tt>true</tt> se la connessione &egrave; riuscita, <tt>false</tt>
	 *         altrimenti.
	 */
	private final boolean connect() {
		final String routerAddress = configuration.getString("router.address", Defaults.ROUTER_ADDRESS).trim();
		final int routerPort = configuration.getInt("router.port", Defaults.ROUTER_PORT);
		final int connectionTimeoutInMillis = configuration.getInt("connection.timeout.ms", Defaults.CONNECTION_TIMEOUT_IN_MILLIS);
		final int socketTimeoutInMillis = configuration.getInt("socket.timeout.ms", Defaults.SOCKET_TIMEOUT_IN_MILLIS);

		telnet.setConnectTimeout(connectionTimeoutInMillis);
		out.println("Connecting to: " + routerAddress + ':' + routerPort + "...");
		boolean connected = false;
		try {
			telnet.connect(routerAddress, routerPort);
			connected = true;
			telnet.setSoTimeout(socketTimeoutInMillis);
		}
		catch (Exception e) {
			out.print(ExceptionUtils.getStackTrace(e));
		}
		return connected;
	}

	/**
	 * Effettua la disconnessione dal server telnet, ma non invia alcun comando
	 * di logout. &Egrave; buona norma richiamare prima il metodo
	 * {@link #logout()} per inviare al server telnet gli opportuni comandi di
	 * chiusura della sessione (ad esempio <tt>logout</tt>).
	 */
	protected final void disconnect() {
		out.println("Disconnecting...");
		try {
			telnet.disconnect();
		}
		catch (Exception e) {
			out.print(ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Effettua l'autenticazione sul server telnet, utilizzando i metodi
	 * {@link #readFromTelnet(String, boolean)} e {@link #writeToTelnet(String)}
	 * per interagire con il server e comunicare le credenziali di accesso.
	 * 
	 * @return <tt>true</tt> se l'autenticazione &egrave; riuscita,
	 *         <tt>false</tt> altrimenti.
	 * @throws IOException in caso di errore nella comunicazione con il server.
	 */
	protected abstract boolean login() throws IOException;

	/**
	 * Effettua il logout dal server telnet inviando il comando <tt>logout</tt>.
	 * &Egrave; possibile sovrascrivere questo metodo per aggiungere altri o
	 * diversi comandi che debbano essere eseguiti in fase di logout. <b>Questo
	 * metodo non effettua esplicitamente la disconnessione dal server</b>.
	 * 
	 * @throws IOException in caso di errore nella comunicazione con il server.
	 */
	protected void logout() throws IOException {
		out.println("Logging out...");
		writeToTelnet("logout");
	}

	private final void loop() throws IOException, InterruptedException {
		// Determinazione numero di iterazioni...
		int iterations = configuration.getInt("logger.iterations", Defaults.ITERATIONS);
		if (iterations <= 0) {
			iterations = Integer.MAX_VALUE;
		}

		// Iterazione...
		for (int iteration = 1, lastLogLength = 0; iteration <= iterations; iteration++) {
			// Chiamata alle implementazioni specifiche...
			final Map<String, String> info = readInfo();
			writer.saveInfo(info);
			// Fine implementazioni specifiche.

			// Scrittura indice dell'iterazione in console...
			final StringBuilder clean = new StringBuilder();
			while (lastLogLength-- > 0) {
				clean.append('\b');
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
				Thread.sleep(getWaitTimeInMillis(info));
			}
		}
	}

	protected long getWaitTimeInMillis(final Map<String, String> info) {
		// Gestione delle soglie...
		if (!thresholds.isEmpty() && info != null && !info.isEmpty()) {
			for (final String key : info.keySet()) {
				if (key != null && !"".equals(key.trim())) {
					for (final Threshold threshold : thresholds) {
						if (key.trim().equals(threshold.getKey()) && threshold.isReached(info.get(key))) {
							return configuration.getLong("logger.interval.fast.ms", Defaults.INTERVAL_FAST_IN_MILLIS);
						}
					}
				}
			}
		}
		return configuration.getLong("logger.interval.normal.ms", Defaults.INTERVAL_NORMAL_IN_MILLIS);
	}

	/**
	 * Invia un comando al server telnet. La stringa passata viene
	 * automaticamente inviata al server e non occorre la presenza del carattere
	 * <tt>\n</tt>. Se nella stringa sono presenti caratteri <tt>\n</tt> o
	 * <tt>\r</tt>, questa viene troncata alla prima occorrenza di uno di questi
	 * caratteri.
	 * 
	 * @param command  il comando da inviare al server telnet.
	 * @return l'eco del testo inviato al server telnet.
	 * @throws IOException in caso di errore nella comunicazione con il server.
	 * @throws NullPointerException se il comando fornito &egrave; null.
	 */
	protected String writeToTelnet(final String command) throws IOException {
		final OutputStream out = telnet.getOutputStream();
		final StringBuilder echo = new StringBuilder();
		for (char character : command.toCharArray()) {
			if (character == '\n' || character == '\r') {
				break;
			}
			out.write(character);
			echo.append(character);
		}
		out.flush();
		// Thread.sleep(50);
		if (configuration.getBoolean("telnet.send.crlf", Defaults.TELNET_SEND_CRLF)) {
			out.write('\r');
			echo.append('\r');
		}
		out.write('\n');
		echo.append('\n');
		out.flush();
		return echo.toString();
	}

	/**
	 * Legge i dati inviati dal server telnet fin quando non incontra la stringa
	 * limite passata come parametro. <b>Se la stringa non viene trovata e lo
	 * stream si esaurisce, il programma si blocca in attesa di altri dati dal
	 * server</b>, che potrebbero non arrivare mai.
	 * 
	 * @param until  la stringa limite che determina la fine della lettura.
	 * @param inclusive
	 *            determina l'inclusione o meno della stringa limite all'interno
	 *            della stringa restituita.
	 * @return la stringa contenente i dati ricevuti dal server telnet.
	 * @throws IOException in caso di errore durante la lettura dei dati.
	 * @throws NullPointerException se la stringa fornita &egrave; null.
	 */
	protected String readFromTelnet(final String until, final boolean inclusive) throws IOException {
		final InputStream in = telnet.getInputStream();
		final char lastChar = until.charAt(until.length() - 1);
		final StringBuilder text = new StringBuilder();
		int currentByte;
		while ((currentByte = in.read()) != -1) {
			final char currentChar = (char) currentByte;
			text.append(currentChar);
			if (currentChar == lastChar && text.toString().endsWith(until)) {
				if (!inclusive) {
					text.delete(text.length() - until.length(), text.length());
				}
				break;
			}
		}
		return text.toString();
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
		if (getDeviceModel() != null && !"".equals(getDeviceModel().trim())) {
			out.println("Device model: " + getDeviceModel().trim() + '.');
			lineBreak = true;
		}
		if (!thresholds.isEmpty()) {
			out.println("Thresholds: " + thresholds.toString());
			lineBreak = true;
		}
		if (lineBreak) {
			out.println();
		}
	}

	/**
	 * Libera le risorse eventualmente allocate (file, connessioni a database,
	 * ecc.).
	 */
	protected void release() {
		writer.release();
	}

}
