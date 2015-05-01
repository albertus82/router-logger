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

	private static final String DEVICE_MODEL = "TP-Link TD-W8970";
	private static final String COMMAND_PROMPT = "#";
	private static final String LOGIN_PROMPT = ":";
	private static final String LINE_SEPARATOR = "\r\n";
	private static final char CSV_SEPARATOR = ';';
	private static final char CSV_SEPARATOR_REPLACEMENT = ' ';

	private static final DateFormat DATE_FORMAT_LOG = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	private static final DateFormat DATE_FORMAT_FILE_NAME = new SimpleDateFormat("yyyyMMdd");

	private FileWriter logFileWriter = null;

	public static void main(String... args) {
		new TpLinkLogger().run();
	}

	@Override
	protected boolean login() throws IOException {
		// Username...
		System.out.print(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(configuration.getProperty("router.username"));

		// Password...
		System.out.println(readFromTelnet(LOGIN_PROMPT, true).trim());
		writeToTelnet(configuration.getProperty("router.password"));

		// Welcome! (salto caratteri speciali (clear screen, ecc.)...
		String welcome = readFromTelnet("-", true);
		System.out.println(welcome.charAt(welcome.length() - 1) + readFromTelnet(COMMAND_PROMPT, true).trim());
		return true;
	}

	@Override
	protected Map<String, String> readInfo() throws IOException {
		writeToTelnet("adsl show info");
		readFromTelnet("{", true); // Avanzamento del reader fino all'inizio dei dati di interesse.

		// Inizio estrazione dati...
		final Map<String, String> info = new LinkedHashMap<String, String>();
		final BufferedReader reader = new BufferedReader(new StringReader(readFromTelnet("}", false).trim()));
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
	protected void saveInfo(final Map<String, String> info) {
		// Selezione del percorso e nome del file di destinazione...
		final String logDestinationDir = configuration.getProperty("log.destination.dir");
		final File logFile;
		if (logDestinationDir != null && !"".equals(logDestinationDir.trim())) {
			File logDestDir = new File(logDestinationDir.trim());
			if (logDestDir.exists() && !logDestDir.isDirectory()) {
				throw new RuntimeException("Invalid path: \"" + logDestDir + "\".");
			}
			if (!logDestDir.exists()) {
				logDestDir.mkdirs();
			}
			logFile = new File(logDestinationDir.trim() + '/' + DATE_FORMAT_FILE_NAME.format(new Date()) + ".csv");
		}
		else {
			logFile = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() + '/' + DATE_FORMAT_FILE_NAME.format(new Date()) + ".csv");
		}

		try {
			// Scrittura header CSV (solo se il file non esiste gia')...
			if (!logFile.exists()) {
				closeOutputFile();
				logFileWriter = new FileWriter(logFile); // Crea nuovo file.
				System.out.println("Logging to: " + logFile.getAbsolutePath() + "...");
				logFileWriter.append(buildCsvHeader(info));
			}

			if (logFileWriter == null) {
				logFileWriter = new FileWriter(logFile, true); // Apre file esistente.
				System.out.println("Logging to: " + logFile.getAbsolutePath() + "...");
			}
			logFileWriter.append(buildCsvRow(info));
			logFileWriter.flush();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			closeOutputFile();
		}
	}

	private String buildCsvHeader(final Map<String, String> info) {
		final StringBuilder header = new StringBuilder("Timestamp").append(CSV_SEPARATOR);
		for (String field : info.keySet()) {
			header.append(field.replace(CSV_SEPARATOR, CSV_SEPARATOR_REPLACEMENT)).append(CSV_SEPARATOR);
		}
		header.replace(header.length() - 1, header.length(), LINE_SEPARATOR);
		return header.toString();
	}

	private String buildCsvRow(final Map<String, String> info) {
		final StringBuilder row = new StringBuilder(DATE_FORMAT_LOG.format(new Date())).append(CSV_SEPARATOR);
		for (String field : info.values()) {
			row.append(field.replace(CSV_SEPARATOR, CSV_SEPARATOR_REPLACEMENT)).append(CSV_SEPARATOR);
		}
		row.replace(row.length() - 1, row.length(), LINE_SEPARATOR);
		return row.toString();
	}

	private void closeOutputFile() {
		try {
			if (logFileWriter != null) {
				System.out.println("Closing output file.");
				logFileWriter.close();
				logFileWriter = null;
			}
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	protected String getDeviceModel() {
		return DEVICE_MODEL;
	}

	@Override
	protected void release() {
		closeOutputFile();
	}

}
