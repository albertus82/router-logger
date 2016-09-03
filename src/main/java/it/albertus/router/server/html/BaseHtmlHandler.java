package it.albertus.router.server.html;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Messages;
import it.albertus.router.server.BaseHttpHandler;
import it.albertus.router.server.BaseHttpServer;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.NewLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import com.sun.net.httpserver.HttpExchange;

public abstract class BaseHtmlHandler extends BaseHttpHandler {

	public interface Defaults {
		byte LOG_REQUEST = 1;
		boolean COMPRESS_RESPONSE = false;
	}

	public static final String DEFAULT_STYLE = "";

	protected static String lastRequest = null;

	private boolean found = true;

	public BaseHtmlHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	public BaseHtmlHandler() {
		super(null);
	}

	protected abstract void service(HttpExchange exchange) throws IOException;

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

	public boolean isFound() {
		return found;
	}

	public void setFound(final boolean found) {
		this.found = found;
	}

	protected boolean isEnabled(final HttpExchange exchange) throws IOException {
		if (!configuration.getBoolean("server.enabled", BaseHttpServer.Defaults.ENABLED) || !isEnabled()) {
			addCommonHeaders(exchange);

			final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.error")));
			html.append("<h3>").append(Messages.get("msg.server.forbidden")).append("</h3>").append(NewLine.CRLF);
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

	protected boolean isFound(final HttpExchange exchange) throws IOException {
		if (!isFound()) {
			addCommonHeaders(exchange);

			final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.error")));
			html.append("<h3>").append(Messages.get("msg.server.not.found")).append("</h3>").append(NewLine.CRLF);
			html.append(buildHtmlFooter());

			final byte[] response = html.toString().getBytes(getCharset());
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
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

			final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.error")));
			html.append("<h3>").append(Messages.get("msg.server.bad.method")).append("</h3>").append(NewLine.CRLF);
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
		if (isEnabled(exchange) && isFound(exchange) && isMethodAllowed(exchange)) {
			try {
				service(exchange);
			}
			catch (final IOException ioe) {
				// Ignore (often caused by the client that interrupts the stream).
			}
			catch (final Exception exception) {
				Logger.getInstance().log(exception);
				addCommonHeaders(exchange);

				final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.error")));
				html.append("<h3>").append(Messages.get("err.server.handler")).append("</h3>").append(NewLine.CRLF);
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
		switch (configuration.getByte("server.log.request", Defaults.LOG_REQUEST)) {
		case 1:
			destinations = new Destination[] { Destination.CONSOLE };
			break;
		case 2:
			destinations = new Destination[] { Destination.FILE };
			break;
		case 3:
			destinations = new Destination[] { Destination.CONSOLE, Destination.FILE };
			break;
		default:
			return;
		}

		if (destinations != null) {
			final String request = exchange.getRemoteAddress() + " " + exchange.getRequestMethod() + " " + exchange.getRequestURI();
			if (!request.equals(lastRequest)) {
				lastRequest = request;
				Logger.getInstance().log(Messages.get("msg.server.log.request", Thread.currentThread().getName() + " " + request), destinations);
			}
		}
	}

	protected byte[] compressResponse(final byte[] uncompressed, final HttpExchange exchange) {
		if (configuration.getBoolean("server.compress.response", Defaults.COMPRESS_RESPONSE)) {
			final List<String> headers = exchange.getRequestHeaders().get("Accept-Encoding");
			if (headers != null) {
				for (final String header : headers) {
					if (header != null && header.trim().toLowerCase().contains("gzip")) {
						try {
							return doCompressResponse(uncompressed, exchange);
						}
						catch (final IOException ioe) {
							Logger.getInstance().log(ioe);
							return uncompressed;
						}
					}
				}
			}
		}
		return uncompressed;
	}

	protected byte[] doCompressResponse(final byte[] uncompressed, final HttpExchange exchange) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(uncompressed.length / 4);
		GZIPOutputStream gzos = null;
		try {
			gzos = new GZIPOutputStream(baos);
			gzos.write(uncompressed);
		}
		finally {
			try {
				gzos.close();
			}
			catch (final Exception e) {}
			try {
				baos.close();
			}
			catch (final Exception e) {}
		}
		exchange.getResponseHeaders().add("Content-Encoding", "gzip");
		return baos.toByteArray();
	}

	/**
	 * Creates HTML5 doctype, {@code <html>} opening tag, full {@code <head>}
	 * with {@code <title>}, {@code <style>} and {@code <body>} opening tag.
	 * 
	 * @param title the title to be included in {@code <title>} tag, after the
	 *        application name. If null or empty, nothing but the application
	 *        name will be used.
	 * 
	 * @return the string containing the HTML code.
	 */
	protected String buildHtmlHeader(final String title) {
		final StringBuilder html = new StringBuilder("<!DOCTYPE html>").append(NewLine.CRLF.toString());
		html.append("<html lang=\"").append(Messages.getLanguage().getLocale().getLanguage()).append("\">");
		html.append(buildHtmlHead(title));
		html.append("<body>").append(NewLine.CRLF.toString());
		html.append("<h1>").append(Messages.get("msg.application.name")).append("</h1>").append(NewLine.CRLF.toString());
		return html.toString();
	}

	/**
	 * Creates full {@code <head>} with {@code <title>}, and {@code <style>}
	 * elements.
	 * 
	 * @param title the title to be included in {@code <title>} tag, after the
	 *        application name. If null or empty, nothing but the application
	 *        name will be used.
	 * 
	 * @return the string containing the HTML code.
	 */
	protected String buildHtmlHead(final String title) {
		final StringBuilder html = new StringBuilder("<head>");
		html.append(buildHtmlHeadTitle(title));
		html.append(buildHtmlHeadStyle());
		html.append("</head>");
		return html.toString();
	}

	/**
	 * Creates {@code <title>} element.
	 * 
	 * @param title the title to be included after the application name. If null
	 *        or empty, nothing but the application name will be used.
	 * 
	 * @return the string containing the HTML code.
	 */
	protected String buildHtmlHeadTitle(final String title) {
		final StringBuilder html = new StringBuilder("<title>").append(Messages.get("msg.application.name"));
		if (title != null && !title.trim().isEmpty()) {
			html.append(" - ").append(title.trim());
		}
		return html.append("</title>").toString();
	}

	/**
	 * Override this method to create {@code <style>} element. The default
	 * implementation returns an empty string.
	 * 
	 * @return the string containing the HTML code.
	 */
	protected String buildHtmlHeadStyle() {
		return DEFAULT_STYLE;
	}

	/**
	 * Closes {@code <body>} and {@code <html>} tags.
	 * 
	 * @return the string containing the HTML code.
	 */
	protected String buildHtmlFooter() {
		return "</body></html>";
	}

	protected String buildHtmlHomeButton() {
		if (configuration.getBoolean(RootHtmlHandler.CFG_KEY_ENABLED, RootHtmlHandler.Defaults.ENABLED)) {
			return new StringBuilder("<form action=\"").append(RootHtmlHandler.PATH).append("\" method=\"").append(RootHtmlHandler.METHODS[0]).append("\"><input type=\"submit\" value=\"").append(Messages.get("lbl.server.home")).append("\" /></form>").append(NewLine.CRLF.toString()).toString();
		}
		else {
			return DEFAULT_STYLE;
		}
	}

	/**
	 * Adds {@code Content-Type: text/html} and {@code Date} headers to the
	 * provided {@link HttpExchange} object.
	 * 
	 * @param exchange the {@link HttpExchange} to be modified.
	 */
	@Override
	protected void addCommonHeaders(final HttpExchange exchange) {
		exchange.getResponseHeaders().add("Content-Type", "text/html; charset=" + getCharset().name());
		addDateHeader(exchange);
	}

}
