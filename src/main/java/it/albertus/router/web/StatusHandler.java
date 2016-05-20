package it.albertus.router.web;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.util.NewLine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import com.sun.net.httpserver.HttpExchange;

public class StatusHandler extends BaseHttpHandler {

	public static final String PATH = "/status";

	public StatusHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void handle(final HttpExchange exchange) throws IOException {
		// Charset...
		Charset charset = getCharset();
		exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=" + charset.name());

		// Response...
		byte[] response;
		try {
			final StringBuilder status = new StringBuilder();
			status.append("Stato: ").append(engine.getCurrentStatus().toString());
			if (engine.getCurrentData() != null) {
				status.append(NewLine.CRLF).append(NewLine.CRLF).append(engine.getCurrentData().toString());
			}
			response = status.toString().getBytes(charset);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
		}
		catch (Exception e) {
			response = "Dati non disponibili.".getBytes(charset);
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
