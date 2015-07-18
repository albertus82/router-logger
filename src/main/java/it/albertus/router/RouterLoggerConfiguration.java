package it.albertus.router;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import it.albertus.router.Threshold.Type;
import it.albertus.util.Configuration;

public class RouterLoggerConfiguration extends Configuration {

	private static final RouterLoggerConfiguration configuration = new RouterLoggerConfiguration();

	public static RouterLoggerConfiguration getInstance() {
		return configuration;
	}

	private static final String THRESHOLD_PREFIX = "threshold";
	private static final String THRESHOLD_SUFFIX_KEY = "key";
	private static final String THRESHOLD_SUFFIX_TYPE = "type";
	private static final String THRESHOLD_SUFFIX_VALUE = "value";

	private final Set<Threshold> thresholds = new TreeSet<Threshold>();

	public Set<Threshold> getThresholds() {
		return thresholds;
	}

	private RouterLoggerConfiguration() {
		// Caricamento della configurazione...
		super("routerlogger.cfg");

		// Valorizzazione delle soglie...
		final Set<String> thresholdsAdded = new HashSet<String>();
		for (Object objectKey : this.getProperties().keySet()) {
			String key = (String) objectKey;
			if (key != null && key.startsWith(THRESHOLD_PREFIX + '.')) {
				if (key.indexOf('.') == key.lastIndexOf('.') || "".equals(key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'))) || (!key.endsWith(THRESHOLD_SUFFIX_KEY) && !key.endsWith(THRESHOLD_SUFFIX_TYPE) && !key.endsWith(THRESHOLD_SUFFIX_VALUE))) {
					throw new IllegalArgumentException("Thresholds misconfigured. Review your " + this.getFileName() + " file.");
				}
				final String thresholdName = key.substring(key.indexOf('.') + 1, key.lastIndexOf('.'));
				if (thresholdsAdded.contains(thresholdName)) {
					continue;
				}
				final String thresholdKey = this.getString(THRESHOLD_PREFIX + '.' + thresholdName + '.' + THRESHOLD_SUFFIX_KEY);
				final Type thresholdType = Type.getEnum(this.getString(THRESHOLD_PREFIX + '.' + thresholdName + '.' + THRESHOLD_SUFFIX_TYPE));
				final String thresholdValue = this.getString(THRESHOLD_PREFIX + '.' + thresholdName + '.' + THRESHOLD_SUFFIX_VALUE);
				if (thresholdKey == null || "".equals(thresholdKey.trim()) || thresholdValue == null || thresholdType == null) {
					throw new IllegalArgumentException("Threshold misconfigured: \"" + thresholdName + "\". Review your " + this.getFileName() + " file.");
				}
				thresholds.add(new Threshold(thresholdKey.trim(), thresholdType, thresholdValue));
				thresholdsAdded.add(thresholdName);
			}
		}
	}

}
