package it.albertus.router.server.html;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Messages;
import it.albertus.router.server.HttpException;
import it.albertus.router.server.HttpMethod;
import it.albertus.util.IOUtils;
import it.albertus.util.NewLine;
import it.albertus.util.StringUtils;
import it.albertus.util.logging.LoggerFactory;

public class ConfigurationHandler extends BaseHtmlHandler {

	private static final String REQUEST_PARAM_NAME = "properties";

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationHandler.class);

	public static class Defaults {
		public static final boolean ENABLED = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public static final String PATH = "/configuration";

	protected static final String[] METHODS = { HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT };

	protected static final String CFG_KEY_ENABLED = "server.handler.configuration.enabled";

	public ConfigurationHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(final HttpExchange exchange) throws IOException, HttpException {
		final String method = exchange.getRequestMethod();
		if (HttpMethod.GET.equalsIgnoreCase(method)) {
			load(exchange);
		}
		else if (HttpMethod.POST.equalsIgnoreCase(method) || exchange.getRequestMethod().equalsIgnoreCase(HttpMethod.PUT)) {
			save(exchange);
		}
		else {
			throw new IllegalStateException(method);
		}
	}

	private void load(final HttpExchange exchange) throws IOException {
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.server.configuration")));
		html.append("<h3>").append(Messages.get("lbl.server.configuration")).append("</h3>").append(NewLine.CRLF);

		html.append(buildHtmlHomeButton());
		html.append(buildHtmlRefreshButton());

		html.append("<form action=\"").append(getPath()).append("\" method=\"").append(HttpMethod.POST).append("\"><div>");
		html.append("<input type=\"submit\" value=\"").append(Messages.get("lbl.server.save")).append("\" onclick=\"return confirm('").append(Messages.get("msg.server.configuration.confirm.save")).append("');\" />").append(NewLine.CRLF.toString());
		html.append("<textarea rows=\"25\" cols=\"80\" name=\"").append(REQUEST_PARAM_NAME).append("\">");
		html.append(escapeHtml(getPropertiesAsString(configuration.getProperties())));
		html.append("</textarea>");
		html.append("</div></form>").append(NewLine.SYSTEM_LINE_SEPARATOR);

		html.append(buildHtmlFooter());

		sendResponse(exchange, html.toString());
	}

	private void save(final HttpExchange exchange) throws IOException, HttpException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(exchange.getRequestBody(), baos, 1024);
		final String requestBody = baos.toString(getCharset().name());
		final String prefix = REQUEST_PARAM_NAME + '=';
		if (!requestBody.startsWith(prefix)) {
			throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, Messages.get("msg.server.bad.request"));
		}
		final String backup = configuration.toString();
		final Properties updatedProperties = new Properties();
		try {
			updatedProperties.load(new StringReader(unescapeHtml(URLDecoder.decode(StringUtils.substringAfter(requestBody, prefix), getCharset().name()))));
		}
		catch (final IOException e) {
			throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, Messages.get("msg.server.bad.request"), e);
		}
		if (!updatedProperties.equals(configuration.getProperties())) {
			try {
				configuration.save(updatedProperties);
				configuration.reload();
				logger.log(Level.WARNING, Messages.get("msg.server.configuration.save"), new Object[] { Thread.currentThread().getName(), exchange.getRemoteAddress(), exchange.getRequestMethod(), exchange.getRequestURI(), backup, configuration.toString(), NewLine.SYSTEM_LINE_SEPARATOR });
			}
			catch (final IOException e) {
				throw new HttpException(HttpURLConnection.HTTP_INTERNAL_ERROR, Messages.get("err.server.handler"), e);
			}
		}

		// Post/Redirect/Get
		addDateHeader(exchange);
		exchange.getResponseHeaders().add("Location", getPath());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP, -1);
		exchange.getResponseBody().close(); // Needed when no write occurs.
	}

	@Override
	protected String buildHtmlHeadStyle() {
		return "<style type=\"text/css\">form {display: inline;} div {display: inline;} textarea {display: block; margin-top: 1.75em;}</style>";
	}

	@Override
	public String getPath() {
		return PATH;
	}

	@Override
	public String[] getMethodsAllowed() {
		return METHODS;
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED);
	}

	@Override
	protected void log(final HttpExchange exchange) {
		final Level level = Level.WARNING;
		if (logger.isLoggable(level) && configuration.getBoolean("server.log.request", BaseHtmlHandler.Defaults.LOG_REQUEST)) {
			final Object[] requestInfo = new Object[] { exchange.getRemoteAddress(), exchange.getRequestURI() };
			if (!Arrays.equals(requestInfo, getLastRequestInfo())) {
				setLastRequestInfo(requestInfo);
				logger.log(level, Messages.get("msg.server.configuration.open"), new Object[] { Thread.currentThread().getName(), exchange.getRemoteAddress(), exchange.getRequestMethod(), exchange.getRequestURI() });
			}
		}
	}

	private static String getPropertiesAsString(final Properties properties) {
		final StringBuilder sb = new StringBuilder();
		try {
			final StringWriter writer = new StringWriter();
			properties.store(writer, null);
			for (final String line : getSortedLines(new BufferedReader(new StringReader(writer.toString())))) {
				sb.append(line).append(NewLine.SYSTEM_LINE_SEPARATOR);
			}
		}
		catch (final IOException e) {
			throw new IllegalStateException(e); // StringWriter cannot throw IOException
		}
		return sb.toString();
	}

	private static Set<String> getSortedLines(final BufferedReader reader) throws IOException {
		final Set<String> lines = new TreeSet<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.startsWith("#")) { // discard comments
				lines.add(line);
			}
		}
		return lines;
	}

	private static String escapeHtml(final String unescaped) {
		return unescaped.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static String unescapeHtml(final String escaped) {
		return escaped.replace("&gt;", ">").replace("&lt;", "<").replace("&quot;", "\"").replace("&amp;", "&");
	}

}
