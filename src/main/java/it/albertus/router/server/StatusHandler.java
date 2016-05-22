package it.albertus.router.server;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.sun.net.httpserver.HttpExchange;

public class StatusHandler extends BaseHttpHandler {

	public interface Defaults {
		boolean REFRESH = false;
		int REFRESH_SECS = 0;
	}

	public static final String PATH = "/status";
	public static final String[] METHODS = { "GET" };

	protected static final char KEY_VALUE_SEPARATOR = ':';
	protected static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	protected StatusHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(final HttpExchange exchange) throws IOException {
		// Charset...
		final Charset charset = getCharset();
		exchange.getResponseHeaders().add("Content-Type", "text/html; charset=" + charset.name());
		exchange.getResponseHeaders().add("Date", httpDateGenerator.getCurrentDate());

		// Refresh...
		if (configuration.getBoolean("server.status.refresh", Defaults.REFRESH)) {
			int refresh = configuration.getInt("server.status.refresh.secs", Defaults.REFRESH_SECS);
			if (refresh == Defaults.REFRESH_SECS) {
				// Division by 2 for sampling theorem
				refresh = Math.max(1, Long.valueOf(configuration.getLong("logger.interval.normal.ms", RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS) / 1000 / 2).intValue());
			}
			exchange.getResponseHeaders().add("Refresh", Integer.toString(refresh));
		}

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Resources.get("lbl.server.status")));
		html.append("<h3>").append(Resources.get("lbl.status")).append(KEY_VALUE_SEPARATOR).append(' ').append(engine.getCurrentStatus().toString()).append("</h3>").append(NewLine.CRLF);
		final RouterData currentData = engine.getCurrentData();
		if (currentData != null) {
			html.append("<p>");
			html.append(NewLine.CRLF).append("<strong>").append(Resources.get("lbl.column.timestamp.text")).append(KEY_VALUE_SEPARATOR).append("</strong>").append(' ').append(dateFormat.format(currentData.getTimestamp())).append("<br />").append(NewLine.CRLF);
			html.append("<strong>").append(Resources.get("lbl.column.response.time.text")).append(KEY_VALUE_SEPARATOR).append("</strong>").append(' ').append(currentData.getResponseTime());
			for (final String key : currentData.getData().keySet()) {
				html.append("<br />").append(NewLine.CRLF);
				html.append("<strong>").append(key).append(KEY_VALUE_SEPARATOR).append("</strong>").append(' ').append(currentData.getData().get(key));
			}
			html.append("</p>").append(NewLine.CRLF);
		}
		html.append("<form action=\"").append(RootHandler.PATH).append("\" method=\"" + RootHandler.METHODS[0] + "\"><input type=\"submit\" value=\"").append(Resources.get("lbl.server.home")).append("\" /></form>").append(NewLine.CRLF.toString());
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(charset);
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
		exchange.getResponseBody().write(response);
	}

	@Override
	public String getPath() {
		return PATH;
	}

	@Override
	public String[] getMethods() {
		return METHODS;
	}

}
