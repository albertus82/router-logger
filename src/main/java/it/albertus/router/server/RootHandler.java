package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;
import it.albertus.util.Version;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

public class RootHandler extends BaseHttpHandler {

	public interface Defaults {
		boolean ENABLED = true;
	}

	public static final String PATH = "/";
	public static final String[] METHODS = { "GET" };

	protected RootHandler(RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(HttpExchange exchange) throws IOException {
		// Headers...
		addCommonHeaders(exchange);

		// Response...
		final Version version = Version.getInstance();
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Resources.get("lbl.server.home")));
		html.append("<h3>").append('v').append(version.getNumber()).append(" (").append(version.getDate()).append(") - <a href=\"").append(Resources.get("msg.website")).append("\">").append(Resources.get("msg.website")).append("</a></h3>").append(NewLine.CRLF.toString());
		html.append("<form style=\"display: inline;\" action=\"").append(StatusHandler.PATH).append("\" method=\"").append(StatusHandler.METHODS[0]).append("\"><input type=\"submit\" value=\"").append(Resources.get("lbl.server.status")).append("\" /></form>").append(NewLine.CRLF.toString());
		html.append("<form style=\"display: inline;\" action=\"").append(RestartHandler.PATH).append("\" method=\"").append(RestartHandler.METHODS[0]).append("\"><input type=\"button\" value=\"").append(Resources.get("lbl.server.restart")).append("\" onclick=\"if (confirm('").append(Resources.get("msg.confirm.restart.message")).append("')) document.forms[1].submit();\" /></form>").append(NewLine.CRLF.toString());
		html.append("<form style=\"display: inline;\" action=\"").append(ConnectHandler.PATH).append("\" method=\"").append(ConnectHandler.METHODS[0]).append("\"><input type=\"submit\" value=\"").append(Resources.get("lbl.server.connect")).append("\" /></form>").append(NewLine.CRLF.toString());
		html.append("<form style=\"display: inline;\" action=\"").append(DisconnectHandler.PATH).append("\" method=\"").append(DisconnectHandler.METHODS[0]).append("\"><input type=\"button\" value=\"").append(Resources.get("lbl.server.disconnect")).append("\" onclick=\"if (confirm('").append(Resources.get("msg.confirm.disconnect.message")).append("')) document.forms[3].submit();\" /></form>").append(NewLine.CRLF.toString());
		html.append(buildHtmlFooter());

		final byte[] response = html.toString().getBytes(getCharset());
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
		exchange.getResponseBody().write(response);
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
		return configuration.getBoolean("server.handler.root.enabled", Defaults.ENABLED);
	}

}
