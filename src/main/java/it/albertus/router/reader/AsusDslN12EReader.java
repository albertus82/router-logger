package it.albertus.router.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import it.albertus.router.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

/**
 * <b>ASUS DSL-N12E</b>. Comandi Telnet disponibili (case sensitive):
 * <ul>
 * <li><tt><b>show wan adsl</b></tt></li>
 * <li><tt><b>show wan interface</b></tt></li>
 * </ul>
 */
public class AsusDslN12EReader extends Reader {

	private static final Logger logger = LoggerFactory.getLogger(AsusDslN12EReader.class);

	public static class Defaults {
		public static final String COMMAND_INFO_ADSL = "show wan adsl";
		public static final String COMMAND_INFO_WAN = "show wan interface";

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public static final String DEVICE_MODEL_KEY = "lbl.device.model.asus.n12e";

	protected static final String COMMAND_PROMPT = "$";
	protected static final String LOGIN_PROMPT = ": ";

	@Override
	public boolean login(final String username, final char[] password) throws IOException {
		String received;

		// Username...
		received = readFromTelnet(LOGIN_PROMPT, true).trim();
		logger.info(LOG_PREFIX_TELNET + received);
		writeToTelnet(username);

		// Password...
		received = readFromTelnet(LOGIN_PROMPT, true).trim();
		logger.info(LOG_PREFIX_TELNET + received);
		writeToTelnet(password);

		// Avanzo fino al prompt dei comandi
		received = readFromTelnet(COMMAND_PROMPT, true);
		logger.info(LOG_PREFIX_TELNET + received);

		return true;
	}

	@Override
	public LinkedHashMap<String, String> readInfo() throws IOException {
		// Informazioni sulla portante ADSL...
		writeToTelnet(configuration.getString("asus.dsln12e.command.info.adsl", Defaults.COMMAND_INFO_ADSL));
		readFromTelnet("wan adsl", true); // Avanzamento del reader fino all'inizio dei dati di interesse.
		final LinkedHashMap<String, String> info = new LinkedHashMap<String, String>();
		BufferedReader reader = new BufferedReader(new StringReader(readFromTelnet(COMMAND_PROMPT, false).trim()));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() > 2) {
				int splitIndex = -1;
				for (int i = 1; i < line.length(); i++) {
					if (line.charAt(i) == ' ' && line.charAt(i - 1) == ' ') {
						splitIndex = i;
						break;
					}
				}
				if (splitIndex != -1) {
					String key = line.substring(0, splitIndex).trim();
					String value = line.substring(splitIndex).trim();
					info.put(key, value);
				}
			}
		}
		reader.close();

		// Informazioni sulla connessione ad Internet...
		writeToTelnet(configuration.getString("asus.dsln12e.command.info.wan", Defaults.COMMAND_INFO_WAN));
		readFromTelnet("Status", true);
		List<String> values = new ArrayList<String>(8);
		for (String field : readFromTelnet(COMMAND_PROMPT, false).trim().split("(\\s\\s)+")) {
			if (field != null && field.trim().length() != 0) {
				values.add(field.trim());
			}
		}
		info.put("Interface", values.get(0));
		info.put("VPI/VCI", values.get(1).replace('/', '\\'));
		info.put("Encap", values.get(2));
		info.put("Droute", values.get(3));
		info.put("Protocol", values.get(4));
		info.put("IP Address", values.get(5));
		info.put("Gateway", values.get(6));
		info.put("Status", values.get(7));

		return info;
	}

	@Override
	public String getDeviceModel() {
		return Messages.get(DEVICE_MODEL_KEY);
	}

}
