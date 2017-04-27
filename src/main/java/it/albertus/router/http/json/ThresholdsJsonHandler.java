package it.albertus.router.http.json;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.annotation.Path;
import it.albertus.router.dto.ThresholdsDto;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.util.Payload;

@Path("/json/thresholds")
public class ThresholdsJsonHandler extends AbstractJsonHandler {

	public ThresholdsJsonHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException {
		final byte[] payload = Payload.createPayload(new ThresholdsDto(engine.getCurrentThresholdsReached()).toJson());
		addRefreshHeader(exchange);
		sendResponse(exchange, payload);
	}

}
