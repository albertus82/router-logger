package it.albertus.router.web;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.util.Configuration;

import com.sun.net.httpserver.BasicAuthenticator;

public class WebServerAuthenticator extends BasicAuthenticator {

	public interface Defaults {
		String USERNAME = "admin";
	}

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();

	public WebServerAuthenticator() {
		super(Resources.get("msg.application.name"));
	}

	@Override
	public boolean checkCredentials(final String username, final String password) {
		try {
			if (configuration.getString("server.username", Defaults.USERNAME).equals(username) && checkPassword(password, configuration.getCharArray("server.password"))) {
				return true;
			}
			else {
				if (username != null && !username.isEmpty()) {
					Logger.getInstance().log(Resources.get("err.server.authentication", username, password));
				}
				return false;
			}
		}
		catch (final Exception exception) {
			Logger.getInstance().log(exception);
			return false;
		}
		catch (final Throwable throwable) {
			return false;
		}
	}

	private boolean checkPassword(final String provided, final char[] stored) {
		if (stored == null || stored.length == 0) {
			return false;
		}

		boolean equal = true;
		if (provided.length() != stored.length) {
			equal = false;
		}

		for (int i = 0; i < 0x400; i++) {
			if (provided.charAt(i % provided.length()) != stored[i % stored.length]) {
				equal = false;
			}
		}
		return equal;
	}

}
