package it.albertus.routerlogger;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

import it.albertus.routerlogger.console.ConsoleFormatter;
import it.albertus.routerlogger.console.RouterLoggerConsole;
import it.albertus.routerlogger.engine.RouterLoggerConfig;
import it.albertus.routerlogger.gui.RouterLoggerGui;
import it.albertus.routerlogger.util.InitializationException;
import it.albertus.util.logging.LoggingSupport;

public class RouterLogger {

	private static final String LOG_FORMAT_CONSOLE = "%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%tL %4$s: %5$s%6$s%n";

	private static InitializationException initializationException;

	static {
		if (LoggingSupport.getFormat() == null) {
			for (final Handler handler : LoggingSupport.getRootHandlers()) {
				if (handler instanceof ConsoleHandler) {
					handler.setFormatter(new ConsoleFormatter(LOG_FORMAT_CONSOLE));
				}
			}
		}

		try {
			RouterLoggerConfig.getInstance();
		}
		catch (final InitializationException e) {
			initializationException = e;
		}
		catch (final RuntimeException e) {
			initializationException = new InitializationException(e.getMessage(), e);
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

	public static InitializationException getInitializationException() {
		return initializationException;
	}

}
