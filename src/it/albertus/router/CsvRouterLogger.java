package it.albertus.router;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public abstract class CsvRouterLogger extends RouterLogger {

	private interface Defaults {
		boolean RECORD_SEPARATOR_CRLF = true;
		String FIELD_SEPARATOR = ";";
		String FIELD_SEPARATOR_REPLACEMENT = " ";
	}

	private static final DateFormat DATE_FORMAT_LOG = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	private static final DateFormat DATE_FORMAT_FILE_NAME = new SimpleDateFormat("yyyyMMdd");

	private FileWriter logFileWriter = null;

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

	@Override
	protected void release() {
		closeOutputFile();
	}

	private String buildCsvHeader(final Map<String, String> info) {
		final String fieldSeparator = getFieldSeparator();

		final StringBuilder header = new StringBuilder("Timestamp").append(fieldSeparator);
		for (String field : info.keySet()) {
			header.append(field.replace(fieldSeparator, getFieldSeparatorReplacement())).append(fieldSeparator);
		}
		header.replace(header.length() - 1, header.length(), getRecordSeparator());
		return header.toString();
	}

	private String buildCsvRow(final Map<String, String> info) {
		final String fieldSeparator = getFieldSeparator();

		final StringBuilder row = new StringBuilder(DATE_FORMAT_LOG.format(new Date())).append(fieldSeparator);
		for (String field : info.values()) {
			row.append(field.replace(fieldSeparator, getFieldSeparatorReplacement())).append(fieldSeparator);
		}
		row.replace(row.length() - 1, row.length(), getRecordSeparator());
		return row.toString();
	}

	private String getFieldSeparator() {
		return configuration.getProperty("csv.field.separator", Defaults.FIELD_SEPARATOR);
	}

	private String getFieldSeparatorReplacement() {
		return configuration.getProperty("csv.field.separator.replacement", Defaults.FIELD_SEPARATOR_REPLACEMENT);
	}

	private String getRecordSeparator() {
		return Boolean.parseBoolean(configuration.getProperty("csv.record.separator.crlf", String.valueOf(Defaults.RECORD_SEPARATOR_CRLF)).trim()) ? "\r\n" : "\n";
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

}
