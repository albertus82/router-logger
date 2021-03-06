package it.albertus.routerlogger.http.json;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.net.httpserver.BaseHttpHandler;
import it.albertus.net.httpserver.config.IHttpServerConfig;
import it.albertus.routerlogger.engine.RouterLoggerConfig;
import it.albertus.routerlogger.engine.RouterLoggerEngine;
import it.albertus.util.logging.LoggerFactory;

public abstract class AbstractJsonHandler extends BaseHttpHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractJsonHandler.class);

	public static class Defaults {
		public static final boolean ENABLED = true;
		public static final boolean REFRESH = true;
		public static final int REFRESH_SECS = 0;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected static final RouterLoggerConfig configuration = RouterLoggerConfig.getInstance();

	protected final RouterLoggerEngine engine;

	public AbstractJsonHandler(final IHttpServerConfig config, final RouterLoggerEngine engine) {
		super(config);
		this.engine = engine;
	}

	@Override
	protected void setContentTypeHeader(final HttpExchange exchange) {
		setContentTypeHeader(exchange, "application/json; charset=" + getCharset().name().toLowerCase());
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
	protected void logRequest(final HttpExchange exchange) {
		Level level = Level.FINEST;
		try {
			level = Level.parse(getHttpServerConfig().getRequestLoggingLevel());
			if (level.intValue() > Level.FINE.intValue()) {
				level = Level.FINE;
			}
		}
		catch (final RuntimeException e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
		doLogRequest(exchange, level);
	}

	@Override
	protected void logResponse(final HttpExchange exchange) {
		Level level = Level.FINEST;
		try {
			level = Level.parse(getHttpServerConfig().getResponseLoggingLevel());
			if (level.intValue() > Level.FINE.intValue()) {
				level = Level.FINE;
			}
		}
		catch (final RuntimeException e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
		doLogResponse(exchange, level);
	}

}
