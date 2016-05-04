package it.albertus.router.writer;

import it.albertus.router.engine.RouterData;
import it.albertus.router.resources.Resources;
import it.albertus.util.ConfigurationException;
import it.albertus.util.Console;
import it.albertus.util.NewLine;
import it.albertus.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

public class CsvWriter extends Writer {

	public static final String DESTINATION_KEY = "lbl.writer.destination.csv";

	protected static final String LINE_SEPARATOR = NewLine.SYSTEM_LINE_SEPARATOR;
	protected static final String CSV_FILENAME_REGEX = "[0-9]{8}\\.(csv|CSV)";
	protected static final String CSV_FILE_EXTENSION = ".csv";
	protected static final String ZIP_FILE_EXTENSION = ".zip";

	protected static final DateFormat dateFormatColumn = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	protected static final DateFormat dateFormatFileName = new SimpleDateFormat("yyyyMMdd");

	public interface Defaults {
		NewLine NEWLINE = LINE_SEPARATOR != null ? NewLine.getEnum(LINE_SEPARATOR) : NewLine.CRLF;
		String DIRECTORY = getDefaultDirectory();
		String FIELD_SEPARATOR = ";";
		String FIELD_SEPARATOR_REPLACEMENT = ",";
		int EMAIL_PORT = 25;
		boolean EMAIL_SSL_CONNECT = false;
		boolean EMAIL_SSL_IDENTITY = false;
		boolean EMAIL_STARTTLS_ENABLED = false;
		boolean EMAIL_STARTTLS_REQUIRED = false;
		String EMAIL_SSL_PORT = "465";
	}

	protected BufferedWriter csvFileWriter = null;
	protected File csvFile = null;
	
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
		final String cfgKey = "csv.newline.characters";
		final String cfg = configuration.getString(cfgKey);
		if (cfg == null || cfg.length() == 0) {
			return Defaults.NEWLINE.toString();
		}
		else {
			final NewLine newLine = NewLine.getEnum(cfg);
			if (newLine != null) {
				return newLine.toString();
			}
			else {
				throw new ConfigurationException(Resources.get("err.invalid.cfg", cfgKey) + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), cfgKey);
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
			catch (IOException ioe) {
				logger.log(ioe);
			}
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

	private void sendEmail() {
		if (configuration.getBoolean("email.active", false)) {
			new Thread("zipAndSendMail") {
				@Override
				public void run() {
					final File currentDestinationFile = getDestinationFile();
					for (final File file : currentDestinationFile.getParentFile().listFiles()) {
						if (!file.equals(currentDestinationFile) && file.getName().matches(CSV_FILENAME_REGEX)) {
							try {
								final File zipFile = zipCsvFile(file);
								if (testZipFile(zipFile)) {
									sendEmail(zipFile);
									out.println(Resources.get("msg.writer.csv.email.sent", zipFile.getName()), true);
									file.delete();
								}
								else {
									file.delete();
									throw new ZipException("ZIP file verification failed for " + file.getPath() + '.');
								}
							}
							catch (final Exception e) {
								logger.log(e);
							}
						}
					}
				}
			}.start();
		}
	}

	private String sendEmail(final File zipFile) throws EmailException {
		// TODO configuration check.
		final MultiPartEmail email = new MultiPartEmail();
		email.setStartTLSEnabled(configuration.getBoolean("email.starttls.enabled", Defaults.EMAIL_STARTTLS_ENABLED));
		email.setStartTLSRequired(configuration.getBoolean("email.starttls.required", Defaults.EMAIL_STARTTLS_REQUIRED));
		email.setSSLCheckServerIdentity(configuration.getBoolean("email.ssl.identity", Defaults.EMAIL_SSL_IDENTITY));
		email.setSSLOnConnect(configuration.getBoolean("email.ssl.connect", Defaults.EMAIL_SSL_CONNECT));
		email.setSmtpPort(configuration.getInt("email.port", Defaults.EMAIL_PORT));
		email.setSslSmtpPort(configuration.getString("email.ssl.port", Defaults.EMAIL_SSL_PORT));

		email.setHostName(configuration.getString("email.host"));

		// Authentication
		if (!configuration.getString("email.username", "").isEmpty() && !configuration.getString("email.password", "").isEmpty()) {
			email.setAuthenticator(new DefaultAuthenticator(configuration.getString("email.username"), configuration.getString("email.password")));
		}

		// Sender
		if (configuration.getString("email.from.name", "").isEmpty()) {
			email.setFrom(configuration.getString("email.from.address"));
		}
		else {
			email.setFrom(configuration.getString("email.from.address"), configuration.getString("email.from.name"));
		}

		// Recipients
		if (!configuration.getString("email.to.addresses", "").isEmpty()) {
			email.addTo(configuration.getString("email.to.addresses").split("[,;]+"));
		}
		if (!configuration.getString("email.cc.addresses", "").isEmpty()) {
			email.addCc(configuration.getString("email.cc.addresses").split("[,;]+"));
		}
		if (!configuration.getString("email.bcc.addresses", "").isEmpty()) {
			email.addBcc(configuration.getString("email.bcc.addresses").split("[,;]+"));
		}

		// Contents
		String formattedDate = zipFile.getName();
		try {
			formattedDate = DateFormat.getDateInstance(DateFormat.LONG, Resources.getLanguage().getLocale()).format(dateFormatFileName.parse(formattedDate.substring(0, formattedDate.indexOf('.'))));
		}
		catch (final Exception e) {
			formattedDate = e.getClass().getSimpleName();
		}
		email.setSubject(Resources.get("msg.writer.csv.email.subject", formattedDate));
		email.setMsg(Resources.get("msg.writer.csv.email.message", zipFile.getName()));

		final EmailAttachment attachment = new EmailAttachment();
		attachment.setPath(zipFile.getPath());
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		attachment.setDescription(zipFile.getName());
		attachment.setName(zipFile.getName());
		email.attach(attachment);

		return email.send();
	}

	private File zipCsvFile(final File csvFile) throws IOException {
		final File zipFile = new File(csvFile.getPath().replace(CSV_FILE_EXTENSION, ZIP_FILE_EXTENSION));
		if (!zipFile.exists() || !testZipFile(zipFile)) {
			ZipOutputStream output = null;
			InputStream input = null;
			try {
				input = new BufferedInputStream(new FileInputStream(csvFile));
				output = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
				output.setLevel(Deflater.BEST_COMPRESSION);
				output.putNextEntry(new ZipEntry(csvFile.getName()));
				final byte[] buffer = new byte[1024];
				int length;
				while ((length = input.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}
				output.closeEntry();
			}
			finally {
				try {
					output.close();
				}
				catch (final Exception e) {}
				try {
					input.close();
				}
				catch (final Exception e) {}
			}
		}
		return zipFile;
	}

	private boolean testZipFile(final File zipFile) {
		ZipFile zf = null;
		ZipInputStream zis = null;
		try {
			zf = new ZipFile(zipFile);
			zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();
			if (ze == null) {
				return false;
			}
			while (ze != null) {
				zf.getInputStream(ze);
				ze.getCrc();
				ze.getCompressedSize();
				ze.getName();
				ze = zis.getNextEntry();
			}
		}
		catch (final Exception exception) {
			return false;
		}
		finally {
			try {
				zf.close();
			}
			catch (final Exception e) {}
			try {
				zis.close();
			}
			catch (final Exception e) {}
		}
		return true;
	}

}
