package it.albertus.router.util;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.albertus.router.RouterLogger;
import it.albertus.router.engine.RouterLoggerConfiguration.Defaults;
import it.albertus.util.logging.DailyRollingFileHandler;

public class LogManager {

	public static final String FILE_EXTENSION = ".log";
	public static final String FILE_NAME = "%d" + FILE_EXTENSION;

	private static final ThreadLocal<DateFormat> dateFormatFileName = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(DailyRollingFileHandler.Defaults.DATE_PATTERN);
		}
	};

	private static final FilenameFilter filenameFilter = new FilenameFilter() {
		@Override
		public boolean accept(final File dir, final String name) {
			return name != null && name.toLowerCase().contains(FILE_EXTENSION) && !name.contains("lck");
		}
	};

	private LogManager() {
		throw new IllegalAccessError();
	}

	public static File[] listFiles() {
		final String loggingPath = getLoggingPath();
		return new File(loggingPath).listFiles(filenameFilter);
	}

	public static boolean deleteFile(final File file) {
		if (getCurrentFile().equals(file)) {
			return false;
		}
		else {
			return file.delete();
		}
	}

	public static File getCurrentFile() {
		return new File(getLoggingPath() + File.separator + FILE_NAME.replace("%d", dateFormatFileName.get().format(new Date())));
	}

	public static String getLoggingPath() {
		return RouterLogger.getConfiguration().getString("logging.files.path", Defaults.LOGGING_FILES_PATH);
	}

	public static int deleteAllFiles() {
		int count = 0;
		for (final File file : listFiles()) {
			if (!file.isDirectory()) {
				count += deleteFile(file) ? 1 : 0;
			}
		}
		return count;
	}

}
