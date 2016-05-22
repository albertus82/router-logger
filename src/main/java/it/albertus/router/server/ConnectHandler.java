package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import com.sun.net.httpserver.HttpExchange;

public class ConnectHandler extends BaseHttpHandler {

	public static final String PATH = "/connect";
	public static final String[] METHODS = { "POST" };

	protected ConnectHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(final HttpExchange exchange) throws IOException {
		// Charset...
		final Charset charset = getCharset();
		exchange.getResponseHeaders().add("Content-Type", "text/html; charset=" + charset.name());
		exchange.getResponseHeaders().add("Date", httpDateGenerator.getCurrentDate());

		// Response...
		final StringBuilder html = new StringBuilder(buildHtmlHeader(Resources.get("lbl.server.connect")));
		final boolean accepted = engine.canConnect();
		if (accepted) {
			engine.connect();
		}
		html.append("<h3>").append(accepted ? Resources.get("msg.server.accepted") : Resources.get("msg.server.not.acceptable")).append("</h3>").append(NewLine.CRLF.toString());
		html.append("<form action=\"").append(RootHandler.PATH).append("\" method=\"" + RootHandler.METHODS[0] + "\"><input type=\"submit\" value=\"").append(Resources.get("lbl.server.home")).append("\" /></form>").append(NewLine.CRLF.toString());
		html.append(buildHtmlFooter());
		final byte[] response = html.toString().getBytes(charset);
		exchange.sendResponseHeaders(accepted ? HttpURLConnection.HTTP_ACCEPTED : HttpURLConnection.HTTP_NOT_ACCEPTABLE, response.length);
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
