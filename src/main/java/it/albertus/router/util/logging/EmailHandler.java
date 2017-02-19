package it.albertus.router.util.logging;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import it.albertus.router.RouterLogger;
import it.albertus.router.email.EmailSender;
import it.albertus.router.email.ThresholdsEmailSender;
import it.albertus.router.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.logging.CustomFormatter;
import it.albertus.util.logging.LoggingSupport;

public class EmailHandler extends Handler {

	public static class Defaults {
		public static final boolean ENABLED = false;
		public static final boolean IGNORE_DUPLICATES = true;
		public static final Level LEVEL = Level.WARNING;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private static final Set<String> exclusions;

	static {
		exclusions = new HashSet<String>();
		exclusions.add(LoggingSupport.getLoggerName(EmailSender.class));
		exclusions.add(LoggingSupport.getLoggerName(ThresholdsEmailSender.class));
	}

	private Throwable lastThrownSent;

	private boolean closed = false;

	public EmailHandler() {
		setFormatter(new CustomFormatter("%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%tL %4$s %3$s - %5$s%6$s"));
	}

	@Override
	public boolean isLoggable(final LogRecord record) {
		final boolean loggable = super.isLoggable(record);
		if (CustomLevel.EMAIL.equals(record.getLevel())) {
			return loggable; // bypass other checks
		}
		else {
			final Configuration configuration = RouterLogger.getConfiguration();
			return loggable && !exclusions.contains(record.getLoggerName()) && configuration != null && configuration.getBoolean("logging.email.enabled", Defaults.ENABLED) && record.getLevel().intValue() >= Level.parse(configuration.getString("logging.email.level", Defaults.LEVEL.getName())).intValue();
		}
	}

	@Override
	public void publish(final LogRecord record) {
		if (closed || !isLoggable(record)) {
			return;
		}

		final String log;
		try {
			log = getFormatter().format(record);
		}
		catch (final Exception e) {
			reportError(null, e, ErrorManager.FORMAT_FAILURE);
			return;
		}

		try {
			final Throwable thrown = record.getThrown();
			if (thrown == null || lastThrownSent == null || !RouterLogger.getConfiguration().getBoolean("logging.email.ignore.duplicates", Defaults.IGNORE_DUPLICATES) || !Arrays.equals(lastThrownSent.getStackTrace(), thrown.getStackTrace())) {
				final String subjectKey;
				if (thrown != null) {
					subjectKey = "msg.log.email.subject.exception";
				}
				else {
					subjectKey = "msg.log.email.subject.event";
				}
				final String subject = Messages.get(subjectKey, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Messages.getLanguage().getLocale()).format(new Date()));
				EmailSender.getInstance().reserve(subject, log);
				lastThrownSent = thrown;
			}
		}
		catch (final Exception e) {
			reportError(null, e, ErrorManager.WRITE_FAILURE);
		}
	}

	@Override
	public void flush() {/* Ignore */}

	@Override
	public void close() {
		closed = true;
	}

}
