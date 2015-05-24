package it.albertus.router.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class TpLink8970Reader extends Reader {

	private static final String DEVICE_MODEL = "TP-Link TD-W8970 V1";
	private static final String COMMAND_PROMPT = "#";
	private static final String LOGIN_PROMPT = ":";

	@Override
	public boolean login() throws IOException {
		// Username...
		out.print(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(configuration.getString("router.username"));

		// Password...
		out.println(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(configuration.getString("router.password"));

		// Welcome! (salto caratteri speciali (clear screen, ecc.)...
		String welcome = readFromTelnet("-", true);
		out.println(welcome.charAt(welcome.length() - 1) + readFromTelnet(COMMAND_PROMPT, true).trim());
		return true;
	}

	@Override
	public Map<String, String> readInfo() throws IOException {
		writeToTelnet("adsl show info");
		readFromTelnet("{", true); // Avanzamento del reader fino all'inizio dei dati di interesse.

		// Inizio estrazione dati...
		final Map<String, String> info = new LinkedHashMap<String, String>();
		final BufferedReader reader = new BufferedReader(new StringReader(readFromTelnet("}", false).trim()));
		String line;
		while ((line = reader.readLine()) != null) {
			info.put(line.substring(0, line.indexOf('=')).trim(), line.substring(line.indexOf('=') + 1).trim());
		}
		reader.close();
		// Fine estrazione dati.

		readFromTelnet(COMMAND_PROMPT, true); // Avanzamento del reader fino al prompt dei comandi.

		return info;
	}

	@Override
	public String getDeviceModel() {
		return DEVICE_MODEL;
	}

}