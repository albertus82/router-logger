package it.albertus.router.tplink;

import it.albertus.router.CsvRouterLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class TDW8970V1Logger extends CsvRouterLogger {

	private static final String DEVICE_MODEL = "TP-Link TD-W8970 V1";
	private static final String COMMAND_PROMPT = "#";
	private static final String LOGIN_PROMPT = ":";

	public static void main(String... args) {
		new TDW8970V1Logger().run();
	}

	@Override
	protected boolean login() throws IOException {
		// Username...
		System.out.print(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(configuration.getProperty("router.username"));

		// Password...
		System.out.println(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(configuration.getProperty("router.password"));

		// Welcome! (salto caratteri speciali (clear screen, ecc.)...
		String welcome = readFromTelnet("-", true);
		System.out.println(welcome.charAt(welcome.length() - 1) + readFromTelnet(COMMAND_PROMPT, true).trim());
		return true;
	}

	@Override
	protected Map<String, String> readInfo() throws IOException {
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
	protected String getDeviceModel() {
		return DEVICE_MODEL;
	}

}
