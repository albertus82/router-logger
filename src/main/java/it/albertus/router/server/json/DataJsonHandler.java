package it.albertus.router.server.json;

import java.io.IOException;

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
		sendResponse(exchange, payload);
	}

	@Override
	public String getPath() {
		return PATH;
	}

}
