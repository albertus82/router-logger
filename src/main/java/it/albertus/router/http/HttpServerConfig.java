package it.albertus.router.http;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import it.albertus.httpserver.HttpPathHandler;
import it.albertus.httpserver.HttpServerAuthenticator;
import it.albertus.httpserver.config.HttpServerDefaultConfig;
import it.albertus.router.engine.RouterLoggerConfig;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.http.html.CloseHandler;
import it.albertus.router.http.html.ConfigurationHandler;
import it.albertus.router.http.html.ConnectHandler;
import it.albertus.router.http.html.DisconnectHandler;
import it.albertus.router.http.html.LogsHandler;
import it.albertus.router.http.html.RestartHandler;
import it.albertus.router.http.html.RootHtmlHandler;
import it.albertus.router.http.html.StatusHtmlHandler;
import it.albertus.router.http.json.DataJsonHandler;
import it.albertus.router.http.json.StatusJsonHandler;
import it.albertus.router.http.json.ThresholdsJsonHandler;
import it.albertus.util.Configuration;

public class HttpServerConfig extends HttpServerDefaultConfig {

	public static final boolean DEFAULT_ENABLED = false;
	public static final boolean DEFAULT_AUTHENTICATION_REQUIRED = true;
	public static final short DEFAULT_MAX_REQ_TIME = 10; // seconds
	public static final short DEFAULT_MAX_RSP_TIME = 900; // seconds
	public static final byte DEFAULT_MAX_THREAD_COUNT = 12;

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
		handlers.add(new DataJsonHandler(this, engine));
		handlers.add(new StatusJsonHandler(this, engine));
		handlers.add(new ThresholdsJsonHandler(this, engine));

		return handlers.toArray(new HttpPathHandler[handlers.size()]);
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean("server.enabled", DEFAULT_ENABLED);
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

}
