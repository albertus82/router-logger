package it.albertus.router.server.html;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringEscapeUtils;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Messages;
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
	public void service(final HttpExchange exchange) throws IOException {
		final boolean save = exchange.getRequestMethod().equalsIgnoreCase(HttpMethod.POST) || exchange.getRequestMethod().equalsIgnoreCase(HttpMethod.PUT);
		if (save) {
			final String backup = configuration.toString();
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(exchange.getRequestBody(), baos, 1024);
			final Properties updatedProperties = new Properties();
			updatedProperties.load(new StringReader(StringUtils.substringAfter(StringEscapeUtils.unescapeHtml4(URLDecoder.decode(baos.toString(getCharset().name()), getCharset().name())), REQUEST_PARAM_NAME + '=')));
			if (!updatedProperties.equals(configuration.getProperties())) {
				FileWriter writer = null;
				try {
					writer = new FileWriter(configuration.getFileName());
					updatedProperties.store(writer, null);
				}
				catch (final IOException e) {
					throw new IllegalStateException(e);
				}
				finally {
					IOUtils.closeQuietly(writer);
				}
				configuration.reload();
				logger.log(Level.WARNING, Messages.get("msg.server.configuration.save"), new Object[] { exchange.getRemoteAddress(), exchange.getRequestHeaders().entrySet(), backup, configuration.toString(), NewLine.SYSTEM_LINE_SEPARATOR });
			}
		}

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.server.configuration")));
		html.append("<h3>").append(Messages.get("lbl.server.configuration")).append("</h3>").append(NewLine.CRLF);

		final File file = new File(configuration.getFileName());
		final Set<String> lines = new LinkedHashSet<String>(); // TODO semplificare
		if (file.exists()) {
			FileReader fr = null;
			BufferedReader br = null;
			try {
				fr = new FileReader(file);
				br = new BufferedReader(fr);
				String line;
				while ((line = br.readLine()) != null) {
					lines.add(line);
				}
			}
			catch (final IOException e) {
				throw new IllegalStateException(e);
			}
			finally {
				IOUtils.closeQuietly(br, fr);
			}
		}

		html.append(buildHtmlHomeButton());
		html.append(buildHtmlRefreshButton());

		html.append("<form action=\"").append(getPath()).append("\" method=\"").append(HttpMethod.POST).append("\"><div>");
		html.append("<input type=\"submit\" value=\"").append(Messages.get("lbl.server.save")).append("\" ").append("\" onclick=\"return confirm('").append(Messages.get("msg.server.configuration.confirm.save")).append("');\" />").append(NewLine.CRLF.toString());
		html.append("<textarea rows=\"25\" cols=\"80\" name=\"").append(REQUEST_PARAM_NAME).append("\">");
		for (final String line : lines) {
			html.append(StringEscapeUtils.escapeHtml4(line)).append(NewLine.SYSTEM_LINE_SEPARATOR);
		}
		html.append("</textarea>");
		html.append("</div></form>");

		html.append(buildHtmlFooter());

		final byte[] payload = html.toString().getBytes(getCharset());

		final String currentEtag = generateEtag(payload);
		addEtagHeader(exchange, currentEtag);

		// If-None-Match...
		final String ifNoneMatch = exchange.getRequestHeaders().getFirst("If-None-Match");
		if (ifNoneMatch != null && currentEtag != null && currentEtag.equals(ifNoneMatch)) {
			addDateHeader(exchange);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, -1);
			exchange.getResponseBody().close(); // Needed when no write occurs.
		}
		else {
			if (!save) {
				logger.log(Level.WARNING, Messages.get("msg.server.configuration.open"), new Object[] { exchange.getRemoteAddress(), exchange.getRequestHeaders().entrySet() });
			}
			addCommonHeaders(exchange);
			final byte[] response = compressResponse(payload, exchange);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			exchange.getResponseBody().write(response);
		}
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
	public String[] getMethods() {
		return METHODS;
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED);
	}

}
