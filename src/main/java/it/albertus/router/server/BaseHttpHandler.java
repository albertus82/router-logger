package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
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

	public interface Defaults {
		int LOG_REQUEST = 1;
	}

	public static final String PREFERRED_CHARSET = "UTF-8";

	protected static final HttpDateGenerator httpDateGenerator = new HttpDateGenerator();

	protected static String lastRequest = null;

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

	/**
	 * Returns the method names (GET, POST, PUT, DELETE) that are allowed for
	 * this handler. <b>By default all methods are allowed.</b> Requests with
	 * forbidden methods will be bounced with <b>HTTP Status-Code 405: Method
	 * Not Allowed.</b>
	 * 
	 * @return the array containing the names of the methods that are allowed.
	 */
	public String[] getMethods() {
		return null;
	}

	/**
	 * Returns if this handler is enabled. <b>Handlers are enabled by
	 * default.</b> Requests to disabled handlers will be bounced with <b>HTTP
	 * Status-Code 403: Forbidden.</b>
	 * 
	 * @return {@code true} if this handler is enabled, otherwise {@code false}.
	 */
	public boolean isEnabled() {
		return true;
	}

	protected boolean isEnabled(final HttpExchange exchange) throws IOException {
		if (!configuration.getBoolean("server.enabled", BaseHttpServer.Defaults.ENABLED) || !isEnabled()) {
			addCommonHeaders(exchange);

			final StringBuilder html = new StringBuilder(buildHtmlHeader(Resources.get("lbl.error")));
			html.append("<h3>").append(Resources.get("msg.server.forbidden")).append("</h3>").append(NewLine.CRLF);
			html.append(buildHtmlFooter());

			final byte[] response = html.toString().getBytes(getCharset());
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
			return false;
		}
		else {
			return true;
		}
	}

	protected boolean isMethodAllowed(final HttpExchange exchange) throws IOException {
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
			addCommonHeaders(exchange);

			final StringBuilder html = new StringBuilder(buildHtmlHeader(Resources.get("lbl.error")));
			html.append("<h3>").append(Resources.get("msg.server.bad.method")).append("</h3>").append(NewLine.CRLF);
			html.append(buildHtmlFooter());

			final byte[] response = html.toString().getBytes(getCharset());
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		}
		return match;
	}

	@Override
	public void handle(final HttpExchange exchange) throws IOException {
		if (isEnabled(exchange) && isMethodAllowed(exchange)) {
			try {
				service(exchange);
			}
			catch (final IOException ioe) {
				// Ignore (often caused by the client that interrupts the stream).
			}
			catch (final Exception exception) {
				Logger.getInstance().log(exception);
				addCommonHeaders(exchange);

				final StringBuilder html = new StringBuilder(buildHtmlHeader(Resources.get("lbl.error")));
				html.append("<h3>").append(Resources.get("err.server.handler")).append("</h3>").append(NewLine.CRLF);
				html.append(buildHtmlFooter());

				final byte[] response = html.toString().getBytes(getCharset());
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, response.length);
				exchange.getResponseBody().write(response);
			}
			finally {
				exchange.close();
			}
		}
		log(exchange);
	}

	protected void log(final HttpExchange exchange) {
		final Destination[] destinations;
		switch (configuration.getInt("server.log.request", Defaults.LOG_REQUEST)) {
		case 1:
			destinations = new Destination[] { Destination.CONSOLE };
			break;
		case 2:
			destinations = new Destination[] { Destination.CONSOLE, Destination.FILE };
			break;
		default:
			return;
		}

		if (destinations != null) {
			final String request = exchange.getRemoteAddress() + " " + exchange.getRequestMethod() + " " + exchange.getRequestURI();
			if (!request.equals(lastRequest)) {
				lastRequest = request;
				Logger.getInstance().log(Resources.get("msg.server.log.request", request), destinations);
			}
		}
	}

	/**
	 * Creates HTML5 doctype, {@code <html>} opening tag, full {@code <head>}
	 * with {@code <title>}, and {@code <body>} opening tag.
	 * 
	 * @param title the title to be included in {@code <title>} tag, after the
	 *        application name. If null or empty, no {@code <title>} will be
	 *        created.
	 * 
	 * @return the string containing the HTML code.
	 */
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

	/**
	 * Closes {@code <body>} and {@code <html>} tags.
	 * 
	 * @return the string containing the HTML code.
	 */
	protected String buildHtmlFooter() {
		return "</body></html>";
	}

	/**
	 * Adds {@code Content-Type: text/html} and {@code Date} headers to the
	 * provided {@link HttpExchange} object.
	 * 
	 * @param exchange the {@link HttpExchange} to be modified.
	 */
	protected void addCommonHeaders(final HttpExchange exchange) {
		final Headers headers = exchange.getResponseHeaders();
		headers.add("Content-Type", "text/html; charset=" + getCharset().name());
		headers.add("Date", httpDateGenerator.getCurrentDate());
	}

	protected Charset getCharset() {
		return charset;
	}

}
