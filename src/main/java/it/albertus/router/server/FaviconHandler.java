package it.albertus.router.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.util.Logger;
import it.albertus.router.util.LoggerFactory;
import it.albertus.util.IOUtils;

public class FaviconHandler extends StaticResourceHandler {

	private static final Logger logger = LoggerFactory.getLogger(FaviconHandler.class);

	private static final String RESOURCE_NAME = "favicon.ico";
	private static final byte[] favicon = loadFavicon(); // Cached

	public FaviconHandler() {
		super("/favicon.ico", RESOURCE_NAME, createHeaders());
		setFound(favicon != null);
	}

	private static final byte[] loadFavicon() {
		byte[] bytes = null;

		InputStream inputStream = null;
		ByteArrayOutputStream outputStream = null;
		try {
			inputStream = FaviconHandler.class.getResourceAsStream(RESOURCE_NAME);
			outputStream = new ByteArrayOutputStream();
			IOUtils.copy(inputStream, outputStream, BUFFER_SIZE);
		}
		catch (final IOException ioe) {
			logger.error(ioe);
		}
		finally {
			IOUtils.closeQuietly(outputStream, inputStream);
		}
		return bytes;
	}

	private static final Headers createHeaders() {
		final Headers faviconHeaders = new Headers();
		faviconHeaders.add("Content-Type", "image/x-icon");
		faviconHeaders.add("Cache-Control", "no-transform,public,max-age=86400,s-maxage=259200");
		return faviconHeaders;
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
