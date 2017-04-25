package it.albertus.router.http;

import it.albertus.httpserver.DefaultHttpServerConfiguration;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Messages;
import it.albertus.util.Configuration;

public class HttpServerConfiguration extends DefaultHttpServerConfiguration {

	private static final Configuration configuration = RouterLoggerConfiguration.getInstance();

	public static class Defaults {
		public static final long MAX_REQ_TIME = 10; // seconds
		public static final long MAX_RSP_TIME = 600; // seconds

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
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
	public int getMaxThreadCount() {
		return configuration.getInt("server.threads", super.getMaxThreadCount());
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
