package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import com.sun.net.httpserver.HttpExchange;

public class RestartHandler extends BaseHttpHandler {

	public static final String PATH = "/restart";

	public RestartHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(final HttpExchange exchange) throws IOException {
		// Charset...
		final Charset charset = getCharset();
		exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=" + charset.name());

		// Response...
		byte[] response;
		engine.restart();
		response = Resources.get("msg.server.accepted").getBytes(charset);
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, response.length);
		exchange.getResponseBody().write(response);
	}

	@Override
	public String getPath() {
		return PATH;
	}

}
