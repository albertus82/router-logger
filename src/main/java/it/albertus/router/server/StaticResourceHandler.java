package it.albertus.router.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.MimetypesFileTypeMap;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class StaticResourceHandler extends BaseHttpHandler {

	public static final String[] METHODS = { "GET" };

	private static final int BUFFER_SIZE = 8192;
	private static final String DEFAULT_CACHE_CONTROL = "no-transform, public, max-age=300, s-maxage=900";
	private static final MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();

	private final String path;
	private final String resourceName;
	private final Map<String, String> headers = new HashMap<String, String>();

	public StaticResourceHandler(final String path, final String resourceName, final Map<String, String> headers) {
		this.path = path;
		this.resourceName = resourceName;
		this.headers.put("Content-Type", mimetypesFileTypeMap.getContentType(resourceName));
		this.headers.put("Cache-Control", DEFAULT_CACHE_CONTROL);
		if (headers != null) {
			this.headers.putAll(headers);
		}
	}

	public StaticResourceHandler(final String path, final String resourceName) {
		this(path, resourceName, null);
	}

	@Override
	protected void service(final HttpExchange exchange) throws IOException {
		addDateHeader(exchange);
		final Headers responseHeaders = exchange.getResponseHeaders();
		for (final Entry<String, String> header : headers.entrySet()) {
			responseHeaders.add(header.getKey(), header.getValue());
		}

		final InputStream inputStream = getClass().getResourceAsStream(resourceName);
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
		return path;
	}

	@Override
	public String[] getMethods() {
		return METHODS;
	}

}
