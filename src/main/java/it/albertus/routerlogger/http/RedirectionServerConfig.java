package it.albertus.routerlogger.http;

import java.net.HttpURLConnection;

import it.albertus.net.httpserver.HttpPathHandler;
import it.albertus.net.httpserver.config.HttpServerDefaultConfig;
import it.albertus.net.httpserver.config.IHttpServerConfig;
import it.albertus.routerlogger.engine.RouterLoggerConfig;
import it.albertus.util.Configuration;

public class RedirectionServerConfig extends HttpServerDefaultConfig {

	public static final boolean DEFAULT_SSL_REDIRECTION_ENABLED = false;
	public static final int DEFAULT_SSL_REDIRECTION_LISTENING_PORT = 8080;
	public static final int DEFAULT_SSL_REDIRECTION_RESPONSE_CODE = HttpURLConnection.HTTP_MOVED_TEMP;
	public static final String DEFAULT_SSL_REDIRECTION_LOCATION_HOST = "0.0.0.0";
	public static final int DEFAULT_SSL_REDIRECTION_LOCATION_PORT = 443;

	private final Configuration routerLoggerConfig = RouterLoggerConfig.getInstance();
	private final IHttpServerConfig httpServerConfig;

	public RedirectionServerConfig(final IHttpServerConfig httpServerConfig) {
		this.httpServerConfig = httpServerConfig;
	}

	@Override
	public HttpPathHandler[] getHandlers() {
		return new HttpPathHandler[] { new HttpsRedirectionHandler(this) };
	}

	@Override
	public long getMaxReqTime() {
		return httpServerConfig.getMaxReqTime();
	}

	@Override
	public long getMaxRspTime() {
		return httpServerConfig.getMaxRspTime();
	}

	@Override
	public int getMaxThreadCount() {
		return 2;
	}

	@Override
	public int getMinThreadCount() {
		return 1;
	}

	@Override
	public int getPort() {
		return routerLoggerConfig.getInt("server.ssl.redirection.listening.port", DEFAULT_SSL_REDIRECTION_LISTENING_PORT);
	}

	public String getSslRedirectionLocationHost() {
		return routerLoggerConfig.getString("server.ssl.redirection.location.host", DEFAULT_SSL_REDIRECTION_LOCATION_HOST);
	}

	public int getSslRedirectionLocationPort() {
		return routerLoggerConfig.getInt("server.ssl.redirection.location.port", DEFAULT_SSL_REDIRECTION_LOCATION_PORT);
	}

	public int getSslRedirectionResponseCode() {
		return routerLoggerConfig.getInt("server.ssl.redirection.response.code", DEFAULT_SSL_REDIRECTION_RESPONSE_CODE);
	}

	@Override
	public boolean isEnabled() {
		return httpServerConfig.isEnabled() && httpServerConfig.isSslEnabled() && routerLoggerConfig.getBoolean("server.ssl.redirection.enabled", DEFAULT_SSL_REDIRECTION_ENABLED);
	}

}
