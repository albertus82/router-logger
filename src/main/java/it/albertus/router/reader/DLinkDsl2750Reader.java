package it.albertus.router.reader;

import it.albertus.router.engine.RouterData;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DLinkDsl2750Reader extends Reader {

	private interface Defaults {
		String COMMAND_INFO_ADSL_STATUS = "adsl status";
		String COMMAND_INFO_ADSL_SNR = "adsl snr";
	}

	private static final String DEVICE_MODEL = "D-Link DSL-2750B";
	private static final String COMMAND_PROMPT = "TBS>>";
	private static final String LOGIN_PROMPT = ":";

	@Override
	public boolean login() throws IOException {
		// Username...
		out.print(readFromTelnet(LOGIN_PROMPT, true).trim(), true);
		writeToTelnet(configuration.getString("router.username"));

		// Password...
		out.print(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(configuration.getString("router.password"));

		// Avanzamento fino al prompt...
		readFromTelnet(COMMAND_PROMPT, true);
		return true;
	}

	@Override
	public RouterData readInfo() throws IOException {
		final Map<String, String> info = new LinkedHashMap<String, String>();

		// Informazioni sulla portante ADSL...
		writeToTelnet(configuration.getString("dlink.2750.command.info.adsl.status", Defaults.COMMAND_INFO_ADSL_STATUS));
		readFromTelnet("-----\r", true); // Avanzamento del reader fino all'inizio dei dati di interesse.
		info.put("ADSL status", readFromTelnet(COMMAND_PROMPT, false).trim());

		writeToTelnet(configuration.getString("dlink.2750.command.info.adsl.snr", Defaults.COMMAND_INFO_ADSL_SNR));
		readFromTelnet("Upstream", true); // Avanzamento del reader fino all'inizio dei dati di interesse.
		String[] snrs = readFromTelnet(COMMAND_PROMPT, false).trim().split("(\\s\\s)+");
		info.put("ADSL SNR Downstream", snrs[0]);
		info.put("ADSL SNR Upstream", snrs[1]);

		return new RouterData(info);
	}

	@Override
	public String getDeviceModel() {
		return DEVICE_MODEL;
	}

}
