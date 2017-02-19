package it.albertus.router.util.logging;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import it.albertus.router.RouterLogger;
import it.albertus.router.email.EmailSender;
import it.albertus.router.resources.Messages;
import it.albertus.util.logging.CustomFormatter;

public class EmailHandler extends Handler {

	public static class Defaults {
		public static final boolean ENABLED = false;
		public static final boolean IGNORE_DUPLICATES = true;
		public static final Level LEVEL = Level.WARNING;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public static final Level MIN_LEVEL = Level.INFO;
	public static final Level MAX_LEVEL = Level.SEVERE;

	private Throwable lastThrownSent;

	private boolean closed = false;

	public EmailHandler() {
		setFormatter(new CustomFormatter("%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%tL %4$s %3$s - %5$s%6$s"));
		super.setFilter(new EmailHandlerFilter());
	}

	@Override
	public boolean isLoggable(final LogRecord record) {
		if (record != null && CustomLevel.EMAIL.equals(record.getLevel())) {
			return true; // bypass other checks
		}
		else {
			return super.isLoggable(record);
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

	@Override
	public EmailHandlerFilter getFilter() {
		return (EmailHandlerFilter) super.getFilter();
	}

	/**
	 * @deprecated This method does nothing.
	 */
	@Override
	@Deprecated
	public void setFilter(final Filter newFilter) {/* Ignore */}

}
