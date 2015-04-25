package it.albertus.router.tplink;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.net.telnet.TelnetClient;

public abstract class RouterLogger {

	private final TelnetClient telnet = new TelnetClient();
	private InputStream in;
	private OutputStream out;
	protected final Map<String, String> info = new LinkedHashMap<String, String>();
	protected final Properties configuration = new Properties();

	/**
	 * Da implementare con la logica che estrae le informazioni di interesse da
	 * telnet, utilizzando i metodi {@link #writeToTelnet(String)} e
	 * {@link #readFromTelnet(char, boolean)}.
	 */
	protected abstract void readInfo() throws IOException;

	/**
	 * Da implementare con la logica che salva le informazioni di interesse
	 * precedentemente estratte con {@link #readInfo()} con le modalita'
	 * desiderate (ad esempio su file o in un database).
	 */
	protected abstract void saveInfo() throws IOException;

	public RouterLogger() {
		try {
			BufferedInputStream reader = new BufferedInputStream(TpLinkLogger.class.getResourceAsStream("/logger.cfg"));
			configuration.load(reader);
			reader.close();
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	/** Effettua la connessione al server telnet, ma non l'autenticazione. */
	private final void connect() throws IOException {
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
	 */
	protected abstract void login() throws IOException;

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
			readInfo();
			saveInfo();
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
	
	public void run() throws IOException, InterruptedException {
		System.out.println("***** TP-Link TD-W8970 ADSL Modem Router Logger *****");
		System.out.println();

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

}