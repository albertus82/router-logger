package it.albertus.router.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map.Entry;

import javax.activation.MimetypesFileTypeMap;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.server.html.BaseHtmlHandler;

public class StaticResourceHandler extends BaseHtmlHandler {

	public static final String[] METHODS = { HttpMethod.GET };

	protected static final int BUFFER_SIZE = 8192;
	protected static final String DEFAULT_CACHE_CONTROL = "no-transform,public,max-age=300,s-maxage=900";

	private final String path;
	private final String resourceName;
	private final Headers headers;

	public StaticResourceHandler(final String path, final String resourceName, final Headers headers) {
		this.path = path;
		this.resourceName = resourceName;
		this.headers = headers;
	}

	public StaticResourceHandler(final String path, final String resourceName) {
		this(path, resourceName, null);
	}

	@Override
	protected void service(final HttpExchange exchange) throws IOException {
		addHeaders(exchange);

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

	public String getResourceName() {
		return resourceName;
	}

	public Headers getHeaders() {
		return headers;
	}

	protected void addHeaders(final HttpExchange exchange) {
		addDateHeader(exchange);
		final Headers responseHeaders = exchange.getResponseHeaders();
		if (this.headers != null) {
			for (final Entry<String, List<String>> entry : this.headers.entrySet()) {
				responseHeaders.put(entry.getKey(), entry.getValue());
			}
		}
		if (!responseHeaders.containsKey("Content-Type")) {
			responseHeaders.add("Content-Type", new MimetypesFileTypeMap().getContentType(resourceName));
		}
		if (!responseHeaders.containsKey("Cache-Control")) {
			responseHeaders.add("Cache-Control", DEFAULT_CACHE_CONTROL);
		}
	}

}
