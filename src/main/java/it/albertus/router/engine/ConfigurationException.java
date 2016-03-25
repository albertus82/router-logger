package it.albertus.router.engine;

public class ConfigurationException extends IllegalArgumentException {

	private static final long serialVersionUID = -7927744373178072220L;

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationException(String s) {
		super(s);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}

}
