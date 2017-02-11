package it.albertus.router.util;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.util.logging.LoggerFactory;

public class Payload {

	private static final Logger logger = LoggerFactory.getLogger(Payload.class);

	public static final String PREFERRED_CHARSET = "UTF-8";

	private Payload() {
		throw new IllegalAccessError("Utility class");
	}

	public static byte[] createPayload(final String string) {
		byte[] payload;
		if (string != null) {
			try {
				payload = string.getBytes(PREFERRED_CHARSET);
			}
			catch (final UnsupportedEncodingException e) {
				logger.log(Level.WARNING, e.toString(), e);
				payload = string.getBytes();
			}
		}
		else {
			payload = "".getBytes();
		}
		return payload;
	}

}
