package it.albertus.router.logger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DummyLogger extends RouterLogger {

	@Override
	protected Map<String, String> readInfo() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		for (int i = 1; i <= 10; i++) {
			StringBuilder field = new StringBuilder();
			for (int j = 1; j <= 10; j++) {
				field.append((char) (32 + Math.random() * 94));
			}
			map.put("Column " + i, field.toString());
		}
		return map;
	}

	@Override
	protected boolean login() throws IOException {
		final String className = getClass().getSimpleName();
		final String message = " - Only for test purposes!";
		final StringBuilder separator = new StringBuilder();
		for (int c = 0; c < className.length() + message.length(); c++) {
			separator.append('-');
		}
		out.println(separator);
		out.println(className + message);
		out.println(separator);
		return true;
	}

}
