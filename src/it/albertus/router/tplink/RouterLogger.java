package it.albertus.router.tplink;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.net.telnet.TelnetClient;

public abstract class RouterLogger {

	private TelnetClient telnet = new TelnetClient();
	private InputStream in;
	private OutputStream out;
	protected Map<String, String> info = new LinkedHashMap<String, String>();
	protected Properties configuration = new Properties();

	public RouterLogger() {
		try {
			BufferedInputStream reader = new BufferedInputStream(TpLinkLogger.class.getResourceAsStream("/logger.cfg"));
			configuration.load(reader);
			reader.close();
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	protected void connect() throws IOException {
		String routerAddress = configuration.getProperty("router.address");
		int routerPort = Integer.parseInt(configuration.getProperty("router.port"));
		int connectionTimeout = Integer.parseInt(configuration.getProperty("connection.timeout.ms"));
		int socketTimeout = Integer.parseInt(configuration.getProperty("socket.timeout.ms"));

		System.out.println("Connecting to: " + routerAddress + ':' + routerPort + "...");
		try {
			telnet.connect(routerAddress, routerPort);
			telnet.setConnectTimeout(connectionTimeout);
			telnet.setSoTimeout(socketTimeout);
			in = telnet.getInputStream();
			out = telnet.getOutputStream();
		}
		catch (Exception e) {
			telnet.disconnect();
			throw e;
		}
	}

	protected void disconnect() {
		try {
			telnet.disconnect();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected abstract void login() throws IOException;

	protected void logout() {
	}

	protected abstract void readInfo() throws IOException;

	protected abstract void saveInfo() throws IOException;

	protected void loop() {
		int iteration = 0;
		while (true) {
			try {
				if (iteration % 15 == 0) {
					System.out.println();
				}
				readInfo();
				saveInfo();
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