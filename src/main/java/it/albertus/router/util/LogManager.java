package it.albertus.router.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;

import it.albertus.router.RouterLogger;
import it.albertus.router.engine.RouterLoggerConfiguration.Defaults;

public class LogManager {

	public static final String LOG_FILE_EXTENSION = ".log";
	public static final String LOG_FILE_NAME = "%d" + LOG_FILE_EXTENSION;

	public static final String LOCK_FILE_EXTENSION = ".lck";

	private static final FilenameFilter logFilenameFilter = new FilenameFilter() {
		@Override
		public boolean accept(final File dir, final String name) {
			return name != null && name.toLowerCase().contains(LOG_FILE_EXTENSION) && !name.endsWith(LOCK_FILE_EXTENSION);
		}
	};

	private static final FilenameFilter lockFilenameFilter = new FilenameFilter() {
		@Override
		public boolean accept(final File dir, final String name) {
			return name != null && name.endsWith(LOCK_FILE_EXTENSION);
		}
	};

	private LogManager() {
		throw new IllegalAccessError();
	}

	public static File[] listFiles() {
		return new File(getLoggingPath()).listFiles(logFilenameFilter);
	}

	public static boolean deleteFile(final File file) {
		if (getLockedFiles().contains(file)) {
			return false;
		}
		else {
			return file.delete();
		}
	}

	public static Set<File> getLockedFiles() {
		final Set<File> lockedFiles = new HashSet<File>();
		final File[] lockFiles = new File(getLoggingPath()).listFiles(lockFilenameFilter);
		if (lockFiles != null) {
			for (final File lockFile : lockFiles) {
				final File lockedFile = new File(lockFile.getPath().replace(LOCK_FILE_EXTENSION, ""));
				if (lockedFile.exists()) {
					lockedFiles.add(lockedFile);
				}
			}
		}
		return lockedFiles;
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
