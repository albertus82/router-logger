package it.albertus.router.writer;

import it.albertus.util.ExceptionUtils;
import it.albertus.util.StringUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelWriter extends Writer {

	private static final DateFormat DATE_FORMAT_LOG = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	private static final DateFormat DATE_FORMAT_FILE_NAME = new SimpleDateFormat("yyyyMMdd");

	private OutputStream outputStream = null;
	private Workbook workbook = null;
	private File backupLogFile = null;

	@Override
	public synchronized void saveInfo(final Map<String, String> info) {
		// Selezione del percorso e nome del file di destinazione...
		final String logDestinationDir = configuration.getString("xls.destination.path");
		final File logFile;
		if (StringUtils.isNotBlank(logDestinationDir)) {
			File logDestDir = new File(logDestinationDir.trim());
			if (logDestDir.exists() && !logDestDir.isDirectory()) {
				throw new RuntimeException("Invalid path: \"" + logDestDir + "\".");
			}
			if (!logDestDir.exists()) {
				logDestDir.mkdirs();
			}
			logFile = new File(logDestinationDir.trim() + '/' + DATE_FORMAT_FILE_NAME.format(new Date()) + ".xls");
		}
		else {
			logFile = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() + '/' + DATE_FORMAT_FILE_NAME.format(new Date()) + ".xls");
		}
		backupLogFile = new File(logFile.getPath() + ".bak");

		try {
			if (logFile.exists()) { // Se il file esiste gia'...
				if (workbook == null) { // Se non l'ho ancora caricato...
					// Caricamento dati preesistenti...
					try {
						workbook = WorkbookFactory.create(logFile);
					}
					catch (Exception e) {
						// Se il documento esistente non e' valido...
						out.print(ExceptionUtils.getStackTrace(e));
						closeOutputFile();
						createNewFile(info);
					}
					out.println("Logging to: " + logFile.getAbsolutePath() + "...");
				}
			}
			else {
				// Creazione nuovo file XLS...
				closeOutputFile();
				out.println("Logging to: " + logFile.getAbsolutePath() + "...");
				createNewFile(info);
			}

			// Creazione riga file XLS...
			buildXlsRow(workbook, info);

			if (outputStream != null) {
				outputStream.close();
			}
			outputStream = new BufferedOutputStream(new FileOutputStream(logFile));
			workbook.write(outputStream);
			outputStream.flush();
			outputStream.close();

			// Backup...
			FileUtils.copyFile(logFile, backupLogFile);

			// Mantiene bloccato il file per evitare lock da altre applicazioni...
			outputStream = new FileOutputStream(logFile, true);
		}
		catch (IOException ioe) {
			out.print(ExceptionUtils.getStackTrace(ioe));
			closeOutputFile();
		}
	}

	private void createNewFile(final Map<String, String> info) {
		workbook = new HSSFWorkbook();
		workbook.createSheet(DATE_FORMAT_FILE_NAME.format(new Date()));
		buildXlsHeader(workbook, info);
	}

	private void buildXlsHeader(final Workbook workbook, final Map<String, String> info) {
		final Row row = workbook.getSheetAt(0).createRow(0);
		int col = 0;
		row.createCell(col).setCellValue("Timestamp");
		for (String field : info.keySet()) {
			row.createCell(++col).setCellValue(field);
		}
	}

	private void buildXlsRow(final Workbook workbook, final Map<String, String> info) {
		final Sheet sheet = workbook.getSheetAt(0);
		final Row row = sheet.createRow(sheet.getLastRowNum() + 1);
		int col = 0;
		row.createCell(col).setCellValue(DATE_FORMAT_LOG.format(new Date()));
		for (String field : info.values()) {
			row.createCell(++col).setCellValue(field);
		}
	}

	@Override
	public void release() {
		closeOutputFile();
		if (backupLogFile != null) {
			FileUtils.deleteQuietly(backupLogFile);
		}
	}

	private void closeOutputFile() {
		if (outputStream != null) {
			try {
				out.println("Closing output file.");
				outputStream.close();
				outputStream = null;
			}
			catch (IOException ioe) {
				out.print(ExceptionUtils.getStackTrace(ioe));
			}
		}
	}

}
