package it.albertus.router.http;

import java.util.logging.Level;

import it.albertus.httpserver.config.SingleUserAuthenticatorDefaultConfig;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.util.Configuration;

public class AuthenticatorConfig extends SingleUserAuthenticatorDefaultConfig {

	public static final String DEFAULT_FAILURE_LOGGING_LEVEL = Level.WARNING.getName();

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();

	@Override
	public String getUsername() {
		return configuration.getString("server.username");
	}

	@Override
	public char[] getPassword() {
		return configuration.getCharArray("server.password");
	}

	@Override
	public String getRealm() {
		return "Restricted area";
	}

	@Override
	public String getFailureLoggingLevel() {
		return configuration.getString("server.log.auth.failed", DEFAULT_FAILURE_LOGGING_LEVEL);
	}

}
