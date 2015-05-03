package it.albertus.router;

import it.albertus.router.Threshold.Type;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
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
		long RETRY_INTERVAL_IN_MILLIS = 60000L;
		boolean TELNET_SEND_CRLF = true;
		boolean CONSOLE_ANIMATION = true;
	}

	private static final String CONFIGURATION_FILE_NAME = "routerlogger.cfg";
	private static final String VERSION_FILE_NAME = "version.properties";

	private static final String THRESHOLD_PREFIX = "threshold";
	private static final String THRESHOLD_SUFFIX_KEY = "key";
	private static final String THRESHOLD_SUFFIX_TYPE = "type";
	private static final String THRESHOLD_SUFFIX_VALUE = "value";
	private static final char[] ANIMATION = { '-', '\\', '|', '/' };

	protected final TelnetClient telnet = new TelnetClient();
	protected final Set<Threshold> thresholds = new TreeSet<Threshold>();
	protected final Properties configuration = new Properties();
	protected final Properties version = new Properties();

	protected final void run() {
		welcome();

		boolean exit = false;

		final int retries = Integer.parseInt(configuration.getProperty("logger.retry.count", Integer.toString(Defaults.RETRIES)));

		for (int index = 0; index <= retries && !exit; index++) {
			// Gestione riconnessione in caso di errore...
			if (index > 0) {
				final long retryIntervalInMillis = Long.parseLong(configuration.getProperty("logger.retry.interval.ms", Long.toString(Defaults.RETRY_INTERVAL_IN_MILLIS)));
				System.out.println("Waiting for reconnection " + index + '/' + retries + " (" + retryIntervalInMillis + " ms)...");
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
					e.printStackTrace();
				}

				// Loop...
				if (loggedIn) {
					try {
						loop();
						exit = true; // Se non si sono verificati errori.
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					finally {
						// In ogni caso, si esegue la disconnessione dal server...
						System.out.println();
						try {
							logout();
						}
						catch (Exception e) {
							e.printStackTrace();
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
		System.out.println("Bye!");
	}

	/**
	 * Estrae le informazioni di interesse dai dati ricevuti dal server telnet,
	 * utilizzando i metodi {@link #writeToTelnet(String)} e
	 * {@link #readFromTelnet(String, boolean)}.
	 * 
	 * @return la mappa contenente le informazioni estratte.
	 * 
	 * @throws IOException in caso di errore nella lettura dei dati.
	 */
	protected abstract Map<String, String> readInfo() throws IOException;

	/**
	 * Salva le informazioni di interesse precedentemente estratte con
	 * {@link #readInfo()} con le modalit&agrave; desiderate, ad esempio su file
	 * o in un database.
	 * 
	 * @param info  le informazioni da salvare.
	 */
	protected abstract void saveInfo(Map<String, String> info);

	/**
	 * Restituisce una stringa contenente marca e modello del router relativo
	 * all'implementazione realizzata.
	 */
	protected String getDeviceModel() {
		return null;
	}

	protected RouterLogger() {
		try {
			// Caricamento file di configurazione...
			loadConfiguration();

			// Valorizzazione delle soglie...
			loadThresholds();

			// Caricamento file versione...
			loadVersion();
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private void loadThresholds() {
		final Set<String> thresholdsAdded = new HashSet<String>();
		for (Object objectKey : configuration.keySet()) {
			String key = (String) objectKey;
			if (key != null && key.startsWith(THRESHOLD_PREFIX + '.')) {
				if (key.indexOf('.') == key.lastIndexOf('.') || "".equals(key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'))) || (!key.endsWith(THRESHOLD_SUFFIX_KEY) && !key.endsWith(THRESHOLD_SUFFIX_TYPE) && !key.endsWith(THRESHOLD_SUFFIX_VALUE))) {
					throw new IllegalArgumentException("Thresholds misconfigured. Review your " + CONFIGURATION_FILE_NAME + " file.");
				}
				final String thresholdName = key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'));
				if (thresholdsAdded.contains(thresholdName)) {
					continue;
				}
				final String thresholdKey = configuration.getProperty(THRESHOLD_PREFIX + '.' + thresholdName + '.' + THRESHOLD_SUFFIX_KEY);
				final Type thresholdType = Type.getEnum(configuration.getProperty(THRESHOLD_PREFIX + '.' + thresholdName + '.' + THRESHOLD_SUFFIX_TYPE));
				final String thresholdValue = configuration.getProperty(THRESHOLD_PREFIX + '.' + thresholdName + '.' + THRESHOLD_SUFFIX_VALUE);
				if (thresholdKey == null || "".equals(thresholdKey.trim()) || thresholdValue == null || thresholdType == null) {
					throw new IllegalArgumentException("Threshold misconfigured: \"" + thresholdName + "\". Review your " + CONFIGURATION_FILE_NAME + " file.");
				}
				thresholds.add(new Threshold(thresholdKey.trim(), thresholdType, thresholdValue));
				thresholdsAdded.add(thresholdName);
			}
		}
	}

	private void loadVersion() {
		final InputStream inputStream = getClass().getResourceAsStream('/' + VERSION_FILE_NAME);
		if (inputStream != null) {
			try {
				version.load(inputStream);
				inputStream.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void loadConfiguration() throws IOException {
		final InputStream inputStream;
		final File config = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() + '/' + CONFIGURATION_FILE_NAME);
		if (config.exists()) {
			inputStream = new BufferedInputStream(new FileInputStream(config));
		}
		else {
			inputStream = getClass().getResourceAsStream('/' + CONFIGURATION_FILE_NAME);
		}
		configuration.load(inputStream);
		inputStream.close();
	}

	/**
	 * Effettua la connessione al server telnet, ma non l'autenticazione.
	 * 
	 * @return <tt>true</tt> se la connessione &egrave; riuscita, <tt>false</tt>
	 *         altrimenti.
	 */
	private final boolean connect() {
		final String routerAddress = configuration.getProperty("router.address", Defaults.ROUTER_ADDRESS).trim();
		final int routerPort = Integer.parseInt(configuration.getProperty("router.port", Integer.toString(Defaults.ROUTER_PORT)));
		final int connectionTimeoutInMillis = Integer.parseInt(configuration.getProperty("connection.timeout.ms", Integer.toString(Defaults.CONNECTION_TIMEOUT_IN_MILLIS)));
		final int socketTimeoutInMillis = Integer.parseInt(configuration.getProperty("socket.timeout.ms", Integer.toString(Defaults.SOCKET_TIMEOUT_IN_MILLIS)));

		telnet.setConnectTimeout(connectionTimeoutInMillis);
		System.out.println("Connecting to: " + routerAddress + ':' + routerPort + "...");
		boolean connected = false;
		try {
			telnet.connect(routerAddress, routerPort);
			connected = true;
			telnet.setSoTimeout(socketTimeoutInMillis);
		}
		catch (Exception e) {
			e.printStackTrace();
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
		System.out.println("Disconnecting...");
		try {
			telnet.disconnect();
		}
		catch (Exception e) {
			e.printStackTrace();
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
		System.out.println("Logging out...");
		writeToTelnet("logout");
	}

	private final void loop() throws IOException, InterruptedException {
		// Determinazione numero di iterazioni...
		int iterations = Integer.parseInt(configuration.getProperty("logger.iterations", Integer.toString(Defaults.ITERATIONS)));
		if (iterations <= 0) {
			iterations = Integer.MAX_VALUE;
		}

		// Iterazione...
		for (int iteration = 1, lastLogLength = 0; iteration <= iterations; iteration++) {
			// Chiamata alle implementazioni specifiche...
			final Map<String, String> info = readInfo();
			saveInfo(info);
			// Fine implementazioni specifiche.

			// Scrittura indice dell'iterazione in console...
			final StringBuilder clean = new StringBuilder();
			while (lastLogLength-- > 0) {
				clean.append('\b');
			}
			final StringBuilder log = new StringBuilder();
			final boolean animate = Boolean.parseBoolean(configuration.getProperty("console.animation", Boolean.toString(Defaults.CONSOLE_ANIMATION)));
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
				for (String keyToShow : configuration.getProperty("console.show.keys", "").split(",")) {
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
			System.out.print(clean.toString() + log.toString());

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
							return Long.parseLong(configuration.getProperty("logger.interval.fast.ms", Long.toString(Defaults.INTERVAL_FAST_IN_MILLIS)));
						}
					}
				}
			}
		}
		return Long.parseLong(configuration.getProperty("logger.interval.normal.ms", Long.toString(Defaults.INTERVAL_NORMAL_IN_MILLIS)));
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
		if (Boolean.parseBoolean(configuration.getProperty("telnet.send.crlf", Boolean.toString(Defaults.TELNET_SEND_CRLF)))) {
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
		final StringBuilder versionInfo = new StringBuilder();
		final String versionNumber = version.getProperty("version.number");
		if (versionNumber != null && !"".equals(versionNumber.trim())) {
			versionInfo.append('v').append(versionNumber.trim()).append(' ');
		}
		String versionDate = version.getProperty("version.date");
		if (versionDate != null && !"".equals(versionDate.trim())) {
			versionInfo.append('(').append(versionDate.trim()).append(") ");
		}

		System.out.println("********** ADSL Modem Router Logger " + versionInfo.toString() + "**********");
		System.out.println();
		boolean lineBreak = false;
		if (getDeviceModel() != null && !"".equals(getDeviceModel().trim())) {
			System.out.println("Device model: " + getDeviceModel().trim() + '.');
			lineBreak = true;
		}
		if (!thresholds.isEmpty()) {
			System.out.println("Thresholds: " + thresholds.toString());
			lineBreak = true;
		}
		if (lineBreak) {
			System.out.println();
		}
	}

	/**
	 * Libera le risorse eventualmente allocate (file, connessioni a database,
	 * ecc.).
	 */
	protected void release() {}

}
