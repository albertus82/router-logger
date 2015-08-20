package it.albertus.router.engine;

import it.albertus.router.engine.Threshold.Type;
import it.albertus.router.resources.Resources;
import it.albertus.util.Configuration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class RouterLoggerConfiguration extends Configuration {

	private static class Singleton {
		private static final RouterLoggerConfiguration CONFIGURATION = new RouterLoggerConfiguration();
	}

	public static RouterLoggerConfiguration getInstance() {
		return Singleton.CONFIGURATION;
	}

	private final Thresholds thresholds;

	public Thresholds getThresholds() {
		return thresholds;
	}

	private RouterLoggerConfiguration() {
		// Caricamento della configurazione...
		super("routerlogger.cfg");

		// Valorizzazione delle soglie...
		thresholds = new Thresholds();
	}

	public class Thresholds {

		private static final String CFG_PREFIX = "threshold";
		private static final String CFG_SUFFIX_KEY = "key";
		private static final String CFG_SUFFIX_TYPE = "type";
		private static final String CFG_SUFFIX_VALUE = "value";

		private final Set<Threshold> thresholds = new TreeSet<Threshold>();

		private Thresholds() {
			final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.this;
			final Set<String> thresholdsAdded = new HashSet<String>();
			for (Object objectKey : configuration.getProperties().keySet()) {
				String key = (String) objectKey;
				if (key != null && key.startsWith(CFG_PREFIX + '.')) {
					if (key.indexOf('.') == key.lastIndexOf('.') || "".equals(key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'))) || (!key.endsWith(CFG_SUFFIX_KEY) && !key.endsWith(CFG_SUFFIX_TYPE) && !key.endsWith(CFG_SUFFIX_VALUE))) {
						throw new IllegalArgumentException(Resources.get("err.threshold.miscfg") + ' ' + Resources.get("err.review.cfg", configuration.getFileName()));
					}
					final String thresholdName = key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'));
					if (thresholdsAdded.contains(thresholdName)) {
						continue;
					}
					final String thresholdKey = configuration.getString(CFG_PREFIX + '.' + thresholdName + '.' + CFG_SUFFIX_KEY);
					final Type thresholdType = Type.getEnum(configuration.getString(CFG_PREFIX + '.' + thresholdName + '.' + CFG_SUFFIX_TYPE));
					final String thresholdValue = configuration.getString(CFG_PREFIX + '.' + thresholdName + '.' + CFG_SUFFIX_VALUE);
					if (thresholdKey == null || "".equals(thresholdKey.trim()) || thresholdValue == null || thresholdType == null) {
						throw new IllegalArgumentException(Resources.get("err.threshold.miscfg.name", thresholdName) + ' ' + Resources.get("err.review.cfg", configuration.getFileName()));
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

		public Map<String, String> getReached(final RouterData data) {
			final Map<String, String> info = data.getData();
			final Map<String, String> reached = new TreeMap<String, String>();

			// Gestione delle soglie...
			if (!thresholds.isEmpty() && info != null && !info.isEmpty()) {
				for (final String key : info.keySet()) {
					if (key != null && key.trim().length() != 0) {
						for (final Threshold threshold : thresholds) {
							if (key.trim().equals(threshold.getKey()) && threshold.isReached(info.get(key))) {
								reached.put(key, info.get(key));
							}
						}
					}
				}
			}
			return reached;
		}

	}

}
