package it.albertus.router.util;

import java.io.UnsupportedEncodingException;

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
			catch (final UnsupportedEncodingException uee) {
				logger.error(uee);
				payload = string.getBytes();
			}
		}
		else {
			payload = "".getBytes();
		}
		return payload;
	}

}
