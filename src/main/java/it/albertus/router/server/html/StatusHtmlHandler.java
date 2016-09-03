package it.albertus.router.server.html;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.engine.Threshold;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;

public class StatusHtmlHandler extends BaseHtmlHandler {

	public interface Defaults {
		boolean ENABLED = true;
		boolean REFRESH = false;
		int REFRESH_SECS = (int) (RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS / 1000);
	}

	public static final String PATH = "/status";
	public static final String[] METHODS = { "GET" };

	protected static final String CFG_KEY_ENABLED = "server.handler.status.enabled";

	protected static final char KEY_VALUE_SEPARATOR = ':';

	private final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
		}
	};

	public StatusHtmlHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(final HttpExchange exchange) throws IOException {
		// Refresh...
		if (configuration.getBoolean("server.handler.status.refresh", Defaults.REFRESH)) {
			int refresh = configuration.getInt("server.handler.status.refresh.secs", Defaults.REFRESH_SECS);
			if (refresh <= 0) { // Auto
				refresh = Math.max(1, Long.valueOf(engine.getWaitTimeInMillis() / 1000).intValue() - 1);
			}
			exchange.getResponseHeaders().add("Refresh", Integer.toString(refresh));
		}

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.server.status")));
		html.append("<h3>").append(Messages.get("lbl.status")).append(KEY_VALUE_SEPARATOR).append(' ').append(engine.getCurrentStatus().getStatus().getDescription()).append("</h3>").append(NewLine.CRLF);
		html.append(buildHtmlHomeButton());
		final RouterData currentData = engine.getCurrentData();
		if (currentData != null) {
			final Set<Threshold> thresholdsReached = configuration.getThresholds().getReached(currentData).keySet();
			html.append("<ul>").append(NewLine.CRLF);
			html.append("<li><strong>").append(Messages.get("lbl.column.timestamp.text")).append(KEY_VALUE_SEPARATOR).append("</strong>").append(' ').append(dateFormat.get().format(currentData.getTimestamp())).append("</li>").append(NewLine.CRLF);
			html.append("<li><strong>").append(Messages.get("lbl.column.response.time.text")).append(KEY_VALUE_SEPARATOR).append("</strong>").append(' ').append(currentData.getResponseTime()).append("</li>").append(NewLine.CRLF);
			for (final String key : currentData.getData().keySet()) {
				html.append("<li>");

				final boolean highlight = key != null && configuration.getGuiImportantKeys().contains(key.trim());
				if (highlight) {
					html.append("<mark>");
				}

				boolean warning = false;
				for (final Threshold threshold : thresholdsReached) {
					if (key.equals(threshold.getKey())) {
						warning = true;
						break;
					}
				}
				if (warning) {
					html.append("<span class=\"warning\">");
				}

				html.append("<strong>").append(key).append(KEY_VALUE_SEPARATOR).append("</strong>").append(' ').append(currentData.getData().get(key));

				if (warning) {
					html.append("</span>");
				}

				if (highlight) {
					html.append("</mark>");
				}

				html.append("</li>").append(NewLine.CRLF);
			}
			html.append("</ul>").append(NewLine.CRLF);
		}
		html.append(buildHtmlFooter());

		// If-Modified-Since...
		final String ifModifiedSince = exchange.getRequestHeaders().getFirst("If-Modified-Since");
		if (ifModifiedSince != null && currentData != null && currentData.getTimestamp() != null && httpDateGenerator.format(currentData.getTimestamp()).equals(ifModifiedSince)) {
			addDateHeader(exchange);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, -1);
			exchange.getResponseBody().close(); // Needed when no write occurs.
		}
		else {
			addCommonHeaders(exchange);
			if (currentData != null && currentData.getTimestamp() != null) {
				exchange.getResponseHeaders().add("Last-Modified", httpDateGenerator.format(currentData.getTimestamp()));
			}
			final byte[] response = compressResponse(html.toString().getBytes(getCharset()), exchange);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			exchange.getResponseBody().write(response);
		}
	}

	@Override
	protected String buildHtmlHeadStyle() {
		return "<style type=\"text/css\">ul {list-style-type: none; padding-left: 0;} span.warning {color: red;}</style>";
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
