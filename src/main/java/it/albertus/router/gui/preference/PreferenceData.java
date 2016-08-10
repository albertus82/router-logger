package it.albertus.router.gui.preference;

import it.albertus.jface.preference.Preference;

public class PreferenceData {

	private final String defaultValue;
	private final Preference parent;
	private final boolean restartRequired;
	private final String configurationKey;
	private final String labelResourceKey;

	public String getDefaultValue() {
		return defaultValue;
	}

	public Preference getParent() {
		return parent;
	}

	public boolean isRestartRequired() {
		return restartRequired;
	}

	public String getConfigurationKey() {
		return configurationKey;
	}

	public String getLabelResourceKey() {
		return labelResourceKey;
	}

	public static class PreferenceDataBuilder {
		private String defaultValue;
		private Preference parent;
		private boolean restartRequired;
		private String configurationKey;
		private String labelResourceKey;

		public PreferenceDataBuilder defaultValue(final String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public PreferenceDataBuilder defaultValue(final boolean defaultValue) {
			this.defaultValue = Boolean.toString(defaultValue);
			return this;
		}

		public PreferenceDataBuilder defaultValue(final byte defaultValue) {
			this.defaultValue = Byte.toString(defaultValue);
			return this;
		}

		public PreferenceDataBuilder defaultValue(final short defaultValue) {
			this.defaultValue = Short.toString(defaultValue);
			return this;
		}

		public PreferenceDataBuilder defaultValue(final char defaultValue) {
			this.defaultValue = Character.toString(defaultValue);
			return this;
		}

		public PreferenceDataBuilder defaultValue(final int defaultValue) {
			this.defaultValue = Integer.toString(defaultValue);
			return this;
		}

		public PreferenceDataBuilder defaultValue(final long defaultValue) {
			this.defaultValue = Long.toString(defaultValue);
			return this;
		}

		public PreferenceDataBuilder defaultValue(final float defaultValue) {
			this.defaultValue = Float.toString(defaultValue);
			return this;
		}

		public PreferenceDataBuilder defaultValue(final double defaultValue) {
			this.defaultValue = Double.toString(defaultValue);
			return this;
		}

		public PreferenceDataBuilder parent(final Preference parent) {
			this.parent = parent;
			return this;
		}

		public PreferenceDataBuilder restartRequired(final boolean restartRequired) {
			this.restartRequired = restartRequired;
			return this;
		}

		public PreferenceDataBuilder configurationKey(final String configurationKey) {
			this.configurationKey = configurationKey;
			return this;
		}

		public PreferenceDataBuilder labelResourceKey(final String labelResourceKey) {
			this.labelResourceKey = labelResourceKey;
			return this;
		}

		public PreferenceData build() {
			return new PreferenceData(this);
		}
	}

	private PreferenceData(final PreferenceDataBuilder builder) {
		this.defaultValue = builder.defaultValue;
		this.parent = builder.parent;
		this.restartRequired = builder.restartRequired;
		this.configurationKey = builder.configurationKey;
		this.labelResourceKey = builder.labelResourceKey;
	}

}
