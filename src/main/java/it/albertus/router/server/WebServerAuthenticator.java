package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.Configuration;

import com.sun.net.httpserver.BasicAuthenticator;

public class WebServerAuthenticator extends BasicAuthenticator {

	private static final String CFG_KEY_SERVER_USERNAME = "server.username";
	private static final String CFG_KEY_SERVER_PASSWORD = "server.password";

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();

	protected WebServerAuthenticator() {
		super(Resources.get("msg.application.name"));
	}

	@Override
	public boolean checkCredentials(final String specifiedUsername, final String specifiedPassword) {
		try {
			if (specifiedUsername == null || specifiedUsername.isEmpty() || specifiedPassword == null || specifiedPassword.isEmpty()) {
				return false;
			}

			final String expectedUsername = configuration.getString(CFG_KEY_SERVER_USERNAME);
			if (expectedUsername == null || expectedUsername.isEmpty()) {
				Logger.getInstance().log(Resources.get("err.server.cfg.error.username"), Destination.CONSOLE, Destination.FILE);
				return false;
			}

			final char[] expectedPassword = configuration.getCharArray(CFG_KEY_SERVER_PASSWORD);
			if (expectedPassword == null || expectedPassword.length == 0) {
				Logger.getInstance().log(Resources.get("err.server.cfg.error.password"), Destination.CONSOLE, Destination.FILE);
				return false;
			}

			if (specifiedUsername.equals(expectedUsername) && checkPassword(specifiedPassword, expectedPassword)) {
				return true;
			}
			else {
				Logger.getInstance().log(Resources.get("err.server.authentication", specifiedUsername, specifiedPassword));
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

	private boolean checkPassword(final String specifiedPassword, final char[] expectedPassword) {
		boolean equal = true;
		if (specifiedPassword.length() != expectedPassword.length) {
			equal = false;
		}

		for (int i = 0; i < 0x400; i++) {
			if (specifiedPassword.charAt(i % specifiedPassword.length()) != expectedPassword[i % expectedPassword.length]) {
				equal = false;
			}
		}
		return equal;
	}

}
