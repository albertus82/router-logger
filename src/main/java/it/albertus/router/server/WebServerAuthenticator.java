package it.albertus.router.server;

import com.sun.net.httpserver.BasicAuthenticator;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Messages;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.router.util.LoggerFactory;
import it.albertus.util.Configuration;
import it.albertus.util.ThreadUtils;

public class WebServerAuthenticator extends BasicAuthenticator {

	private static final Logger logger = LoggerFactory.getLogger(WebServerAuthenticator.class);

	private static final String CFG_KEY_SERVER_USERNAME = "server.username";
	private static final String CFG_KEY_SERVER_PASSWORD = "server.password";
	private static final int FAIL_DELAY_IN_MILLIS = 3000;

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();

	protected WebServerAuthenticator() {
		super(Messages.get("msg.application.name"));
	}

	@Override
	public boolean checkCredentials(final String specifiedUsername, final String specifiedPassword) {
		try {
			if (specifiedUsername == null || specifiedUsername.isEmpty() || specifiedPassword == null || specifiedPassword.isEmpty()) {
				return fail();
			}

			final String expectedUsername = configuration.getString(CFG_KEY_SERVER_USERNAME);
			if (expectedUsername == null || expectedUsername.isEmpty()) {
				logger.info(Messages.get("err.server.cfg.error.username"), Destination.CONSOLE, Destination.FILE);
				return fail();
			}

			final char[] expectedPassword = configuration.getCharArray(CFG_KEY_SERVER_PASSWORD);
			if (expectedPassword == null || expectedPassword.length == 0) {
				logger.info(Messages.get("err.server.cfg.error.password"), Destination.CONSOLE, Destination.FILE);
				return fail();
			}

			if (specifiedUsername.equals(expectedUsername) && checkPassword(specifiedPassword, expectedPassword)) {
				return true;
			}
			else {
				logger.info(Messages.get("err.server.authentication", specifiedUsername, specifiedPassword));
				return fail();
			}
		}
		catch (final Exception exception) {
			logger.error(exception);
			return fail();
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

	private boolean fail() {
		ThreadUtils.sleep(FAIL_DELAY_IN_MILLIS);
		return false;
	}

}
