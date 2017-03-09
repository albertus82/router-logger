package it.albertus.router.server.html;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Messages;
import it.albertus.router.server.BaseHttpHandler;
import it.albertus.router.server.BaseHttpServer;
import it.albertus.router.server.HttpException;
import it.albertus.router.server.HttpMethod;
import it.albertus.util.NewLine;
import it.albertus.util.StringUtils;
import it.albertus.util.logging.LoggerFactory;

public abstract class BaseHtmlHandler extends BaseHttpHandler {

	private static final Logger logger = LoggerFactory.getLogger(BaseHtmlHandler.class);

	public static class Defaults {
		public static final boolean LOG_REQUEST = true;
		public static final boolean COMPRESS_RESPONSE = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public static final String DEFAULT_STYLE = "";

	private static Object[] lastRequestInfo;

	private static final String MSG_KEY_LBL_ERROR = "lbl.error";

	private boolean found = true;

	public BaseHtmlHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	public BaseHtmlHandler() {
		super(null);
	}

	protected abstract void service(HttpExchange exchange) throws IOException, HttpException;

	/**
	 * Returns the method names (GET, POST, PUT, DELETE) that are allowed for
	 * this handler. Requests with forbidden methods will be bounced with
	 * <b>HTTP Status-Code 405: Method Not Allowed.</b>
	 * 
	 * @return the array containing the names of the methods that are allowed.
	 */
	public abstract String[] getMethodsAllowed();

	public boolean isFound() {
		return found;
	}

	public void setFound(final boolean found) {
		this.found = found;
	}

	protected boolean isEnabled(final HttpExchange exchange) throws IOException {
		if (!configuration.getBoolean("server.enabled", BaseHttpServer.Defaults.ENABLED) || !isEnabled()) {
			addCommonHeaders(exchange);

			final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get(MSG_KEY_LBL_ERROR)));
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

			final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get(MSG_KEY_LBL_ERROR)));
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
		boolean match = false;
		for (final String method : getMethodsAllowed()) {
			if (method.equalsIgnoreCase(exchange.getRequestMethod())) {
				match = true;
				break;
			}
		}
		if (!match) {
			addCommonHeaders(exchange);

			final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get(MSG_KEY_LBL_ERROR)));
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
		log(exchange);
		if (isEnabled(exchange) && isFound(exchange) && isMethodAllowed(exchange)) {
			try {
				service(exchange);
			}
			catch (final HttpException e) {
				logger.log(Level.WARNING, e.toString(), e);
				addCommonHeaders(exchange);

				final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get(MSG_KEY_LBL_ERROR)));
				html.append("<h3>").append(StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : getHttpStatusCodes().get(e.getStatusCode())).append("</h3>").append(NewLine.CRLF);
				html.append(buildHtmlFooter());

				final byte[] response = html.toString().getBytes(getCharset());
				exchange.sendResponseHeaders(e.getStatusCode(), response.length);
				exchange.getResponseBody().write(response);
			}
			catch (final IOException e) {
				logger.log(Level.FINE, e.toString(), e); // often caused by the client that interrupts the stream.
			}
			catch (final Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
				addCommonHeaders(exchange);

				final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get(MSG_KEY_LBL_ERROR)));
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
	}

	protected void sendResponse(final HttpExchange exchange, final String html) throws IOException {
		sendResponse(exchange, html.getBytes(getCharset()));
	}

	protected void log(final HttpExchange exchange) {
		final Level level = Level.INFO;
		if (logger.isLoggable(level) && configuration.getBoolean("server.log.request", Defaults.LOG_REQUEST)) {
			final Object[] requestInfo = new Object[] { exchange.getRemoteAddress(), exchange.getRequestMethod(), exchange.getRequestURI() };
			if (!Arrays.equals(requestInfo, getLastRequestInfo())) {
				setLastRequestInfo(requestInfo);
				logger.log(level, Messages.get("msg.server.log.request"), new Object[] { Thread.currentThread().getName(), exchange.getRemoteAddress(), exchange.getRequestMethod(), exchange.getRequestURI() });
			}
		}
	}

	protected static void updateLastRequestInfo(final Object[] request) {
		lastRequestInfo = request;
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
		html.append("<html lang=\"").append(Messages.getLanguage().getLocale().getLanguage()).append("\" xmlns=\"http://www.w3.org/1999/xhtml\">");
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
		html.append(buildHtmlHeadMeta());
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

	protected String buildHtmlHeadMeta() {
		final StringBuilder html = new StringBuilder();
		html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"); // responsive
		html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=").append(getCharset().name().toLowerCase()).append("\" />"); // XHTML
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

	protected String buildHtmlHomeButton() {
		if (configuration.getBoolean(RootHtmlHandler.CFG_KEY_ENABLED, RootHtmlHandler.Defaults.ENABLED)) {
			return new StringBuilder("<form action=\"").append(RootHtmlHandler.PATH).append("\" method=\"").append(RootHtmlHandler.METHODS[0]).append("\"><div><input type=\"submit\" value=\"").append(Messages.get("lbl.server.home")).append("\" /></div></form>").append(NewLine.CRLF.toString()).toString();
		}
		else {
			return "";
		}
	}

	protected String buildHtmlRefreshButton() {
		return new StringBuilder("<form action=\"").append(getPath()).append("\" method=\"").append(HttpMethod.GET).append("\"><div><input type=\"submit\" value=\"").append(Messages.get("lbl.server.refresh")).append("\" /></div></form>").append(NewLine.CRLF.toString()).toString();
	}

	/**
	 * Adds {@code Content-Type: text/html} and {@code Date} headers to the
	 * provided {@link HttpExchange} object.
	 * 
	 * @param exchange the {@link HttpExchange} to be modified.
	 */
	@Override
	protected void addCommonHeaders(final HttpExchange exchange) {
		exchange.getResponseHeaders().add("Content-Type", "text/html; charset=" + getCharset().name().toLowerCase());
		addDateHeader(exchange);
	}

	@Override
	protected boolean canCompressResponse(final HttpExchange exchange) {
		return configuration.getBoolean("server.compress.response", Defaults.COMPRESS_RESPONSE) && super.canCompressResponse(exchange);
	}

	protected static Object[] getLastRequestInfo() {
		return lastRequestInfo;
	}

	protected static void setLastRequestInfo(final Object[] lastRequestInfo) {
		BaseHtmlHandler.lastRequestInfo = lastRequestInfo;
	}

}
