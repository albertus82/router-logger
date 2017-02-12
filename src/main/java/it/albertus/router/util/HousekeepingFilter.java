package it.albertus.router.util;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import it.albertus.router.resources.Messages;
import it.albertus.util.logging.LoggerFactory;
import it.albertus.util.logging.TimeBasedRollingFileHandler;

public class HousekeepingFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(HousekeepingFilter.class);

	private final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(TimeBasedRollingFileHandler.Defaults.DATE_PATTERN);
		}
	};

	private String currentFileNamePart;

	private final short keep;

	public HousekeepingFilter(final short keep) {
		this.keep = keep;
	}

	@Override
	public synchronized boolean isLoggable(final LogRecord record) {
		final String newFileNamePart = dateFormat.get().format(new Date());
		if (!newFileNamePart.equals(currentFileNamePart)) {
			currentFileNamePart = newFileNamePart;
			deleteOldLogs();
		}
		return true;
	}

	private void deleteOldLogs() {
		final File[] files = LogManager.listFiles();
		if (files.length > keep) {
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
