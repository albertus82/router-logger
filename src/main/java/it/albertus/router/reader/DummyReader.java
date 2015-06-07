package it.albertus.router.reader;

import java.util.LinkedHashMap;
import java.util.Map;

public class DummyReader extends Reader {

	@Override
	public boolean connect() {
		return true;
	}

	@Override
	public boolean login() {
		final String className = getClass().getSimpleName();
		final String message = " - Only for test purposes!";
		final StringBuilder separator = new StringBuilder();
		for (int c = 0; c < className.length() + message.length(); c++) {
			separator.append('-');
		}
		out.printOnNewLine(separator.toString());
		out.printOnNewLine(className + message);
		out.printlnOnNewLine(separator.toString());
		return true;
	}

	@Override
	public Map<String, String> readInfo() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (int i = 1; i <= 10; i++) {
			StringBuilder field = new StringBuilder();
			for (int j = 1; j <= 10; j++) {
				field.append((char) (97 + Math.random() * 25));
			}
			map.put("Column " + i, field.toString());
		}
		return map;
	}

	@Override
	public void disconnect() {}

}
