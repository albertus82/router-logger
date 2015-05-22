package it.albertus.router.writer;

import it.albertus.util.ExceptionUtils;
import it.albertus.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelWriter extends Writer {

	private static final DateFormat DATE_FORMAT_LOG = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	private static final DateFormat DATE_FORMAT_FILE_NAME = new SimpleDateFormat("yyyyMMdd");

	private OutputStream outputStream = null;

	@Override
	public void saveInfo(final Map<String, String> info) {
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

		try {
			Workbook workbook;
			if (logFile.exists()) {
				// Caricamento dati preesistenti....
				InputStream is = new FileInputStream(logFile);
				workbook = new HSSFWorkbook(is);
				is.close();
			}
			else {
				// Creazione nuovo file XLS...
				workbook = new HSSFWorkbook();
				workbook.createSheet(DATE_FORMAT_FILE_NAME.format(new Date()));
				buildXlsHeader(workbook, info);
			}

			// Creazione riga file XLS...
			buildXlsRow(workbook, info);

			if (outputStream != null) {
				outputStream.close();
			}
			else {
				out.println("Logging to: " + logFile.getAbsolutePath() + "...");
			}
			outputStream = new FileOutputStream(logFile);
			workbook.write(outputStream);
			outputStream.flush();
			outputStream.close();

			// Mantiene bloccato il file per evitare lock da altre applicazioni...
			outputStream = new FileOutputStream(logFile, true);
		}
		catch (IOException ioe) {
			out.print(ExceptionUtils.getStackTrace(ioe));
			closeOutputFile();
		}
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
