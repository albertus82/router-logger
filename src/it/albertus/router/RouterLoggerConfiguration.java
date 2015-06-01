package it.albertus.router;

import it.albertus.util.Configuration;

public class RouterLoggerConfiguration extends Configuration {

	private static final RouterLoggerConfiguration configuration = new RouterLoggerConfiguration();

	public static RouterLoggerConfiguration getInstance() {
		return configuration;
	}

	private RouterLoggerConfiguration() {
		super("routerlogger.cfg");
	}

}
