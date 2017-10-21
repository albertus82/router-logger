package it.albertus.routerlogger.http.json;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.annotation.Path;
import it.albertus.httpserver.config.IHttpServerConfig;
import it.albertus.routerlogger.dto.DeviceStatusDto;
import it.albertus.routerlogger.engine.RouterLoggerEngine;

@Path("/status/device")
public class DeviceStatusJsonHandler extends AbstractJsonHandler {

	public DeviceStatusJsonHandler(final IHttpServerConfig config, final RouterLoggerEngine engine) {
		super(config, engine);
	}

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException {
		final byte[] payload = Payload.createPayload(new DeviceStatusDto(engine.getCurrentData(), engine.getCurrentThresholdsReached()).toJson());
		addRefreshHeader(exchange);
		sendResponse(exchange, payload);
	}

}
