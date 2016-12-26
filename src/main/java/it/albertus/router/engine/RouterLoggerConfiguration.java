package it.albertus.router.engine;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import it.albertus.jface.JFaceMessages;
import it.albertus.router.engine.Threshold.Type;
import it.albertus.router.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.StringUtils;

public class RouterLoggerConfiguration extends Configuration {

	public interface Defaults {
		String LANGUAGE = Locale.getDefault().getLanguage();
		boolean THRESHOLDS_SPLIT = false;
		String GUI_IMPORTANT_KEYS_SEPARATOR = ",";
		String CONSOLE_SHOW_KEYS_SEPARATOR = ",";
		String THRESHOLDS_EXCLUDED_SEPARATOR = ",";
	}

	private static class Singleton {
		private static final RouterLoggerConfiguration instance = new RouterLoggerConfiguration();
	}

	public static RouterLoggerConfiguration getInstance() {
		return Singleton.instance;
	}

	public static final String CFG_KEY_LANGUAGE = "language";
	public static final String FILE_NAME = "routerlogger.cfg";

	private Thresholds thresholds;
	private final Set<String> guiImportantKeys = new LinkedHashSet<String>();
	private final Set<String> consoleKeysToShow = new LinkedHashSet<String>();

	public Set<String> getGuiImportantKeys() {
		return guiImportantKeys;
	}

	public Thresholds getThresholds() {
		return thresholds;
	}

	public Set<String> getConsoleKeysToShow() {
		return consoleKeysToShow;
	}

	private RouterLoggerConfiguration() {
		// Caricamento della configurazione...
		super(Messages.get("msg.application.name") + File.separator + FILE_NAME, true);
		init();
	}

	private void init() {
		/* Impostazione lingua */
		if (this.contains("language")) {
			final String language = getString(CFG_KEY_LANGUAGE, Defaults.LANGUAGE);
			Messages.setLanguage(language);
			JFaceMessages.setLanguage(language);
		}

		/* Caricamento chiavi da evidenziare */
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

		/* Valorizzazione delle soglie... */
		if (this.getBoolean("thresholds.split", Defaults.THRESHOLDS_SPLIT)) {
			thresholds = new SplitThresholds(); /* Vecchio stile */
		}
		else {
			thresholds = new ExpressionThresholds(); /* Nuovo stile */
		}
	}

	@Override
	public void reload() {
		super.reload();
		init();
	}

	public abstract class Thresholds {

		public static final String CFG_PREFIX = "threshold";

		protected final Set<Threshold> thresholds = new TreeSet<Threshold>();

		private Thresholds() {
			try {
				load();
			}
			catch (IllegalThresholdException ite) {
				throw ite;
			}
			catch (RuntimeException re) {
				throw new IllegalThresholdException(Messages.get("err.threshold.miscfg") + ' ' + Messages.get("err.review.cfg", RouterLoggerConfiguration.this.getFileName()), re);
			}
		}

		protected abstract void load();

		protected boolean isThresholdExcluded(final String thresholdName) {
			for (final String name : getString("thresholds.excluded", true).split(getString("thresholds.excluded.separator", Defaults.THRESHOLDS_EXCLUDED_SEPARATOR).trim())) {
				if (StringUtils.isNotBlank(name)) {
					if (name.trim().equals(thresholdName)) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean isEmpty() {
			return thresholds.isEmpty();
		}

		@Override
		public String toString() {
			return thresholds.toString();
		}

		public Map<Threshold, String> getReached(final RouterData data) {
			final Map<String, String> info = data.getData();
			final Map<Threshold, String> reached = new TreeMap<Threshold, String>();

			// Gestione delle soglie...
			if (!thresholds.isEmpty() && info != null && !info.isEmpty()) {
				for (final String key : info.keySet()) {
					if (key != null && key.trim().length() != 0) {
						for (final Threshold threshold : thresholds) {
							if (key.trim().equals(threshold.getKey()) && threshold.isReached(info.get(key))) {
								reached.put(threshold, info.get(key));
							}
						}
					}
				}
			}
			return reached;
		}

	}

	/* Soglie vecchio stile (tre proprieta') */
	private class SplitThresholds extends Thresholds {

		private static final String CFG_SUFFIX_KEY = "key";
		private static final String CFG_SUFFIX_TYPE = "type";
		private static final String CFG_SUFFIX_VALUE = "value";

		@Override
		protected void load() {
			final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.this;
			final Set<String> thresholdsAdded = new HashSet<String>();
			for (Object objectKey : configuration.getProperties().keySet()) {
				String key = (String) objectKey;
				if (key != null && key.startsWith(CFG_PREFIX + '.')) {
					if (key.indexOf('.') == key.lastIndexOf('.') || "".equals(key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'))) || (!key.endsWith(CFG_SUFFIX_KEY) && !key.endsWith(CFG_SUFFIX_TYPE) && !key.endsWith(CFG_SUFFIX_VALUE))) {
						new IllegalThresholdException(Messages.get("err.threshold.miscfg") + ' ' + Messages.get("err.review.cfg", configuration.getFileName())).printStackTrace();
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
						System.err.println(Messages.get("err.threshold.miscfg.name", thresholdName) + ' ' + Messages.get("err.review.cfg", configuration.getFileName()));
						continue;
					}
					thresholds.add(new Threshold(thresholdName, thresholdKey.trim(), thresholdType, thresholdValue, isThresholdExcluded(thresholdName)));
					thresholdsAdded.add(thresholdName);
				}
			}
		}

	}

	/* Nuove soglie con espressione */
	private class ExpressionThresholds extends Thresholds {

		@Override
		protected void load() {
			final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.this;
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
							break; /* Operatore trovato */
						}
					}
					if (thresholdType == null) {
						System.err.println(Messages.get("err.threshold.miscfg.name", thresholdName) + ' ' + Messages.get("err.review.cfg", configuration.getFileName()));
						continue;
					}
					final String thresholdKey = expression.substring(0, expression.indexOf(operator) - 1);
					final String thresholdValue = expression.substring(expression.indexOf(operator) + operator.length() + 1);
					if (thresholdKey == null || "".equals(thresholdKey.trim()) || thresholdValue == null) {
						System.err.println(Messages.get("err.threshold.miscfg.name", thresholdName) + ' ' + Messages.get("err.review.cfg", configuration.getFileName()));
						continue;
					}
					thresholds.add(new Threshold(thresholdName, thresholdKey.trim(), thresholdType, thresholdValue, isThresholdExcluded(thresholdName)));
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
