package it.albertus.router.reader;

import it.albertus.router.resources.Messages;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * <b>D-Link DSL-2750B</b>. Comandi Telnet disponibili (case sensitive):
 * <ul>
 * <li><tt><b>adsl status</b></tt></li>
 * <li><tt><b>adsl snr</b></tt></li>
 * </ul>
 */
public class DLinkDsl2750Reader extends Reader {

	public interface Defaults {
		String COMMAND_INFO_ADSL_STATUS = "adsl status";
		String COMMAND_INFO_ADSL_SNR = "adsl snr";
	}

	public static final String DEVICE_MODEL_KEY = "lbl.device.model.dlink.2750b";

	protected static final String COMMAND_PROMPT = "TBS>>";
	protected static final String LOGIN_PROMPT = ":";

	@Override
	public boolean login(final String username, final char[] password) throws IOException {
		// Username...
		out.print(readFromTelnet(LOGIN_PROMPT, true).trim(), true);
		writeToTelnet(username);

		// Password...
		out.print(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(password);

		// Avanzamento fino al prompt...
		readFromTelnet(COMMAND_PROMPT, true);
		return true;
	}

	@Override
	public LinkedHashMap<String, String> readInfo() throws IOException {
		final LinkedHashMap<String, String> info = new LinkedHashMap<String, String>();

		// Informazioni sulla portante ADSL...
		writeToTelnet(configuration.getString("dlink.2750.command.info.adsl.status", Defaults.COMMAND_INFO_ADSL_STATUS));
		readFromTelnet("-----\r", true); // Avanzamento del reader fino all'inizio dei dati di interesse.
		info.put("ADSL status", readFromTelnet(COMMAND_PROMPT, false).trim());

		writeToTelnet(configuration.getString("dlink.2750.command.info.adsl.snr", Defaults.COMMAND_INFO_ADSL_SNR));
		readFromTelnet("Upstream", true); // Avanzamento del reader fino all'inizio dei dati di interesse.
		String[] snrs = readFromTelnet(COMMAND_PROMPT, false).trim().split("(\\s\\s)+");
		info.put("ADSL SNR Downstream", snrs[0]);
		info.put("ADSL SNR Upstream", snrs[1]);

		return info;
	}

	@Override
	public String getDeviceModel() {
		return Messages.get(DEVICE_MODEL_KEY);
	}

}
