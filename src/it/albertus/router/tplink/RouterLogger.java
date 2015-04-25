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
	protected void connect() throws IOException {
		String routerAddress = configuration.getProperty("router.address");
		int routerPort = Integer.parseInt(configuration.getProperty("router.port"));
		int connectionTimeout = Integer.parseInt(configuration.getProperty("connection.timeout.ms"));
		int socketTimeout = Integer.parseInt(configuration.getProperty("socket.timeout.ms"));

		System.out.println("Connecting to: " + routerAddress + ':' + routerPort + "...");
		try {
			telnet.connect(routerAddress, routerPort);
			telnet.setConnectTimeout(connectionTimeout);
			telnet.setSoTimeout(socketTimeout);
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
	protected void disconnect() {
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
	 * aggiungere altri eventuali comandi che debbano essere eseguiti in fase di
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

	protected void loop() throws IOException, InterruptedException {
		int iterations = Integer.parseInt(configuration.getProperty("logger.iterations"));
		int iteration = 0;
		byte consoleColumn = 0;
		while (iteration < (iterations > 0 ? iterations : Integer.MAX_VALUE)) {
			if (consoleColumn > 60) {
				System.out.println();
				consoleColumn = 0;
			}

			// Chiamata alle implementazioni specifiche...
			readInfo();
			saveInfo();
			// Fine implementazioni specifiche.

			String log = ++iteration + " ";
			System.out.print(log);
			consoleColumn += log.length();
			Thread.sleep(Long.parseLong(configuration.getProperty("logger.interval.ms")));
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