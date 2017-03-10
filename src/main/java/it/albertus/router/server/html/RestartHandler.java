package it.albertus.router.server.html;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;

public class RestartHandler extends BaseHtmlHandler {

	public static class Defaults {
		public static final boolean ENABLED = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public static final String PATH = "/restart";

	protected static final String CFG_KEY_ENABLED = "server.handler.restart.enabled";

	public RestartHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void doPost(final HttpExchange exchange) throws IOException {
		// Headers...
		addCommonHeaders(exchange);

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.server.restart")));
		html.append("<h3>").append(Messages.get("msg.server.accepted")).append("</h3>").append(NewLine.CRLF.toString());
		html.append(buildHtmlHomeButton());
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
		exchange.getResponseBody().write(response);
		exchange.close();

		engine.restart();
	}

	@Override
	public String getPath() {
		return PATH;
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED);
	}

}
