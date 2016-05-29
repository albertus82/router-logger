package it.albertus.router.server;

import it.albertus.router.gui.Images;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

public class FaviconHandler extends BaseHttpHandler {

	public static final String PATH = "/favicon.ico";
	public static final String[] METHODS = { "GET" };

	private static final int BUFFER_SIZE = 4096;

	@Override
	public void service(final HttpExchange exchange) throws IOException {
		addDateHeader(exchange);
		exchange.getResponseHeaders().add("Content-Type", "image/x-icon");
		exchange.getResponseHeaders().add("Cache-Control", "no-transform, public, max-age=86400, s-maxage=259200");

		final InputStream inputStream = Images.class.getResourceAsStream("main.ico");
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		final byte[] buffer = new byte[BUFFER_SIZE];
		int len;
		while ((len = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, len);
		}
		outputStream.close();
		inputStream.close();

		final byte[] response = compressResponse(outputStream.toByteArray(), exchange);
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
