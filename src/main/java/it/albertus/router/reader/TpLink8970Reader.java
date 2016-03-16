package it.albertus.router.reader;

import it.albertus.router.engine.RouterData;
import it.albertus.router.resources.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <b>TP-Link TD-W8970 V1</b>. Comandi Telnet disponibili (case sensitive):
 * <ul>
 * <li><tt><b>adsl show info</b></tt></li>
 * <li><tt><b>wan show connection info</b></tt> (sconsigliato, verboso)</li>
 * <li><tt><b>wan show connection info <i>name</i></b></tt></li>
 * </ul>
 */
public class TpLink8970Reader extends Reader {

	private interface Defaults {
		String COMMAND_INFO_ADSL = "adsl show info";
	}

	private static final String DEVICE_MODEL = "TP-Link TD-W8970 V1";
	private static final String COMMAND_PROMPT = "#";
	private static final String LOGIN_PROMPT = ":";

	@Override
	public boolean login(final String username, final char[] password) throws IOException {
		// Username...
		out.print(readFromTelnet(LOGIN_PROMPT, true).trim(), true);
		writeToTelnet(username);

		// Password...
		out.print(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(password);

		// Welcome! (salto caratteri speciali (clear screen, ecc.)...
		String welcome = readFromTelnet("-", true);
		out.println(welcome.charAt(welcome.length() - 1) + readFromTelnet(COMMAND_PROMPT, true).trim(), true);
		return true;
	}

	@Override
	public RouterData readInfo() throws IOException {
		// Informazioni sulla portante ADSL...
		writeToTelnet(configuration.getString("tplink.8970.command.info.adsl", Defaults.COMMAND_INFO_ADSL));
		readFromTelnet("{", true); // Avanzamento del reader fino all'inizio dei dati di interesse.
		final Map<String, String> info = new LinkedHashMap<String, String>();
		BufferedReader reader = new BufferedReader(new StringReader(readFromTelnet("}", false).trim()));
		String line;
		while ((line = reader.readLine()) != null) {
			info.put(line.substring(0, line.indexOf('=')).trim(), line.substring(line.indexOf('=') + 1).trim());
		}
		reader.close();
		readFromTelnet(COMMAND_PROMPT, true); // Avanzamento del reader fino al prompt dei comandi.

		// Informazioni sulla connessione ad Internet...
		final String commandInfoWan = configuration.getString("tplink.8970.command.info.wan");
		if (commandInfoWan != null && commandInfoWan.trim().length() != 0) {
			writeToTelnet(commandInfoWan);
			readFromTelnet("{", true);
			reader = new BufferedReader(new StringReader(readFromTelnet("}", false).trim()));
			while ((line = reader.readLine()) != null) {
				info.put(line.substring(0, line.indexOf('=')).trim(), line.substring(line.indexOf('=') + 1).trim());
			}
			reader.close();
			readFromTelnet(COMMAND_PROMPT, true);
		}

		return new RouterData(info);
	}

	@Override
	public void logout() throws IOException {
		out.println(Resources.get("msg.logging.out"), true);
		writeToTelnet("logout");
	}

	@Override
	public String getDeviceModel() {
		return DEVICE_MODEL;
	}

}
