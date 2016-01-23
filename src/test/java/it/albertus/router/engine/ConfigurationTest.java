package it.albertus.router.engine;

public class ConfigurationTest {

	private static final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	public static final void main(String args[]) {
		System.out.println("Configuration: " + configuration);
		System.out.println(configuration.getString("asus.dsln14u.command.info.wan", "tcapi show Wan"));
	}

}
