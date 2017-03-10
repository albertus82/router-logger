package it.albertus.router.server.json;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.dto.ThresholdsDto;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.util.Payload;

public class ThresholdsJsonHandler extends BaseJsonHandler {

	public static final String PATH = "/json/thresholds";

	public ThresholdsJsonHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException {
		final byte[] payload = Payload.createPayload(new ThresholdsDto(engine.getCurrentThresholdsReached()).toJson());
		addRefreshHeader(exchange);
		sendResponse(exchange, payload);
	}

	@Override
	public String getPath() {
		return PATH;
	}

}
