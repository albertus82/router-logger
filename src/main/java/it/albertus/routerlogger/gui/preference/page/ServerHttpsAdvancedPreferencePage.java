package it.albertus.routerlogger.gui.preference.page;

import java.net.HttpURLConnection;

import it.albertus.jface.preference.StaticLabelsAndValues;
import it.albertus.jface.preference.page.RestartHeaderPreferencePage;
import it.albertus.net.httpserver.AbstractHttpHandler;

public class ServerHttpsAdvancedPreferencePage extends RestartHeaderPreferencePage {

	public static StaticLabelsAndValues getRedirectStatusComboOptions() {
		final StaticLabelsAndValues options = new StaticLabelsAndValues(2);
		options.put(AbstractHttpHandler.getHttpStatusCodes().get(HttpURLConnection.HTTP_MOVED_PERM), HttpURLConnection.HTTP_MOVED_PERM);
		options.put(AbstractHttpHandler.getHttpStatusCodes().get(HttpURLConnection.HTTP_MOVED_TEMP), HttpURLConnection.HTTP_MOVED_TEMP);
		return options;
	}

}
