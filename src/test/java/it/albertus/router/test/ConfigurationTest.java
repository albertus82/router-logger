package it.albertus.router.test;

import it.albertus.router.engine.RouterLoggerConfiguration;

public class ConfigurationTest {

	private static final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	public static final void main(String args[]) {
		System.out.println(configuration.getString("asus.dsln14u.command.info.wan", "tcapi show Wan"));
	}

}
