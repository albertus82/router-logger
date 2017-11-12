package it.albertus.routerlogger.gui.preference.page;

import java.net.HttpURLConnection;
import java.util.Map.Entry;

import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.StaticLabelsAndValues;
import it.albertus.jface.preference.page.RestartHeaderPreferencePage;
import it.albertus.net.httpserver.HttpStatusCodes;
import it.albertus.routerlogger.resources.Messages;
import it.albertus.util.ISupplier;

public class ServerHttpsAdvancedPreferencePage extends RestartHeaderPreferencePage {

	private static final int[] maxAgeValues = { 300, 604800, 2592000, 15552000, 31536000, 63072000 };

	public static StaticLabelsAndValues getRedirectStatusComboOptions() {
		final StaticLabelsAndValues options = new StaticLabelsAndValues();
		for (final Entry<Integer, String> entry : HttpStatusCodes.getMap().entrySet()) {
			if (entry.getKey() >= HttpURLConnection.HTTP_MULT_CHOICE && entry.getKey() < HttpURLConnection.HTTP_BAD_REQUEST) {
				options.put(entry.getKey() + " - " + entry.getValue(), entry.getKey());
			}
		}
		return options;
	}

	public static LocalizedLabelsAndValues getHSTSMaxAgeComboOptions() {
		final LocalizedLabelsAndValues options = new LocalizedLabelsAndValues(maxAgeValues.length);
		for (final int maxAgeValue : maxAgeValues) {
			options.add(new ISupplier<String>() {
				@Override
				public String get() {
					return Messages.get("lbl.preferences.server.ssl.hsts.maxage." + maxAgeValue);
				}
			}, maxAgeValue);
		}
		return options;
	}

}
