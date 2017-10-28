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

	private final DateFormat ansiSqlFormat = new SimpleDateFormat("yyyy-M-dd HH:mm:ss.SSS"); // '1998-3-24 04:21:23.456'

	private final DateFormat csvDateFormat;
	private final String tableName;
	private final String columnNamesPrefix;
	private final String timestampColumnName;
	private final String responseTimeColumnName;
	private final int maxLengthColumnNames;

	public CsvToSqlTransformer(final String tableName, final String columnNamesPrefix, final String timestampColumnName, final String timestampDateFormat, final String responseTimeColumnName, final int maxLengthColumnNames) {
		this.tableName = tableName;
		this.columnNamesPrefix = columnNamesPrefix;
		this.timestampColumnName = timestampColumnName;
		this.responseTimeColumnName = responseTimeColumnName;
		this.maxLengthColumnNames = maxLengthColumnNames;
		this.csvDateFormat = new SimpleDateFormat(timestampDateFormat);
	}

	protected void transform(final File csvFile) throws ParseException, IOException {
		FileReader fr = null;
		BufferedReader br = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fr = new FileReader(csvFile);
			br = new BufferedReader(fr);
			final String csvPath = csvFile.getAbsolutePath();
			final String sqlPath;
			if (csvPath.toLowerCase().endsWith(".csv")) {
				sqlPath = csvPath.substring(0, csvPath.lastIndexOf('.')) + ".sql";
			}
			else {
				sqlPath = csvPath + ".sql";
			}
			final File sqlFile = new File(sqlPath);
			if (sqlFile.exists() || sqlFile.isDirectory()) {
				throw new IllegalStateException("File " + sqlFile + " already exists or is a directory.");
			}
			fw = new FileWriter(sqlFile);
			bw = new BufferedWriter(fw);
			final String firstLine = br.readLine();
			final List<String> sqlColumnNames = new ArrayList<String>();
			if (firstLine != null) {
				final String[] csvColumnNames = firstLine.split(";");
				sqlColumnNames.add(getSqlColumnName(timestampColumnName, columnNamesPrefix, maxLengthColumnNames));
				sqlColumnNames.add(getSqlColumnName(responseTimeColumnName, columnNamesPrefix, maxLengthColumnNames));
				for (int i = 2; i < csvColumnNames.length; i++) {
					sqlColumnNames.add(getSqlColumnName(csvColumnNames[i], columnNamesPrefix, maxLengthColumnNames));
				}
				String line;
				while ((line = br.readLine()) != null) {
					bw.append("INSERT INTO ").append(tableName).append(" (");
					final String[] values = line.split(";");
					for (int i = 0; i < values.length; i++) {
						bw.append(sqlColumnNames.get(i));
						if (i != values.length - 1) {
							bw.append(',');
						}
					}
					bw.append(") VALUES (TIMESTAMP '").append(ansiSqlFormat.format(csvDateFormat.parse(values[0]))).append("',").append(values[1]).append(',');
					for (int i = 2; i < values.length; i++) {
						bw.append('\'').append(values[i].replace("'", "''")).append('\'');
						if (i != values.length - 1) {
							bw.append(',');
						}
					}
					bw.append(");");
					bw.newLine();
				}
				bw.write("COMMIT;");
				bw.newLine();
			}
		}
		finally {
			IOUtils.closeQuietly(bw, fw, br, fr);
		}
	}

	protected String getSqlColumnName(final String name, final String prefix, final int maxLength) {
		String completeName = SqlUtils.sanitizeName(prefix + name);
		if (completeName.length() > maxLength) {
			completeName = completeName.substring(0, maxLength);
		}
		return completeName;
	}

	public static void main(String[] args) throws ParseException, IOException {
		new CsvToSqlTransformer("ROUTER_LOG", "RL_", "TIMESTAMP", "dd/MM/yyyy HH:mm:ss.SSS", "RESPONSE_TIME_MS", 30).transform(new File(args[0]));
	}

}
