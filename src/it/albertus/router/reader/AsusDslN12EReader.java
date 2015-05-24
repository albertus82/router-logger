package it.albertus.router.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AsusDslN12EReader extends Reader {

	private interface Defaults {
		String COMMAND_INFO_ADSL = "show wan adsl";
		String COMMAND_INFO_WAN = "show wan interface";
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
		writeToTelnet(configuration.getString("asus.dsln12e.command.info.adsl", Defaults.COMMAND_INFO_ADSL));
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

		// Informazioni sulla connessione ad Internet...
		final String commandInfoWan = configuration.getString("asus.dsln12e.command.info.wan", Defaults.COMMAND_INFO_WAN);
		writeToTelnet(commandInfoWan);
		readFromTelnet("Status", true);
		List<String> values = new ArrayList<String>(8);
		for (String field : readFromTelnet("$", false).trim().split("  ")) {
			if (field != null && field.trim().length() > 0) {
				values.add(field.trim());
			}
		}
		info.put("Interface", values.get(0));
		info.put("VPI/VCI", "\""+values.get(1)+"\"");
		info.put("Encap", values.get(2));
		info.put("Droute", values.get(3));
		info.put("Protocol", values.get(4));
		info.put("IP Addres", values.get(5));
		info.put("Gateway", values.get(6));
		info.put("Status", values.get(7));

		return info;
	}

	@Override
	public String getDeviceModel() {
		return DEVICE_MODEL;
	}

}
