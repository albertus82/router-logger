package it.albertus.router.tplink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.net.telnet.TelnetClient;

public abstract class RouterLogger {

	protected TelnetClient telnet = new TelnetClient();
	protected InputStream in;
	protected OutputStream out;
	protected Map<String, String> info = new LinkedHashMap<String, String>();
	protected Properties configuration = new Properties();

	protected abstract void login() throws IOException;

	protected abstract void logout();

	protected abstract void info() throws IOException;

	protected abstract void save() throws IOException;
	
	protected void loop() {
		int iteration = 0;
		while (true) {
			try {
				if (iteration % 15 == 0) {
					System.out.println();
				}
				info();
				save();
				System.out.print(++iteration + " ");
				Thread.sleep(Long.parseLong(configuration.getProperty("logger.interval.ms")));
			}
			catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}

	protected String writeToTelnet(String command) throws IOException {
		StringBuilder echo = new StringBuilder();
		for (char character : command.toCharArray()) {
			if (character == '\n' || character == '\r') {
				break;
			}
			out.write(character);
			echo.append(character);
		}
		out.flush();
		// Thread.sleep(50);
		out.write('\n');
		echo.append('\n');
		out.flush();
		return echo.toString();
	}

	protected String readFromTelnet(char until, boolean inclusive) throws IOException {
		StringBuilder text = new StringBuilder();
		char bt;
		while ((bt = (char) in.read()) != -1) {
			if (bt == until) {
				if (inclusive) {
					text.append(bt);
				}
				break;
			}
			text.append(bt);
		}
		return text.toString().trim();
	}

}