package it.albertus.router.reader;

import it.albertus.router.engine.RouterData;
import it.albertus.router.resources.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class AsusDslN14UReader extends Reader {

	private interface Defaults {
		String COMMAND_INFO_ADSL = "tcapi show Info_Adsl";
		String COMMAND_INFO_WAN = "tcapi show Wan";
	}

	private static final String DEVICE_MODEL = "ASUS DSL-N14U";
	private static final String COMMAND_PROMPT = "# ";
	private static final String LOGIN_PROMPT = ": ";

	@Override
	public boolean login() throws IOException {
		// Username...
		out.print(readFromTelnet(LOGIN_PROMPT, true).trim(), true);
		writeToTelnet(configuration.getString("router.username"));

		// Password...
		out.print(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(configuration.getString("router.password"));

		// Avanzamento fino al prompt...
		out.print(readFromTelnet(COMMAND_PROMPT, true).trim());
		return true;
	}

	@Override
	public RouterData readInfo() throws IOException {
		final Map<String, String> info = new LinkedHashMap<String, String>();

		// Informazioni sulla portante ADSL...
		final String commandInfoAdsl = configuration.getString("asus.dsln14u.command.info.adsl", Defaults.COMMAND_INFO_ADSL);
		writeToTelnet(commandInfoAdsl);
		readFromTelnet(commandInfoAdsl, false); // Avanzamento del reader fino all'inizio dei dati di interesse.
		BufferedReader reader = new BufferedReader(new StringReader(readFromTelnet(COMMAND_PROMPT, false).trim()));
		String line;
		while ((line = reader.readLine()) != null) {
			info.put(line.substring(0, line.indexOf('=')).trim(), line.substring(line.indexOf('=') + 1).trim());
		}
		reader.close();

		// Informazioni sulla connessione ad Internet...
		final String commandInfoWan = configuration.getString("asus.dsln14u.command.info.wan", Defaults.COMMAND_INFO_WAN);
		if (commandInfoWan != null && commandInfoWan.trim().length() != 0) {
			writeToTelnet(commandInfoWan);
			readFromTelnet(commandInfoWan, false); // Avanzamento del reader fino all'inizio dei dati di interesse.
			reader = new BufferedReader(new StringReader(readFromTelnet(COMMAND_PROMPT, false).trim()));
			String prefix = "";
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() != 0) {
					if (line.startsWith("Node:")) {
						prefix = line.replace(':', '_').trim() + '_';
					}
					else if (line.indexOf('=') != -1) {
						info.put(prefix + line.substring(0, line.indexOf('=')).trim(), line.substring(line.indexOf('=') + 1).trim());
					}
				}
			}
			reader.close();
		}

		return new RouterData(info);
	}

	@Override
	public void logout() throws IOException {
		out.println(Resources.get("msg.logging.out"), true);
		writeToTelnet("exit");
	}

	@Override
	public String getDeviceModel() {
		return DEVICE_MODEL;
	}

}
