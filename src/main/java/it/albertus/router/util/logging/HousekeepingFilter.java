package it.albertus.router.util.logging;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import it.albertus.router.resources.Messages;
import it.albertus.router.util.FileSorter;
import it.albertus.util.logging.LoggerFactory;
import it.albertus.util.logging.TimeBasedRollingFileHandler;

public class HousekeepingFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(HousekeepingFilter.class);

	private static final int MIN_HISTORY = 1;

	private final int maxHistory;
	private DateFormat dateFormat;

	private String currentFileNamePart;

	public HousekeepingFilter(final int maxHistory) {
		this(maxHistory, TimeBasedRollingFileHandler.Defaults.DATE_PATTERN);
	}

	public HousekeepingFilter(final int maxHistory, final String datePattern) {
		if (maxHistory < MIN_HISTORY) {
			logger.warning(Messages.get("err.log.housekeeping.maxHistory", MIN_HISTORY));
			this.maxHistory = MIN_HISTORY;
		}
		else {
			this.maxHistory = maxHistory;
		}
		try {
			this.dateFormat = new SimpleDateFormat(datePattern);
		}
		catch (final RuntimeException e) {
			final String defaultDatePattern = TimeBasedRollingFileHandler.Defaults.DATE_PATTERN;
			logger.log(Level.WARNING, Messages.get("err.log.housekeeping.datePattern", datePattern, defaultDatePattern), e);
			this.dateFormat = new SimpleDateFormat(defaultDatePattern);
		}
	}

	@Override
	public synchronized boolean isLoggable(final LogRecord record) {
		final String newFileNamePart = dateFormat.format(new Date());
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
				final boolean deleted = LogManager.deleteFile(files[i]);
				if (deleted) {
					logger.info(Messages.get("msg.log.file.deleted", files[i]));
				}
			}
		}
	}

}
