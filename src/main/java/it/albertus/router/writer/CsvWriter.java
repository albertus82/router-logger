package it.albertus.router.writer;

import it.albertus.util.ExceptionUtils;
import it.albertus.util.NewLine;
import it.albertus.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class CsvWriter extends Writer {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private interface Defaults {
		NewLine NEW_LINE = LINE_SEPARATOR != null ? NewLine.getEnum(LINE_SEPARATOR) : NewLine.CRLF;
		String FIELD_SEPARATOR = ";";
		String FIELD_SEPARATOR_REPLACEMENT = ",";
	}

	private static final DateFormat DATE_FORMAT_LOG = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	private static final DateFormat DATE_FORMAT_FILE_NAME = new SimpleDateFormat("yyyyMMdd");

	private FileWriter logFileWriter = null;

	@Override
	public synchronized void saveInfo(final Map<String, String> info) {
		// Selezione del percorso e nome del file di destinazione...
		final String logDestinationDir = configuration.getString("csv.destination.path");
		final File logFile;
		if (StringUtils.isNotBlank(logDestinationDir)) {
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
			final boolean thread = configuration.getBoolean("logger.writer.thread", false);
			if (!logFile.exists()) {
				closeOutputFile();
				logFileWriter = new FileWriter(logFile); // Crea nuovo file.
				if (!thread) {
					out.println("Logging to: " + logFile.getAbsolutePath() + "...");
				}
				logFileWriter.append(buildCsvHeader(info));
			}

			if (logFileWriter == null) {
				logFileWriter = new FileWriter(logFile, true); // Apre file esistente.
				if (!thread) {
					out.println("Logging to: " + logFile.getAbsolutePath() + "...");
				}
			}
			logFileWriter.append(buildCsvRow(info));
			logFileWriter.flush();
		}
		catch (IOException ioe) {
			out.print(ExceptionUtils.getStackTrace(ioe));
			closeOutputFile();
		}
	}

	@Override
	public void release() {
		closeOutputFile();
	}

	private String buildCsvHeader(final Map<String, String> info) {
		final String fieldSeparator = getFieldSeparator();

		final StringBuilder header = new StringBuilder("Timestamp").append(fieldSeparator);
		for (String field : info.keySet()) {
			header.append(field.replace(fieldSeparator, getFieldSeparatorReplacement())).append(fieldSeparator);
		}
		header.replace(header.length() - fieldSeparator.length(), header.length(), getRecordSeparator());
		return header.toString();
	}

	private String buildCsvRow(final Map<String, String> info) {
		final String fieldSeparator = getFieldSeparator();

		final StringBuilder row = new StringBuilder(DATE_FORMAT_LOG.format(new Date())).append(fieldSeparator);
		for (String field : info.values()) {
			row.append(field.replace(fieldSeparator, getFieldSeparatorReplacement())).append(fieldSeparator);
		}
		row.replace(row.length() - fieldSeparator.length(), row.length(), getRecordSeparator());
		return row.toString();
	}

	private String getFieldSeparator() {
		return configuration.getString("csv.field.separator", Defaults.FIELD_SEPARATOR);
	}

	private String getFieldSeparatorReplacement() {
		return configuration.getString("csv.field.separator.replacement", Defaults.FIELD_SEPARATOR_REPLACEMENT);
	}

	private String getRecordSeparator() {
		final String cfgKey = "csv.newline.characters";
		final String cfg = configuration.getString(cfgKey);
		if (cfg == null || cfg.length() == 0) {
			return Defaults.NEW_LINE.toString();
		}
		else {
			final NewLine newLine = NewLine.getEnum(cfg);
			if (newLine != null) {
				return newLine.toString();
			}
			else {
				throw new RuntimeException("Invalid \"" + cfgKey + "\" property. Review your " + configuration.getFileName() + " file.");
			}
		}
	}

	private void closeOutputFile() {
		if (logFileWriter != null) {
			try {
				out.println("Closing output file.");
				logFileWriter.close();
				logFileWriter = null;
			}
			catch (IOException ioe) {
				out.print(ExceptionUtils.getStackTrace(ioe));
			}
		}
	}

}