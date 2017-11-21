package it.albertus.routerlogger.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.routerlogger.resources.Messages;
import it.albertus.util.IOUtils;
import it.albertus.util.logging.LoggerFactory;

/**
 * <b>ASUS DSL-N14U</b>. Comandi Telnet disponibili (case sensitive):
 * <ul>
 * <li><tt><b>tcapi show Info</b></tt></li>
 * <li><tt><b>tcapi show Info_<i>Node</i></b></tt> (si consiglia:
 * <tt><b>tcapi show Info_Adsl</b></tt>)</li>
 * <li><tt><b>tcapi show Wan</b></tt> (sconsigliato, verboso)</li>
 * <li><tt><b>tcapi show Wan_<i>Node</i></b></tt> (si consiglia:
 * <tt><b>tcapi show Wan_PVC0</b></tt>)</li>
 * </ul>
 */
public class AsusDslN14UReader extends Reader {

	private static final Logger logger = LoggerFactory.getLogger(AsusDslN14UReader.class);

	public static class Defaults {
		public static final String COMMAND_INFO_ADSL = "tcapi show Info_Adsl";

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public static final String DEVICE_MODEL_KEY = "lbl.device.model.asus.n14u";

	protected static final String COMMAND_PROMPT = "# ";
	protected static final String LOGIN_PROMPT = ": ";
	protected static final String NODE_PREFIX = "Node:";

	@Override
	public boolean login(final String username, final char[] password) throws IOException {
		final StringBuilder received = new StringBuilder();

		// Username...
		received.append(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(username);

		// Password...
		received.append(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(password);

		logger.log(Level.INFO, LOG_MASK_TELNET, received);
		received.setLength(0);

		// Avanzamento fino al prompt...
		received.append(readFromTelnet(COMMAND_PROMPT, true).trim());
		logger.log(Level.INFO, LOG_MASK_TELNET, received);

		return true;
	}

	@Override
	public LinkedHashMap<String, String> readInfo() throws IOException {
		final LinkedHashMap<String, String> info = new LinkedHashMap<String, String>();

		// Informazioni sulla portante ADSL...
		info.putAll(execute(configuration.getString("asus.dsln14u.command.info.adsl", Defaults.COMMAND_INFO_ADSL)));

		// Informazioni sulla connessione ad Internet...
		final String command = configuration.getString("asus.dsln14u.command.info.wan");
		if (command != null && !command.trim().isEmpty()) {
			info.putAll(execute(command));
		}

		return info;
	}

	private Map<String, String> execute(final String command) throws IOException {
		final Map<String, String> info = new LinkedHashMap<String, String>();
		writeToTelnet(command);
		readFromTelnet(command, false); // Avanzamento del reader fino all'inizio dei dati di interesse.
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(readFromTelnet(COMMAND_PROMPT, false).trim()));
			String line;
			String prefix = "";
			while ((line = reader.readLine()) != null) {
				if (!line.trim().isEmpty()) {
					if (line.startsWith(NODE_PREFIX)) {
						prefix = line.substring(NODE_PREFIX.length()).trim() + '_';
					}
					else if (line.indexOf('=') != -1) {
						info.put(prefix + line.substring(0, line.indexOf('=')).trim(), line.substring(line.indexOf('=') + 1).trim());
					}
				}
			}
		}
		finally {
			IOUtils.closeQuietly(reader);
		}
		return info;
	}

	@Override
	public String getDeviceModel() {
		return Messages.get(DEVICE_MODEL_KEY);
	}

	@Override
	public String getImageFileName() {
		return "asus_dsl_n14u.png";
	}

}
