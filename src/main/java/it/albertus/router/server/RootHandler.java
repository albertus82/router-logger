package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;
import it.albertus.util.Version;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import com.sun.net.httpserver.HttpExchange;

public class RootHandler extends BaseHttpHandler {

	public static final String PATH = "/";
	public static final String[] METHODS = { "GET" };

	protected RootHandler(RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(HttpExchange exchange) throws IOException {
		// Charset...
		final Charset charset = getCharset();
		exchange.getResponseHeaders().add("Content-Type", "text/html; charset=" + charset.name());

		// Response...
		byte[] response;
		final Version version = Version.getInstance();
		StringBuilder html = new StringBuilder();
		html.append(buildHtmlHeader());

		html.append("<h4>").append(Resources.get("msg.application.name")).append(' ').append(Resources.get("msg.version", version.getNumber(), version.getDate())).append(" - <a href=\"").append(Resources.get("msg.website")).append("\">").append(Resources.get("msg.website")).append("</a></h4>").append(NewLine.CRLF);
		html.append("<form style=\"display: inline;\" action=\"").append(StatusHandler.PATH).append("\" method=\"GET\"><input type=\"submit\" value=\"").append(Resources.get("lbl.server.button.status")).append("\" /></form>").append(NewLine.CRLF);
		html.append("<form style=\"display: inline;\" action=\"").append(RestartHandler.PATH).append("\" method=\"POST\"><input type=\"button\" value=\"").append(Resources.get("lbl.server.button.restart")).append("\" onclick=\"if (confirm('").append(Resources.get("msg.confirm.restart.message")).append("')) document.forms[1].submit();\" /></form>").append(NewLine.CRLF);
		html.append("<form style=\"display: inline;\" action=\"").append(ConnectHandler.PATH).append("\" method=\"POST\"><input type=\"submit\" value=\"").append(Resources.get("lbl.server.button.connect")).append("\" /></form>").append(NewLine.CRLF);
		html.append("<form style=\"display: inline;\" action=\"").append(DisconnectHandler.PATH).append("\" method=\"POST\"><input type=\"button\" value=\"").append(Resources.get("lbl.server.button.disconnect")).append("\" onclick=\"if (confirm('").append(Resources.get("msg.confirm.disconnect.message")).append("')) document.forms[3].submit();\" /></form>").append(NewLine.CRLF);

		html.append(buildHtmlFooter());
		response = html.toString().getBytes(charset);
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

}
