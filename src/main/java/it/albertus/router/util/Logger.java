package it.albertus.router.util;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.util.Configuration;
import it.albertus.util.Console;
import it.albertus.util.ExceptionUtils;
import it.albertus.util.StringUtils;
import it.albertus.util.TerminalConsole;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
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

	private static final DateFormat DATE_FORMAT_FILE_NAME = new SimpleDateFormat("yyyyMMdd");
	private static final DateFormat DATE_FORMAT_LOG = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final Destination[] DEFAULT_DESTINATIONS = { Destination.CONSOLE, Destination.FILE };

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();

	private interface Defaults {
		boolean DEBUG = false;
	}

	// Lazy initialization...
	private static class Singleton {
		private static final Logger LOGGER = new Logger();
	}

	public static Logger getInstance() {
		return Singleton.LOGGER;
	}

	public void init(final Console console) {
		this.out = console;
	}

	private Console out = TerminalConsole.getInstance(); // Fail-safe.

	public boolean isDebugEnabled() {
		return configuration.getBoolean("console.debug", Defaults.DEBUG);
	}

	public void log(final String text, final Destination... destinations) {
		final Set<Destination> dest = getDestinations(destinations);

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
		final Set<Destination> dest = new HashSet<Destination>();
		if (destinations != null && destinations.length != 0) {
			dest.addAll(Arrays.asList(destinations));
		}
		else {
			dest.addAll(Arrays.asList(DEFAULT_DESTINATIONS));
		}
		return dest;
	}

	public void log(final Throwable throwable, Destination... destinations) {
		final Set<Destination> dest = getDestinations(destinations);

		final String shortLog = ExceptionUtils.getLogMessage(throwable);
		final String longLog = ExceptionUtils.getStackTrace(throwable);

		if (dest.contains(Destination.CONSOLE)) {
			if (isDebugEnabled()) {
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
		out.println(base + StringUtils.trimToEmpty(text), true);
	}

	private void logToFile(final String text) throws IOException {
		final String logDestinationDir = configuration.getString("logger.error.log.destination.path");
		File logFile;
		if (StringUtils.isNotBlank(logDestinationDir)) {
			File logDestDir = new File(logDestinationDir.trim());
			if (logDestDir.exists() && !logDestDir.isDirectory()) {
				throw new RuntimeException(Resources.get("err.invalid.path", logDestDir));
			}
			if (!logDestDir.exists()) {
				logDestDir.mkdirs();
			}
			logFile = new File(logDestinationDir.trim() + File.separator + DATE_FORMAT_FILE_NAME.format(new Date()) + ".log");
		}
		else {
			try {
				logFile = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart()).getParent() + File.separator + DATE_FORMAT_FILE_NAME.format(new Date()) + ".log");
			}
			catch (URISyntaxException use) {
				/* Nella peggiore delle ipotesi, scrive nella directory del profilo dell'utente */
				logFile = new File(System.getProperty("user.home") + File.separator + DATE_FORMAT_FILE_NAME.format(new Date()) + ".csv");
			}
		}
		final BufferedWriter logFileWriter = new BufferedWriter(new FileWriter(logFile, true));
		final String base = new Date().toString() + " - ";
		logFileWriter.write(base);
		logFileWriter.write(StringUtils.trimToEmpty(text));
		logFileWriter.newLine();
		logFileWriter.close();
	}

}
