package it.albertus.router;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.net.telnet.TelnetClient;

public abstract class RouterLogger {
	
	private static final String CONFIGURATION_FILE_NAME = "routerlogger.cfg";
	private static final String VERSION_FILE_NAME = "version.properties";
	
	private final TelnetClient telnet = new TelnetClient();
	protected InputStream in;
	protected OutputStream out;
	private final Map<String, String> info = new LinkedHashMap<String, String>();
	protected final Properties configuration = new Properties();
	protected final Properties version = new Properties();

	protected final void run() throws Exception {
		welcome();
		
		boolean end = false;

		int retries = Integer.parseInt(configuration.getProperty("logger.retry.count"));

		for (int index = 0; index <= retries && !end; index++) {
			// Gestione riconnessione in caso di errore...
			if (index > 0) {
				long retryIntervalInMillis = Long.parseLong(configuration.getProperty("logger.retry.interval.ms"));
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
	 * {@link #writeToTelnet(String)} e {@link #readFromTelnet(char, boolean)}.
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

	public RouterLogger() {
		try {
			// Caricamento file di configurazione...
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

			// Caricamento file versione...
			inputStream = getClass().getResourceAsStream('/' + VERSION_FILE_NAME);
			if (inputStream != null) {
				version.load(inputStream);
				inputStream.close();
			}
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	/** Effettua la connessione al server telnet, ma non l'autenticazione. */
	private final void connect() throws Exception {
		String routerAddress = configuration.getProperty("router.address");
		int routerPort = Integer.parseInt(configuration.getProperty("router.port"));
		int connectionTimeoutInMillis = Integer.parseInt(configuration.getProperty("connection.timeout.ms"));
		int socketTimeoutInMillis = Integer.parseInt(configuration.getProperty("socket.timeout.ms"));

		System.out.println("Connecting to: " + routerAddress + ':' + routerPort + "...");
		try {
			telnet.connect(routerAddress, routerPort);
			telnet.setConnectTimeout(connectionTimeoutInMillis);
			telnet.setSoTimeout(socketTimeoutInMillis);
			in = telnet.getInputStream();
			out = telnet.getOutputStream();
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
		String iterationsProperty = configuration.getProperty("logger.iterations");
		final int iterations;
		if (iterationsProperty != null) {
			int iterationsPropertyNumeric = Integer.parseInt(iterationsProperty);
			if (iterationsPropertyNumeric > 0) {
				iterations = iterationsPropertyNumeric;
			}
			else {
				iterations = Integer.MAX_VALUE;
			}
		}
		else {
			iterations = Integer.MAX_VALUE;
		}

		// Iterazione...
		for (int iteration = 1, lastLogLength = 0; iteration <= iterations; iteration++) {
			// Chiamata alle implementazioni specifiche...
			info.putAll(readInfo());
			saveInfo(info);
			// Fine implementazioni specifiche.

			// Scrittura indice dell'iterazione in console...
			String clean = "";
			while (lastLogLength-- > 0) {
				clean += '\b';
			}
			String log = Integer.toString(iteration) + ' ';
			lastLogLength = log.length();
			System.out.print(clean + log);

			// All'ultimo giro non deve esserci il tempo di attesa tra le iterazioni.
			if (iteration != iterations) {
				Thread.sleep(Long.parseLong(configuration.getProperty("logger.interval.ms")));
			}
		}
	}

	protected String writeToTelnet(String command) throws IOException {
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

	protected String readFromTelnet(char until, boolean inclusive) throws IOException {
		StringBuilder text = new StringBuilder();
		char character;
		while ((character = (char) in.read()) != -1) {
			if (character == until) {
				if (inclusive) {
					text.append(character);
				}
				break;
			}
			text.append(character);
		}
		return text.toString().trim();
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
		if (getDeviceModel() != null && !"".equals(getDeviceModel().trim())) {
			System.out.println("Device model: " + getDeviceModel().trim() + '.');
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