package it.albertus.router.server;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.sun.net.httpserver.HttpExchange;

public class StatusHandler extends BaseHttpHandler {

	public interface Defaults {
		boolean ENABLED = true;
		boolean REFRESH = false;
		int REFRESH_SECS = 5;
	}

	public static final String PATH = "/status";
	public static final String[] METHODS = { "GET" };

	protected static final String CFG_KEY_ENABLED = "server.handler.status.enabled";

	protected static final char KEY_VALUE_SEPARATOR = ':';
	protected static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	protected StatusHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(final HttpExchange exchange) throws IOException {
		// Refresh...
		if (configuration.getBoolean("server.handler.status.refresh", Defaults.REFRESH)) {
			int refresh = configuration.getInt("server.handler.status.refresh.secs", Defaults.REFRESH_SECS);
			if (refresh <= 0) { // Auto
				refresh = Math.max(1, Long.valueOf(configuration.getLong("logger.interval.normal.ms", RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS) / 1000).intValue() - 1);
			}
			exchange.getResponseHeaders().add("Refresh", Integer.toString(refresh));
		}

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Resources.get("lbl.server.status")));
		html.append("<h3>").append(Resources.get("lbl.status")).append(KEY_VALUE_SEPARATOR).append(' ').append(engine.getCurrentStatus().toString()).append("</h3>").append(NewLine.CRLF);
		final RouterData currentData = engine.getCurrentData();
		if (currentData != null) {
			html.append("<ul>").append(NewLine.CRLF);
			html.append("<li><strong>").append(Resources.get("lbl.column.timestamp.text")).append(KEY_VALUE_SEPARATOR).append("</strong>").append(' ').append(dateFormat.format(currentData.getTimestamp())).append("</li>").append(NewLine.CRLF);
			html.append("<li><strong>").append(Resources.get("lbl.column.response.time.text")).append(KEY_VALUE_SEPARATOR).append("</strong>").append(' ').append(currentData.getResponseTime()).append("</li>").append(NewLine.CRLF);
			for (final String key : currentData.getData().keySet()) {
				final boolean highlight = key != null && configuration.getGuiImportantKeys().contains(key.trim());
				html.append("<li>");
				if (highlight) {
					html.append("<mark>");
				}
				html.append("<strong>").append(key).append(KEY_VALUE_SEPARATOR).append("</strong>").append(' ').append(currentData.getData().get(key));
				if (highlight) {
					html.append("</mark>");
				}
				html.append("</li>").append(NewLine.CRLF);
			}
			html.append("</ul>").append(NewLine.CRLF);
		}
		html.append(buildHtmlHomeButton());
		html.append(buildHtmlFooter());

		// If-Modified-Since...
		final String ifModifiedSince = exchange.getRequestHeaders().getFirst("If-Modified-Since");
		if (ifModifiedSince != null && currentData != null && currentData.getTimestamp() != null && httpDateGenerator.getDateFormat().format(currentData.getTimestamp()).equals(ifModifiedSince)) {
			addDateHeader(exchange);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, -1);
			exchange.getResponseBody().close(); // Needed when no write occurs.
		}
		else {
			addCommonHeaders(exchange);
			if (currentData != null && currentData.getTimestamp() != null) {
				exchange.getResponseHeaders().add("Last-Modified", httpDateGenerator.getDateFormat().format(currentData.getTimestamp()));
			}
			final byte[] response = compressResponse(html.toString().getBytes(getCharset()), exchange);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			exchange.getResponseBody().write(response);
		}
	}

	@Override
	protected String buildHtmlHead(final String title) {
		final StringBuilder html = new StringBuilder("<head>");
		if (title != null && !title.isEmpty()) {
			html.append("<title>").append(Resources.get("msg.application.name")).append(" - ").append(title).append("</title>");
		}
		html.append("<style type=\"text/css\">ul {list-style-type: none; padding-left: 0;}</style>");
		html.append("</head>");
		return html.toString();
	}

	@Override
	public String getPath() {
		return PATH;
	}

	@Override
	public String[] getMethods() {
		return METHODS;
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED);
	}

}
