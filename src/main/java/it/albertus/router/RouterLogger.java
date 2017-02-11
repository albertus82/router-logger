package it.albertus.router;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.router.console.RouterLoggerConsole;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.resources.Messages;
import it.albertus.util.logging.CustomFormatter;
import it.albertus.util.logging.LoggerFactory;
import it.albertus.util.logging.LoggingSupport;

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
		final Logger rootLogger = LoggingSupport.getRootLogger();
		for (int i = 0; i < rootLogger.getHandlers().length; i++) {
			final Handler oldHandler = rootLogger.getHandlers()[i];
			if (oldHandler instanceof ConsoleHandler) {
				final ConsoleHandler newConsoleHandler = new ConsoleHandler();
				newConsoleHandler.setLevel(oldHandler.getLevel());
				newConsoleHandler.setFilter(oldHandler.getFilter());
				newConsoleHandler.setFormatter(new CustomFormatter("%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%tL %4$s: %5$s%6$s%n"));
				newConsoleHandler.setErrorManager(oldHandler.getErrorManager());
				try {
					newConsoleHandler.setEncoding(oldHandler.getEncoding());
				}
				catch (final UnsupportedEncodingException uee) {
					throw new IllegalStateException(uee);
				}
				rootLogger.removeHandler(oldHandler);
				rootLogger.addHandler(newConsoleHandler);
			}
		}

		try {
			configuration = new RouterLoggerConfiguration();
		}
		catch (final IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
			initializationException = new InitializationException(Messages.get("err.open.cfg", RouterLoggerConfiguration.CFG_FILE_NAME), e);
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
