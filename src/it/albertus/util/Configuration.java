package it.albertus.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class Configuration {

	private final Properties properties = new Properties();
	private final String fileName;

	protected Configuration(String fileName) {
		this.fileName = fileName;
	}

	protected void load() throws IOException {
		final InputStream inputStream;
		final File config = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() + '/' + fileName);
		if (config.exists()) {
			inputStream = new BufferedInputStream(new FileInputStream(config));
		}
		else {
			inputStream = getClass().getResourceAsStream('/' + fileName);
		}
		properties.load(inputStream);
		inputStream.close();
	}

	public Properties getProperties() {
		return properties;
	}

	public String getFileName() {
		return fileName;
	}

	public String getString(String key) {
		return properties.getProperty(key);
	}

	public String getString(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public Boolean getBoolean(String key) {
		String value = getString(key);
		if (value != null) {
			return Boolean.valueOf(value.trim());
		}
		return null;
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		String value = getString(key);
		if (value != null) {
			return Boolean.parseBoolean(value.trim());
		}
		return defaultValue;
	}

	public Long getLong(String key) {
		String value = getString(key);
		if (value != null) {
			return Long.valueOf(value);
		}
		return null;
	}

	public long getLong(String key, long defaultValue) {
		String value = getString(key);
		if (value != null) {
			return Long.parseLong(value);
		}
		return defaultValue;
	}

	public Integer getInt(String key) {
		String value = getString(key);
		if (value != null) {
			return Integer.valueOf(value);
		}
		return null;
	}

	public int getInt(String key, int defaultValue) {
		String value = getString(key);
		if (value != null) {
			return Integer.parseInt(value);
		}
		return defaultValue;
	}

	public Float getFloat(String key) {
		String value = getString(key);
		if (value != null) {
			return Float.valueOf(value);
		}
		return null;
	}

	public float getFloat(String key, float defaultValue) {
		String value = getString(key);
		if (value != null) {
			return Float.parseFloat(value);
		}
		return defaultValue;
	}

	public Double getDouble(String key) {
		String value = getString(key);
		if (value != null) {
			return Double.valueOf(value);
		}
		return null;
	}

	public double getDouble(String key, double defaultValue) {
		String value = getString(key);
		if (value != null) {
			return Double.parseDouble(value);
		}
		return defaultValue;
	}

	public boolean contains(String key) {
		return properties.get(key) != null;
	}

}
