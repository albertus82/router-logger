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

import org.apache.http.protocol.HttpDateGenerator;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class BaseHttpHandler implements HttpHandler {

	public static final String PREFERRED_CHARSET = "UTF-8";

	protected static final HttpDateGenerator httpDateGenerator = new HttpDateGenerator();

	private static final Charset charset = initCharset();

	private static Charset initCharset() {
		try {
			return Charset.forName(PREFERRED_CHARSET);
		}
		catch (final Exception e) {
			return Charset.defaultCharset();
		}
	}

	protected final Configuration configuration = RouterLoggerConfiguration.getInstance();
	protected final RouterLoggerEngine engine;

	protected BaseHttpHandler(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	protected abstract void service(HttpExchange exchange) throws IOException;

	public abstract String getPath();

	public String[] getMethods() {
		return null;
	}

	@Override
	public void handle(final HttpExchange exchange) throws IOException {
		// Check if the server is enabled...
		if (!configuration.getBoolean("server.enabled", Defaults.ENABLED)) {
			final Charset charset = getCharset();
			exchange.getResponseHeaders().add("Content-Type", "text/html; charset=" + charset.name());

			final StringBuilder html = new StringBuilder(buildHtmlHeader(Resources.get("lbl.error")));
			html.append("<h3>").append(Resources.get("msg.server.forbidden")).append("</h3>").append(NewLine.CRLF);
			html.append(buildHtmlFooter());

			final byte[] response = html.toString().getBytes(charset);
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
			exchange.getResponseHeaders().add("Content-Type", "text/html; charset=" + charset.name());

			final StringBuilder html = new StringBuilder(buildHtmlHeader(Resources.get("lbl.error")));
			html.append("<h3>").append(Resources.get("msg.server.bad.method")).append("</h3>").append(NewLine.CRLF);
			html.append(buildHtmlFooter());

			final byte[] response = html.toString().getBytes(charset);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
			return;
		}

		// Service...
		try {
			service(exchange);
		}
		catch (final IOException ioe) {
			// Ignore (often caused by the client that interrupts the stream).
		}
		catch (final Exception exception) {
			Logger.getInstance().log(exception);
			final Charset charset = getCharset();
			exchange.getResponseHeaders().add("Content-Type", "text/html; charset=" + charset.name());

			final StringBuilder html = new StringBuilder(buildHtmlHeader(Resources.get("lbl.error")));
			html.append("<h3>").append(Resources.get("err.server.handler")).append("</h3>").append(NewLine.CRLF);
			html.append(buildHtmlFooter());

			final byte[] response = html.toString().getBytes(charset);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, response.length);
			exchange.getResponseBody().write(response);
		}
		finally {
			exchange.close();
		}
	}

	protected String buildHtmlHeader(final String title) {
		final StringBuilder html = new StringBuilder("<!DOCTYPE html>").append(NewLine.CRLF.toString());
		html.append("<html lang=\"").append(Resources.getLanguage().getLocale().getLanguage()).append("\"><head>");
		if (title != null && !title.isEmpty()) {
			html.append("<title>").append(Resources.get("msg.application.name")).append(" - ").append(title).append("</title>");
		}
		html.append("</head><body>").append(NewLine.CRLF.toString());
		html.append("<h1>").append(Resources.get("msg.application.name")).append("</h1>").append(NewLine.CRLF.toString());
		return html.toString();
	}

	protected String buildHtmlFooter() {
		return "</body></html>";
	}

	protected void addCommonHeaders(final HttpExchange exchange) {
		final Headers headers = exchange.getResponseHeaders();
		headers.add("Content-Type", "text/html; charset=" + getCharset().name());
		headers.add("Date", httpDateGenerator.getCurrentDate());
	}

	protected Charset getCharset() {
		return charset;
	}

}
