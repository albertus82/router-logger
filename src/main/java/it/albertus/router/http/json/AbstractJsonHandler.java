package it.albertus.router.http.json;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.httpserver.AbstractHttpHandler;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.util.logging.LoggerFactory;

public abstract class AbstractJsonHandler extends AbstractHttpHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractJsonHandler.class);

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

	public AbstractJsonHandler(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	/**
	 * Adds {@code Content-Type: application/json} header to the provided
	 * {@link HttpExchange} object.
	 * 
	 * @param exchange the {@link HttpExchange} to be modified.
	 */
	@Override
	protected String getContentType(final String fileName) {
		return "application/json; charset=" + getCharset().name();
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean(CFG_KEY_SERVER_HANDLER_JSON_ENABLED, Defaults.ENABLED);
	}

	protected boolean isEnabled(final HttpExchange exchange) throws IOException {
		if (!getHttpServerConfiguration().isEnabled() || !isEnabled()) {
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
		Level level = Level.OFF;
		try {
			level = Level.parse(getHttpServerConfiguration().getRequestLoggingLevel());
			if (level.intValue() > Level.FINE.intValue()) {
				level = Level.FINE;
			}
		}
		catch (final RuntimeException e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
		doLog(exchange, level);
	}

}
