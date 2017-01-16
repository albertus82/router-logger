package it.albertus.router.server.html;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Messages;
import it.albertus.router.server.HttpMethod;
import it.albertus.util.NewLine;

public class DisconnectHandler extends BaseHtmlHandler {

	public static class Defaults {
		public static final boolean ENABLED = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public static final String PATH = "/disconnect";

	protected static final String[] METHODS = { HttpMethod.POST };

	protected static final String CFG_KEY_ENABLED = "server.handler.disconnect.enabled";

	public DisconnectHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(final HttpExchange exchange) throws IOException {
		// Headers...
		addCommonHeaders(exchange);

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.server.disconnect")));
		final boolean accepted = engine.canDisconnect();
		if (accepted) {
			engine.disconnect();
		}
		html.append("<h3>").append(accepted ? Messages.get("msg.server.accepted") : Messages.get("msg.server.not.acceptable")).append("</h3>").append(NewLine.CRLF.toString());
		html.append(buildHtmlHomeButton());
		html.append(buildHtmlFooter());
		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(accepted ? HttpURLConnection.HTTP_ACCEPTED : HttpURLConnection.HTTP_UNAVAILABLE, response.length);
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

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED);
	}

}
