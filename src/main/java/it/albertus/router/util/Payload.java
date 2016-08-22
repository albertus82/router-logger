package it.albertus.router.util;

import java.io.UnsupportedEncodingException;

public class Payload {

	public static final String PREFERRED_CHARSET = "UTF-8";

	public static byte[] createPayload(final String string) {
		byte[] payload;
		if (string != null) {
			try {
				payload = string.getBytes(PREFERRED_CHARSET);
			}
			catch (final UnsupportedEncodingException uee) {
				payload = string.getBytes();
			}
		}
		else {
			payload = "".getBytes();
		}
		return payload;
	}

}
