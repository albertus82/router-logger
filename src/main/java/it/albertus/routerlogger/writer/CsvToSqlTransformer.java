package it.albertus.routerlogger.writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import it.albertus.util.IOUtils;
import it.albertus.util.sql.SqlUtils;

public class CsvToSqlTransformer {

	protected static final String CSV_FILE_EXTENSION = ".csv";
	protected static final String SQL_FILE_EXTENSION = ".sql";

	private final DateFormat ansiSqlFormat = new SimpleDateFormat("yyyy-M-dd HH:mm:ss.SSS"); // '1998-3-24 04:21:23.456'

	private final String tableName;
	private final String columnNamesPrefix;
	private final String timestampColumnName;
	private final String responseTimeColumnName;
	private final int maxLengthColumnNames;
	private final String separator;
	private final DateFormat csvDateFormat;

	public CsvToSqlTransformer(final String tableName, final String columnNamesPrefix, final String timestampColumnName, final String responseTimeColumnName, final int maxLengthColumnNames, final String separator, final String timestampPattern) {
		this.tableName = tableName;
		this.columnNamesPrefix = columnNamesPrefix;
		this.timestampColumnName = timestampColumnName;
		this.responseTimeColumnName = responseTimeColumnName;
		this.maxLengthColumnNames = maxLengthColumnNames;
		this.separator = separator;
		this.csvDateFormat = new SimpleDateFormat(timestampPattern);
	}

	protected void transform(final File csvFile, final String destDir) throws ParseException, IOException {
		FileReader fr = null;
		BufferedReader br = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			final File sqlFile = getDestinationFile(csvFile, destDir);
			fr = new FileReader(csvFile);
			br = new BufferedReader(fr);
			final String firstLine = br.readLine();
			final List<String> sqlColumnNames = new ArrayList<String>();
			if (firstLine != null) {
				final String[] csvColumnNames = firstLine.split(separator);
				sqlColumnNames.add(getSqlColumnName(timestampColumnName, columnNamesPrefix, maxLengthColumnNames));
				sqlColumnNames.add(getSqlColumnName(responseTimeColumnName, columnNamesPrefix, maxLengthColumnNames));
				for (int i = 2; i < csvColumnNames.length; i++) {
					sqlColumnNames.add(getSqlColumnName(csvColumnNames[i], columnNamesPrefix, maxLengthColumnNames));
				}
				fw = new FileWriter(sqlFile);
				bw = new BufferedWriter(fw);
				String line;
				while ((line = br.readLine()) != null) {
					writeLine(line, bw, sqlColumnNames);
				}
				bw.write("COMMIT;");
				bw.newLine();
			}
		}
		finally {
			IOUtils.closeQuietly(bw, fw, br, fr);
		}
	}

	protected void writeLine(final String csv, final BufferedWriter sql, final List<? extends CharSequence> tableColumnNames) throws IOException, ParseException {
		sql.append("INSERT INTO ").append(tableName).append(" (");
		final String[] values = csv.split(separator);
		for (int i = 0; i < values.length; i++) {
			sql.append(tableColumnNames.get(i));
			if (i != values.length - 1) {
				sql.append(',');
			}
		}
		sql.append(") VALUES (TIMESTAMP '").append(ansiSqlFormat.format(csvDateFormat.parse(values[0]))).append("',").append(values[1]).append(',');
		for (int i = 2; i < values.length; i++) {
			sql.append('\'').append(values[i].replace("'", "''")).append('\'');
			if (i != values.length - 1) {
				sql.append(',');
			}
		}
		sql.append(");");
		sql.newLine();
	}

	protected File getDestinationFile(final File csvFile, final String destDir) {
		final String csvFileName = csvFile.getName();
		final String sqlFileName;
		if (csvFileName.toLowerCase().endsWith(CSV_FILE_EXTENSION)) {
			sqlFileName = csvFileName.substring(0, csvFileName.lastIndexOf('.')) + SQL_FILE_EXTENSION;
		}
		else {
			sqlFileName = csvFileName + SQL_FILE_EXTENSION;
		}
		final File sqlFile = new File(destDir + File.separator + sqlFileName);
		if (sqlFile.exists() || sqlFile.isDirectory()) {
			throw new IllegalStateException("File " + sqlFile + " already exists or is a directory.");
		}
		return sqlFile;
	}

	protected String getSqlColumnName(final String name, final String prefix, final int maxLength) {
		String completeName = SqlUtils.sanitizeName(prefix + name);
		if (completeName.length() > maxLength) {
			completeName = completeName.substring(0, maxLength);
		}
		return completeName;
	}

	public static void main(String[] args) throws ParseException, IOException {
		new CsvToSqlTransformer("ROUTER_LOG", "RL_", "TIMESTAMP", "RESPONSE_TIME_MS", 30, ";", "dd/MM/yyyy HH:mm:ss.SSS").transform(new File(args[0]), new File(args[0]).getParentFile().getPath());
	}

}
