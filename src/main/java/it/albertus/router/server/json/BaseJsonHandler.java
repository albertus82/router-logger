package it.albertus.router.server.json;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.server.BaseHttpHandler;
import it.albertus.router.server.BaseHttpServer;
import it.albertus.router.util.Logger;
import it.albertus.router.util.LoggerFactory;

public abstract class BaseJsonHandler extends BaseHttpHandler {

	private static final Logger logger = LoggerFactory.getLogger(BaseJsonHandler.class);

	public static class Defaults {
		public static final boolean ENABLED = true;
		public static final boolean REFRESH = true;
		public static final int REFRESH_SECS = 0;
		public static final boolean COMPRESS_RESPONSE = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected static final String CFG_KEY_SERVER_HANDLER_JSON_ENABLED = "server.handler.json.enabled";

	public BaseJsonHandler(final RouterLoggerEngine engine) {
		super(engine);
	}

	protected abstract void service(HttpExchange exchange) throws IOException;

	@Override
	public void handle(final HttpExchange exchange) throws IOException {
		if (isEnabled(exchange)) {
			try {
				service(exchange);
			}
			catch (final IOException ioe) {
				logger.debug(ioe); // often caused by the client that interrupts the stream.
			}
			catch (final Exception exception) {
				logger.error(exception);
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
			}
			finally {
				exchange.close();
			}
		}
	}

	/**
	 * Adds {@code Content-Type: application/json} and {@code Date} headers to
	 * the provided {@link HttpExchange} object.
	 * 
	 * @param exchange the {@link HttpExchange} to be modified.
	 */
	@Override
	protected void addCommonHeaders(final HttpExchange exchange) {
		exchange.getResponseHeaders().add("Content-Type", "application/json; charset=" + getCharset().name());
		addDateHeader(exchange);
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_SERVER_HANDLER_JSON_ENABLED, Defaults.ENABLED);
	}

	protected boolean isEnabled(final HttpExchange exchange) throws IOException {
		if (!configuration.getBoolean("server.enabled", BaseHttpServer.Defaults.ENABLED) || !isEnabled()) {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, -1);
			exchange.close();
			return false;
		}
		else {
			return true;
		}
	}

	protected void addRefreshHeader(final HttpExchange exchange) {
		if (configuration.getBoolean("server.handler.json.refresh", Defaults.REFRESH)) {
			int refresh = configuration.getInt("server.handler.json.refresh.secs", Defaults.REFRESH_SECS);
			if (refresh <= 0) { // Auto
				refresh = Math.max(1, (int) (engine.getWaitTimeInMillis() / 1000) - 1);
			}
			exchange.getResponseHeaders().add("Refresh", Integer.toString(refresh));
		}
	}

	@Override
	protected boolean canCompressResponse(final HttpExchange exchange) {
		return configuration.getBoolean("server.compress.response.json", Defaults.COMPRESS_RESPONSE) && super.canCompressResponse(exchange);
	}

}
