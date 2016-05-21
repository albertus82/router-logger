package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import com.sun.net.httpserver.HttpExchange;

public class ConnectHandler extends BaseHttpHandler {

	public interface Defaults {
		boolean REFRESH = false;
		int REFRESH_SECS = 0;
	}

	public static final String PATH = "/connect";

	public ConnectHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void handle(final HttpExchange exchange) throws IOException {
		// Charset...
		final Charset charset = getCharset();
		exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=" + charset.name());

		// Response...
		byte[] response;
		try {
			if (engine.canConnect()) {
				engine.connect();
				response = Resources.get("msg.server.accepted").getBytes(charset);
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
			}
			else {
				response = Resources.get("msg.server.not.acceptable").getBytes(charset);
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_ACCEPTABLE, response.length);
			}
		}
		catch (final Exception exception) {
			Logger.getInstance().log(exception);
			response = Resources.get("err.server.disconnect").getBytes(charset);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, response.length);
		}
		exchange.getResponseBody().write(response);
		exchange.close();
	}

	@Override
	public String getPath() {
		return PATH;
	}

}
