package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.router.server.WebServer.Defaults;
import it.albertus.router.util.Logger;
import it.albertus.util.Configuration;
import it.albertus.util.NewLine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class BaseHttpHandler implements HttpHandler {

	public static final String PREFERRED_CHARSET = "UTF-8";

	protected final Configuration configuration = RouterLoggerConfiguration.getInstance();
	protected final RouterLoggerEngine engine;

	protected BaseHttpHandler(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	public abstract void service(HttpExchange exchange) throws IOException;

	public abstract String getPath();

	public String[] getMethods() {
		return null;
	}

	protected Charset getCharset() {
		try {
			return Charset.forName(PREFERRED_CHARSET);
		}
		catch (final Exception e) {
			return Charset.defaultCharset();
		}
	}

	@Override
	public void handle(final HttpExchange exchange) throws IOException {
		// Check if the server is enabled...
		if (!configuration.getBoolean("server.enabled", Defaults.ENABLED)) {
			final Charset charset = getCharset();
			final byte[] response = Resources.get("msg.server.forbidden").getBytes(charset);
			exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=" + charset.name());
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
			return;
		}

		// Check HTTP method...
		boolean match;
		if (getMethods() == null) {
			match = true;
		}
		else {
			match = false;
			for (final String method : getMethods()) {
				if (method.equalsIgnoreCase(exchange.getRequestMethod())) {
					match = true;
					break;
				}
			}
		}
		if (!match) {
			final Charset charset = getCharset();
			final byte[] response = Resources.get("msg.server.bad.method").getBytes(charset);
			exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=" + charset.name());
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
			return;
		}

		// Service...
		try {
			service(exchange);
		}
		catch (final Exception exception) {
			Logger.getInstance().log(exception);
			final Charset charset = getCharset();
			final byte[] response = Resources.get("err.server.handler").getBytes(charset);
			exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=" + charset.name());
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, response.length);
			exchange.getResponseBody().write(response);
		}
		finally {
			exchange.close();
		}
	}

	protected String buildHtmlHeader() {
		StringBuilder html = new StringBuilder();
		html.append("<html><head></head><body>");
		html.append("<h1>").append(Resources.get("msg.application.name")).append("</h1>").append(NewLine.CRLF);
		return html.toString();
	}

	protected String buildHtmlFooter() {
		return "</body></html>";
	}

}
