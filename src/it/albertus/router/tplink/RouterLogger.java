package it.albertus.router.tplink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.net.telnet.TelnetClient;

public abstract class RouterLogger {

	protected TelnetClient telnet = new TelnetClient();
	protected InputStream in;
	protected OutputStream out;
	protected Properties info = new Properties();
	protected Properties configuration = new Properties();
	protected int intervalInMillis;

	protected abstract void login();
	
	protected abstract void logout();

	protected abstract void info() throws IOException;

	protected abstract void save();

	protected String write(String command) {
		StringBuilder echo = new StringBuilder();
		try {
			for (char character : command.toCharArray()) {
				if (character == '\n' || character == '\r') {
					break;
				}
				out.write(character);
				echo.append(character);
			}
			out.flush();
//			Thread.sleep(50);
			out.write('\n');
			echo.append('\n');
			out.flush();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return echo.toString();
	}
	
	protected String read(char until, boolean inclusive) {
		StringBuilder text = new StringBuilder();
		char bt;
		try {
			while ((bt = (char) in.read()) != -1) {
				if (bt == until) {
					if (inclusive) {
						text.append(bt);
					}
					break;
				}
				text.append(bt);
			}
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		return text.toString().trim();
	}

}