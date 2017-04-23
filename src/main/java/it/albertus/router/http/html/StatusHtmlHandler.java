package it.albertus.router.http.html;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.annotation.Path;
import it.albertus.httpserver.html.HtmlUtils;
import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.engine.Status;
import it.albertus.router.engine.Threshold;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;

@Path("/status")
public class StatusHtmlHandler extends AbstractHtmlHandler {

	public static class Defaults {
		public static final boolean ENABLED = true;
		public static final boolean REFRESH = false;
		public static final int REFRESH_SECS = (int) (RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS / 1000);

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	static final String CFG_KEY_ENABLED = "server.handler.status.enabled";

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
			setRefreshHeader(exchange, refresh);
		}

		// Response...
		final Status status = engine.getCurrentStatus().getStatus();
		final String labelClass;
		switch (status) {
		case OK:
			labelClass = "label-success";
			break;
		case WARNING:
			labelClass = "label-warning";
			break;
		default:
			labelClass = "label-default";
			break;
		}
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.server.status")));
		html.append("<div class=\"page-header\">").append(NewLine.CRLF);
		html.append("<h2>").append(HtmlUtils.escapeHtml(Messages.get("lbl.status"))).append(" <span class=\"label ").append(labelClass).append(" label-header\">").append(HtmlUtils.escapeHtml(status.getDescription())).append("</span> ").append(buildHtmlRefreshButton()).append("</h2>").append(NewLine.CRLF);
		html.append("</div>").append(NewLine.CRLF);
		final RouterData currentData = engine.getCurrentData();
		if (currentData != null) {
			html.append(buildList(currentData));
		}
		else {
			html.append("<div class=\"alert alert-info\" role=\"alert\">").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.status.list.empty"))).append("</div>").append(NewLine.CRLF);
		}
		html.append(buildHtmlFooter());

		sendResponse(exchange, html.toString());
	}

	private String buildList(final RouterData currentData) {
		final Set<Threshold> thresholdsReached = configuration.getThresholds().getReached(currentData).keySet();
		final StringBuilder html = new StringBuilder();
		html.append("<ul class=\"list-group\">").append(NewLine.CRLF);
		html.append("<li class=\"list-group-item\"><span class=\"list-group-item-key\">").append(HtmlUtils.escapeHtml(Messages.get("lbl.column.timestamp.text"))).append(KEY_VALUE_SEPARATOR).append("</span>").append(' ').append(HtmlUtils.escapeHtml(dateFormat.get().format(currentData.getTimestamp()))).append("</li>").append(NewLine.CRLF);
		html.append("<li class=\"list-group-item\"><span class=\"list-group-item-key\">").append(HtmlUtils.escapeHtml(Messages.get("lbl.column.response.time.text"))).append(KEY_VALUE_SEPARATOR).append("</span>").append(' ').append(currentData.getResponseTime()).append("</li>").append(NewLine.CRLF);
		for (final Entry<String, String> entry : currentData.getData().entrySet()) {
			final String key = entry.getKey();
			html.append("<li class=\"list-group-item");
			if (key != null && configuration.getGuiImportantKeys().contains(key.trim())) {
				html.append(" list-group-item-mark");
			}
			for (final Threshold threshold : thresholdsReached) {
				if (key != null && key.equals(threshold.getKey())) {
					html.append(" list-group-item-warning");
					break;
				}
			}
			html.append("\">");
			html.append("<span class=\"list-group-item-key\">").append(HtmlUtils.escapeHtml(key)).append(KEY_VALUE_SEPARATOR).append("</span>").append(' ').append(HtmlUtils.escapeHtml(entry.getValue()));
			html.append("</li>").append(NewLine.CRLF);
		}
		html.append("</ul>").append(NewLine.CRLF);
		return html.toString();
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED);
	}

}
