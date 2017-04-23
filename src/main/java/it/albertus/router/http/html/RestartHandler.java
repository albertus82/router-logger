package it.albertus.router.http.html;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.annotation.Path;
import it.albertus.httpserver.html.HtmlUtils;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;

@Path("/restart")
public class RestartHandler extends AbstractHtmlHandler {

	public static class Defaults {
		public static final boolean ENABLED = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	static final String CFG_KEY_ENABLED = "server.handler.restart.enabled";

	private final RouterLoggerEngine engine;

	public RestartHandler(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	@Override
	public void doPost(final HttpExchange exchange) throws IOException {
		// Headers...
		setCommonHeaders(exchange);

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.server.restart")));
		html.append("<div class=\"page-header\"><h2>").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.restart"))).append("</h2></div>").append(NewLine.CRLF);
		html.append("<div class=\"alert alert-success alert-h4\" role=\"alert\">").append(HtmlUtils.escapeHtml(Messages.get("msg.server.accepted"))).append("</div>").append(NewLine.CRLF);
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
		exchange.getResponseBody().write(response);
		exchange.getResponseBody().close();
		exchange.close();

		engine.restart();
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED);
	}

}
