package it.albertus.router.reader;

import it.albertus.router.engine.RouterData;
import it.albertus.router.resources.Resources;

import java.util.LinkedHashMap;
import java.util.Map;

public class DummyReader extends Reader {

	private static final int CHARACTERS = 15;
	private static final int COLUMNS = 30;

	@Override
	public boolean connect() {
		return true;
	}

	@Override
	public boolean login() {
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
	public RouterData readInfo() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (int i = 1; i <= COLUMNS; i++) {
			StringBuilder field = new StringBuilder();
			for (int j = 1; j <= CHARACTERS; j++) {
				field.append((char) (97 + Math.random() * 25));
			}
			map.put(Resources.get("lbl.column.number", i), field.toString());
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
