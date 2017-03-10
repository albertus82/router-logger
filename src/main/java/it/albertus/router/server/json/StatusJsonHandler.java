package it.albertus.router.server.json;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.dto.StatusDto;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.util.Payload;

public class StatusJsonHandler extends BaseJsonHandler {

	public static final String PATH = "/json/status";

	public StatusJsonHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException {
		final byte[] payload = Payload.createPayload(new StatusDto(engine.getCurrentStatus()).toJson());
		addRefreshHeader(exchange);
		sendResponse(exchange, payload);
	}

	@Override
	public String getPath() {
		return PATH;
	}

}
