package it.albertus.router.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.jface.JFaceMessages;
import it.albertus.router.engine.Threshold.Type;
import it.albertus.router.resources.Messages;
import it.albertus.router.util.InitializationException;
import it.albertus.router.util.logging.EmailHandler;
import it.albertus.router.util.logging.EmailHandlerFilter;
import it.albertus.util.Configuration;
import it.albertus.util.IOUtils;
import it.albertus.util.StringUtils;
import it.albertus.util.Supplier;
import it.albertus.util.logging.CustomFormatter;
import it.albertus.util.logging.HousekeepingFilter;
import it.albertus.util.logging.LogFileManager;
import it.albertus.util.logging.LoggerFactory;
import it.albertus.util.logging.LoggingSupport;
import it.albertus.util.logging.TimeBasedRollingFileHandler;
import it.albertus.util.logging.TimeBasedRollingFileHandlerConfig;

public class RouterLoggerConfig extends Configuration {

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerConfig.class);

	public static class Defaults {
		public static final boolean LOGGING_FILES_ENABLED = true;
		public static final Level LOGGING_LEVEL = Level.INFO;
		public static final String LOGGING_FILES_PATH = getOsSpecificLocalAppDataDir() + File.separator + Messages.get("msg.application.name");
		public static final int LOGGING_FILES_LIMIT = 0;
		public static final int LOGGING_FILES_COUNT = 1;
		public static final boolean LOGGING_FILES_AUTOCLEAN_ENABLED = true;
		public static final short LOGGING_FILES_AUTOCLEAN_KEEP = 30;

		public static final boolean THRESHOLDS_SPLIT = false;
		public static final String GUI_IMPORTANT_KEYS_SEPARATOR = ",";
		public static final String CONSOLE_SHOW_KEYS_SEPARATOR = ",";
		public static final String THRESHOLDS_EXCLUDED_SEPARATOR = ",";

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	public static final String CFG_FILE_NAME = "routerlogger.cfg";

	public static final String LOG_FILE_DATE_PATTERN = "yyyyMMdd";
	public static final String LOG_FILE_NAME = "%d" + LogFileManager.DEFAULT_LOG_FILE_EXTENSION;

	private static final String MSG_KEY_ERR_THRESHOLD_MISCFG_NAME = "err.threshold.miscfg.name";
	private static final String MSG_KEY_ERR_CONFIGURATION_REVIEW = "err.configuration.review";

	private Thresholds thresholds;
	private final Set<String> guiImportantKeys = new LinkedHashSet<String>();
	private final Set<String> consoleKeysToShow = new LinkedHashSet<String>();

	private TimeBasedRollingFileHandler fileHandler;
	private EmailHandler emailHandler;

	private final LogFileManager logFileManager;

	private static RouterLoggerConfig instance;

	private RouterLoggerConfig() throws IOException {
		super(Messages.get("msg.application.name") + File.separator + CFG_FILE_NAME, true);
		this.logFileManager = new LogFileManager(new Supplier<String>() {
			@Override
			public String get() {
				return getString("logging.files.path", Defaults.LOGGING_FILES_PATH);
			}
		});
		init();
	}

	public static synchronized RouterLoggerConfig getInstance() throws InitializationException {
		if (instance == null) {
			try {
				instance = new RouterLoggerConfig();
			}
			catch (final IOException e) {
				final String message = Messages.get("err.open.cfg", CFG_FILE_NAME);
				logger.log(Level.SEVERE, message, e);
				throw new InitializationException(message, e);
			}
		}
		return instance;
	}

	public LogFileManager getLogFileManager() {
		return logFileManager;
	}

	public Set<String> getGuiImportantKeys() {
		return guiImportantKeys;
	}

	public Thresholds getThresholds() {
		return thresholds;
	}

	public Set<String> getConsoleKeysToShow() {
		return consoleKeysToShow;
	}

	private void init() {
		updateLanguage();
		updateLogging();

		// Caricamento chiavi da evidenziare
		guiImportantKeys.clear();
		for (final String importantKey : this.getString("gui.important.keys", true).split(this.getString("gui.important.keys.separator", Defaults.GUI_IMPORTANT_KEYS_SEPARATOR).trim())) {
			if (StringUtils.isNotBlank(importantKey)) {
				this.guiImportantKeys.add(importantKey.trim());
			}
		}
		consoleKeysToShow.clear();
		for (final String keyToShow : this.getString("console.show.keys", true).split(this.getString("console.show.keys.separator", Defaults.CONSOLE_SHOW_KEYS_SEPARATOR).trim())) {
			if (StringUtils.isNotBlank(keyToShow)) {
				this.consoleKeysToShow.add(keyToShow.trim());
			}
		}

		// Valorizzazione delle soglie...
		if (this.getBoolean("thresholds.split", Defaults.THRESHOLDS_SPLIT)) {
			thresholds = new SplitThresholds(); // Vecchio stile
		}
		else {
			thresholds = new ExpressionThresholds(); // Nuovo stile
		}
	}

	private void updateLanguage() {
		final String language = this.getString("language", Messages.Defaults.LANGUAGE);
		Messages.setLanguage(language);
		JFaceMessages.setLanguage(language);
	}

	private void updateLogging() {
		if (LoggingSupport.getInitialConfigurationProperty() == null) {
			updateLoggingLevel();

			if (this.getBoolean("logging.files.enabled", Defaults.LOGGING_FILES_ENABLED)) {
				enableLoggingFileHandler();
			}
			else {
				disableLoggingFileHandler();
			}

			updateLoggingEmailHandler();
		}
	}

	private void updateLoggingLevel() {
		try {
			LoggingSupport.setRootLevel(Level.parse(this.getString("logging.level", Defaults.LOGGING_LEVEL.getName())));
		}
		catch (final IllegalArgumentException e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
	}

	private void enableLoggingFileHandler() {
		final String loggingPath = getString("logging.files.path", Defaults.LOGGING_FILES_PATH);
		if (loggingPath != null && !loggingPath.isEmpty()) {
			final TimeBasedRollingFileHandlerConfig newConfig = new TimeBasedRollingFileHandlerConfig();
			newConfig.setFileNamePattern(loggingPath + File.separator + LOG_FILE_NAME);
			newConfig.setLimit(getInt("logging.files.limit", Defaults.LOGGING_FILES_LIMIT) * 1024);
			newConfig.setCount(getInt("logging.files.count", Defaults.LOGGING_FILES_COUNT));
			newConfig.setAppend(true);
			newConfig.setDatePattern(LOG_FILE_DATE_PATTERN);
			newConfig.setFormatter(new CustomFormatter("%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%tL %4$s %3$s - %5$s%6$s%n"));

			if (getBoolean("logging.files.autoclean.enabled", Defaults.LOGGING_FILES_AUTOCLEAN_ENABLED)) {
				final HousekeepingFilter hf = new HousekeepingFilter(logFileManager, getShort("logging.files.autoclean.keep", Defaults.LOGGING_FILES_AUTOCLEAN_KEEP), LOG_FILE_DATE_PATTERN);
				hf.addObserver(new Observer() {
					@Override
					public void update(final Observable o, final Object deletedFile) {
						LoggerFactory.getLogger(o.getClass()).log(Level.INFO, Messages.get("msg.logging.housekeeping.deleted"), deletedFile);
					}
				});
				newConfig.setFilter(hf);
			}

			if (fileHandler != null) {
				final TimeBasedRollingFileHandlerConfig oldConfig = TimeBasedRollingFileHandlerConfig.fromHandler(fileHandler);
				if (!oldConfig.getFileNamePattern().equals(newConfig.getFileNamePattern()) || oldConfig.getLimit() != newConfig.getLimit() || oldConfig.getCount() != newConfig.getCount() || !equals(oldConfig.getFilter(), newConfig.getFilter())) {
					logger.log(Level.FINE, "Logging configuration has changed; closing and removing old {0}...", fileHandler.getClass().getSimpleName());
					LoggingSupport.getRootLogger().removeHandler(fileHandler);
					fileHandler.close();
					fileHandler = null;
					logger.log(Level.FINE, "Old FileHandler closed and removed.");
				}
			}

			if (fileHandler == null) {
				logger.log(Level.FINE, "FileHandler not found; creating one...");
				try {
					new File(loggingPath).mkdirs();
					fileHandler = new TimeBasedRollingFileHandler(newConfig);
					LoggingSupport.getRootLogger().addHandler(fileHandler);
					logger.log(Level.FINE, "{0} created successfully.", fileHandler.getClass().getSimpleName());
				}
				catch (final IOException e) {
					logger.log(Level.SEVERE, e.toString(), e);
				}
			}
		}
	}

	private void disableLoggingFileHandler() {
		if (fileHandler != null) {
			LoggingSupport.getRootLogger().removeHandler(fileHandler);
			fileHandler.close();
			fileHandler = null;
			logger.log(Level.FINE, "FileHandler closed and removed.");
		}
	}

	private void updateLoggingEmailHandler() {
		if (emailHandler == null) {
			emailHandler = new EmailHandler();
			LoggingSupport.getRootLogger().addHandler(emailHandler);
		}
		final EmailHandlerFilter filter = new EmailHandlerFilter();
		filter.setEnabled(this.getBoolean("logging.email.enabled", EmailHandler.Defaults.ENABLED));
		try {
			final Level level = Level.parse(this.getString("logging.email.level", EmailHandler.Defaults.LEVEL.getName()));
			if (level.intValue() >= EmailHandler.MIN_LEVEL.intValue() && level.intValue() <= EmailHandler.MAX_LEVEL.intValue()) {
				filter.setLevel(level);
			}
			else {
				logger.log(Level.WARNING, Messages.get("err.log.email.level"), new Level[] { EmailHandler.MIN_LEVEL, EmailHandler.MAX_LEVEL });
			}
		}
		catch (final IllegalArgumentException e) {
			logger.log(Level.WARNING, Messages.get("err.log.email.level", EmailHandler.MIN_LEVEL, EmailHandler.MAX_LEVEL), e);
		}
		emailHandler.setFilter(filter);
	}

	@Override
	public void reload() throws IOException {
		super.reload();
		init();
	}

	public void save(final Properties properties) throws IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(getFileName());
			properties.store(writer, null);
		}
		finally {
			IOUtils.closeQuietly(writer);
		}
	}

	public abstract class Thresholds {

		public static final String CFG_PREFIX = "threshold";

		protected final Collection<Threshold> thresholdsCollection = new TreeSet<Threshold>();

		private Thresholds() {
			try {
				load();
			}
			catch (final IllegalThresholdException ite) {
				throw ite;
			}
			catch (final RuntimeException re) {
				throw new IllegalThresholdException(Messages.get("err.threshold.miscfg") + ' ' + JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_REVIEW, RouterLoggerConfig.this.getFileName()), re);
			}
		}

		protected abstract void load();

		protected boolean isThresholdExcluded(final String thresholdName) {
			for (final String name : getString("thresholds.excluded", true).split(getString("thresholds.excluded.separator", Defaults.THRESHOLDS_EXCLUDED_SEPARATOR).trim())) {
				if (StringUtils.isNotBlank(name) && name.trim().equals(thresholdName)) {
					return true;
				}
			}
			return false;
		}

		public boolean isEmpty() {
			return thresholdsCollection.isEmpty();
		}

		@Override
		public String toString() {
			return thresholdsCollection.toString();
		}

		public Map<Threshold, String> getReached(final RouterData data) {
			final Map<String, String> info = data.getData();
			final Map<Threshold, String> reached = new TreeMap<Threshold, String>();

			// Gestione delle soglie...
			if (!thresholdsCollection.isEmpty() && info != null && !info.isEmpty()) {
				for (final Entry<String, String> entry : info.entrySet()) {
					final String key = entry.getKey();
					final String value = entry.getValue();
					if (key != null && key.trim().length() != 0) {
						for (final Threshold threshold : thresholdsCollection) {
							if (key.trim().equals(threshold.getKey()) && threshold.isReached(value)) {
								reached.put(threshold, value);
							}
						}
					}
				}
			}
			return reached;
		}

	}

	// Soglie vecchio stile (tre proprieta')
	private class SplitThresholds extends Thresholds {

		private static final String CFG_SUFFIX_KEY = "key";
		private static final String CFG_SUFFIX_TYPE = "type";
		private static final String CFG_SUFFIX_VALUE = "value";

		@Override
		protected void load() {
			final RouterLoggerConfig configuration = RouterLoggerConfig.this;
			final Set<String> thresholdsAdded = new HashSet<String>();
			for (Object objectKey : configuration.getProperties().keySet()) {
				String key = (String) objectKey;
				if (key != null && key.startsWith(CFG_PREFIX + '.')) {
					if (key.indexOf('.') == key.lastIndexOf('.') || "".equals(key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'))) || (!key.endsWith(CFG_SUFFIX_KEY) && !key.endsWith(CFG_SUFFIX_TYPE) && !key.endsWith(CFG_SUFFIX_VALUE))) {
						final IllegalThresholdException e = new IllegalThresholdException(Messages.get("err.threshold.miscfg") + ' ' + JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_REVIEW, configuration.getFileName()));
						logger.log(Level.WARNING, e.toString(), e);
						continue;
					}
					final String thresholdName = key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'));
					if (thresholdsAdded.contains(thresholdName)) {
						continue;
					}
					final String thresholdKey = configuration.getString(CFG_PREFIX + '.' + thresholdName + '.' + CFG_SUFFIX_KEY);
					final Type thresholdType = Type.getEnum(configuration.getString(CFG_PREFIX + '.' + thresholdName + '.' + CFG_SUFFIX_TYPE));
					final String thresholdValue = configuration.getString(CFG_PREFIX + '.' + thresholdName + '.' + CFG_SUFFIX_VALUE);
					if (thresholdKey == null || "".equals(thresholdKey.trim()) || thresholdValue == null || thresholdType == null) {
						logger.info(Messages.get(MSG_KEY_ERR_THRESHOLD_MISCFG_NAME, thresholdName) + ' ' + JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_REVIEW, configuration.getFileName()));
						continue;
					}
					thresholdsCollection.add(new Threshold(thresholdName, thresholdKey.trim(), thresholdType, thresholdValue, isThresholdExcluded(thresholdName)));
					thresholdsAdded.add(thresholdName);
				}
			}
		}

	}

	// Nuove soglie con espressione
	private class ExpressionThresholds extends Thresholds {

		@Override
		protected void load() {
			final RouterLoggerConfig configuration = RouterLoggerConfig.this;
			for (Object objectKey : configuration.getProperties().keySet()) {
				String key = (String) objectKey;
				if (key != null && key.startsWith(CFG_PREFIX + '.')) {
					final String thresholdName = key.substring(key.indexOf('.') + 1);
					final String expression = configuration.getString(key);
					String[] tokens = expression.split("\\s");
					String operator = null;
					Type thresholdType = null;
					for (int i = 1; i < tokens.length; i++) {
						operator = tokens[i];
						thresholdType = Type.getEnum(operator);
						if (thresholdType != null) {
							break; // Operatore trovato
						}
					}
					if (thresholdType == null) {
						logger.info(Messages.get(MSG_KEY_ERR_THRESHOLD_MISCFG_NAME, thresholdName) + ' ' + JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_REVIEW, configuration.getFileName()));
						continue;
					}
					final String thresholdKey = expression.substring(0, expression.indexOf(operator) - 1);
					final String thresholdValue = expression.substring(expression.indexOf(operator) + operator.length() + 1);
					if (thresholdKey == null || "".equals(thresholdKey.trim()) || thresholdValue == null) {
						logger.info(Messages.get(MSG_KEY_ERR_THRESHOLD_MISCFG_NAME, thresholdName) + ' ' + JFaceMessages.get(MSG_KEY_ERR_CONFIGURATION_REVIEW, configuration.getFileName()));
						continue;
					}
					thresholdsCollection.add(new Threshold(thresholdName, thresholdKey.trim(), thresholdType, thresholdValue, isThresholdExcluded(thresholdName)));
				}
			}
		}

	}

	private class IllegalThresholdException extends RuntimeException {

		private static final long serialVersionUID = -8617343007639969676L;

		private IllegalThresholdException(String message) {
			super(message);
		}

		private IllegalThresholdException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	private static boolean equals(final Object a, final Object b) {
		return (a == b) || (a != null && a.equals(b));
	}

//	public static void main(String... args) throws IOException {
//		Properties p = new Properties();
//		InputStream r = RouterLoggerConfiguration.class.getResourceAsStream("/routerlogger.cfg");
//		p.load(r);
//		r.close();
//		System.out.println("Presenti in routerlogger.cfg e non presenti in Preference:");
//		outer: for (String key : p.stringPropertyNames()) {
//			for (Preference preference : Preference.values()) {
//				if (preference.getConfigurationKey().equals(key)) {
//					continue outer;
//				}
//			}
//			System.out.println(key);
//		}
//		System.out.println();
//		System.out.println("Presenti in Preference e non presenti in routerlogger.cfg:");
//		outer: for (Preference preference : Preference.values()) {
//			for (String key : p.stringPropertyNames()) {
//				if (preference.getConfigurationKey().equals(key)) {
//					continue outer;
//				}
//			}
//			System.out.println(preference.getConfigurationKey());
//		}
//	}

}
