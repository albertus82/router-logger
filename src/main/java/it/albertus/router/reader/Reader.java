package it.albertus.router.reader;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.util.ConfigurationException;
import it.albertus.util.Console;
import it.albertus.util.NewLine;
import it.albertus.util.SystemConsole;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;

import org.apache.commons.net.telnet.TelnetClient;

public abstract class Reader {

	public interface Defaults {
		String ROUTER_ADDRESS = "192.168.1.1";
		int ROUTER_PORT = 23;
		int SOCKET_TIMEOUT_IN_MILLIS = 30000;
		int CONNECTION_TIMEOUT_IN_MILLIS = 20000;
		String TELNET_NEWLINE_CHARACTERS = NewLine.CRLF.name();
	}

	protected static final String CFG_KEY_SOCKET_TIMEOUT_MS = "socket.timeout.ms";
	protected static final String CFG_KEY_CONNECTION_TIMEOUT_MS = "connection.timeout.ms";
	protected static final String CFG_KEY_ROUTER_PORT = "router.port";
	protected static final String CFG_KEY_ROUTER_ADDRESS = "router.address";

	protected final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	protected final Logger logger = Logger.getInstance();
	protected final Console out = SystemConsole.getInstance();
	protected final TelnetClient telnet = new TelnetClient();

	/**
	 * Effettua la connessione al server Telnet, ma non l'autenticazione.
	 * <b>Normalmente non occorre sovrascrivere questo metodo</b>.
	 * 
	 * @return <tt>true</tt> se la connessione &egrave; riuscita, <tt>false</tt>
	 *         altrimenti.
	 * 
	 * @throws ConfigurationException se i parametri di connessione non sono
	 *         validi.
	 */
	public boolean connect() {
		/* Verifica dei parametri di configurazione... */
		final String routerAddress;
		try {
			routerAddress = configuration.getString(CFG_KEY_ROUTER_ADDRESS, Defaults.ROUTER_ADDRESS).trim();
		}
		catch (final Exception exception) {
			throw new ConfigurationException(Resources.get("err.invalid.cfg", CFG_KEY_ROUTER_ADDRESS) + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), exception, CFG_KEY_ROUTER_ADDRESS);
		}

		final int routerPort;
		try {
			routerPort = configuration.getInt(CFG_KEY_ROUTER_PORT, Defaults.ROUTER_PORT);
		}
		catch (final Exception exception) {
			throw new ConfigurationException(Resources.get("err.invalid.cfg", CFG_KEY_ROUTER_PORT) + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), exception, CFG_KEY_ROUTER_PORT);
		}

		final int connectionTimeoutInMillis;
		try {
			connectionTimeoutInMillis = configuration.getInt(CFG_KEY_CONNECTION_TIMEOUT_MS, Defaults.CONNECTION_TIMEOUT_IN_MILLIS);
		}
		catch (final Exception exception) {
			throw new ConfigurationException(Resources.get("err.invalid.cfg", CFG_KEY_CONNECTION_TIMEOUT_MS) + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), exception, CFG_KEY_CONNECTION_TIMEOUT_MS);
		}

		final int socketTimeoutInMillis;
		try {
			socketTimeoutInMillis = configuration.getInt(CFG_KEY_SOCKET_TIMEOUT_MS, Defaults.SOCKET_TIMEOUT_IN_MILLIS);
		}
		catch (final Exception exception) {
			throw new ConfigurationException(Resources.get("err.invalid.cfg", CFG_KEY_SOCKET_TIMEOUT_MS) + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), exception, CFG_KEY_SOCKET_TIMEOUT_MS);
		}

		/* Connessione... */
		telnet.setConnectTimeout(connectionTimeoutInMillis);
		out.println(Resources.get("msg.connecting", routerAddress, routerPort), true);
		boolean connected = false;
		try {
			telnet.connect(routerAddress, routerPort);
			connected = true;
			telnet.setSoTimeout(socketTimeoutInMillis);
		}
		catch (Exception e) {
			logger.log(e);
		}
		return connected;
	}

	/**
	 * Effettua l'autenticazione sul server Telnet, utilizzando i metodi
	 * {@link #readFromTelnet(String, boolean)} e {@link #writeToTelnet(String)}
	 * per interagire con il server e comunicare le credenziali di accesso.
	 * 
	 * @param username proveniente dalla propriet&agrave;
	 *        <tt>router.username</tt> del file di configurazione.
	 * @param password proveniente dalla propriet&agrave;
	 *        <tt>router.password</tt> del file di configurazione.
	 * 
	 * @return <tt>true</tt> se l'autenticazione &egrave; riuscita,
	 *         <tt>false</tt> altrimenti.
	 * 
	 * @throws IOException in caso di errore nella comunicazione con il server.
	 */
	public abstract boolean login(String username, char[] password) throws IOException;

	/**
	 * Effettua il logout dal server Telnet inviando il comando <tt>exit</tt>.
	 * &Egrave; possibile sovrascrivere questo metodo per aggiungere altri o
	 * diversi comandi che debbano essere eseguiti in fase di logout. <b>Questo
	 * metodo non effettua esplicitamente la disconnessione dal server</b>.
	 * 
	 * @throws IOException in caso di errore nella comunicazione con il server.
	 */
	public void logout() throws IOException {
		out.println(Resources.get("msg.logging.out"), true);
		writeToTelnet("exit");
	}

	/**
	 * Effettua la disconnessione dal server Telnet, ma non invia alcun comando
	 * di logout. &Egrave; buona norma richiamare prima il metodo
	 * {@link #logout()} per inviare al server Telnet gli opportuni comandi di
	 * chiusura della sessione (ad esempio <tt>logout</tt>). <b>Normalmente non
	 * occorre sovrascrivere questo metodo</b>.
	 */
	public void disconnect() {
		out.println(Resources.get("msg.disconnecting"), true);
		try {
			telnet.disconnect();
		}
		catch (final NullPointerException npe) {/* Ignore */}
		catch (final IOException ioe) {
			logger.log(ioe);
		}
	}

	/**
	 * Estrae le informazioni di interesse dai dati ricevuti dal server Telnet,
	 * utilizzando i metodi {@link #writeToTelnet(String)} e
	 * {@link #readFromTelnet(String, boolean)}.
	 * 
	 * @return la mappa contenente le informazioni estratte.
	 * @throws IOException in caso di errore nella lettura dei dati.
	 */
	public abstract LinkedHashMap<String, String> readInfo() throws IOException;

	/**
	 * Restituisce una stringa contenente marca e modello del router relativo
	 * all'implementazione realizzata.
	 */
	public String getDeviceModel() {
		return getClass().getSimpleName();
	}

	/**
	 * Legge i dati inviati dal server Telnet fin quando non incontra la stringa
	 * limite passata come parametro. <b>Se la stringa non viene trovata e lo
	 * stream si esaurisce, il programma si blocca in attesa di altri dati dal
	 * server</b>, che potrebbero non arrivare mai.
	 * 
	 * @param until la stringa limite che determina la fine della lettura.
	 * @param inclusive determina l'inclusione o meno della stringa limite
	 *        all'interno della stringa restituita.
	 * @return la stringa contenente i dati ricevuti dal server Telnet.
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

	/**
	 * Invia un comando al server Telnet. La stringa passata viene
	 * automaticamente inviata al server e non occorre la presenza del carattere
	 * <tt>\n</tt>. Se nella stringa sono presenti caratteri <tt>\n</tt> o
	 * <tt>\r</tt>, questa viene troncata alla prima occorrenza di uno di questi
	 * caratteri.
	 * 
	 * @param command il comando da inviare al server Telnet.
	 * @return l'eco del testo inviato al server Telnet.
	 * @throws IOException in caso di errore nella comunicazione con il server.
	 * @throws NullPointerException se il comando fornito &egrave; null.
	 */
	protected String writeToTelnet(final String command) throws IOException {
		final OutputStream out = telnet.getOutputStream();
		final StringBuilder echo = new StringBuilder();
		for (final char character : command.toCharArray()) {
			if (character == '\n' || character == '\r') {
				break;
			}
			out.write(character);
			echo.append(character);
		}
		out.flush();
		// Thread.sleep(50);
		for (final char character : NewLine.getEnum(configuration.getString("telnet.newline.characters", Defaults.TELNET_NEWLINE_CHARACTERS)).toCharArray()) {
			out.write(character);
			echo.append(character);
		}
		out.flush();
		return echo.toString();
	}

	/**
	 * Invia una <b>password</b> o altro contenuto sensibile al server Telnet.
	 * L'array di caratteri passato viene automaticamente inviato al server e
	 * non occorre la presenza del carattere <tt>\n</tt>. Se nell'array sono
	 * presenti caratteri <tt>\n</tt> o <tt>\r</tt>, questo viene troncato alla
	 * prima occorrenza di uno di questi caratteri.
	 * 
	 * @param password l'array di caratteri da inviare al server Telnet
	 *        (tipicamente una password).
	 * @throws IOException in caso di errore nella comunicazione con il server.
	 */
	protected void writeToTelnet(final char[] password) throws IOException {
		final OutputStream out = telnet.getOutputStream();
		if (password != null) {
			for (final char character : password) {
				if (character == '\n' || character == '\r') {
					break;
				}
				out.write(character);
			}
			out.flush();
		}
		// Thread.sleep(50);
		for (final char character : NewLine.getEnum(configuration.getString("telnet.newline.characters", Defaults.TELNET_NEWLINE_CHARACTERS)).toCharArray()) {
			out.write(character);
		}
		out.flush();
	}

	public void release() {}

}
