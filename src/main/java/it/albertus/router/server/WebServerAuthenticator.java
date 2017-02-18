package it.albertus.router.server;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.BasicAuthenticator;

import it.albertus.router.RouterLogger;
import it.albertus.router.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.logging.LoggerFactory;

public class WebServerAuthenticator extends BasicAuthenticator {

	private static final Logger logger = LoggerFactory.getLogger(WebServerAuthenticator.class);

	private static final Configuration configuration = RouterLogger.getConfiguration();

	private static final String CFG_KEY_SERVER_USERNAME = "server.username";
	private static final String CFG_KEY_SERVER_PASSWORD = "server.password";

	private static final int FAIL_DELAY_IN_MILLIS = 3000;

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
				logger.warning(Messages.get("err.server.cfg.error.username"));
				return fail();
			}

			final char[] expectedPassword = configuration.getCharArray(CFG_KEY_SERVER_PASSWORD);
			if (expectedPassword == null || expectedPassword.length == 0) {
				logger.warning(Messages.get("err.server.cfg.error.password"));
				return fail();
			}

			if (specifiedUsername.equals(expectedUsername) && checkPassword(specifiedPassword, expectedPassword)) {
				return true;
			}
			else {
				logger.log(Level.WARNING, Messages.get("err.server.authentication"), new String[] { specifiedUsername, specifiedPassword });
				return fail();
			}
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
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
		try {
			TimeUnit.MILLISECONDS.sleep(FAIL_DELAY_IN_MILLIS);
		}
		catch (final InterruptedException e) {
			logger.log(Level.FINE, e.toString(), e);
			Thread.currentThread().interrupt();
		}
		return false;
	}

}
