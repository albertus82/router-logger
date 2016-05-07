package it.albertus.router.writer;

import it.albertus.router.email.EmailSender;
import it.albertus.router.engine.RouterData;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger.Destination;
import it.albertus.router.util.Zipper;
import it.albertus.util.ConfigurationException;
import it.albertus.util.Console;
import it.albertus.util.NewLine;
import it.albertus.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipException;

public class CsvWriter extends Writer {

	public static final String CFG_KEY_CSV_NEWLINE_CHARACTERS = "csv.newline.characters";
	public static final String DESTINATION_KEY = "lbl.writer.destination.csv";

	protected static final String LINE_SEPARATOR = NewLine.SYSTEM_LINE_SEPARATOR;
	protected static final String CSV_FILENAME_REGEX = "[0-9]{8}\\.(csv|CSV)";
	protected static final String CSV_FILE_EXTENSION = ".csv";

	protected static final DateFormat dateFormatColumn = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	public static final DateFormat dateFormatFileName = new SimpleDateFormat("yyyyMMdd");

	public interface Defaults {
		NewLine NEWLINE = LINE_SEPARATOR != null ? NewLine.getEnum(LINE_SEPARATOR) : NewLine.CRLF;
		String DIRECTORY = getDefaultDirectory();
		String FIELD_SEPARATOR = ";";
		String FIELD_SEPARATOR_REPLACEMENT = ",";
		boolean EMAIL = false;
	}

	protected final EmailSender emailSender = EmailSender.getInstance();
	protected final Zipper zipper = Zipper.getInstance();

	protected BufferedWriter csvFileWriter = null;
	protected File csvFile = null;

	protected class CsvEmailRunnable implements Runnable {
		@Override
		public void run() {
			final File currentDestinationFile = getDestinationFile();
			for (final File csvFile : currentDestinationFile.getParentFile().listFiles()) {
				if (!csvFile.equals(currentDestinationFile) && csvFile.getName().matches(CSV_FILENAME_REGEX)) {
					try {
						final File zipFile = new File(csvFile.getPath().replace(CSV_FILE_EXTENSION, Zipper.ZIP_FILE_EXTENSION));
						zipper.zip(zipFile, csvFile);
						if (zipper.test(zipFile)) {
							String formattedDate = zipFile.getName();
							try {
								formattedDate = DateFormat.getDateInstance(DateFormat.LONG, Resources.getLanguage().getLocale()).format(CsvWriter.dateFormatFileName.parse(formattedDate.substring(0, formattedDate.indexOf('.'))));
							}
							catch (final Exception e) {
								formattedDate = e.getClass().getSimpleName();
							}
							final String subject = Resources.get("msg.writer.csv.email.subject", formattedDate);
							final String message = Resources.get("msg.writer.csv.email.message", zipFile.getName());
							emailSender.send(subject, message, zipFile);
							csvFile.delete();
						}
						else {
							zipFile.delete();
							throw new ZipException("ZIP file verification failed for " + csvFile.getPath() + '.');
						}
					}
					catch (final Exception exception) {
						logger.log(exception, Destination.CONSOLE);
					}
				}
			}
		}
	}

	@Override
	public void init(final Console console) {
		super.init(console);
		sendEmail();
	}

	@Override
	public synchronized void saveInfo(final RouterData info) {
		try {
			// Selezione del percorso e nome del file di destinazione...
			final File file = getDestinationFile();

			String path;
			try {
				path = file.getCanonicalPath();
			}
			catch (final Exception e1) {
				try {
					path = file.getAbsolutePath();
				}
				catch (final Exception e2) {
					path = file.getPath();
				}
			}

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
				csvFileWriter = new BufferedWriter(new FileWriter(file));
				out.println(Resources.get("msg.logging.to.file", path), true);
				csvFileWriter.append(buildCsvHeader(info));
			}

			if (csvFileWriter == null) {
				// Open existing file...
				csvFileWriter = new BufferedWriter(new FileWriter(file, true));
				out.println(Resources.get("msg.logging.to.file", path), true);
			}
			csvFileWriter.append(buildCsvRow(info));
			csvFileWriter.flush();
		}
		catch (final Exception exception) {
			logger.log(exception);
			closeOutputFile();
		}
	}

	@Override
	public void release() {
		closeOutputFile();
	}

	protected File getDestinationFile() {
		final String csvDestinationDir = configuration.getString("csv.destination.path");
		final File file;
		if (StringUtils.isNotBlank(csvDestinationDir)) {
			final File logDestDir = new File(csvDestinationDir.trim());
			if (logDestDir.exists() && !logDestDir.isDirectory()) {
				throw new RuntimeException(Resources.get("err.invalid.path", logDestDir));
			}
			if (!logDestDir.exists()) {
				logDestDir.mkdirs();
			}
			file = new File(csvDestinationDir.trim() + File.separator + dateFormatFileName.format(new Date()) + CSV_FILE_EXTENSION);
		}
		else {
			file = getDefaultFile();
		}
		return file;
	}

	protected String buildCsvHeader(final RouterData info) {
		final String fieldSeparator = getFieldSeparator();

		final StringBuilder header = new StringBuilder(Resources.get("lbl.column.timestamp.text")).append(fieldSeparator);
		header.append(Resources.get("lbl.column.response.time.text")).append(fieldSeparator); // Response time
		for (String field : info.getData().keySet()) {
			header.append(field.replace(fieldSeparator, getFieldSeparatorReplacement())).append(fieldSeparator);
		}
		header.replace(header.length() - fieldSeparator.length(), header.length(), getRecordSeparator());
		return header.toString();
	}

	protected String buildCsvRow(final RouterData info) {
		final String fieldSeparator = getFieldSeparator();

		final StringBuilder row = new StringBuilder(dateFormatColumn.format(info.getTimestamp())).append(fieldSeparator);
		row.append(info.getResponseTime()).append(fieldSeparator); // Response time
		for (String field : info.getData().values()) {
			row.append(field.replace(fieldSeparator, getFieldSeparatorReplacement())).append(fieldSeparator);
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
				throw new ConfigurationException(Resources.get("err.invalid.cfg", CFG_KEY_CSV_NEWLINE_CHARACTERS) + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), CFG_KEY_CSV_NEWLINE_CHARACTERS);
			}
		}
	}

	protected void closeOutputFile() {
		if (csvFileWriter != null) {
			try {
				out.println(Resources.get("msg.closing.output.file"), true);
				csvFileWriter.close();
				csvFileWriter = null;
			}
			catch (final IOException ioe) {
				logger.log(ioe);
			}
		}
	}

	protected void sendEmail() {
		if (configuration.getBoolean("csv.email", Defaults.EMAIL)) {
			new Thread(new CsvEmailRunnable(), "csvEmailThread").start();
		}
	}

	protected static File getDefaultFile() {
		File csvFile;
		try {
			csvFile = new File(new File(CsvWriter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart()).getParent() + File.separator + dateFormatFileName.format(new Date()) + CSV_FILE_EXTENSION);
		}
		catch (final Exception e1) {
			try {
				// In caso di problemi, scrive nella directory del profilo dell'utente
				csvFile = new File(System.getProperty("user.home").toString() + File.separator + dateFormatFileName.format(new Date()) + CSV_FILE_EXTENSION);
			}
			catch (final Exception e2) {
				// Nella peggiore delle ipotesi, scrive nella directory corrente
				csvFile = new File(dateFormatFileName.format(new Date()) + CSV_FILE_EXTENSION);
			}
		}
		return csvFile;
	}

	protected static String getDefaultDirectory() {
		String directory;
		try {
			directory = getDefaultFile().getParentFile().getCanonicalPath();
		}
		catch (Exception e1) {
			try {
				directory = getDefaultFile().getParentFile().getAbsolutePath();
			}
			catch (Exception e2) {
				directory = getDefaultFile().getParentFile().getPath();
			}
		}
		return directory;
	}

}
