package it.albertus.router.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
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
		String InfoIP = readFromTelnet("$", false).trim();
		String[] arrayInfo = InfoIP.split("  ");
		info.put("Interface", arrayInfo[0].trim());
		info.put("VPI/VCI", arrayInfo[1].trim());
		info.put("Encap", arrayInfo[2].trim());
		info.put("Droute", arrayInfo[3].trim());
		info.put("Protocol", arrayInfo[4].trim());
		info.put("IP Addres", arrayInfo[5].trim());
		info.put("Gateway", arrayInfo[6].trim());
		info.put("Status", arrayInfo[7].trim());
	
		return info;
	}

	@Override
	public String getDeviceModel() {
		return DEVICE_MODEL;
	}

}
