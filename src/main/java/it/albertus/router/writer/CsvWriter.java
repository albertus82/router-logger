package it.albertus.router.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.mail.EmailException;

import it.albertus.jface.JFaceMessages;
import it.albertus.router.email.EmailSender;
import it.albertus.router.engine.RouterData;
import it.albertus.router.resources.Messages;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.router.util.LoggerFactory;
import it.albertus.util.Configuration;
import it.albertus.util.ConfigurationException;
import it.albertus.util.IOUtils;
import it.albertus.util.NewLine;
import it.albertus.util.ZipUtils;

public class CsvWriter extends Writer {

	private static final Logger logger = LoggerFactory.getLogger(CsvWriter.class);

	public static final String DESTINATION_KEY = "lbl.writer.destination.csv";

	protected static final String CFG_KEY_CSV_NEWLINE_CHARACTERS = "csv.newline.characters";

	protected static final String LINE_SEPARATOR = NewLine.SYSTEM_LINE_SEPARATOR;

	protected static final String CSV_FILENAME_REGEX = "[0-9]{8}\\.(csv|CSV)";
	protected static final String CSV_FILE_EXTENSION = ".csv";

	protected static final DateFormat dateFormatColumn = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	protected static final DateFormat dateFormatFileName = new SimpleDateFormat("yyyyMMdd");

	public static class Defaults {
		public static final NewLine NEWLINE = LINE_SEPARATOR != null ? NewLine.getEnum(LINE_SEPARATOR) : NewLine.CRLF;
		public static final String DIRECTORY = getDefaultDirectory();
		public static final String FIELD_SEPARATOR = ";";
		public static final String FIELD_SEPARATOR_REPLACEMENT = ",";
		public static final boolean EMAIL = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected BufferedWriter csvBufferedWriter = null;
	protected File csvFile = null;

	private FileWriter csvFileWriter;

	protected class CsvEmailRunnable implements Runnable {
		@Override
		public void run() {
			final File currentDestinationFile = getDestinationFile();
			for (final File file : currentDestinationFile.getParentFile().listFiles()) {
				if (!file.equals(currentDestinationFile) && file.getName().matches(CSV_FILENAME_REGEX)) {
					try {
						sendEmail(file);
					}
					catch (final Exception exception) {
						logger.log(exception, Destination.CONSOLE, Destination.FILE);
					}
				}
			}
		}

		private void sendEmail(final File uncompressedAttachment) throws IOException, EmailException {
			final File compressedAttachment = new File(uncompressedAttachment.getPath().replace(CSV_FILE_EXTENSION, ZipUtils.ZIP_FILE_EXTENSION));
			ZipUtils.zip(compressedAttachment, uncompressedAttachment);
			try {
				ZipUtils.test(compressedAttachment);
				final String formattedDate = getFormattedDate(compressedAttachment);
				final String subject = Messages.get("msg.writer.csv.email.subject", formattedDate);
				final String message = Messages.get("msg.writer.csv.email.message", compressedAttachment.getName());
				EmailSender.getInstance().send(subject, message, compressedAttachment);
				if (!uncompressedAttachment.delete()) {
					uncompressedAttachment.deleteOnExit();
				}
			}
			catch (final IOException ioe) {
				if (!compressedAttachment.delete()) {
					compressedAttachment.deleteOnExit();
				}
				throw new IOException("ZIP file verification failed for " + uncompressedAttachment.getPath() + '.', ioe);
			}
		}

		private synchronized String getFormattedDate(final File zipFile) {
			String formattedDate;
			try {
				final String zipFileName = zipFile.getName();
				formattedDate = DateFormat.getDateInstance(DateFormat.LONG, Messages.getLanguage().getLocale()).format(CsvWriter.dateFormatFileName.parse(zipFileName.substring(0, zipFileName.indexOf('.'))));
			}
			catch (final Exception e) {
				logger.log(e, Destination.CONSOLE, Destination.FILE);
				formattedDate = "";
			}
			return formattedDate;
		}
	}

	public CsvWriter() {
		sendEmail();
	}

	@Override
	public synchronized void saveInfo(final RouterData info) {
		try {
			// Selezione del percorso e nome del file di destinazione...
			final File file = getDestinationFile();

			String path = getFilePath(file);

			if (!file.equals(this.csvFile)) {
				final boolean first = this.csvFile == null;
				closeOutputFile();
				this.csvFile = file;
				if (!first) {
					sendEmail();
				}
			}

			if (!file.exists()) {
				// Create new file...
				closeOutputFile();
				csvFileWriter = new FileWriter(file);
				csvBufferedWriter = new BufferedWriter(csvFileWriter);
				logger.log(Messages.get("msg.logging.to.file", path), Destination.CONSOLE);
				csvBufferedWriter.append(buildCsvHeader(info));
			}

			if (csvBufferedWriter == null) {
				// Open existing file...
				csvFileWriter = new FileWriter(file, true);
				csvBufferedWriter = new BufferedWriter(csvFileWriter);
				logger.log(Messages.get("msg.logging.to.file", path), Destination.CONSOLE);
			}
			csvBufferedWriter.append(buildCsvRow(info));
			csvBufferedWriter.flush();
		}
		catch (final Exception exception) {
			logger.log(exception);
			closeOutputFile();
		}
	}

	@Override
	public synchronized void release() {
		closeOutputFile();
	}

	protected File getDestinationFile() {
		final String csvDestinationDir = configuration.getString("csv.destination.path", true).trim();
		final File file;
		if (!csvDestinationDir.isEmpty()) {
			file = new File(csvDestinationDir + File.separator + dateFormatFileName.format(new Date()) + CSV_FILE_EXTENSION);
		}
		else {
			file = getDefaultFile();
		}
		final File parentFile = file.getParentFile();
		if (parentFile != null && !parentFile.exists()) {
			parentFile.mkdirs(); // Create directories if not exists
		}
		return file;
	}

	protected String buildCsvHeader(final RouterData info) {
		final String fieldSeparator = getFieldSeparator();
		final String fieldSeparatorReplacement = getFieldSeparatorReplacement();

		final StringBuilder header = new StringBuilder(Messages.get("lbl.column.timestamp.text")).append(fieldSeparator);
		header.append(Messages.get("lbl.column.response.time.text")).append(fieldSeparator); // Response time
		for (String field : info.getData().keySet()) {
			header.append(field.replace(fieldSeparator, fieldSeparatorReplacement)).append(fieldSeparator);
		}
		header.replace(header.length() - fieldSeparator.length(), header.length(), getRecordSeparator());
		return header.toString();
	}

	protected String buildCsvRow(final RouterData info) {
		final String fieldSeparator = getFieldSeparator();
		final String fieldSeparatorReplacement = getFieldSeparatorReplacement();

		final StringBuilder row = new StringBuilder(dateFormatColumn.format(info.getTimestamp())).append(fieldSeparator);
		row.append(info.getResponseTime()).append(fieldSeparator); // Response time
		for (String field : info.getData().values()) {
			row.append(field.replace(fieldSeparator, fieldSeparatorReplacement)).append(fieldSeparator);
		}
		row.replace(row.length() - fieldSeparator.length(), row.length(), getRecordSeparator());
		return row.toString();
	}

	protected String getFieldSeparator() {
		return configuration.getString("csv.field.separator", Defaults.FIELD_SEPARATOR);
	}

	protected String getFieldSeparatorReplacement() {
		return configuration.getString("csv.field.separator.replacement", Defaults.FIELD_SEPARATOR_REPLACEMENT);
	}

	protected String getRecordSeparator() {
		final String cfg = configuration.getString(CFG_KEY_CSV_NEWLINE_CHARACTERS);
		if (cfg == null || cfg.length() == 0) {
			return Defaults.NEWLINE.toString();
		}
		else {
			final NewLine newLine = NewLine.getEnum(cfg);
			if (newLine != null) {
				return newLine.toString();
			}
			else {
				throw new ConfigurationException(JFaceMessages.get("err.configuration.invalid", CFG_KEY_CSV_NEWLINE_CHARACTERS) + ' ' + JFaceMessages.get("err.configuration.review", configuration.getFileName()), CFG_KEY_CSV_NEWLINE_CHARACTERS);
			}
		}
	}

	protected void closeOutputFile() {
		if (csvBufferedWriter != null || csvFileWriter != null) {
			logger.log(Messages.get("msg.closing.output.file"), Destination.CONSOLE);
			IOUtils.closeQuietly(csvBufferedWriter, csvFileWriter);
			csvBufferedWriter = null;
			csvFileWriter = null;
		}
	}

	protected void sendEmail() {
		if (configuration.getBoolean("csv.email", Defaults.EMAIL)) {
			new Thread(new CsvEmailRunnable(), "csvEmailThread").start();
		}
	}

	protected static String getFilePath(final File file) {
		String path;
		try {
			path = file.getCanonicalPath();
		}
		catch (final Exception e1) {
			if (logger.isDebugEnabled()) {
				logger.log(e1, Destination.CONSOLE, Destination.FILE);
			}
			try {
				path = file.getAbsolutePath();
			}
			catch (final Exception e2) {
				if (logger.isDebugEnabled()) {
					logger.log(e2, Destination.CONSOLE, Destination.FILE);
				}
				path = file.getPath();
			}
		}
		return path;
	}

	protected static File getDefaultFile() {
		return new File(getDefaultDirectory() + File.separator + dateFormatFileName.format(new Date()) + CSV_FILE_EXTENSION);
	}

	protected static String getDefaultDirectory() {
		return Configuration.getOsSpecificDocumentsDir() + File.separator + Messages.get("msg.application.name");
	}

}
