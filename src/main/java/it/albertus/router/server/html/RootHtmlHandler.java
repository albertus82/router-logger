package it.albertus.router.server.html;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DateFormat;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Messages;
import it.albertus.router.server.HttpMethod;
import it.albertus.util.NewLine;
import it.albertus.util.Version;

public class RootHtmlHandler extends BaseHtmlHandler {

	public static class Defaults {
		public static final boolean ENABLED = true;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public static final String PATH = "/";

	protected static final String[] METHODS = { HttpMethod.GET };

	protected static final String CFG_KEY_ENABLED = "server.handler.root.enabled";

	public RootHtmlHandler(RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(HttpExchange exchange) throws IOException {
		// Headers...
		addCommonHeaders(exchange);

		// Response...
		final Version version = Version.getInstance();
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Messages.get("lbl.server.home")));
		html.append("<h3>").append('v').append(version.getNumber()).append(" (").append(DateFormat.getDateInstance(DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(version.getDate())).append(")</h3>").append(NewLine.CRLF.toString());

		if (configuration.getBoolean(StatusHtmlHandler.CFG_KEY_ENABLED, StatusHtmlHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(StatusHtmlHandler.PATH).append("\" method=\"").append(StatusHtmlHandler.METHODS[0]).append("\"><div><input type=\"submit\" value=\"").append(Messages.get("lbl.server.status")).append("\" /></div></form>").append(NewLine.CRLF.toString());
		}
		if (configuration.getBoolean(LogsHandler.CFG_KEY_ENABLED, LogsHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(LogsHandler.PATH).append("\" method=\"").append(LogsHandler.METHODS[0]).append("\"><div><input type=\"submit\" value=\"").append(Messages.get("lbl.server.logs")).append("\" /></div></form>").append(NewLine.CRLF.toString());
		}
		if (configuration.getBoolean(ConfigurationHandler.CFG_KEY_ENABLED, ConfigurationHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(ConfigurationHandler.PATH).append("\" method=\"").append(ConfigurationHandler.METHODS[0]).append("\"><div><input type=\"submit\" value=\"").append(Messages.get("lbl.server.configuration")).append("\" onclick=\"return confirm('").append(Messages.get("msg.server.configuration.confirm.open")).append("');\" /></div></form>").append(NewLine.CRLF.toString());
		}
		if (configuration.getBoolean(RestartHandler.CFG_KEY_ENABLED, RestartHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(RestartHandler.PATH).append("\" method=\"").append(RestartHandler.METHODS[0]).append("\"><div><input type=\"submit\" value=\"").append(Messages.get("lbl.server.restart")).append("\" onclick=\"return confirm('").append(Messages.get("msg.confirm.restart.message")).append("');\" /></div></form>").append(NewLine.CRLF.toString());
		}
		if (configuration.getBoolean(ConnectHandler.CFG_KEY_ENABLED, ConnectHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(ConnectHandler.PATH).append("\" method=\"").append(ConnectHandler.METHODS[0]).append("\"><div><input type=\"submit\" value=\"").append(Messages.get("lbl.server.connect")).append("\" /></div></form>").append(NewLine.CRLF.toString());
		}
		if (configuration.getBoolean(DisconnectHandler.CFG_KEY_ENABLED, DisconnectHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(DisconnectHandler.PATH).append("\" method=\"").append(DisconnectHandler.METHODS[0]).append("\"><div><input type=\"submit\" value=\"").append(Messages.get("lbl.server.disconnect")).append("\" onclick=\"return confirm('").append(Messages.get("msg.confirm.disconnect.message")).append("');\" /></div></form>").append(NewLine.CRLF.toString());
		}
		if (configuration.getBoolean(CloseHandler.CFG_KEY_ENABLED, CloseHandler.Defaults.ENABLED)) {
			html.append("<form action=\"").append(CloseHandler.PATH).append("\" method=\"").append(CloseHandler.METHODS[0]).append("\"><div><input type=\"submit\" value=\"").append(Messages.get("lbl.server.close")).append("\" onclick=\"return confirm('").append(Messages.get("msg.confirm.close.message")).append("');\" /></div></form>").append(NewLine.CRLF.toString());
		}

		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
		exchange.getResponseBody().write(response);
	}

	@Override
	protected String buildHtmlHeadStyle() {
		return "<style type=\"text/css\">form {display: inline;} div {display: inline;}</style>";
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
