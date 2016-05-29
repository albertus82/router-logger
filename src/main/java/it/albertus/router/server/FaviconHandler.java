package it.albertus.router.server;

import it.albertus.router.gui.Images;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

		final String iconPath = '/' + Images.class.getPackage().getName().replace('.', '/') + "/main.ico";
		final BufferedInputStream bis = new BufferedInputStream(this.getClass().getResourceAsStream(iconPath));
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		final byte[] buffer = new byte[BUFFER_SIZE];
		int len;
		while ((len = bis.read(buffer)) != -1) {
			baos.write(buffer, 0, len);
		}
		baos.close();
		bis.close();

		final byte[] response = compressResponse(baos.toByteArray(), exchange);
		System.out.println(response.length);
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
