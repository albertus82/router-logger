package it.albertus.router.server.html;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Messages;
import it.albertus.router.server.HttpMethod;
import it.albertus.router.util.LogManager;
import it.albertus.util.IOUtils;
import it.albertus.util.NewLine;
import it.albertus.util.StringUtils;
import it.albertus.util.logging.LoggerFactory;

public class LogsHandler extends BaseHtmlHandler {

	private static final Logger logger = LoggerFactory.getLogger(LogsHandler.class);

	public static class Defaults {
		public static final boolean ENABLED = true;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public static final String PATH = "/logs";
	public static final String CLEAR_PATH_INFO = "clear";

	protected static final String[] METHODS = { HttpMethod.GET, HttpMethod.DELETE, HttpMethod.POST };

	protected static final String CFG_KEY_ENABLED = "server.handler.logs.enabled";

	private static final int BUFFER_SIZE = 4 * 1024;

	public LogsHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(final HttpExchange exchange) throws IOException {
		final String requestMethod = exchange.getRequestMethod();
		final String pathInfo = StringUtils.substringAfter(exchange.getRequestURI().toString(), PATH + '/');

		if (pathInfo == null || pathInfo.trim().isEmpty()) { // List log files (no file name present in URL)
			fileList(exchange);
		}
		else if (pathInfo.trim().equals(CLEAR_PATH_INFO) && (HttpMethod.DELETE.equalsIgnoreCase(requestMethod) || HttpMethod.POST.equalsIgnoreCase(requestMethod))) { // Delete all log files
			deleteAll(exchange);
		}
		else { // The URL contains a log file name
			final String decodedFileName = URLDecoder.decode(pathInfo, getCharset().name());
			final File file = new File(LogManager.getCurrentFile().getParentFile() + File.separator + decodedFileName);
			if (!file.exists() || file.isDirectory()) {
				notFound(exchange);
			}
			else {
				if (HttpMethod.GET.equalsIgnoreCase(requestMethod)) { // Download log file
					download(exchange, file);
				}
				else if (HttpMethod.DELETE.equalsIgnoreCase(requestMethod) || HttpMethod.POST.equalsIgnoreCase(requestMethod)) { // Delete log file
					delete(exchange, file);
				}
				else {
					throw new IllegalStateException("Method not supported: " + requestMethod);
				}
			}
		}
	}

	private void deleteAll(final HttpExchange exchange) throws IOException {
		LogManager.deleteAllFiles();
		refresh(exchange);
	}

	private void delete(final HttpExchange exchange, final File file) throws IOException {
		LogManager.deleteFile(file);
		refresh(exchange);
	}

	private void download(final HttpExchange exchange, final File file) throws IOException {
		if (LogManager.getCurrentFile().equals(file)) {
			synchronized (LogManager.class) {
				doDownload(exchange, file);
			}
		}
		else {
			doDownload(exchange, file);
		}
	}

	private void doDownload(final HttpExchange exchange, final File file) throws IOException {
		FileInputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(file);
			addDateHeader(exchange);
			exchange.getResponseHeaders().add("Content-Type", "text/x-log");
			exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
			if (canCompressResponse(exchange)) {
				addGzipHeader(exchange);
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0); // Transfer-Encoding: chunked
				output = new GZIPOutputStream(exchange.getResponseBody(), BUFFER_SIZE);
			}
			else {
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, file.length());
				output = exchange.getResponseBody();
			}
			IOUtils.copy(input, output, BUFFER_SIZE);
		}
		catch (final FileNotFoundException e) {
			logger.log(Level.WARNING, e.toString(), e);
			notFound(exchange);
		}
		finally {
			IOUtils.closeQuietly(output, input);
			exchange.close();
		}
	}

	private void notFound(final HttpExchange exchange) throws IOException {
		addCommonHeaders(exchange);

		final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.error")));
		html.append("<h3>").append(Messages.get("msg.server.not.found")).append("</h3>").append(NewLine.CRLF);
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
		exchange.getResponseBody().write(response);
		exchange.getResponseBody().close();
		exchange.close();
	}

	private void fileList(final HttpExchange exchange) throws IOException {
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.server.logs")));

		final File[] files = LogManager.listFiles();

		html.append("<h3>").append(files == null || files.length == 0 ? Messages.get("lbl.server.logs.title.empty") : Messages.get("lbl.server.logs.title", files.length)).append("</h3>").append(NewLine.CRLF);

		if (files != null && files.length > 0) {
			html.append(buildHtmlTable(files));
		}

		html.append(buildHtmlHomeButton());
		html.append(buildHtmlRefreshButton());
		if (files != null && files.length > 0) {
			html.append(buildHtmlDeleteAllButton());
		}
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
			addCommonHeaders(exchange);
			final byte[] response = compressResponse(payload, exchange);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			exchange.getResponseBody().write(response);
		}
	}

	private String buildHtmlTable(final File[] files) throws UnsupportedEncodingException {
		Arrays.sort(files);
		final StringBuilder html = new StringBuilder();
		html.append("<table><thead><tr>");
		html.append("<th>").append(Messages.get("lbl.server.logs.list.name")).append("</th>");
		html.append("<th>").append(Messages.get("lbl.server.logs.list.date")).append("</th>");
		html.append("<th>").append(Messages.get("lbl.server.logs.list.size")).append("</th>");
		html.append("<th>").append(Messages.get("lbl.server.logs.list.action")).append("</th>");
		html.append("</tr></thead><tbody>").append(NewLine.CRLF);
		final DateFormat dateFormatFileList = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Messages.getLanguage().getLocale());
		final NumberFormat numberFormatFileList = NumberFormat.getIntegerInstance(Messages.getLanguage().getLocale());
		for (final File file : files) {
			final String encodedFileName = URLEncoder.encode(file.getName(), getCharset().name());
			html.append("<tr>");
			html.append("<td>");
			html.append("<a href=\"").append(PATH).append('/').append(encodedFileName).append("\">");
			html.append(file.getName());
			html.append("</a>");
			html.append("</td>");
			html.append("<td class=\"right\">").append(dateFormatFileList.format(new Date(file.lastModified()))).append("</td>");
			html.append("<td class=\"right\">").append(Messages.get("lbl.server.logs.list.size.kb", numberFormatFileList.format(Math.max(1, file.length() / 1024)))).append("</td>");
			html.append("<td class=\"center\">");
			html.append("<form action=\"").append(PATH).append('/').append(encodedFileName).append("\" method=\"POST\">");
			html.append("<input type=\"submit\" value=\"").append(Messages.get("lbl.server.logs.list.delete")).append("\" onclick=\"return confirm('").append(Messages.get("msg.server.logs.delete", file.getName().replace("'", "\\x27"))).append("');\"").append(file.equals(LogManager.getCurrentFile()) ? " disabled=\"disabled\"" : "").append(" />");
			html.append("</form>");
			html.append("</td>");
			html.append("</tr>").append(NewLine.CRLF);
		}
		html.append("</tbody></table>").append(NewLine.CRLF);
		return html.toString();
	}

	private String buildHtmlDeleteAllButton() {
		return new StringBuilder("<form action=\"").append(PATH).append('/').append(CLEAR_PATH_INFO).append("\" method=\"").append(HttpMethod.POST).append("\"><input type=\"submit\" value=\"").append(Messages.get("lbl.server.logs.delete.all")).append("\" onclick=\"return confirm('").append(Messages.get("msg.server.logs.delete.all")).append("');\" /></form>").append(NewLine.CRLF.toString()).toString();
	}

	private void refresh(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders().add("Location", PATH);
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP, -1);
		exchange.getResponseBody().close();
		exchange.close();
	}

	@Override
	protected String buildHtmlHeadStyle() {
		return "<style type=\"text/css\">form {display: inline;} table {margin-top: 1em; margin-bottom: 1em;} td.center {text-align: center;} td.right {text-align: right;}</style>";
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
