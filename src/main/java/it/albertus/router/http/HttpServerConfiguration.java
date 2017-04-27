package it.albertus.router.http;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import it.albertus.httpserver.AbstractHttpHandler;
import it.albertus.httpserver.DefaultHttpServerConfiguration;
import it.albertus.router.engine.RouterLoggerConfiguration;
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
import it.albertus.router.resources.Messages;
import it.albertus.util.Configuration;

public class HttpServerConfiguration extends DefaultHttpServerConfiguration {

	public static class Defaults {
		public static final long MAX_REQ_TIME = 10; // seconds
		public static final long MAX_RSP_TIME = 600; // seconds
		public static final int MAX_THREAD_COUNT = 12;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();

	private final RouterLoggerEngine engine; // Injected

	public HttpServerConfiguration(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	@Override
	public AbstractHttpHandler[] getHandlers() {
		final List<AbstractHttpHandler> handlers = new ArrayList<AbstractHttpHandler>();

		// HTML
		handlers.add(new RootHtmlHandler()); // serves also static resources
		handlers.add(new StatusHtmlHandler(engine));
		handlers.add(new RestartHandler(engine));
		handlers.add(new DisconnectHandler(engine));
		handlers.add(new ConnectHandler(engine));
		handlers.add(new CloseHandler(engine));
		handlers.add(new LogsHandler());
		handlers.add(new ConfigurationHandler());

		// JSON
		handlers.add(new DataJsonHandler(engine));
		handlers.add(new StatusJsonHandler(engine));
		handlers.add(new ThresholdsJsonHandler(engine));

		return handlers.toArray(new AbstractHttpHandler[0]);
	}

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean("server.enabled", super.isEnabled());
	}

	@Override
	public boolean isAuthenticationRequired() {
		return configuration.getBoolean("server.authentication", super.isAuthenticationRequired());
	}

	@Override
	public String getRealm() {
		return Messages.get("msg.application.name");
	}

	@Override
	public String getUsername() {
		return configuration.getString("server.username");
	}

	@Override
	public char[] getPassword() {
		return configuration.getCharArray("server.password");
	}

	@Override
	public int getPort() {
		return configuration.getInt("server.port", super.getPort());
	}

	@Override
	public long getMaxReqTime() {
		return configuration.getLong("server.maxreqtime", Defaults.MAX_REQ_TIME);
	}

	@Override
	public long getMaxRspTime() {
		return configuration.getLong("server.maxrsptime", Defaults.MAX_RSP_TIME);
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
		params.setCipherSuites(cipherSuites.toArray(new String[0]));
		return params;
	}

	@Override
	public int getMaxThreadCount() {
		return configuration.getInt("server.threads", Defaults.MAX_THREAD_COUNT);
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
