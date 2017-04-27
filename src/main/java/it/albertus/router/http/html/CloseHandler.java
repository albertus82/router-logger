package it.albertus.router.http.html;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.HttpException;
import it.albertus.httpserver.HttpMethod;
import it.albertus.httpserver.RequestParameterExtractor;
import it.albertus.httpserver.annotation.Path;
import it.albertus.httpserver.html.HtmlUtils;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.engine.ShutdownDaemon;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;
import it.albertus.util.logging.LoggerFactory;

@Path("/close")
public class CloseHandler extends AbstractHtmlHandler {

	private static final Logger logger = LoggerFactory.getLogger(CloseHandler.class);

	public static class Defaults {
		public static final boolean ENABLED = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	static final String CFG_KEY_ENABLED = "server.handler.close.enabled";

	private static final int DEFAULT_TIMEOUT = 60;

	private static final String REQUEST_PARAM_NAME_TIMEOUT = "timeout";
	private static final String REQUEST_PARAM_NAME_REVOKE = "revoke";

	private static final String MSG_KEY_SERVER_CLOSE = "lbl.server.close";

	private final RouterLoggerEngine engine;

	public CloseHandler(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException, HttpException {
		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get(MSG_KEY_SERVER_CLOSE)));
		html.append("<div class=\"page-header\"><h2>").append(HtmlUtils.escapeHtml(Messages.get(MSG_KEY_SERVER_CLOSE))).append("</h2></div>").append(NewLine.CRLF);
		html.append("<form class=\"form-inline\" action=\"").append(getPath()).append("\" method=\"").append(HttpMethod.POST).append("\">").append(NewLine.CRLF);
		final ShutdownDaemon shutdownDaemon = engine.getShutdownDaemon();
		if (shutdownDaemon != null) {
			html.append("<div class=\"alert alert-info\" role=\"alert\">").append(HtmlUtils.escapeHtml(Messages.get("msg.close.schedule", DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Messages.getLanguage().getLocale()).format(shutdownDaemon.getShutdownTime()), shutdownDaemon.getTimeoutInSecs()))).append(NewLine.CRLF);
			html.append("<input class=\"btn btn-info btn-xs btn-alert\" name=\"").append(REQUEST_PARAM_NAME_REVOKE).append("\" type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.close.revoke"))).append("\" />").append(NewLine.CRLF);
			html.append("</div>").append(NewLine.CRLF);
		}
		html.append("<div class=\"form-group\">").append(NewLine.CRLF);
		html.append("<label for=\"timeout\">").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.close.timeout"))).append("</label>").append(NewLine.CRLF);
		html.append("<input id=\"timeout\" class=\"form-control\" type=\"number\" name=\"").append(REQUEST_PARAM_NAME_TIMEOUT).append("\" min=\"0\" max=\"99999999\" value=\"").append(DEFAULT_TIMEOUT).append('"').append(shutdownDaemon == null ? " required=\"required\" " : ' ').append("/>").append(NewLine.CRLF);
		html.append("</div>").append(NewLine.CRLF);
		html.append("<div class=\"form-group\">").append(NewLine.CRLF);
		html.append("<input class=\"btn btn-danger btn-md\" type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get(shutdownDaemon == null ? "lbl.server.close.confirm" : "lbl.server.close.reschedule"))).append("\" />").append(NewLine.CRLF);
		html.append("</div>").append(NewLine.CRLF);
		html.append("</form>").append(NewLine.CRLF);
		html.append(buildHtmlFooter());

		sendResponse(exchange, html.toString());
	}

	@Override
	protected void doPost(final HttpExchange exchange) throws IOException {
		final RequestParameterExtractor requestParameterExtractor = new RequestParameterExtractor(exchange);
		int timeoutInSecs = -1;
		if (requestParameterExtractor.getParameter(REQUEST_PARAM_NAME_REVOKE) == null) {
			try {
				final String timeoutParam = requestParameterExtractor.getParameter(REQUEST_PARAM_NAME_TIMEOUT);
				if (timeoutParam != null) {
					timeoutInSecs = Math.max(0, Integer.parseInt(timeoutParam));
				}
			}
			catch (final NumberFormatException e) {
				logger.log(Level.INFO, e.toString(), e);
			}
		}

		// Headers...
		setCommonHeaders(exchange);

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get(MSG_KEY_SERVER_CLOSE)));
		html.append("<div class=\"page-header\"><h2>").append(HtmlUtils.escapeHtml(Messages.get(MSG_KEY_SERVER_CLOSE))).append("</h2></div>").append(NewLine.CRLF);
		html.append("<div class=\"alert alert-success alert-h4\" role=\"alert\">").append(HtmlUtils.escapeHtml(Messages.get("msg.server.accepted"))).append("</div>").append(NewLine.CRLF);
		if (timeoutInSecs >= 0) {
			final Calendar shutdownCalendar = Calendar.getInstance();
			shutdownCalendar.add(Calendar.SECOND, timeoutInSecs);
			html.append("<div class=\"alert alert-warning\" role=\"alert\">").append(HtmlUtils.escapeHtml(Messages.get("msg.close.schedule", DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Messages.getLanguage().getLocale()).format(shutdownCalendar.getTime()), timeoutInSecs))).append("</div>").append(NewLine.CRLF);
		}
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
		exchange.getResponseBody().write(response);
		exchange.getResponseBody().close();
		exchange.close();

		engine.scheduleShutdown(timeoutInSecs);
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED);
	}

}
