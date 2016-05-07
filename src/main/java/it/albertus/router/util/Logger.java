package it.albertus.router.util;

import it.albertus.router.email.EmailSender;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Logger {

	private static final String FILE_EXTENSION = ".log";
	private static final Destination[] DEFAULT_DESTINATIONS = { Destination.CONSOLE, Destination.FILE, Destination.EMAIL };

	public static final DateFormat dateFormatFileName = new SimpleDateFormat("yyyyMMdd");
	public static final DateFormat dateFormatLog = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public enum Destination {
		CONSOLE,
		FILE,
		EMAIL;
	}

	protected class LogEmailRunnable implements Runnable {

		protected final String log;
		protected final Date date;

		protected LogEmailRunnable(final String log, final Date date) {
			this.log = log;
			this.date = date;
		}

		@Override
		public void run() {
			try {
				final String subject = Resources.get("msg.log.email.subject", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Resources.getLanguage().getLocale()).format(date));
				emailSender.reserve(subject, log);
			}
			catch (final Exception exception) {
				log(exception, Destination.CONSOLE);
			}
		}
	}

	public interface Defaults {
		boolean DEBUG = false;
		String DIRECTORY = getDefaultDirectory();
		boolean EMAIL = false;
	}

	// Lazy initialization...
	private static class Singleton {
		private static final Logger instance = new Logger();
	}

	public static Logger getInstance() {
		return Singleton.instance;
	}

	public void init(final Console console) {
		this.out = console;
	}

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();
	private final EmailSender emailSender = EmailSender.getInstance();
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

		if (dest.contains(Destination.EMAIL)) {
			try {
				logToEmail(text);
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
				logToEmail(longLog);
			}
			catch (Exception e) {
				log(e, Destination.CONSOLE);
			}
		}
	}

	private void logToConsole(final String text) {
		final String base = dateFormatLog.format(new Date()) + ' ';
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
			logFile = new File(logDestinationDir.trim() + File.separator + dateFormatFileName.format(new Date()) + FILE_EXTENSION);
		}
		else {
			logFile = getDefaultFile();
		}
		final BufferedWriter logFileWriter = new BufferedWriter(new FileWriter(logFile, true));
		final String base = new Date().toString() + " - ";
		logFileWriter.write(base);
		logFileWriter.write(StringUtils.trimToEmpty(text));
		logFileWriter.newLine();
		logFileWriter.close();
	}

	private void logToEmail(final String log) {
		if (configuration.getBoolean("log.email", Defaults.EMAIL)) {
			new Thread(new LogEmailRunnable(log, new Date()), "logEmailThread").start();
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
		File logFile;
		try {
			logFile = new File(new File(Logger.class.getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart()).getParent() + File.separator + dateFormatFileName.format(new Date()) + FILE_EXTENSION);
		}
		catch (final Exception e1) {
			try {
				// In caso di problemi, scrive nella directory del profilo dell'utente
				logFile = new File(System.getProperty("user.home").toString() + File.separator + dateFormatFileName.format(new Date()) + FILE_EXTENSION);
			}
			catch (final Exception e2) {
				// Nella peggiore delle ipotesi, scrive nella directory corrente
				logFile = new File(dateFormatFileName.format(new Date()) + FILE_EXTENSION);
			}
		}
		return logFile;
	}

	private static String getDefaultDirectory() {
		String directory;
		try {
			directory = getDefaultFile().getParentFile().getCanonicalPath();
		}
		catch (Exception e1) {
			try {
				directory = getDefaultFile().getParentFile().getAbsolutePath();
			}
			catch (Exception e2) {
				directory = getDefaultFile().getParentFile().getPath();
			}
		}
		return directory;
	}

}
