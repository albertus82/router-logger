package it.albertus.router.tplink;

import it.albertus.router.RouterLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

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
		System.out.println("***** TP-Link TD-W8970 ADSL Modem Router Logger v" + version.getProperty("version.number") + " (" + version.getProperty("version.date") + ") *****");
		System.out.println();
		new TpLinkLogger().run();
	}

	@Override
	protected void login() throws Exception {
		try {
			// Username...
			System.out.print(readFromTelnet(LOGIN_PROMPT, true));
			writeToTelnet(configuration.getProperty("router.username"));

			// Password...
			System.out.println(readFromTelnet(LOGIN_PROMPT, true));
			writeToTelnet(configuration.getProperty("router.password"));

			readFromTelnet('-', false); // Salto caratteri speciali (clear screen).

			// Prompt...
			System.out.print(readFromTelnet(COMMAND_PROMPT, true));
		}
		catch (Exception e) {
			disconnect();
			throw e;
		}
	}

	@Override
	protected Map<String, String> readInfo() throws IOException {
		writeToTelnet("adsl show info");
		readFromTelnet('{', true); // Avanzamento del reader fino all'inizio dei dati di interesse.

		// Inizio estrazione dati...
		Map<String, String> info = new LinkedHashMap<String, String>();
		BufferedReader reader = new BufferedReader(new StringReader(readFromTelnet('}', false)));
		String line;
		while ((line = reader.readLine()) != null) {
			info.put(line.substring(0, line.indexOf('=')).trim(), line.substring(line.indexOf('=') + 1).trim());
		}
		reader.close();
		// Fine estrazione dati.

		readFromTelnet(COMMAND_PROMPT, true); // Avanzamento del reader fino al prompt dei comandi.

		return info;
	}

	@Override
	protected void saveInfo(Map<String, String> info) throws IOException {
		// Selezione del percorso e nome del file di destinazione...
		String logDestinationDir = configuration.getProperty("log.destination.dir");
		if (logDestinationDir != null && !"".equals(logDestinationDir)) {
			logFile = new File(logDestinationDir + '/' + dateFormatFileName.format(new Date()) + ".csv");
		}
		else {
			logFile = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() + '/' + dateFormatFileName.format(new Date()) + ".csv");
		}

		// Scrittura header CSV (solo se il file non esiste gia')...
		if (!logFile.exists()) {
			logFileWriter = new FileWriter(logFile);
			System.out.println();
			System.out.println("Logging to: " + logFile.getAbsolutePath());
			logFileWriter.append(buildCsvHeader(info));
		}

		if (logFileWriter == null) {
			logFileWriter = new FileWriter(logFile, true);
			System.out.println();
			System.out.println("Logging to: " + logFile.getAbsolutePath());
		}
		logFileWriter.append(buildCsvRow(info));
		logFileWriter.flush();
	}

	private String buildCsvHeader(Map<String, String> info) {
		StringBuilder header = new StringBuilder("Timestamp").append(CSV_SEPARATOR);
		for (String field : info.keySet()) {
			header.append(field).append(CSV_SEPARATOR);
		}
		header.replace(header.length() - 1, header.length(), LINE_SEPARATOR);
		return header.toString();
	}

	private String buildCsvRow(Map<String, String> info) {
		StringBuilder row = new StringBuilder(dateFormatLog.format(new Date())).append(CSV_SEPARATOR);
		for (String field : info.values()) {
			row.append(field.replace(CSV_SEPARATOR, ' ')).append(CSV_SEPARATOR);
		}
		row.replace(row.length() - 1, row.length(), LINE_SEPARATOR);
		return row.toString();
	}

}