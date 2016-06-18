package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

public class DisconnectHandler extends BaseHttpHandler {

	public interface Defaults {
		boolean ENABLED = false;
	}

	public static final String PATH = "/disconnect";
	public static final String[] METHODS = { "POST" };

	protected static final String CFG_KEY_ENABLED = "server.handler.disconnect.enabled";

	protected DisconnectHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(final HttpExchange exchange) throws IOException {
		// Headers...
		addCommonHeaders(exchange);

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Resources.get("lbl.server.disconnect")));
		final boolean accepted = engine.canDisconnect();
		if (accepted) {
			engine.disconnect();
		}
		html.append("<h3>").append(accepted ? Resources.get("msg.server.accepted") : Resources.get("msg.server.not.acceptable")).append("</h3>").append(NewLine.CRLF.toString());
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
