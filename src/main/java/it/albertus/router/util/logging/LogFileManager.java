package it.albertus.router.util.logging;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerConfiguration.Defaults;
import it.albertus.util.logging.AbstractLogFileManager;

public class LogFileManager extends AbstractLogFileManager {

	public static final String LOG_FILE_NAME = "%d" + LOG_FILE_EXTENSION;

	private LogFileManager() {}

	private static class Singleton {
		private static final LogFileManager instance = new LogFileManager();

		private Singleton() {
			throw new IllegalAccessError();
		}
	}

	public static LogFileManager getInstance() {
		return Singleton.instance;
	}

	@Override
	public String getPath() {
		return RouterLoggerConfiguration.getInstance().getString("logging.files.path", Defaults.LOGGING_FILES_PATH);
	}

}
