package it.albertus.router.server.json;

import it.albertus.router.dto.StatusDto;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.util.Payload;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

public class StatusJsonHandler extends BaseJsonHandler {

	public static final String PATH = "/json/status";

	public StatusJsonHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	public void service(final HttpExchange exchange) throws IOException {
		final int refresh = Math.max(1, Long.valueOf(engine.getWaitTimeInMillis() / 1000).intValue() - 1);
		exchange.getResponseHeaders().add("Refresh", Integer.toString(refresh));

		final byte[] payload = Payload.createPayload(new StatusDto(engine.getCurrentStatus()).toJson());

		final String currentEtag = generateEtag(payload);

		// If-None-Match...
		final String ifNoneMatch = exchange.getRequestHeaders().getFirst("If-None-Match");
		if (ifNoneMatch != null && currentEtag != null && currentEtag.equals(ifNoneMatch)) {
			addDateHeader(exchange);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, -1);
			exchange.getResponseBody().close(); // Needed when no write occurs.
		}
		else {
			addCommonHeaders(exchange);
			if (currentEtag != null) {
				exchange.getResponseHeaders().add("ETag", currentEtag);
			}
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, payload.length);
			exchange.getResponseBody().write(payload);
		}
	}

	@Override
	public String getPath() {
		return PATH;
	}

}
