package it.albertus.router.tplink;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TpLinkLogger extends RouterLogger {

	private static final String LINE_SEPARATOR = "\r\n";
	private static final char CSV_SEPARATOR = ';';
	private static final DateFormat dateFormatLog = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final DateFormat dateFormatFileName = new SimpleDateFormat("yyyyMMdd");

	public static void main(String... args) throws Exception {
		RouterLogger logger = new TpLinkLogger();

		while (true) {
			logger.login();
			logger.loop();
			logger.logout();
		}
	}

	public TpLinkLogger() throws IOException {
		BufferedInputStream reader = new BufferedInputStream(TpLinkLogger.class.getResourceAsStream("/logger.cfg"));
		configuration.load(reader);
		reader.close();
	}

	@Override
	protected void login() {
		try {
			telnet.connect(configuration.getProperty("router.address"), Integer.parseInt(configuration.getProperty("router.port")));
			telnet.setConnectTimeout(Integer.parseInt(configuration.getProperty("connection.timeout.ms")));
			telnet.setSoTimeout(Integer.parseInt(configuration.getProperty("socket.timeout.ms")));
			in = telnet.getInputStream();
			out = telnet.getOutputStream();
			read(':', true);
			write(configuration.getProperty("router.username"));
			read(':', true);
			write(configuration.getProperty("router.password"));
			read('#', true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void info() throws IOException {
		write("adsl show info");
		read('{', true);
		BufferedReader reader = new BufferedReader(new StringReader(read('}', false)));
		String line;
		while ((line = reader.readLine()) != null) {
			info.put(line.substring(0, line.indexOf('=')).trim(), line.substring(line.indexOf('=') + 1).trim());
		}
		reader.close();
	}

	@Override
	protected void logout() {
		try {
			telnet.disconnect();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void save() {
		FileWriter output = null;

		File file = new File(configuration.getProperty("log.destination.dir") + '/' + dateFormatFileName.format(new Date()) + ".csv");

		// Header...
		if (!file.exists()) {
			try {
				output = new FileWriter(file);
				output.append(buildCsvHeader());
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			if (output == null) {
				output = new FileWriter(file, true);
			}
			output.append(buildCsv());
			output.flush();
			output.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String buildCsvHeader() {
		StringBuilder header = new StringBuilder("Timestamp").append(CSV_SEPARATOR);
		for (String field : info.keySet()) {
			header.append(field).append(CSV_SEPARATOR);
		}
		header.replace(header.length() - 1, header.length(), LINE_SEPARATOR);
		return header.toString();
	}

	private String buildCsv() {
		StringBuilder row = new StringBuilder(dateFormatLog.format(new Date())).append(CSV_SEPARATOR);
		for (String field : info.values()) {
			row.append(field.replace(CSV_SEPARATOR, ' ')).append(CSV_SEPARATOR);
		}
		row.replace(row.length() - 1, row.length(), LINE_SEPARATOR);
		return row.toString();
	}

}