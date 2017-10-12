package it.albertus.routerlogger.util.logging;

import java.util.logging.Level;

public class CustomLevel extends Level {

	private static final long serialVersionUID = -4952662145029636993L;

	/**
	 * EMAIL is a message level for messages that must be sent by email.
	 * <p>
	 * Typically EMAIL messages will also be written to the console or its
	 * equivalent. This level is initialized to {@code 850}.
	 */
	public static final CustomLevel EMAIL = new CustomLevel("EMAIL", 850);

	protected CustomLevel(final String name, final int value) {
		super(name, value);
	}

}
