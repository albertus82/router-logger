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

import org.apache.commons.net.telnet.TelnetClient;

public abstract class RouterLogger {
	
	private interface Defaults {
		int ROUTER_PORT = 23;
		int SOCKET_TIMEOUT_IN_MILLIS = 30000;
		int CONNECTION_TIMEOUT_IN_MILLIS = 30000;
		int ITERATIONS = -1;
		long INTERVAL_FAST_IN_MILLIS = 1000;
		long INTERVAL_NORMAL_IN_MILLIS = 5000;
		int RETRIES = 3;
		long RETRY_INTERVAL_IN_MILLIS = 60000;
	}

	private static final String CONFIGURATION_FILE_NAME = "routerlogger.cfg";
	private static final String VERSION_FILE_NAME = "version.properties";
	
	private static final String THRESHOLD_PREFIX = "threshold";
	private static final String THRESHOLD_SUFFIX_KEY = "key";
	private static final String THRESHOLD_SUFFIX_TYPE = "type";
	private static final String THRESHOLD_SUFFIX_VALUE = "value";

	protected final TelnetClient telnet = new TelnetClient();
	protected final Set<Threshold> thresholds = new HashSet<Threshold>();
	protected final Properties configuration = new Properties();
	protected final Properties version = new Properties();
	private char[] animation = { '-', '\\', '|', '/' };

	protected final void run() throws Exception {
		welcome();
		
		boolean end = false;

		int retries = Integer.parseInt(configuration.getProperty("logger.retry.count",Integer.toString(Defaults.RETRIES)));

		for (int index = 0; index <= retries && !end; index++) {
			// Gestione riconnessione in caso di errore...
			if (index > 0) {
				long retryIntervalInMillis = Long.parseLong(configuration.getProperty("logger.retry.interval.ms",Long.toString(Defaults.RETRY_INTERVAL_IN_MILLIS)));
				System.out.println("Waiting for reconnection " + index + '/' + retries + " (" + retryIntervalInMillis + " ms)...");
				Thread.sleep(retryIntervalInMillis);
			}

			// Avvio della procedura...
			connect();
			login();
			try {
				loop();
				end = true; // Se non si sono verificati errori.
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				// In ogni caso, si esegue la disconnessione dal server...
				System.out.println();
				logout();
				disconnect();
			}
		}
		finalize();
		System.out.println("Bye!");
	}

	/**
	 * Estrae le informazioni di interesse da telnet, utilizzando i metodi
	 * {@link #writeToTelnet(String)} e {@link #readFromTelnet(String, boolean)}.
	 * 
	 * @return la mappa contenente le informazioni estratte.
	 * 
	 * @throws IOException
	 */
	protected abstract Map<String, String> readInfo() throws IOException;

	/**
	 * Salva le informazioni di interesse precedentemente estratte con
	 * {@link #readInfo()} con le modalita' desiderate (ad esempio su file o in
	 * un database).
	 * 
	 * @param info  le informazioni da salvare.
	 * 
	 * @throws IOException
	 */
	protected abstract void saveInfo(Map<String, String> info) throws IOException;
	
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
		Set<String> thresholdsAdded = new HashSet<String>();
		for (Object objectKey : configuration.keySet()) {
			String key = (String) objectKey;
			if (key.startsWith(THRESHOLD_PREFIX + '.')) {
				final String thresholdName = key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'));
				if (thresholdsAdded.contains(thresholdName)) {
					continue;
				}
				final String thresholdKey = configuration.getProperty(THRESHOLD_PREFIX + '.' + thresholdName + '.' + THRESHOLD_SUFFIX_KEY);
				final Type thresholdType = Type.findByName(configuration.getProperty(THRESHOLD_PREFIX + '.' + thresholdName + '.' + THRESHOLD_SUFFIX_TYPE));
				final String thresholdValue = configuration.getProperty(THRESHOLD_PREFIX + '.' + thresholdName + '.' + THRESHOLD_SUFFIX_VALUE);
				if (thresholdKey == null || "".equals(thresholdKey) || thresholdValue == null || thresholdType == null) {
					throw new IllegalArgumentException("Threshold misconfigured: \"" + thresholdName + "\".");
				}
				thresholds.add(new Threshold(thresholdKey, thresholdType, thresholdValue));
				thresholdsAdded.add(thresholdName);
			}
		}
	}

	private void loadVersion() throws IOException {
		InputStream inputStream = getClass().getResourceAsStream('/' + VERSION_FILE_NAME);
		if (inputStream != null) {
			version.load(inputStream);
			inputStream.close();
		}
	}

	private void loadConfiguration() throws IOException {
		InputStream inputStream;
		File config = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() + '/' + CONFIGURATION_FILE_NAME);
		if (config.exists()) {
			inputStream = new BufferedInputStream(new FileInputStream(config));
		}
		else {
			inputStream = new BufferedInputStream(getClass().getResourceAsStream('/' + CONFIGURATION_FILE_NAME));
		}
		configuration.load(inputStream);
		inputStream.close();
	}

	/** Effettua la connessione al server telnet, ma non l'autenticazione. */
	private final void connect() throws Exception {
		String routerAddress = configuration.getProperty("router.address");
		int routerPort = Integer.parseInt(configuration.getProperty("router.port", Integer.toString(Defaults.ROUTER_PORT)));
		int connectionTimeoutInMillis = Integer.parseInt(configuration.getProperty("connection.timeout.ms", Integer.toString(Defaults.CONNECTION_TIMEOUT_IN_MILLIS)));
		int socketTimeoutInMillis = Integer.parseInt(configuration.getProperty("socket.timeout.ms", Integer.toString(Defaults.SOCKET_TIMEOUT_IN_MILLIS)));

		System.out.println("Connecting to: " + routerAddress + ':' + routerPort + "...");
		try {
			telnet.connect(routerAddress, routerPort);
			telnet.setConnectTimeout(connectionTimeoutInMillis);
			telnet.setSoTimeout(socketTimeoutInMillis);
		}
		catch (Exception e) {
			disconnect();
			throw e;
		}
	}

	/**
	 * Effettua la disconnessione dal server telnet, ma non invia alcun comando
	 * di logout.
	 */
	protected final void disconnect() {
		System.out.println("Disconnecting...");
		try {
			telnet.disconnect();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Effettua l'autenticazione sul server telnet, utilizzando i metodi
	 * {@link #readFromTelnet(char, boolean)} e {@link #writeToTelnet(String)}
	 * per interagire con il server e comunicare le credenziali di accesso.
	 * 
	 * @throws Exception
	 */
	protected abstract void login() throws Exception;

	/**
	 * Effettua il logout dal server telnet inviando il comando
	 * <code>logout</code>. E' possibile sovrascrivere questo metodo per
	 * aggiungere altri o diversi comandi che debbano essere eseguiti in fase di
	 * logout.
	 */
	protected void logout() {
		System.out.println("Logging out...");
		try {
			writeToTelnet("logout");
		}
		catch (IOException e) {
			disconnect();
		}
	}

	private final void loop() throws IOException, InterruptedException {
		// Determinazione numero di iterazioni...
		String iterationsProperty = configuration.getProperty("logger.iterations", Integer.toString(Defaults.ITERATIONS));
		final int iterations;
		int iterationsPropertyNumeric = Integer.parseInt(iterationsProperty);
		if (iterationsPropertyNumeric > 0) {
			iterations = iterationsPropertyNumeric;
		}
		else {
			iterations = Integer.MAX_VALUE;
		}

		// Iterazione...
		for (int iteration = 1, lastLogLength = 0; iteration <= iterations; iteration++) {
			// Chiamata alle implementazioni specifiche...
			final Map<String, String> info = readInfo();
			saveInfo(info);
			// Fine implementazioni specifiche.

			// Scrittura indice dell'iterazione in console...
			StringBuilder clean = new StringBuilder();
			while (lastLogLength-- > 0) {
				clean.append('\b');
			}
			StringBuilder log = new StringBuilder();
			boolean animate = Boolean.parseBoolean(configuration.getProperty("console.animation"));
			if (animate) {
				log.append(animation[(iteration & ((1 << 2) - 1))]).append(' ');
			}
			log.append(iteration).append(' ');
			if (animate) {
				log.append(animation[(iteration & ((1 << 2) - 1))]).append(' ');
			}
			
			// Stampa informazioni aggiuntive richieste...
			final StringBuilder infoToShow = new StringBuilder();
			for (String key : configuration.getProperty("console.show.keys", "").split(",")) {
				key = key.trim();
				if (info.containsKey(key)) {
					if (infoToShow.length() == 0) {
						infoToShow.append('[');
					}
					else {
						infoToShow.append(", ");
					}
					infoToShow.append(key + ": " + info.get(key));
				}
			}
			if (infoToShow.length() != 0) {
				infoToShow.append("] ");
			}
			log.append(infoToShow);
			
			lastLogLength = log.length();
			System.out.print(clean.toString() + log.toString());

			// All'ultimo giro non deve esserci il tempo di attesa tra le iterazioni.
			if (iteration != iterations) {
				long wait = Long.parseLong(configuration.getProperty("logger.interval.normal.ms", Long.toString(Defaults.INTERVAL_NORMAL_IN_MILLIS)));
				for (Threshold threshold : thresholds) {
					try {
						if (info.keySet().contains(threshold.getKey()) && threshold.isReached(info.get(threshold.getKey()))) {
							wait = Long.parseLong(configuration.getProperty("logger.interval.fast.ms", Long.toString(Defaults.INTERVAL_FAST_IN_MILLIS)));
							break;
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				Thread.sleep(wait);
			}
		}
	}

	protected String writeToTelnet(String command) throws IOException {
		OutputStream out = telnet.getOutputStream();
		StringBuilder echo = new StringBuilder();
		for (char character : command.toCharArray()) {
			if (character == '\n' || character == '\r') {
				break;
			}
			out.write(character);
			echo.append(character);
		}
		out.flush();
		// Thread.sleep(50);
		out.write('\n');
		echo.append('\n');
		out.flush();
		return echo.toString();
	}

	protected String readFromTelnet(String until, boolean inclusive) throws IOException {
		InputStream in = telnet.getInputStream();
		char lastChar = until.charAt(until.length() - 1);
		StringBuilder text = new StringBuilder();
		int currentByte;
		while ((currentByte = in.read()) != -1) {
			char currentChar = (char) currentByte;
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
		StringBuilder versionInfo = new StringBuilder();
		String versionNumber = version.getProperty("version.number");
		if ( versionNumber != null && !"".equals(versionNumber.trim())) {
			versionInfo.append('v').append(versionNumber.trim()).append(' ');
		}
		String versionDate = version.getProperty("version.date");
		if ( versionDate != null && !"".equals(versionDate.trim())) {
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
	 * Da implementare con la logica che libera le risorse eventualmente
	 * allocate (file, connessioni a database, ecc.).
	 */
	@Override
	protected void finalize() {
		try {
			super.finalize();
		}
		catch (Throwable t) {
		}
	}

}