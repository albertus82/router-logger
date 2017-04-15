package it.albertus.router.server.html;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Messages;
import it.albertus.router.server.annotation.Path;
import it.albertus.util.NewLine;

@Path("/connect")
public class ConnectHandler extends BaseHtmlHandler {

	public static class Defaults {
		public static final boolean ENABLED = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected static final String CFG_KEY_ENABLED = "server.handler.connect.enabled";

	private final RouterLoggerEngine engine;

	public ConnectHandler(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	@Override
	protected void doPost(final HttpExchange exchange) throws IOException {
		// Headers...
		addCommonHeaders(exchange);

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(HtmlUtils.escapeHtml(Messages.get("lbl.server.connect"))));
		final boolean accepted = engine.canConnect();
		if (accepted) {
			engine.connect();
		}
		html.append("<h3>").append(accepted ? HtmlUtils.escapeHtml(Messages.get("msg.server.accepted")) : HtmlUtils.escapeHtml(Messages.get("msg.server.not.acceptable"))).append("</h3>").append(NewLine.CRLF);
		html.append(buildHtmlHomeButton());
		html.append(buildHtmlFooter());
		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(accepted ? HttpURLConnection.HTTP_ACCEPTED : HttpURLConnection.HTTP_PRECON_FAILED, response.length);
		exchange.getResponseBody().write(response);
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED);
	}

}
