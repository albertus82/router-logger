package it.albertus.router.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class AsusDslN12EReader extends Reader {

	private interface Defaults {
		String COMMAND_INFO_ADSL = "show wan adsl";
	}

	private static final String DEVICE_MODEL = "ASUS DSL-N12E";
	private static final String COMMAND_PROMPT = "$";
	private static final String LOGIN_PROMPT = ": ";

	@Override
	public boolean login() throws IOException {
		// Username...
		out.print(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(configuration.getString("router.username"));

		// Password...
		out.println(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(configuration.getString("router.password"));

		// Avanzo fino al prompt dei comandi
		readFromTelnet(COMMAND_PROMPT, true);
		return true;
	}

	@Override
	public Map<String, String> readInfo() throws IOException {
		// Informazioni sulla portante ADSL...
		writeToTelnet(configuration.getString("ASUSdsln12e.command.info.adsl", Defaults.COMMAND_INFO_ADSL));
		readFromTelnet("wan adsl", true); // Avanzamento del reader fino all'inizio dei dati di interesse.
		final Map<String, String> info = new LinkedHashMap<String, String>();
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

/*
		// Informazioni sulla connessione ad Internet...
		final String commandInfoWan = configuration.getString("tplink.8970.command.info.wan");
		if (StringUtils.isNotBlank(commandInfoWan)) {
			writeToTelnet(commandInfoWan);
			readFromTelnet("{", true);
			reader = new BufferedReader(new StringReader(readFromTelnet("}", false).trim()));
			while ((line = reader.readLine()) != null) {
				info.put(line.substring(0, line.indexOf('=')).trim(), line.substring(line.indexOf('=') + 1).trim());
			}
			reader.close();
			readFromTelnet(COMMAND_PROMPT, true);
		}
*/
		return info;
	}

	@Override
	public String getDeviceModel() {
		return DEVICE_MODEL;
	}

}
