package it.albertus.router.util;

import java.util.Date;

import it.albertus.router.RouterLoggerConfiguration;
import it.albertus.util.Console;
import it.albertus.util.ExceptionUtils;

public class Logger {

	private interface Defaults {
		boolean DEBUG = false;
	}

	// Lazy initialization...
	private static class Singleton {
		private static final Logger logger = new Logger(RouterLoggerConfiguration.getInstance().getBoolean("logger.debug", Defaults.DEBUG));
	}

	public static Logger getInstance() {
		return Singleton.logger;
	}

	private final Console out = Console.getInstance();
	private final boolean debug;

	private Logger(boolean debug) {
		this.debug = debug;
	}

	public boolean isDebug() {
		return debug;
	}

	public void log(Throwable throwable) {
		if (debug) {
			out.print(new Date().toString() + " - " + ExceptionUtils.getStackTrace(throwable), true);
		}
		else {
			out.println(ExceptionUtils.getLogMessage(throwable), true);
		}
	}

}
