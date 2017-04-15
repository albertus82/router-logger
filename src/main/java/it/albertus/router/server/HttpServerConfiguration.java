package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.util.Configuration;

public class HttpServerConfiguration extends DefaultHttpServerConfiguration {

	private static final Configuration configuration = RouterLoggerConfiguration.getInstance();

	@Override
	public boolean isEnabled() {
		return configuration.getBoolean("server.enabled", super.isEnabled());
	}

	@Override
	public boolean isAuthenticationRequired() {
		return configuration.getBoolean("server.authentication", super.isAuthenticationRequired());
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
	public short getMaxReqTime() {
		return configuration.getShort("server.maxreqtime", super.getMaxReqTime());
	}

	@Override
	public short getMaxRspTime() {
		return configuration.getShort("server.maxrsptime", super.getMaxRspTime());
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
	public byte getThreadCount() {
		return configuration.getByte("server.threads", super.getThreadCount());
	}

	@Override
	public String getRequestLoggingLevel() {
		return configuration.getString("server.log.request", super.getRequestLoggingLevel());
	}

}
