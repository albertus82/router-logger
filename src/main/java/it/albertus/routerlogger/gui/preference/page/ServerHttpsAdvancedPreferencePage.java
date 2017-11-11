package it.albertus.routerlogger.gui.preference.page;

import java.net.HttpURLConnection;
import java.util.Map.Entry;

import it.albertus.jface.preference.StaticLabelsAndValues;
import it.albertus.jface.preference.page.RestartHeaderPreferencePage;
import it.albertus.net.httpserver.AbstractHttpHandler;

public class ServerHttpsAdvancedPreferencePage extends RestartHeaderPreferencePage {

	public static StaticLabelsAndValues getRedirectStatusComboOptions() {
		final StaticLabelsAndValues options = new StaticLabelsAndValues();
		for (final Entry<Integer, String> entry : AbstractHttpHandler.getHttpStatusCodes().entrySet()) {
			if (entry.getKey() >= HttpURLConnection.HTTP_MULT_CHOICE && entry.getKey() < HttpURLConnection.HTTP_BAD_REQUEST) {
				options.put(entry.getKey() + " - " + entry.getValue(), entry.getKey());
			}
		}
		return options;
	}

}
