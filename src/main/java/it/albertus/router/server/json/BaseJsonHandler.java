package it.albertus.router.server.json;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.server.BaseHttpHandler;
import it.albertus.router.server.BaseHttpServer;
import it.albertus.router.util.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;

public abstract class BaseJsonHandler extends BaseHttpHandler {

	public interface Defaults {
		boolean ENABLED = true;
	}

	protected static final String CFG_KEY_SERVER_HANDLER_JSON_ENABLED = "server.handler.json.enabled";

	protected final RouterLoggerEngine engine;

	public BaseJsonHandler(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	protected abstract void service(HttpExchange exchange) throws IOException;

	@Override
	public void handle(final HttpExchange exchange) throws IOException {
		if (isEnabled(exchange)) {
			try {
				service(exchange);
			}
			catch (final IOException ioe) {
				// Ignore (often caused by the client that interrupts the stream).
			}
			catch (final Exception exception) {
				Logger.getInstance().log(exception);
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
		final int refresh = Math.max(1, Long.valueOf(engine.getWaitTimeInMillis() / 1000).intValue() - 1);
		exchange.getResponseHeaders().add("Refresh", Integer.toString(refresh));
	}

}
