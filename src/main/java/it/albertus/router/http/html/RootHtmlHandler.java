package it.albertus.router.http.html;

import java.io.IOException;
import java.text.DateFormat;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.HttpMethod;
import it.albertus.httpserver.annotation.Path;
import it.albertus.httpserver.html.HtmlUtils;
import it.albertus.router.resources.Messages;
import it.albertus.util.NewLine;
import it.albertus.util.Version;

@Path("/")
public class RootHtmlHandler extends BaseHtmlHandler {

	public static class Defaults {
		public static final boolean ENABLED = true;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected static final String CFG_KEY_ENABLED = "server.handler.root.enabled";

	@Override
	protected void doGet(HttpExchange exchange) throws IOException {
		// Response...
		final Version version = Version.getInstance();
		final StringBuilder html = new StringBuilder(buildHtmlHeader(HtmlUtils.escapeHtml(Messages.get("lbl.server.home"))));
		html.append("<h3>").append('v').append(version.getNumber()).append(" (").append(DateFormat.getDateInstance(DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(version.getDate())).append(")</h3>").append(NewLine.CRLF);

		if (configuration.getBoolean(StatusHtmlHandler.CFG_KEY_ENABLED, StatusHtmlHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(getPath(StatusHtmlHandler.class)).append("\" method=\"").append(HttpMethod.GET).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.status"))).append("\" /></div></form>").append(NewLine.CRLF);
		}
		if (configuration.getBoolean(LogsHandler.CFG_KEY_ENABLED, LogsHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(getPath(LogsHandler.class)).append("\" method=\"").append(HttpMethod.GET).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.logs"))).append("\" /></div></form>").append(NewLine.CRLF);
		}
		if (configuration.getBoolean(ConfigurationHandler.CFG_KEY_ENABLED, ConfigurationHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(getPath(ConfigurationHandler.class)).append("\" method=\"").append(HttpMethod.GET).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.configuration"))).append("\" onclick=\"return confirm('").append(HtmlUtils.escapeEcmaScript(Messages.get("msg.server.configuration.confirm.open"))).append("');\" /></div></form>").append(NewLine.CRLF);
		}
		if (configuration.getBoolean(RestartHandler.CFG_KEY_ENABLED, RestartHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(getPath(RestartHandler.class)).append("\" method=\"").append(HttpMethod.POST).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.restart"))).append("\" onclick=\"return confirm('").append(HtmlUtils.escapeEcmaScript(Messages.get("msg.confirm.restart.message"))).append("');\" /></div></form>").append(NewLine.CRLF);
		}
		if (configuration.getBoolean(ConnectHandler.CFG_KEY_ENABLED, ConnectHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(getPath(ConnectHandler.class)).append("\" method=\"").append(HttpMethod.POST).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.connect"))).append("\" /></div></form>").append(NewLine.CRLF);
		}
		if (configuration.getBoolean(DisconnectHandler.CFG_KEY_ENABLED, DisconnectHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(getPath(DisconnectHandler.class)).append("\" method=\"").append(HttpMethod.POST).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.disconnect"))).append("\" onclick=\"return confirm('").append(HtmlUtils.escapeEcmaScript(Messages.get("msg.confirm.disconnect.message"))).append("');\" /></div></form>").append(NewLine.CRLF);
		}
		if (configuration.getBoolean(CloseHandler.CFG_KEY_ENABLED, CloseHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(getPath(CloseHandler.class)).append("\" method=\"").append(HttpMethod.POST).append("\"><div><input type=\"submit\" value=\"").append(HtmlUtils.escapeHtml(Messages.get("lbl.server.close"))).append("\" onclick=\"return confirm('").append(HtmlUtils.escapeEcmaScript(Messages.get("msg.confirm.close.message"))).append("');\" /></div></form>").append(NewLine.CRLF);
		}

		html.append(buildHtmlFooter());

		sendResponse(exchange, html.toString());
	}

	@Override
	protected String buildHtmlHeadStyle() {
		return "<style type=\"text/css\">form {display: inline;} div {display: inline;}</style>";
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_ENABLED, Defaults.ENABLED);
	}

}
