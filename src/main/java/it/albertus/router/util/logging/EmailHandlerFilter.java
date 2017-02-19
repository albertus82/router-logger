package it.albertus.router.util.logging;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import it.albertus.router.email.EmailSender;
import it.albertus.router.email.ThresholdsEmailSender;
import it.albertus.util.logging.LoggingSupport;

public class EmailHandlerFilter implements Filter {

	private static final Set<String> exclusions;

	static {
		exclusions = new HashSet<String>();
		exclusions.add(LoggingSupport.getLoggerName(EmailSender.class));
		exclusions.add(LoggingSupport.getLoggerName(EmailHandlerFilter.class));
		exclusions.add(LoggingSupport.getLoggerName(ThresholdsEmailSender.class));
		exclusions.add("javax.mail");
	}

	private Level level = EmailHandler.Defaults.LEVEL;
	private boolean enabled = EmailHandler.Defaults.ENABLED;

	@Override
	public boolean isLoggable(final LogRecord record) {
		return enabled && !exclusions.contains(record.getLoggerName()) && record.getLevel().intValue() >= level.intValue();
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(final Level level) {
		this.level = level;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

}
