package it.albertus.router.server.json;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.dto.RouterDataDto;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.util.Payload;

public class DataJsonHandler extends BaseJsonHandler {

	public static final String PATH = "/json/data";

	public DataJsonHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(final HttpExchange exchange) throws IOException {
		final byte[] payload = Payload.createPayload(new RouterDataDto(engine.getCurrentData()).toJson());

		addRefreshHeader(exchange);

		final String currentEtag = generateEtag(payload);
		if (currentEtag != null) {
			exchange.getResponseHeaders().add("ETag", currentEtag);
		}

		// If-None-Match...
		final String ifNoneMatch = exchange.getRequestHeaders().getFirst("If-None-Match");
		if (ifNoneMatch != null && currentEtag != null && currentEtag.equals(ifNoneMatch)) {
			addDateHeader(exchange);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, -1);
			exchange.getResponseBody().close(); // Needed when no write occurs.
		}
		else {
			addCommonHeaders(exchange);
			final byte[] response = compressResponse(payload, exchange);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			exchange.getResponseBody().write(response);
		}
	}

	@Override
	public String getPath() {
		return PATH;
	}

}
