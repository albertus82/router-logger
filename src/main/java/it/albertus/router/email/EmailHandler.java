package it.albertus.router.email;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import it.albertus.router.RouterLogger;
import it.albertus.router.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.logging.CustomFormatter;

public class EmailHandler extends Handler {

	public static class Defaults {
		public static final boolean EMAIL = false;
		public static final boolean EMAIL_IGNORE_DUPLICATES = true;
		public static final Level EMAIL_LEVEL = Level.WARNING;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private Throwable lastThrownSent;

	private boolean closed = false;

	public EmailHandler() {
		setFormatter(new CustomFormatter("%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%tL %4$s %3$s - %5$s%6$s"));
		setFilter(new Filter() {
			@Override
			public boolean isLoggable(LogRecord record) {
				return RouterLogger.getConfiguration() != null ? record.getLevel().intValue() >= RouterLogger.getConfiguration().getInt("logging.email.level", Defaults.EMAIL_LEVEL.intValue()) : false;
			}
		});
	}

	@Override
	public void publish(final LogRecord record) {
		if (closed || !isLoggable(record)) {
			return;
		}

		final Configuration configuration = RouterLogger.getConfiguration();
		if (configuration != null && configuration.getBoolean("logging.email.enabled", Defaults.EMAIL)) {
			String log;
			try {
				log = getFormatter().format(record);
			}
			catch (final Exception e) {
				reportError(null, e, ErrorManager.FORMAT_FAILURE);
				return;
			}

			final Throwable thrown = record.getThrown();
			if (thrown == null || lastThrownSent == null || !configuration.getBoolean("logging.email.ignore.duplicates", Defaults.EMAIL_IGNORE_DUPLICATES) || !Arrays.equals(lastThrownSent.getStackTrace(), thrown.getStackTrace())) {
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
	}

	@Override
	public void flush() {/* Ignore */}

	@Override
	public void close() {
		closed = true;
	}

}
