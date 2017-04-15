package it.albertus.router.server.json;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.server.AbstractHttpHandler;
import it.albertus.router.server.HttpException;
import it.albertus.router.server.HttpServerConfiguration;
import it.albertus.util.logging.LoggerFactory;

public abstract class BaseJsonHandler extends AbstractHttpHandler {

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

	protected static final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	protected final RouterLoggerEngine engine;

	public BaseJsonHandler(final RouterLoggerEngine engine) {
		super(new HttpServerConfiguration());
		this.engine = engine;
	}

	@Override
	public final void handle(final HttpExchange exchange) throws IOException {
		if (isEnabled(exchange)) {
			try {
				service(exchange);
			}
			catch (final HttpException e) {
				logger.log(Level.WARNING, e.toString(), e);
				exchange.sendResponseHeaders(e.getStatusCode(), -1);
			}
			catch (final IOException e) {
				logger.log(Level.FINE, e.toString(), e); // often caused by the client that interrupts the stream.
			}
			catch (final Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1);
			}
			finally {
				exchange.close();
			}
		}
	}

	/**
	 * Adds {@code Content-Type: application/json} header to the provided
	 * {@link HttpExchange} object.
	 * 
	 * @param exchange the {@link HttpExchange} to be modified.
	 */
	@Override
	protected void addContentTypeHeader(HttpExchange exchange) {
		exchange.getResponseHeaders().add("Content-Type", "application/json; charset=" + getCharset().name());
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_SERVER_HANDLER_JSON_ENABLED, Defaults.ENABLED);
	}

	protected boolean isEnabled(final HttpExchange exchange) throws IOException {
		if (!httpServerConfiguration.isEnabled() || !isEnabled()) {
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

	@Override
	protected void log(final HttpExchange exchange) {
		/* do nothing */
	}

}
