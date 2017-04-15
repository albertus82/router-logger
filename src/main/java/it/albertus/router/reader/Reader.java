package it.albertus.router.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.net.telnet.TelnetClient;

import it.albertus.jface.JFaceMessages;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Messages;
import it.albertus.util.ConfigurationException;
import it.albertus.util.NewLine;
import it.albertus.util.logging.LoggerFactory;

public abstract class Reader implements IReader {

	private static final Logger logger = LoggerFactory.getLogger(Reader.class);

	protected static final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	private static final String MSG_KEY_ERR_CONFIGURATION_REVIEW = "err.configuration.review";

	public static class Defaults {
		public static final String ROUTER_ADDRESS = "192.168.1.1";
		public static final int ROUTER_PORT = 23;
		public static final int SOCKET_TIMEOUT_IN_MILLIS = 30000;
		public static final int CONNECTION_TIMEOUT_IN_MILLIS = 20000;
		public static final String TELNET_NEWLINE_CHARACTERS = NewLine.CRLF.name();

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected static final String CFG_KEY_SOCKET_TIMEOUT_MS = "socket.timeout.ms";
	protected static final String CFG_KEY_CONNECTION_TIMEOUT_MS = "connection.timeout.ms";
	protected static final String CFG_KEY_ROUTER_PORT = "router.port";
	protected static final String CFG_KEY_ROUTER_ADDRESS = "router.address";

	protected static final String LOG_MASK_TELNET = NewLine.SYSTEM_LINE_SEPARATOR + "{0}";

	private static final String MSG_KEY_ERR_CONFIGURATION_INVALID = "err.configuration.invalid";

	protected final TelnetClient telnet = new TelnetClient();

	@Override
	public boolean connect() {
		/* Verifica dei parametri di configurazione... */
		final String routerAddress;
		try {
			routerAddress = configuration.getString(CFG_KEY_ROUTER_ADDRESS, Defaults.ROUTER_ADDRESS).trim();
		}
		catch (final Exception exception) {
			throw new ConfigurationException(JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_INVALID, CFG_KEY_ROUTER_ADDRESS) + ' ' + JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_REVIEW, configuration.getFileName()), exception, CFG_KEY_ROUTER_ADDRESS);
		}

		final int routerPort;
		try {
			routerPort = configuration.getInt(CFG_KEY_ROUTER_PORT, Defaults.ROUTER_PORT);
		}
		catch (final Exception exception) {
			throw new ConfigurationException(JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_INVALID, CFG_KEY_ROUTER_PORT) + ' ' + JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_REVIEW, configuration.getFileName()), exception, CFG_KEY_ROUTER_PORT);
		}

		final int connectionTimeoutInMillis;
		try {
			connectionTimeoutInMillis = configuration.getInt(CFG_KEY_CONNECTION_TIMEOUT_MS, Defaults.CONNECTION_TIMEOUT_IN_MILLIS);
		}
		catch (final Exception exception) {
			throw new ConfigurationException(JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_INVALID, CFG_KEY_CONNECTION_TIMEOUT_MS) + ' ' + JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_REVIEW, configuration.getFileName()), exception, CFG_KEY_CONNECTION_TIMEOUT_MS);
		}

		final int socketTimeoutInMillis;
		try {
			socketTimeoutInMillis = configuration.getInt(CFG_KEY_SOCKET_TIMEOUT_MS, Defaults.SOCKET_TIMEOUT_IN_MILLIS);
		}
		catch (final Exception exception) {
			throw new ConfigurationException(JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_INVALID, CFG_KEY_SOCKET_TIMEOUT_MS) + ' ' + JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_REVIEW, configuration.getFileName()), exception, CFG_KEY_SOCKET_TIMEOUT_MS);
		}

		/* Connessione... */
		telnet.setConnectTimeout(connectionTimeoutInMillis);
		logger.log(Level.INFO, Messages.get("msg.connecting"), new Object[] { routerAddress, routerPort });
		boolean connected = false;
		try {
			telnet.connect(routerAddress, routerPort);
			connected = true;
			telnet.setSoTimeout(socketTimeoutInMillis);
		}
		catch (final Exception e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
		return connected;
	}

	@Override
	public void logout() throws IOException {
		logger.info(Messages.get("msg.logging.out"));
		writeToTelnet("exit");
	}

	@Override
	public void disconnect() {
		logger.info(Messages.get("msg.disconnecting"));
		try {
			telnet.disconnect();
		}
		catch (final IOException e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
	}

	@Override
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
		final OutputStream os = telnet.getOutputStream();
		final StringBuilder echo = new StringBuilder();
		for (final char character : command.toCharArray()) {
			if (character == '\n' || character == '\r') {
				break;
			}
			os.write(character);
			echo.append(character);
		}
		os.flush();
		// Thread.sleep(50);
		for (final char character : NewLine.getEnum(configuration.getString("telnet.newline.characters", Defaults.TELNET_NEWLINE_CHARACTERS)).toCharArray()) {
			os.write(character);
			echo.append(character);
		}
		os.flush();
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
		final OutputStream os = telnet.getOutputStream();
		if (password != null) {
			for (final char character : password) {
				if (character == '\n' || character == '\r') {
					break;
				}
				os.write(character);
			}
			os.flush();
		}
		// Thread.sleep(50);
		for (final char character : NewLine.getEnum(configuration.getString("telnet.newline.characters", Defaults.TELNET_NEWLINE_CHARACTERS)).toCharArray()) {
			os.write(character);
		}
		os.flush();
	}

	@Override
	public void release() {}

}
