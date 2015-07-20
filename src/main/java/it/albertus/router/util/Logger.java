package it.albertus.router.util;

import it.albertus.router.RouterLoggerConfiguration;
import it.albertus.router.gui.GuiConsole;
import it.albertus.util.Configuration;
import it.albertus.util.Console;
import it.albertus.util.ExceptionUtils;
import it.albertus.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Logger {

	public enum Destination {
		CONSOLE,
		FILE;
	}

	private static final Configuration configuration = RouterLoggerConfiguration.getInstance();
	private static final DateFormat DATE_FORMAT_FILE_NAME = new SimpleDateFormat("yyyyMMdd");
	private static final DateFormat DATE_FORMAT_LOG = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final Destination[] DEFAULT_DESTINATIONS = { Destination.CONSOLE, Destination.FILE };

	private interface Defaults {
		boolean DEBUG = false;
	}

	// Lazy initialization...
	private static class Singleton {
		private static final Logger logger = new Logger(configuration.getBoolean("console.debug", Defaults.DEBUG));
	}

	public static Logger getInstance() {
		return Singleton.logger;
	}

	private final Console out = GuiConsole.getInstance();
	private final boolean debug;

	private Logger(boolean debug) {
		this.debug = debug;
	}

	public boolean isDebug() {
		return debug;
	}

	public void log(final String text, final Destination... destinations) {
		Set<Destination> dest = getDestinations(destinations);

		if (dest.contains(Destination.CONSOLE)) {
			logToConsole(text);
		}

		if (dest.contains(Destination.FILE)) {
			try {
				logToFile(text);
			}
			catch (Exception e) {
				log(e, Destination.CONSOLE);
			}
		}
	}

	private Set<Destination> getDestinations(final Destination... destinations) {
		Set<Destination> dest = new HashSet<Destination>();
		if (destinations != null && destinations.length != 0) {
			dest.addAll(Arrays.asList(destinations));
		}
		else {
			dest.addAll(Arrays.asList(DEFAULT_DESTINATIONS));
		}
		return dest;
	}

	public void log(final Throwable throwable, Destination... destinations) {
		Set<Destination> dest = getDestinations(destinations);

		String shortLog = ExceptionUtils.getLogMessage(throwable);
		String longLog = ExceptionUtils.getStackTrace(throwable);

		if (dest.contains(Destination.CONSOLE)) {
			if (debug) {
				logToConsole(longLog);
			}
			else {
				logToConsole(shortLog);
			}
		}

		if (dest.contains(Destination.FILE)) {
			try {
				logToFile(longLog);
			}
			catch (Exception e) {
				log(e, Destination.CONSOLE);
			}
		}
	}

	private void logToConsole(final String text) {
		final String base = DATE_FORMAT_LOG.format(new Date()) + ' ';
		out.println(base + text, true);
	}

	private void logToFile(final String text) throws IOException {
		final String logDestinationDir = configuration.getString("logger.error.log.destination.path");
		final File logFile;
		if (StringUtils.isNotBlank(logDestinationDir)) {
			File logDestDir = new File(logDestinationDir.trim());
			if (logDestDir.exists() && !logDestDir.isDirectory()) {
				throw new RuntimeException("Invalid path: \"" + logDestDir + "\".");
			}
			if (!logDestDir.exists()) {
				logDestDir.mkdirs();
			}
			logFile = new File(logDestinationDir.trim() + '/' + DATE_FORMAT_FILE_NAME.format(new Date()) + ".log");
		}
		else {
			logFile = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() + '/' + DATE_FORMAT_FILE_NAME.format(new Date()) + ".log");
		}
		final FileWriter logFileWriter = new FileWriter(logFile, true);
		final String base = new Date().toString() + " - ";
		logFileWriter.write(base + text);
		logFileWriter.flush();
		logFileWriter.close();
	}

}
