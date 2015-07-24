package it.albertus.router.engine;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import it.albertus.router.engine.Threshold.Type;
import it.albertus.util.Configuration;

public class RouterLoggerConfiguration extends Configuration {

	private static final RouterLoggerConfiguration configuration = new RouterLoggerConfiguration();

	public static RouterLoggerConfiguration getInstance() {
		return configuration;
	}

	private final Thresholds thresholds;

	public Thresholds getThresholds() {
		return thresholds;
	}

	private RouterLoggerConfiguration() {
		// Caricamento della configurazione...
		super("routerlogger.cfg");

		// Valorizzazione delle soglie...
		thresholds = new Thresholds(this);
	}

	public class Thresholds {

		private static final String CFG_PREFIX = "threshold";
		private static final String CFG_SUFFIX_KEY = "key";
		private static final String CFG_SUFFIX_TYPE = "type";
		private static final String CFG_SUFFIX_VALUE = "value";

		private final Set<Threshold> thresholds = new TreeSet<Threshold>();

		public Thresholds(Configuration configuration) {
			final Set<String> thresholdsAdded = new HashSet<String>();
			for (Object objectKey : configuration.getProperties().keySet()) {
				String key = (String) objectKey;
				if (key != null && key.startsWith(CFG_PREFIX + '.')) {
					if (key.indexOf('.') == key.lastIndexOf('.') || "".equals(key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'))) || (!key.endsWith(CFG_SUFFIX_KEY) && !key.endsWith(CFG_SUFFIX_TYPE) && !key.endsWith(CFG_SUFFIX_VALUE))) {
						throw new IllegalArgumentException("Thresholds misconfigured. Review your " + configuration.getFileName() + " file.");
					}
					final String thresholdName = key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'));
					if (thresholdsAdded.contains(thresholdName)) {
						continue;
					}
					final String thresholdKey = configuration.getString(CFG_PREFIX + '.' + thresholdName + '.' + CFG_SUFFIX_KEY);
					final Type thresholdType = Type.getEnum(configuration.getString(CFG_PREFIX + '.' + thresholdName + '.' + CFG_SUFFIX_TYPE));
					final String thresholdValue = configuration.getString(CFG_PREFIX + '.' + thresholdName + '.' + CFG_SUFFIX_VALUE);
					if (thresholdKey == null || "".equals(thresholdKey.trim()) || thresholdValue == null || thresholdType == null) {
						throw new IllegalArgumentException("Threshold misconfigured: \"" + thresholdName + "\". Review your " + configuration.getFileName() + " file.");
					}
					thresholds.add(new Threshold(thresholdKey.trim(), thresholdType, thresholdValue));
					thresholdsAdded.add(thresholdName);
				}
			}
		}

		public boolean isEmpty() {
			return thresholds.isEmpty();
		}

		@Override
		public String toString() {
			return thresholds.toString();
		}

		public Set<String> getReachedKeys(final Map<String, String> info) {
			final Set<String> keys = new HashSet<String>();

			// Gestione delle soglie...
			if (!thresholds.isEmpty() && info != null && !info.isEmpty()) {
				for (final String key : info.keySet()) {
					if (key != null && key.trim().length() != 0) {
						for (final Threshold threshold : thresholds) {
							if (key.trim().equals(threshold.getKey()) && threshold.isReached(info.get(key))) {
								keys.add(key);
							}
						}
					}
				}
			}
			return keys;
		}

	}

}
