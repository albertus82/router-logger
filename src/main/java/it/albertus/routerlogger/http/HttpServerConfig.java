package it.albertus.routerlogger.http;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import com.sun.net.httpserver.Filter;

import it.albertus.net.httpserver.HttpPathHandler;
import it.albertus.net.httpserver.HttpServerAuthenticator;
import it.albertus.net.httpserver.config.HttpServerDefaultConfig;
import it.albertus.net.httpserver.filter.HSTSResponseFilter;
import it.albertus.routerlogger.engine.RouterLoggerConfig;
import it.albertus.routerlogger.engine.RouterLoggerEngine;
import it.albertus.routerlogger.http.html.CloseHandler;
import it.albertus.routerlogger.http.html.ConfigurationHandler;
import it.albertus.routerlogger.http.html.ConnectHandler;
import it.albertus.routerlogger.http.html.DisconnectHandler;
import it.albertus.routerlogger.http.html.LogsHandler;
import it.albertus.routerlogger.http.html.RestartHandler;
import it.albertus.routerlogger.http.html.RootHtmlHandler;
import it.albertus.routerlogger.http.html.StatusHtmlHandler;
import it.albertus.routerlogger.http.json.AppStatusJsonHandler;
import it.albertus.routerlogger.http.json.DeviceStatusJsonHandler;
import it.albertus.util.Configuration;

public class HttpServerConfig extends HttpServerDefaultConfig {

	public static final boolean DEFAULT_ENABLED = false;
	public static final boolean DEFAULT_AUTHENTICATION_REQUIRED = true;
	public static final short DEFAULT_MAX_REQ_TIME = 10; // seconds
	public static final short DEFAULT_MAX_RSP_TIME = 15 * 60; // 15 mins (in seconds)
	public static final byte DEFAULT_MAX_THREAD_COUNT = 12;
	public static final boolean DEFAULT_SSL_HSTS_ENABLED = false;
	public static final int DEFAULT_SSL_HSTS_MAX_AGE = 180 * 24 * 60 * 60; // 180 days (in seconds)
	public static final boolean DEFAULT_SSL_HSTS_INCLUDESUBDOMAINS = true;
	public static final boolean DEFAULT_SSL_HSTS_PRELOAD = false;
	public static final boolean DEFAULT_SSL_REDIRECTION_ENABLED = false;
	public static final int DEFAULT_SSL_REDIRECTION_LISTENING_PORT = 8080;
	public static final int DEFAULT_SSL_REDIRECTION_RESPONSE_CODE = HttpURLConnection.HTTP_MOVED_TEMP;
	public static final String DEFAULT_SSL_REDIRECTION_LOCATION_HOST = "0.0.0.0";
	public static final int DEFAULT_SSL_REDIRECTION_LOCATION_PORT = 443;

	private final Configuration configuration = RouterLoggerConfig.getInstance();

	private final RouterLoggerEngine engine; // Injected

	public HttpServerConfig(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	@Override
	public HttpPathHandler[] getHandlers() {
		final List<HttpPathHandler> handlers = new ArrayList<HttpPathHandler>();

		// HTML
		handlers.add(new RootHtmlHandler(this, engine)); // serves also static resources
		handlers.add(new StatusHtmlHandler(this, engine));
		handlers.add(new RestartHandler(this, engine));
		handlers.add(new DisconnectHandler(this, engine));
		handlers.add(new ConnectHandler(this, engine));
		handlers.add(new CloseHandler(this, engine));
		handlers.add(new LogsHandler(this));
		handlers.add(new ConfigurationHandler(this));

		// JSON
		handlers.add(new DeviceStatusJsonHandler(this, engine));
		handlers.add(new AppStatusJsonHandler(this, engine));

		return handlers.toArray(new HttpPathHandler[handlers.size()]);
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean("server.enabled", DEFAULT_ENABLED);
	}

	@Override
	public Filter[] getFilters() {
		final Filter[] defaultFilters = super.getFilters();
		if (isSslEnabled() && configuration.getBoolean("server.ssl.hsts.enabled", DEFAULT_SSL_HSTS_ENABLED)) {
			final Filter[] filters = Arrays.copyOf(defaultFilters, defaultFilters.length + 1);
			filters[filters.length - 1] = new HSTSResponseFilter(configuration.getInt("server.ssl.hsts.maxage", DEFAULT_SSL_HSTS_MAX_AGE), configuration.getBoolean("server.ssl.hsts.includesubdomains", DEFAULT_SSL_HSTS_INCLUDESUBDOMAINS), configuration.getBoolean("server.ssl.hsts.preload", DEFAULT_SSL_HSTS_PRELOAD));
			return filters;
		}
		else {
			return defaultFilters;
		}
	}

	@Override
	public HttpServerAuthenticator getAuthenticator() {
		if (configuration.getBoolean("server.authentication", DEFAULT_AUTHENTICATION_REQUIRED)) {
			return new HttpServerAuthenticator(new AuthenticatorConfig());
		}
		else {
			return null;
		}
	}

	@Override
	public int getPort() {
		return configuration.getInt("server.port", super.getPort());
	}

	@Override
	public long getMaxReqTime() {
		return configuration.getLong("server.maxreqtime", DEFAULT_MAX_REQ_TIME);
	}

	@Override
	public long getMaxRspTime() {
		return configuration.getLong("server.maxrsptime", DEFAULT_MAX_RSP_TIME);
	}

	@Override
	public boolean isSslEnabled() {
		return configuration.getBoolean("server.ssl.enabled", super.isSslEnabled());
	}

	@Override
	public char[] getStorePass() {
		return configuration.getCharArray("server.ssl.storepass");
	}

	@Override
	public String getKeyStoreType() {
		return configuration.getString("server.ssl.keystore.type", super.getKeyStoreType());
	}

	@Override
	public String getKeyStoreFileName() {
		return configuration.getString("server.ssl.keystore.file", true);
	}

	@Override
	public char[] getKeyPass() {
		return configuration.getCharArray("server.ssl.keypass");
	}

	@Override
	public String getKeyManagerFactoryAlgorithm() {
		return configuration.getString("server.ssl.kmf.algorithm", super.getKeyManagerFactoryAlgorithm());
	}

	@Override
	public String getTrustManagerFactoryAlgorithm() {
		return configuration.getString("server.ssl.tmf.algorithm", super.getTrustManagerFactoryAlgorithm());
	}

	@Override
	public String getSslProtocol() {
		return configuration.getString("server.ssl.protocol", super.getSslProtocol());
	}

	@Override
	public SSLParameters getSslParameters(final SSLContext context) {
		final SSLParameters params = super.getSslParameters(context);
		final Set<String> cipherSuites = new LinkedHashSet<String>();
		for (final String cipherSuite : params.getCipherSuites()) {
			final String cipherSuiteUpperCase = cipherSuite.toUpperCase();
			// Excluding unsafe suites (3DES is slow and weak, avoid the RSA key exchange unless absolutely necessary). See https://github.com/ssllabs/research/wiki/SSL-and-TLS-Deployment-Best-Practices and https://weakdh.org
			if (!cipherSuiteUpperCase.contains("_DHE_") && !cipherSuiteUpperCase.startsWith("TLS_RSA_")) {
				cipherSuites.add(cipherSuite);
			}
		}
		params.setCipherSuites(cipherSuites.toArray(new String[cipherSuites.size()]));
		return params;
	}

	@Override
	public int getMaxThreadCount() {
		return configuration.getInt("server.threads", DEFAULT_MAX_THREAD_COUNT);
	}

	@Override
	public String getRequestLoggingLevel() {
		return configuration.getString("server.log.request", super.getRequestLoggingLevel());
	}

	@Override
	public boolean isCompressionEnabled() {
		return configuration.getBoolean("server.compress.response", super.isCompressionEnabled());
	}

	public boolean isSslRedirectionEnabled() {
		return configuration.getBoolean("server.ssl.redirection.enabled", DEFAULT_SSL_REDIRECTION_ENABLED);
	}

	public int getSslRedirectionListeningPort() {
		return configuration.getInt("server.ssl.redirection.listening.port", DEFAULT_SSL_REDIRECTION_LISTENING_PORT);
	}

	public int getSslRedirectionResponseCode() {
		return configuration.getInt("server.ssl.redirection.response.code", DEFAULT_SSL_REDIRECTION_RESPONSE_CODE);
	}

	public String getSslRedirectionLocationHost() {
		return configuration.getString("server.ssl.redirection.location.host", DEFAULT_SSL_REDIRECTION_LOCATION_HOST);
	}

	public int getSslRedirectionLocationPort() {
		return configuration.getInt("server.ssl.redirection.location.port", DEFAULT_SSL_REDIRECTION_LOCATION_PORT);
	}

}
