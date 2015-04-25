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
	private static final String VERSION_FILE_PATH = "/";
	private static final String VERSION_FILE_NAME = "version.properties";
	protected static final Properties version = new Properties();
	
	static {
		InputStream is = RouterLogger.class.getResourceAsStream(VERSION_FILE_PATH + VERSION_FILE_NAME);
		if (is != null) {
			try {
				version.load(is);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				try {
					is.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private final TelnetClient telnet = new TelnetClient();
	private InputStream in;
	private OutputStream out;
	private final Map<String, String> info = new LinkedHashMap<String, String>();
	protected final Properties configuration = new Properties();

	public void run() throws Exception {
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
	}

	/**
	 * Da implementare con la logica che estrae le informazioni di interesse da
	 * telnet, utilizzando i metodi {@link #writeToTelnet(String)} e
	 * {@link #readFromTelnet(char, boolean)}.
	 * 
	 * @return la mappa contenente le informazioni estratte.
	 * 
	 * @throws IOException
	 */
	protected abstract Map<String, String> readInfo() throws IOException;

	/**
	 * Da implementare con la logica che salva le informazioni di interesse
	 * precedentemente estratte con {@link #readInfo()} con le modalita'
	 * desiderate (ad esempio su file o in un database).
	 * 
	 * @param info le informazioni da salvare.
	 * 
	 * @throws IOException
	 */
	protected abstract void saveInfo(Map<String, String> info) throws IOException;

	public RouterLogger() {
		try {
			BufferedInputStream reader;
			File config = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() + '/' + CONFIGURATION_FILE_NAME);
			if (config.exists()) {
				reader = new BufferedInputStream(new FileInputStream(config));
			}
			else {
				reader = new BufferedInputStream(getClass().getResourceAsStream('/' + CONFIGURATION_FILE_NAME));
			}
			configuration.load(reader);
			reader.close();
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
	 * Da implementare con la logica che effettua l'autenticazione sul server
	 * telnet, utilizzando i metodi {@link #readFromTelnet(char, boolean)} e
	 * {@link #writeToTelnet(String)} per interagire con il server e comunicare
	 * le credenziali di accesso.
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

	protected final void loop() throws IOException, InterruptedException {
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
		for (int iteration = 1, consoleColumn = 0; iteration <= iterations; iteration++) {
			if (consoleColumn > 60) {
				System.out.println();
				consoleColumn = 0;
			}

			// Chiamata alle implementazioni specifiche...
			info.putAll(readInfo());
			saveInfo(info);
			// Fine implementazioni specifiche.

			String log = iteration + " ";
			System.out.print(log);
			consoleColumn += log.length();

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

}