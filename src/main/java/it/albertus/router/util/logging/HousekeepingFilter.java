package it.albertus.router.util.logging;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

import it.albertus.router.util.FileSorter;
import it.albertus.util.logging.TimeBasedRollingFileHandler;

public class HousekeepingFilter implements Filter {

	private static final int MIN_HISTORY = 1;

	private final int maxHistory;
	private final String datePattern;

	private String currentFileNamePart;

	private final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(datePattern);
		}
	};

	public HousekeepingFilter(final int maxHistory) {
		this(maxHistory, TimeBasedRollingFileHandler.Defaults.DATE_PATTERN);
	}

	public HousekeepingFilter(final int maxHistory, final String datePattern) {
		if (maxHistory < MIN_HISTORY) {
			this.maxHistory = MIN_HISTORY;
		}
		else {
			this.maxHistory = maxHistory;
		}
		this.datePattern = datePattern;
	}

	@Override
	public synchronized boolean isLoggable(final LogRecord record) {
		final String newFileNamePart = dateFormat.get().format(new Date());
		if (!newFileNamePart.equals(currentFileNamePart)) {
			int keep = this.maxHistory;
			if (currentFileNamePart == null) {
				keep++;
			}
			currentFileNamePart = newFileNamePart;
			deleteOldLogs(keep);
		}
		return true;
	}

	private void deleteOldLogs(final int keep) {
		final File[] files = LogManager.listFiles();
		if (files != null && files.length > keep) {
			FileSorter.sortByLastModified(files);
			for (int i = 0; i < files.length - keep; i++) {
				LogManager.deleteFile(files[i]);
			}
		}
	}

	@Override
	public String toString() {
		return "HousekeepingFilter [maxHistory=" + maxHistory + ", datePattern=" + datePattern + "]";
	}

}
