package it.albertus.router;

import java.io.IOException;

import it.albertus.router.console.RouterLoggerConsole;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.resources.Messages;
import it.albertus.router.util.Logger;
import it.albertus.router.util.LoggerFactory;

public class RouterLogger {

	private static final Logger logger = LoggerFactory.getLogger(RouterLogger.class);

	public static class InitializationException extends Exception {
		private static final long serialVersionUID = -5943702854022883885L;

		private InitializationException(final String message, final Throwable cause) {
			super(message, cause);
		}
	}

	private static RouterLoggerConfiguration configuration = null;

	private static InitializationException initializationException = null;

	static {
		try {
			configuration = new RouterLoggerConfiguration();
		}
		catch (final IOException ioe) {
			logger.error(ioe);
			initializationException = new InitializationException(Messages.get("err.open.cfg", RouterLoggerConfiguration.FILE_NAME), ioe);
		}
	}

	private RouterLogger() {
		throw new IllegalAccessError();
	}

	/* Unique entry point */
	public static final void main(final String[] args) {
		if (args.length > 0) {
			RouterLoggerConsole.start(initializationException, args);
		}
		else {
			RouterLoggerGui.start(initializationException);
		}
	}

	public static RouterLoggerConfiguration getConfiguration() {
		return configuration;
	}

	public static InitializationException getInitializationException() {
		return initializationException;
	}

}
