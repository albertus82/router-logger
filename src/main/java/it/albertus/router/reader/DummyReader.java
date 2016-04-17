package it.albertus.router.reader;

import it.albertus.router.engine.RouterData;
import it.albertus.router.resources.Resources;
import it.albertus.util.ThreadUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class DummyReader extends Reader {

	private static final byte CHARACTERS = 15;
	private static final byte COLUMNS = 30;

	private static final short LAG_IN_MILLIS = 100;
	private static final short CONNECTION_TIME_IN_MILLIS = 1000;
	private static final short AUTHENTICATION_TIME_IN_MILLIS = 1000;

	private static final double CONNECTION_ERROR_PERCENTAGE = 0.0;
	private static final double AUTHENTICATION_ERROR_PERCENTAGE = 0.0;
	private static final double READ_ERROR_PERCENTAGE = 0.0;

	@Override
	public boolean connect() {
		out.println(Resources.get("msg.dummy.connect"));
		if (CONNECTION_TIME_IN_MILLIS > 0) {
			ThreadUtils.sleep(CONNECTION_TIME_IN_MILLIS);
		}
		if (Math.random() > (100.0 - CONNECTION_ERROR_PERCENTAGE) / 100.0) {
			logger.log(Resources.get("msg.dummy.connect.error", CONNECTION_ERROR_PERCENTAGE));
			return false;
		}
		return true;
	}

	@Override
	public boolean login(final String username, final char[] password) {
		out.println("Username: " + username);
		out.println("Password: " + (password != null ? String.valueOf(password) : Arrays.toString(password)));
		if (AUTHENTICATION_TIME_IN_MILLIS > 0) {
			ThreadUtils.sleep(AUTHENTICATION_TIME_IN_MILLIS);
		}
		if (Math.random() > (100.0 - AUTHENTICATION_ERROR_PERCENTAGE) / 100.0) {
			throw new RuntimeException(Resources.get("msg.dummy.authentication.error", AUTHENTICATION_ERROR_PERCENTAGE));
		}
		final String message = getClass().getSimpleName() + " - " + Resources.get("msg.test.purposes.only");
		final StringBuilder separator = new StringBuilder();
		for (int c = 0; c < message.length(); c++) {
			separator.append('-');
		}
		out.println(separator.toString(), true);
		out.println(message);
		out.println(separator.toString());
		return true;
	}

	@Override
	public RouterData readInfo() throws IOException {
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (byte i = 1; i <= COLUMNS; i++) {
			StringBuilder field = new StringBuilder();
			for (byte j = 1; j <= CHARACTERS; j++) {
				field.append((char) (97 + Math.random() * 25));
			}
			map.put(Resources.get("lbl.column.number", i), field.toString());
		}
		if (LAG_IN_MILLIS != 0) {
			ThreadUtils.sleep(LAG_IN_MILLIS);
		}
		if (Math.random() > (100.0 - READ_ERROR_PERCENTAGE) / 100.0) {
			throw new IOException(Resources.get("msg.dummy.readinfo.error", READ_ERROR_PERCENTAGE));
		}
		return new RouterData(map);
	}

	@Override
	public void logout() {
		out.println(Resources.get("msg.dummy.logout"), true);
	}

	@Override
	public void disconnect() {
		out.println(Resources.get("msg.dummy.disconnect"), true);
	}

}
