package it.albertus.routerlogger.http.json;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.net.httpserver.annotation.Path;
import it.albertus.net.httpserver.config.IHttpServerConfig;
import it.albertus.routerlogger.dto.AppStatusDto;
import it.albertus.routerlogger.engine.RouterLoggerEngine;

@Path("/status/app")
public class AppStatusJsonHandler extends AbstractJsonHandler {

	public AppStatusJsonHandler(final IHttpServerConfig config, final RouterLoggerEngine engine) {
		super(config, engine);
	}

	@Override
	protected void doGet(final HttpExchange exchange) throws IOException {
		final byte[] payload = Payload.createPayload(new AppStatusDto(engine.getCurrentStatus()).toJson());
		addRefreshHeader(exchange);
		sendResponse(exchange, payload);
	}

}
