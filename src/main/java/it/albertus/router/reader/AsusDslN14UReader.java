package it.albertus.router.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;

import it.albertus.router.resources.Messages;

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
		// Username...
		out.print(readFromTelnet(LOGIN_PROMPT, true).trim(), true);
		writeToTelnet(username);

		// Password...
		out.print(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(password);

		// Avanzamento fino al prompt...
		out.print(readFromTelnet(COMMAND_PROMPT, true).trim());
		return true;
	}

	@Override
	public LinkedHashMap<String, String> readInfo() throws IOException {
		final LinkedHashMap<String, String> info = new LinkedHashMap<String, String>();

		// Informazioni sulla portante ADSL...
		final String commandInfoAdsl = configuration.getString("asus.dsln14u.command.info.adsl", Defaults.COMMAND_INFO_ADSL);
		writeToTelnet(commandInfoAdsl);
		readFromTelnet(commandInfoAdsl, false); // Avanzamento del reader fino all'inizio dei dati di interesse.
		BufferedReader reader = new BufferedReader(new StringReader(readFromTelnet(COMMAND_PROMPT, false).trim()));
		String line;
		String prefix = "";
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() != 0) {
				if (line.startsWith(NODE_PREFIX)) {
					prefix = line.substring(NODE_PREFIX.length()).trim() + '_';
				}
				else if (line.indexOf('=') != -1) {
					info.put(prefix + line.substring(0, line.indexOf('=')).trim(), line.substring(line.indexOf('=') + 1).trim());
				}
			}
		}
		reader.close();

		// Informazioni sulla connessione ad Internet...
		final String commandInfoWan = configuration.getString("asus.dsln14u.command.info.wan");
		if (commandInfoWan != null && commandInfoWan.trim().length() != 0) {
			writeToTelnet(commandInfoWan);
			readFromTelnet(commandInfoWan, false); // Avanzamento del reader fino all'inizio dei dati di interesse.
			reader = new BufferedReader(new StringReader(readFromTelnet(COMMAND_PROMPT, false).trim()));
			prefix = "";
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() != 0) {
					if (line.startsWith(NODE_PREFIX)) {
						prefix = line.substring(NODE_PREFIX.length()).trim() + '_';
					}
					else if (line.indexOf('=') != -1) {
						info.put(prefix + line.substring(0, line.indexOf('=')).trim(), line.substring(line.indexOf('=') + 1).trim());
					}
				}
			}
			reader.close();
		}

		return info;
	}

	@Override
	public String getDeviceModel() {
		return Messages.get(DEVICE_MODEL_KEY);
	}

}
