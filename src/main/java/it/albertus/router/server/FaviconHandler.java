package it.albertus.router.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class FaviconHandler extends StaticResourceHandler {

	private static final String RESOURCE_NAME = "favicon.ico";
	private static final byte[] favicon = loadFavicon(); // Cached

	private static final byte[] loadFavicon() {
		byte[] bytes = null;
		final InputStream inputStream = FaviconHandler.class.getResourceAsStream(RESOURCE_NAME);
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		final byte[] buffer = new byte[BUFFER_SIZE];
		int len;
		try {
			while ((len = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, len);
			}
			bytes = outputStream.toByteArray();
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				outputStream.close();
			}
			catch (final Exception e) {/* Ignore */}
			try {
				inputStream.close();
			}
			catch (final Exception e) {/* Ignore */}
		}
		return bytes;
	}

	private static final Headers createHeaders() {
		final Headers faviconHeaders = new Headers();
		faviconHeaders.add("Content-Type", "image/x-icon");
		faviconHeaders.add("Cache-Control", "no-transform,public,max-age=86400,s-maxage=259200");
		return faviconHeaders;
	}

	public FaviconHandler() {
		super("/favicon.ico", RESOURCE_NAME, createHeaders());
		setFound(favicon != null);
	}

	@Override
	protected void service(final HttpExchange exchange) throws IOException {
		addHeaders(exchange);
		final byte[] response = compressResponse(favicon, exchange);
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
		exchange.getResponseBody().write(response);
		exchange.getResponseBody().flush();
		exchange.getResponseBody().close();
	}

}
