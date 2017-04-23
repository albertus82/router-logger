package it.albertus.router.http.json;

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
		public static final boolean COMPRESS_RESPONSE = true;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected static final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	protected final RouterLoggerEngine engine;

	public AbstractJsonHandler(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	@Override
	protected String getContentType(final String fileName) {
		return "application/json; charset=" + getCharset().name();
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean("server.handler.json.enabled", Defaults.ENABLED);
	}

	protected void addRefreshHeader(final HttpExchange exchange) {
		if (configuration.getBoolean("server.handler.json.refresh", Defaults.REFRESH)) {
			int refresh = configuration.getInt("server.handler.json.refresh.secs", Defaults.REFRESH_SECS);
			if (refresh <= 0) { // Auto
				refresh = Math.max(1, (int) (engine.getWaitTimeInMillis() / 1000) - 1);
			}
			setRefreshHeader(exchange, refresh);
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
