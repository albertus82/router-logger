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

	private static final char COMMAND_PROMPT = '#';
	private static final char LOGIN_PROMPT = ':';
	private static final String LINE_SEPARATOR = "\r\n";
	private static final char CSV_SEPARATOR = ';';

	private static final DateFormat dateFormatLog = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static final DateFormat dateFormatFileName = new SimpleDateFormat("yyyyMMdd");

	private File logFile = null;
	private FileWriter logFileWriter = null;

	public static void main(String... args) throws Exception {
		RouterLogger logger = new TpLinkLogger();

		int retries = 0;
		while (retries < 3) {
			logger.login();
			logger.loop();
			retries++;
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
			System.out.print(read(LOGIN_PROMPT, true));
			write(configuration.getProperty("router.username"));
			System.out.println(read(LOGIN_PROMPT, true));
			write(configuration.getProperty("router.password"));
			System.out.print(read(COMMAND_PROMPT, true));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void loop() {
		int iteration = 1;
		while (true) {
			try {
				System.out.print(" " + iteration++);

				info();

				logFile = new File(configuration.getProperty("log.destination.dir") + '/' + dateFormatFileName.format(new Date()) + ".csv");

				// Scrittura header CSV (solo se il file non esiste gia')...
				if (!logFile.exists()) {
					logFileWriter = new FileWriter(logFile);
					logFileWriter.append(buildCsvHeader());
				}

				save();

				Thread.sleep(Long.parseLong(configuration.getProperty("logger.interval.ms")));
			}
			catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}

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

	protected void save() throws IOException {
		if (logFileWriter == null) {
			logFileWriter = new FileWriter(logFile, true);
		}
		logFileWriter.append(buildCsv());
		logFileWriter.flush();
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