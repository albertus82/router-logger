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
		System.out.println("***** TP-Link TD-W8970 ADSL Modem Router Logger *****");
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
	protected void login() throws IOException {
		// Lettura parametri da file di configurazione...
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

			// Username...
			System.out.print(readFromTelnet(LOGIN_PROMPT, true));
			writeToTelnet(configuration.getProperty("router.username"));

			// Password...
			System.out.println(readFromTelnet(LOGIN_PROMPT, true));
			writeToTelnet(configuration.getProperty("router.password"));

			readFromTelnet('-', false); // Salto caratteri speciali (clear
										// screen)

			// Prompt...
			System.out.print(readFromTelnet(COMMAND_PROMPT, true));
		}
		catch (Exception e) {
			telnet.disconnect();
			throw e;
		}
	}

	@Override
	protected void info() throws IOException {
		writeToTelnet("adsl show info");
		readFromTelnet('{', true); // Avanzamento del reader fino all'inizio dei
									// dati di interesse.

		// Inizio estrazione dati...
		BufferedReader reader = new BufferedReader(new StringReader(readFromTelnet('}', false)));
		String line;
		while ((line = reader.readLine()) != null) {
			info.put(line.substring(0, line.indexOf('=')).trim(), line.substring(line.indexOf('=') + 1).trim());
		}
		reader.close();
		// Fine estrazione dati.

		readFromTelnet(COMMAND_PROMPT, true); // Avanzamento del reader fino al
												// prompt dei comandi.
	}

	@Override
	protected void save() throws IOException {
		logFile = new File(configuration.getProperty("log.destination.dir") + '/' + dateFormatFileName.format(new Date()) + ".csv");

		// Scrittura header CSV (solo se il file non esiste gia')...
		if (!logFile.exists()) {
			logFileWriter = new FileWriter(logFile);
			System.out.println("Logging to: " + logFile.getAbsolutePath());
			logFileWriter.append(buildCsvHeader());
		}

		if (logFileWriter == null) {
			logFileWriter = new FileWriter(logFile, true);
			System.out.println("Logging to: " + logFile.getAbsolutePath());
		}
		logFileWriter.append(buildCsv());
		logFileWriter.flush();
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