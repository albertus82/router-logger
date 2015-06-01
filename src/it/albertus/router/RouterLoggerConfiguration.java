package it.albertus.router;

import it.albertus.util.Configuration;

import java.util.Set;
import java.util.TreeSet;

public class RouterLoggerConfiguration extends Configuration {

	private static final RouterLoggerConfiguration configuration = new RouterLoggerConfiguration();

	public static RouterLoggerConfiguration getInstance() {
		return configuration;
	}

	private RouterLoggerConfiguration() {
		super("routerlogger.cfg");
	}

	@Override
	public String toString() {
		final Set<String> properties = new TreeSet<String>();
		for (final Object key : getProperties().keySet()) {
			properties.add((String) key + '=' + getProperties().getProperty((String) key));
		}
		return properties.toString();
	}

}
