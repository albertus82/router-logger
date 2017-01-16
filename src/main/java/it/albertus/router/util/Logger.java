package it.albertus.router.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
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
import it.albertus.util.IOUtils;
import it.albertus.util.StringUtils;
import it.albertus.util.SystemConsole;

public class Logger {

	public static class Defaults {
		public static final boolean DEBUG = false;
		public static final String DIRECTORY = getDefaultDirectory();
		public static final boolean EMAIL = false;
		public static final boolean EMAIL_IGNORE_DUPLICATES = true;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private static class Singleton {
		private static final Logger instance = new Logger();

		private Singleton() {
			throw new IllegalAccessError();
		}
	}

	public static final String FILE_EXTENSION = ".log";

	private static final Destination[] DEFAULT_DESTINATIONS = { Destination.CONSOLE, Destination.FILE, Destination.EMAIL };

	private static final ThreadLocal<DateFormat> dateFormatFileName = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyyMMdd");
		}
	};

	private static final ThreadLocal<DateFormat> timestampFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		}
	};

	private static final FilenameFilter filenameFilter = new FilenameFilter() {
		@Override
		public boolean accept(final File dir, final String name) {
			return name != null && name.trim().toLowerCase().endsWith(FILE_EXTENSION);
		}
	};

	private String lastEmailLog;
	private Console out = SystemConsole.getInstance();

	private Logger() {}

	private Configuration getConfiguration() {
		try {
			return RouterLoggerConfiguration.getInstance();
		}
		catch (final RuntimeException re) {
			error(re);
			return null;
		}
	}

	static Logger getInstance() {
		return Singleton.instance;
	}

	public enum Destination {
		CONSOLE,
		FILE,
		EMAIL;
	}

	public boolean isDebugEnabled() {
		final Configuration configuration = getConfiguration();
		return configuration != null ? configuration.getBoolean("debug", Defaults.DEBUG) : true;
	}

	public void debug(final Throwable throwable) {
		if (isDebugEnabled()) {
			error(throwable, Destination.CONSOLE, Destination.FILE);
		}
	}

	public void debug(final String message) {
		if (isDebugEnabled()) {
			info(message, Destination.CONSOLE, Destination.FILE);
		}
	}

	public void info(final String message, final Destination... destinations) {
		final Set<Destination> dest = getDestinations(destinations);

		if (dest.contains(Destination.CONSOLE)) {
			logToConsole(message);
		}

		if (dest.contains(Destination.FILE)) {
			try {
				logToFile(message);
			}
			catch (final Exception e) {
				error(e, Destination.CONSOLE);
			}
		}

		if (dest.contains(Destination.EMAIL)) {
			try {
				logToEmail(message, null);
			}
			catch (final Exception e) {
				error(e, Destination.CONSOLE);
			}
		}
	}

	public void error(final Throwable throwable, final Destination... destinations) {
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
			catch (final Exception e) {
				error(e, Destination.CONSOLE);
			}
		}

		if (dest.contains(Destination.EMAIL)) {
			try {
				logToEmail(longLog, throwable);
			}
			catch (final Exception e) {
				error(e, Destination.CONSOLE);
			}
		}
	}

	private void logToConsole(final String text) {
		final String base = timestampFormat.get().format(new Date()) + ' ';
		out.println(base + StringUtils.trimToEmpty(text), true);
	}

	private synchronized void logToFile(final String text) throws IOException {
		final File logFile = getCurrentFile();
		final File parentFile = logFile.getParentFile();
		if (parentFile != null && !parentFile.exists()) {
			parentFile.mkdirs(); // Create directories if not exists
		}
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(logFile, true);
			bw = new BufferedWriter(fw);
			final String base = new Date().toString() + " - ";
			bw.write(base);
			bw.write(StringUtils.trimToEmpty(text));
			bw.newLine();
		}
		finally {
			IOUtils.closeQuietly(bw, fw);
		}
	}

	public File[] listFiles() {
		return getCurrentFile().getParentFile().listFiles(filenameFilter);
	}

	public boolean deleteFile(final File file) {
		if (getCurrentFile().equals(file)) {
			synchronized (this) {
				return file.delete();
			}
		}
		else {
			return file.delete();
		}
	}

	public int deleteAllFiles() {
		int count = 0;
		for (final File file : listFiles()) {
			if (!file.isDirectory()) {
				count += deleteFile(file) ? 1 : 0;
			}
		}
		return count;
	}

	public File getCurrentFile() {
		final Configuration configuration = getConfiguration();
		final String logDestinationDir = configuration != null ? configuration.getString("logger.error.log.destination.path") : null;
		final File logFile;
		if (logDestinationDir != null && !logDestinationDir.trim().isEmpty()) {
			logFile = new File(logDestinationDir.trim() + File.separator + dateFormatFileName.get().format(new Date()) + FILE_EXTENSION);
		}
		else {
			logFile = getDefaultFile();
		}
		return logFile;
	}

	private void logToEmail(final String log, final Throwable throwable) {
		final Configuration configuration = getConfiguration();
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
		return new File(getDefaultDirectory() + File.separator + dateFormatFileName.get().format(new Date()) + FILE_EXTENSION);
	}

	private static String getDefaultDirectory() {
		return Configuration.getOsSpecificDocumentsDir() + File.separator + Messages.get("msg.application.name");
	}

}
