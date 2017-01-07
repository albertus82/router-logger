package it.albertus.router.writer;

public class DatabaseException extends RuntimeException {

	private static final long serialVersionUID = 1334108635306801193L;

	public DatabaseException() {
		super();
	}

	public DatabaseException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public DatabaseException(final String message) {
		super(message);
	}

	public DatabaseException(final Throwable cause) {
		super(cause);
	}

}
