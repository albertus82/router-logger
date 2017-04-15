package it.albertus.router.http.html;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.HttpMethod;
import it.albertus.httpserver.annotation.Path;
import it.albertus.httpserver.html.HtmlUtils;
import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.engine.Threshold;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;

@Path("/status")
public class StatusHtmlHandler extends BaseHtmlHandler {

	public static class Defaults {
		public static final boolean ENABLED = true;
		public static final boolean REFRESH = false;
		public static final int REFRESH_SECS = (int) (RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS / 1000);

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected static final String CFG_KEY_ENABLED = "server.handler.status.enabled";

	protected static final char KEY_VALUE_SEPARATOR = ':';

	private final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
		}
	};

	private final RouterLoggerEngine engine;

	public StatusHtmlHandler(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException {
		// Refresh...
		if (configuration.getBoolean("server.handler.status.refresh", Defaults.REFRESH)) {
			int refresh = configuration.getInt("server.handler.status.refresh.secs", Defaults.REFRESH_SECS);
			if (refresh <= 0) { // Auto
				refresh = Math.max(1, (int) (engine.getWaitTimeInMillis() / 1000) - 1);
			}
			exchange.getResponseHeaders().add("Refresh", Integer.toString(refresh));
		}

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(HtmlUtils.escapeHtml(Messages.get("lbl.server.status"))));
		html.append("<h3>").append(HtmlUtils.escapeHtml(Messages.get("lbl.status"))).append(KEY_VALUE_SEPARATOR).append(' ').append(HtmlUtils.escapeHtml(engine.getCurrentStatus().getStatus().getDescription())).append("</h3>").append(NewLine.CRLF);
		html.append(buildHtmlHomeButton());
		html.append(buildHtmlRefreshButton());
		final RouterData currentData = engine.getCurrentData();
		if (currentData != null) {
			html.append(buildList(currentData));
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
			if (HttpMethod.HEAD.equalsIgnoreCase(exchange.getRequestMethod())) {
				exchange.getResponseHeaders().set("Content-Length", Integer.toString(response.length));
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
				exchange.getResponseBody().close(); // no body
			}
			else {
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
				exchange.getResponseBody().write(response);
			}
		}
	}

	private String buildList(final RouterData currentData) {
		final Set<Threshold> thresholdsReached = configuration.getThresholds().getReached(currentData).keySet();
		final StringBuilder html = new StringBuilder();
		html.append("<ul>").append(NewLine.CRLF);
		html.append("<li><strong>").append(HtmlUtils.escapeHtml(Messages.get("lbl.column.timestamp.text"))).append(KEY_VALUE_SEPARATOR).append("</strong>").append(' ').append(HtmlUtils.escapeHtml(dateFormat.get().format(currentData.getTimestamp()))).append("</li>").append(NewLine.CRLF);
		html.append("<li><strong>").append(HtmlUtils.escapeHtml(Messages.get("lbl.column.response.time.text"))).append(KEY_VALUE_SEPARATOR).append("</strong>").append(' ').append(currentData.getResponseTime()).append("</li>").append(NewLine.CRLF);
		for (final Entry<String, String> entry : currentData.getData().entrySet()) {
			final String key = entry.getKey();

			html.append("<li>");

			final boolean highlight = key != null && configuration.getGuiImportantKeys().contains(key.trim());
			if (highlight) {
				html.append("<mark>");
			}

			boolean warning = false;
			for (final Threshold threshold : thresholdsReached) {
				if (key != null && key.equals(threshold.getKey())) {
					warning = true;
					break;
				}
			}
			if (warning) {
				html.append("<span class=\"warning\">");
			}

			html.append("<strong>").append(HtmlUtils.escapeHtml(key)).append(KEY_VALUE_SEPARATOR).append("</strong>").append(' ').append(HtmlUtils.escapeHtml(entry.getValue()));

			if (warning) {
				html.append("</span>");
			}

			if (highlight) {
				html.append("</mark>");
			}

			html.append("</li>").append(NewLine.CRLF);
		}
		html.append("</ul>").append(NewLine.CRLF);
		return html.toString();
	}

	@Override
	protected String buildHtmlHeadStyle() {
		return "<style type=\"text/css\">form {display: inline;} div {display: inline;} ul {list-style-type: none; padding-left: 0;} span.warning {color: red;}</style>";
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED);
	}

}
