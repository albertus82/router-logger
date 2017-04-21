package it.albertus.router.http.html;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DateFormat;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.AbstractHttpHandler;
import it.albertus.httpserver.HttpException;
import it.albertus.httpserver.HttpMethod;
import it.albertus.httpserver.html.HtmlUtils;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;
import it.albertus.util.StringUtils;
import it.albertus.util.Version;

public abstract class AbstractHtmlHandler extends AbstractHttpHandler {

	public static class Defaults {
		public static final boolean COMPRESS_RESPONSE = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private static final String MSG_KEY_LBL_ERROR = "lbl.error";

	protected final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	@Override
	protected void sendForbidden(final HttpExchange exchange) throws IOException {
		addCommonHeaders(exchange);

		final StringBuilder html = new StringBuilder(buildHtmlHeader(HtmlUtils.escapeHtml(Messages.get(MSG_KEY_LBL_ERROR))));
		html.append("<h3>").append(HtmlUtils.escapeHtml(Messages.get("msg.server.forbidden"))).append("</h3>").append(NewLine.CRLF);
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, response.length);
		exchange.getResponseBody().write(response);
	}

	@Override
	protected void sendNotFound(final HttpExchange exchange) throws IOException {
		addCommonHeaders(exchange);

		final StringBuilder html = new StringBuilder(buildHtmlHeader(HtmlUtils.escapeHtml(Messages.get(MSG_KEY_LBL_ERROR))));
		html.append("<h3>").append(HtmlUtils.escapeHtml(Messages.get("msg.server.not.found"))).append("</h3>").append(NewLine.CRLF);
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length);
		exchange.getResponseBody().write(response);
	}

	@Override
	protected void sendInternalError(final HttpExchange exchange) throws IOException {
		addCommonHeaders(exchange);

		final StringBuilder html = new StringBuilder(buildHtmlHeader(HtmlUtils.escapeHtml(Messages.get(MSG_KEY_LBL_ERROR))));
		html.append("<h3>").append(HtmlUtils.escapeHtml(Messages.get("err.server.handler"))).append("</h3>").append(NewLine.CRLF);
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, response.length);
		exchange.getResponseBody().write(response);
	}

	@Override
	protected void sendError(final HttpExchange exchange, final HttpException e) throws IOException {
		addCommonHeaders(exchange);

		final StringBuilder html = new StringBuilder(buildHtmlHeader(HtmlUtils.escapeHtml(Messages.get(MSG_KEY_LBL_ERROR))));
		html.append("<h3>").append(StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : getHttpStatusCodes().get(e.getStatusCode())).append("</h3>").append(NewLine.CRLF);
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(e.getStatusCode(), response.length);
		exchange.getResponseBody().write(response);
	}

	protected void sendResponse(final HttpExchange exchange, final String html) throws IOException {
		sendResponse(exchange, html.getBytes(getCharset()));
	}

	protected void sendResponse(final HttpExchange exchange, final String html, final int statusCode) throws IOException {
		sendResponse(exchange, html.getBytes(getCharset()), statusCode);
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
	protected final String buildHtmlHeader(final String title) {
		final StringBuilder html = new StringBuilder("<!DOCTYPE html>").append(NewLine.CRLF);
		html.append("<html lang=\"").append(Messages.getLanguage().getLocale().getLanguage()).append("\">").append(NewLine.CRLF);
		html.append(buildHtmlHead(title));
		html.append("<body>").append(NewLine.CRLF);
		html.append(buildHtmlNavigationBar());
		html.append("<div class=\"container\" role=\"main\">").append(NewLine.CRLF);
		return html.toString();
	}

	private String buildHtmlNavigationBar() {
		final StringBuilder html = new StringBuilder();
		html.append("<div class=\"navbar navbar-default navbar-static-top\">").append(NewLine.CRLF);
		html.append("<div class=\"container\">").append(NewLine.CRLF);
		html.append("<div class=\"navbar-header\">").append(NewLine.CRLF);
		html.append("<button type=\"button\" class=\"navbar-toggle collapsed\" data-toggle=\"collapse\" data-target=\"#navbar\" aria-expanded=\"false\" aria-controls=\"navbar\">").append(NewLine.CRLF);
		html.append("<span class=\"sr-only\">").append(Messages.get("lbl.server.navigation.toggle")).append("</span>").append(NewLine.CRLF);
		for (byte i = 0; i < 3; i++) { // hamburger button
			html.append("<span class=\"icon-bar\"></span>").append(NewLine.CRLF);
		}
		html.append("</button>").append(NewLine.CRLF);
		html.append("<a class=\"navbar-brand active\" href=\"").append(getAnnotatedPath(RootHtmlHandler.class)).append("\">").append(HtmlUtils.escapeHtml(Messages.get("msg.application.name"))).append("</a>").append(NewLine.CRLF);
		html.append("</div><div id=\"navbar\" class=\"navbar-collapse collapse\"><ul class=\"nav navbar-nav\">");
		if (configuration.getBoolean(StatusHtmlHandler.CFG_KEY_ENABLED, StatusHtmlHandler.Defaults.ENABLED)) {
			html.append("<li><a href=\"").append(getAnnotatedPath(StatusHtmlHandler.class)).append("\">").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.status"))).append("</a></li>").append(NewLine.CRLF);
		}
		if (configuration.getBoolean(LogsHandler.CFG_KEY_ENABLED, LogsHandler.Defaults.ENABLED)) {
			html.append("<li><a href=\"").append(getAnnotatedPath(LogsHandler.class)).append("\">").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.logs"))).append("</a></li>").append(NewLine.CRLF);
		}
		if (configuration.getBoolean(ConfigurationHandler.CFG_KEY_ENABLED, ConfigurationHandler.Defaults.ENABLED)) {
			html.append("<li><a href=\"").append(getAnnotatedPath(ConfigurationHandler.class)).append("\" onclick=\"return confirm('").append(HtmlUtils.escapeEcmaScript(Messages.get("msg.server.configuration.confirm.open"))).append("');\">").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.configuration"))).append("</a></li>").append(NewLine.CRLF);
		}
		final boolean restartEnabled = configuration.getBoolean(RestartHandler.CFG_KEY_ENABLED, RestartHandler.Defaults.ENABLED);
		final boolean connectEnabled = configuration.getBoolean(ConnectHandler.CFG_KEY_ENABLED, ConnectHandler.Defaults.ENABLED);
		final boolean disconnectEnabled = configuration.getBoolean(DisconnectHandler.CFG_KEY_ENABLED, DisconnectHandler.Defaults.ENABLED);
		final boolean closeEnabled = configuration.getBoolean(CloseHandler.CFG_KEY_ENABLED, CloseHandler.Defaults.ENABLED);
		if (restartEnabled || connectEnabled || disconnectEnabled || closeEnabled) {
			html.append("<li class=\"dropdown\">");
			html.append("<a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\" role=\"button\" aria-haspopup=\"true\" aria-expanded=\"false\">").append(Messages.get("lbl.server.commands")).append(" <span class=\"caret\"></span></a>").append(NewLine.CRLF);
			html.append("<ul class=\"dropdown-menu\">").append(NewLine.CRLF);
			if (connectEnabled) {
				html.append("<li><form action=\"").append(getAnnotatedPath(ConnectHandler.class)).append("\" method=\"").append(HttpMethod.POST).append("\"><div><input class=\"btn-md btn-link dropdown-menu-item\" type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.connect"))).append("\" /></div></form></li>").append(NewLine.CRLF);
			}
			if (disconnectEnabled) {
				html.append("<li><form action=\"").append(getAnnotatedPath(DisconnectHandler.class)).append("\" method=\"").append(HttpMethod.POST).append("\"><div><input class=\"btn-md btn-link dropdown-menu-item\" type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.disconnect"))).append("\" onclick=\"return confirm('").append(HtmlUtils.escapeEcmaScript(Messages.get("msg.confirm.disconnect.message"))).append("');\" /></div></form></li>").append(NewLine.CRLF);
			}
			if (restartEnabled) {
				html.append("<li><form action=\"").append(getAnnotatedPath(RestartHandler.class)).append("\" method=\"").append(HttpMethod.POST).append("\"><div><input class=\"btn-md btn-link dropdown-menu-item\" type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.restart"))).append("\" onclick=\"return confirm('").append(HtmlUtils.escapeEcmaScript(Messages.get("msg.confirm.restart.message"))).append("');\" /></div></form></li>").append(NewLine.CRLF);
			}
			if (closeEnabled) {
				html.append("<li><form action=\"").append(getAnnotatedPath(CloseHandler.class)).append("\" method=\"").append(HttpMethod.POST).append("\"><div><input class=\"btn-md btn-link dropdown-menu-item\" type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.close"))).append("\" onclick=\"return confirm('").append(HtmlUtils.escapeEcmaScript(Messages.get("msg.confirm.close.message"))).append("');\" /></div></form></li>").append(NewLine.CRLF);
			}
			html.append("</ul>").append(NewLine.CRLF).append("</li>").append(NewLine.CRLF);
		}
		html.append("</ul>").append(NewLine.CRLF).append("</div>").append(NewLine.CRLF).append("</div>").append(NewLine.CRLF).append("</div>").append(NewLine.CRLF);
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
	private String buildHtmlHead(final String title) {
		final StringBuilder html = new StringBuilder("<head>").append(NewLine.CRLF);
		html.append(buildHtmlHeadMeta());
		html.append(buildHtmlHeadLink());
		html.append(buildHtmlHeadScript());
		html.append(buildHtmlHeadTitle(title));
		html.append("</head>").append(NewLine.CRLF);
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
	private String buildHtmlHeadTitle(final String title) {
		final StringBuilder html = new StringBuilder("<title>");
		if (title != null && !title.trim().isEmpty()) {
			html.append(title.trim()).append(" - ");
		}
		return html.append(HtmlUtils.escapeHtml(Messages.get("msg.application.name"))).append("</title>").append(NewLine.CRLF).toString();
	}

	/**
	 * Override this method to create {@code <style>} element. The default
	 * implementation returns an empty string.
	 * 
	 * @return the string containing the HTML code.
	 */
	private String buildHtmlHeadLink() {
		final StringBuilder html = new StringBuilder();
		html.append("<link rel=\"stylesheet\" href=\"/css/bootstrap.min.css\" />").append(NewLine.CRLF);
		html.append("<link rel=\"stylesheet\" href=\"/css/bootstrap-theme.min.css\" />").append(NewLine.CRLF);
		html.append("<link rel=\"stylesheet\" href=\"/css/routerlogger.css\" />").append(NewLine.CRLF);
		html.append("<link rel=\"stylesheet\" href=\"/fonts/fonts.css\" />").append(NewLine.CRLF);
		return html.toString();
	}

	private String buildHtmlHeadScript() {
		final StringBuilder html = new StringBuilder();
		html.append("<script type=\"text/javascript\" src=\"/js/jquery.min.js\"></script>").append(NewLine.CRLF);
		html.append("<script type=\"text/javascript\" src=\"/js/bootstrap.min.js\"></script>").append(NewLine.CRLF);
		return html.toString();
	}

	private String buildHtmlHeadMeta() {
		final StringBuilder html = new StringBuilder();
		html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />").append(NewLine.CRLF); // responsive
		html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=").append(getCharset().name().toLowerCase()).append("\" />").append(NewLine.CRLF); // XHTML
		html.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />").append(NewLine.CRLF);
		return html.toString();
	}

	/**
	 * Closes {@code <body>} and {@code <html>} tags.
	 * 
	 * @return the string containing the HTML code.
	 */
	protected final String buildHtmlFooter() {
		final StringBuilder html = new StringBuilder("</div>").append(NewLine.CRLF);
		html.append("<div class=\"footer\"><div class=\"container\"><p class=\"text-muted\">");
		html.append("<a href=\"").append(HtmlUtils.escapeHtml(Messages.get("msg.website"))).append("\">").append(HtmlUtils.escapeHtml(Messages.get("msg.application.name"))).append("</a> ");
		final Version version = Version.getInstance();
		html.append(Messages.get("msg.version", version.getNumber(), DateFormat.getDateInstance(DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(version.getDate())));
		html.append("</p></div></div>").append(NewLine.CRLF);
		html.append("</body>").append(NewLine.CRLF);
		html.append("</html>");
		return html.toString();
	}

	@Deprecated
	protected String buildHtmlHomeButton() {
		if (configuration.getBoolean(RootHtmlHandler.CFG_KEY_ENABLED, RootHtmlHandler.Defaults.ENABLED)) {
			return new StringBuilder("<form action=\"").append(getAnnotatedPath(RootHtmlHandler.class)).append("\" method=\"").append(HttpMethod.GET).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.home"))).append("\" /></div></form>").append(NewLine.CRLF).toString();
		}
		else {
			return "";
		}
	}

	protected String buildHtmlRefreshButton() {
		return new StringBuilder("<a href=\"").append(getPath()).append("\" class=\"btn btn-default btn-sm pull-right\"><span class=\"glyphicon glyphicon-refresh\"></span> ").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.refresh"))).append("</a>").append(NewLine.CRLF).toString();
	}

	/**
	 * Adds {@code Content-Type: text/html} header to the provided
	 * {@link HttpExchange} object.
	 * 
	 * @param exchange the {@link HttpExchange} to be modified.
	 */
	@Override
	protected void addContentTypeHeader(final HttpExchange exchange) {
		exchange.getResponseHeaders().add("Content-Type", "text/html; charset=" + getCharset().name().toLowerCase());
	}

	@Override
	protected boolean canCompressResponse(final HttpExchange exchange) {
		return configuration.getBoolean("server.compress.response.html", Defaults.COMPRESS_RESPONSE) && super.canCompressResponse(exchange);
	}

}
