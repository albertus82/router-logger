package it.albertus.router.util.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import it.albertus.util.logging.LoggerNameFilter;
import it.albertus.util.logging.annotation.FilterExclusions;

@FilterExclusions(names = { "javax.mail", "it.albertus.router.email", "it.albertus.router.util.logging" })
public class EmailHandlerFilter extends LoggerNameFilter {

	private Level level = EmailHandler.Defaults.LEVEL;
	private boolean enabled = EmailHandler.Defaults.ENABLED;

	@Override
	public boolean isLoggable(final LogRecord record) {
		return enabled && record.getLevel().intValue() >= level.intValue() && super.isLoggable(record);
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
