package it.albertus.routerlogger.util.logging;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class EmailHandlerFilter implements Filter {

	private Level level = EmailHandler.Defaults.LEVEL;
	private boolean enabled = EmailHandler.Defaults.ENABLED;

	@Override
	public boolean isLoggable(final LogRecord record) {
		return enabled && record.getLevel().intValue() >= level.intValue();
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
