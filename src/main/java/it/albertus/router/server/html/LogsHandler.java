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
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.resources.Messages;
import it.albertus.router.server.HttpException;
import it.albertus.router.server.HttpMethod;
import it.albertus.router.server.RequestParameterExtractor;
import it.albertus.router.server.annotation.Path;
import it.albertus.router.util.logging.LogFileManager;
import it.albertus.util.IOUtils;
import it.albertus.util.NewLine;
import it.albertus.util.StringUtils;
import it.albertus.util.logging.LoggerFactory;

@Path("/logs")
public class LogsHandler extends BaseHtmlHandler {

	private static final Logger logger = LoggerFactory.getLogger(LogsHandler.class);

	public static class Defaults {
		public static final boolean ENABLED = true;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected static final String CFG_KEY_ENABLED = "server.handler.logs.enabled";

	private static final int BUFFER_SIZE = 4 * 1024;

	private static final LogFileManager logFileManager = LogFileManager.getInstance();

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException, HttpException {
		final String pathInfo = StringUtils.substringAfter(exchange.getRequestURI().toString(), getPath(this.getClass()) + '/');
		if (pathInfo == null || pathInfo.trim().isEmpty()) { // List log files (no file name present in URL)
			fileList(exchange);
		}
		else {
			final String decodedFileName = URLDecoder.decode(pathInfo, getCharset().name());
			final File file = new File(logFileManager.getPath() + File.separator + decodedFileName);
			if (!file.exists() || file.isDirectory()) {
				notFound(exchange);
			}
			else {
				download(exchange, file);
			}
		}
	}

	@Override
	protected void doPost(final HttpExchange exchange) throws IOException, HttpException {
		if (HttpMethod.DELETE.equalsIgnoreCase(new RequestParameterExtractor(exchange, getCharset()).getParameter("_method"))) {
			doDelete(exchange);
		}
		else {
			super.doPost(exchange);
		}
	}

	@Override
	protected void doDelete(final HttpExchange exchange) throws IOException, HttpException {
		final String pathInfo = StringUtils.substringAfter(exchange.getRequestURI().toString(), getPath(this.getClass()) + '/');
		if (pathInfo.trim().isEmpty()) { // Delete all log files
			deleteAll(exchange);
		}
		else {
			final String decodedFileName = URLDecoder.decode(pathInfo, getCharset().name());
			final File file = new File(logFileManager.getPath() + File.separator + decodedFileName);
			if (!file.exists() || file.isDirectory()) {
				notFound(exchange);
			}
			else {
				delete(exchange, file);
			}
		}
	}

	private void deleteAll(final HttpExchange exchange) throws IOException {
		logFileManager.deleteAllFiles();
		refresh(exchange);
	}

	private void delete(final HttpExchange exchange, final File file) throws IOException {
		logFileManager.deleteFile(file);
		refresh(exchange);
	}

	private void download(final HttpExchange exchange, final File file) throws IOException {
		final boolean headMethod = HttpMethod.HEAD.equalsIgnoreCase(exchange.getRequestMethod());
		FileInputStream input = null;
		OutputStream output = null;
		try {
			if (!headMethod) {
				input = new FileInputStream(file);
			}
			addDateHeader(exchange);
			exchange.getResponseHeaders().add("Content-Type", "text/x-log");
			exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
			if (canCompressResponse(exchange)) {
				addGzipHeader(exchange);
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0); // Transfer-Encoding: chunked
				if (!headMethod) {
					output = new GZIPOutputStream(exchange.getResponseBody(), BUFFER_SIZE);
				}
			}
			else {
				if (!headMethod) {
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, file.length());
					output = exchange.getResponseBody();
				}
				else {
					exchange.getResponseHeaders().set("Content-Length", Long.toString(file.length()));
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
				}
			}
			if (!headMethod) {
				IOUtils.copy(input, output, BUFFER_SIZE);
			}
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

		final StringBuilder html = new StringBuilder(buildHtmlHeader(escapeHtml(Messages.get("lbl.error"))));
		html.append("<h3>").append(escapeHtml(Messages.get("msg.server.not.found"))).append("</h3>").append(NewLine.CRLF);
		html.append(buildHtmlFooter());

		sendResponse(exchange, html.toString(), HttpURLConnection.HTTP_NOT_FOUND);
	}

	private void fileList(final HttpExchange exchange) throws IOException {
		final StringBuilder html = new StringBuilder(buildHtmlHeader(escapeHtml(Messages.get("lbl.server.logs"))));

		final File[] files = logFileManager.listFiles();
		final Collection<File> lockedFiles = logFileManager.getLockedFiles();

		html.append("<h3>").append(escapeHtml(files == null || files.length == 0 ? Messages.get("lbl.server.logs.title.empty") : Messages.get("lbl.server.logs.title", files.length))).append("</h3>").append(NewLine.CRLF);

		// Button bar
		html.append(buildHtmlHomeButton());
		html.append(buildHtmlRefreshButton());
		if (files != null && files.length > 0) {
			html.append(buildHtmlDeleteAllButton(lockedFiles.containsAll(Arrays.asList(files))));
		}

		// Table
		if (files != null && files.length > 0) {
			html.append(buildHtmlTable(files, lockedFiles));
		}

		// Footer
		html.append(buildHtmlFooter());

		sendResponse(exchange, html.toString());
	}

	private String buildHtmlTable(final File[] files, final Collection<File> lockedFiles) throws UnsupportedEncodingException {
		Arrays.sort(files);
		final StringBuilder html = new StringBuilder();
		html.append("<table><thead><tr>");
		html.append("<th>").append(escapeHtml(Messages.get("lbl.server.logs.list.name"))).append("</th>");
		html.append("<th>").append(escapeHtml(Messages.get("lbl.server.logs.list.date"))).append("</th>");
		html.append("<th>").append(escapeHtml(Messages.get("lbl.server.logs.list.size"))).append("</th>");
		html.append("<th>").append(escapeHtml(Messages.get("lbl.server.logs.list.action"))).append("</th>");
		html.append("</tr></thead><tbody>").append(NewLine.CRLF);
		final DateFormat dateFormatFileList = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Messages.getLanguage().getLocale());
		final NumberFormat numberFormatFileList = NumberFormat.getIntegerInstance(Messages.getLanguage().getLocale());
		for (final File file : files) {
			final String encodedFileName = URLEncoder.encode(file.getName(), getCharset().name());
			html.append("<tr>");
			html.append("<td>");
			html.append("<a href=\"").append(getPath(this.getClass())).append('/').append(encodedFileName).append("\">");
			html.append(escapeHtml(file.getName()));
			html.append("</a>");
			html.append("</td>");
			html.append("<td class=\"right\">").append(dateFormatFileList.format(new Date(file.lastModified()))).append("</td>");
			html.append("<td class=\"right\">").append(escapeHtml(Messages.get("lbl.server.logs.list.size.kb", numberFormatFileList.format(getKibLength(file))))).append("</td>");
			html.append("<td class=\"center\">");
			if (lockedFiles.contains(file)) {
				html.append("<form action=\"?\"><div>");
				html.append("<input type=\"submit\" value=\"").append(escapeHtml(Messages.get("lbl.server.logs.list.delete"))).append("\" disabled=\"disabled\" />");
			}
			else {
				html.append("<form action=\"").append(getPath(this.getClass())).append('/').append(encodedFileName).append("\" method=\"").append(HttpMethod.POST).append("\"><div>");
				html.append("<input type=\"hidden\" name=\"_method\" value=\"").append(HttpMethod.DELETE).append("\" /><input type=\"submit\" value=\"").append(escapeHtml(Messages.get("lbl.server.logs.list.delete"))).append("\" onclick=\"return confirm('").append(escapeEcmaScript(Messages.get("msg.server.logs.delete", file.getName()))).append("');\"").append(" />");
			}
			html.append("</div></form>");
			html.append("</td>");
			html.append("</tr>").append(NewLine.CRLF);
		}
		html.append("</tbody></table>").append(NewLine.CRLF);
		return html.toString();
	}

	private String buildHtmlDeleteAllButton(final boolean disabled) {
		final StringBuilder html = new StringBuilder();
		if (disabled) {
			html.append("<form action=\"?\"><div><input type=\"submit\" value=\"").append(escapeHtml(Messages.get("lbl.server.logs.delete.all"))).append("\" disabled=\"disabled\" /></div></form>");
		}
		else {
			html.append("<form action=\"").append(getPath(this.getClass())).append("\" method=\"").append(HttpMethod.POST).append("\"><div><input type=\"hidden\" name=\"_method\" value=\"").append(HttpMethod.DELETE).append("\" /><input type=\"submit\" value=\"").append(escapeHtml(Messages.get("lbl.server.logs.delete.all"))).append("\" onclick=\"return confirm('").append(escapeEcmaScript(Messages.get("msg.server.logs.delete.all"))).append("');\"").append(" /></div></form>");
		}
		return html.append(NewLine.CRLF).toString();
	}

	private void refresh(final HttpExchange exchange) throws IOException {
		addDateHeader(exchange);
		exchange.getResponseHeaders().add("Location", getPath(this.getClass()));
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP, -1);
		exchange.getResponseBody().close();
		exchange.close();
	}

	@Override
	protected String buildHtmlHeadStyle() {
		return "<style type=\"text/css\">form {display: inline;} div {display: inline;} table {margin-top: 1em; margin-bottom: 1em;} td.center {text-align: center;} td.right {text-align: right;}</style>";
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED);
	}

	public static long getKibLength(final File file) {
		return (file.length() + 1023) / 1024;
	}

}
