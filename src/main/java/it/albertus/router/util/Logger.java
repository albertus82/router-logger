package it.albertus.router.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import it.albertus.router.email.EmailSender;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.Console;
import it.albertus.util.ExceptionUtils;
import it.albertus.util.StringUtils;
import it.albertus.util.SystemConsole;

public class Logger {

	private static final String FILE_EXTENSION = ".log";
	private static final Destination[] DEFAULT_DESTINATIONS = { Destination.CONSOLE, Destination.FILE, Destination.EMAIL };

	private static final DateFormat dateFormatFileName = new SimpleDateFormat("yyyyMMdd");
	private static final DateFormat timestampFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	private static synchronized String formatTimestamp(final Date timestamp) {
		return timestampFormat.format(timestamp);
	}

	private static class Singleton {
		private static final Logger instance = new Logger();
	}

	public static Logger getInstance() {
		return Singleton.instance;
	}

	private Logger() {
		Configuration configuration;
		try {
			configuration = RouterLoggerConfiguration.getInstance();
		}
		catch (final Throwable t) {
			t.printStackTrace();
			configuration = null;
		}
		this.configuration = configuration;
	}

	public enum Destination {
		CONSOLE,
		FILE,
		EMAIL;
	}

	public interface Defaults {
		boolean DEBUG = false;
		String DIRECTORY = getDefaultDirectory();
		boolean EMAIL = false;
		boolean EMAIL_IGNORE_DUPLICATES = true;
	}

	private final Configuration configuration;
	private String lastEmailLog;
	private Console out = SystemConsole.getInstance();

	public boolean isDebugEnabled() {
		return configuration != null ? configuration.getBoolean("console.debug", Defaults.DEBUG) : true;
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

		if (dest.contains(Destination.EMAIL)) {
			try {
				logToEmail(text, null);
			}
			catch (Exception e) {
				log(e, Destination.CONSOLE);
			}
		}
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

		if (dest.contains(Destination.EMAIL)) {
			try {
				logToEmail(longLog, throwable);
			}
			catch (Exception e) {
				log(e, Destination.CONSOLE);
			}
		}
	}

	private void logToConsole(final String text) {
		final String base = formatTimestamp(new Date()) + ' ';
		out.println(base + StringUtils.trimToEmpty(text), true);
	}

	private void logToFile(final String text) throws IOException {
		final String logDestinationDir = configuration != null ? configuration.getString("logger.error.log.destination.path") : null;
		final File logFile;
		if (logDestinationDir != null && !logDestinationDir.trim().isEmpty()) {
			logFile = new File(logDestinationDir.trim() + File.separator + dateFormatFileName.format(new Date()) + FILE_EXTENSION);
		}
		else {
			logFile = getDefaultFile();
		}
		final File parentFile = logFile.getParentFile();
		if (parentFile != null && !parentFile.exists()) {
			parentFile.mkdirs(); // Create directories if not exists
		}
		final BufferedWriter logFileWriter = new BufferedWriter(new FileWriter(logFile, true));
		final String base = new Date().toString() + " - ";
		logFileWriter.write(base);
		logFileWriter.write(StringUtils.trimToEmpty(text));
		logFileWriter.newLine();
		logFileWriter.close();
	}

	private void logToEmail(final String log, final Throwable throwable) {
		if (configuration != null && configuration.getBoolean("log.email", Defaults.EMAIL)) {
			if (throwable == null || lastEmailLog == null || !configuration.getBoolean("log.email.ignore.duplicates", Defaults.EMAIL_IGNORE_DUPLICATES) || !lastEmailLog.equals(log)) {
				final String subjectKey;
				if (throwable != null) {
					subjectKey = "msg.log.email.subject.exception";
				}
				else {
					subjectKey = "msg.log.email.subject.event";
				}
				final String subject = Messages.get(subjectKey, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(new Date()));
				EmailSender.getInstance().reserve(subject, log);
				lastEmailLog = log;
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

	private static File getDefaultFile() {
		return new File(getDefaultDirectory() + File.separator + dateFormatFileName.format(new Date()) + FILE_EXTENSION);
	}

	private static String getDefaultDirectory() {
		return Configuration.getOsSpecificDocumentsDir() + File.separator + Messages.get("msg.application.name");
	}

}
