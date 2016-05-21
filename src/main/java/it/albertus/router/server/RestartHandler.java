package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.util.Configuration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.sun.net.httpserver.HttpExchange;

public class RestartHandler extends BaseHttpHandler {

	public interface Defaults {
		boolean REFRESH = false;
		int REFRESH_SECS = 0;
	}

	public static final String PATH = "/restart";

	protected static final String KEY_VALUE_SEPARATOR = ": ";
	protected static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	protected final Configuration configuration = RouterLoggerConfiguration.getInstance();

	public RestartHandler(final RouterLoggerEngine engine) {
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
			engine.restart();
			response = "OK".getBytes(charset);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
		}
		catch (final Exception exception) {
			Logger.getInstance().log(exception);
			response = Resources.get("err.server.restart").getBytes(charset);
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
