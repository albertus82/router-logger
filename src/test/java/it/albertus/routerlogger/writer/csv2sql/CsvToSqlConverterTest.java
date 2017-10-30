package it.albertus.routerlogger.writer.csv2sql;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import it.albertus.util.IOUtils;

public class CsvToSqlConverterTest {

	@Test
	public void testConvertUsingStreams() throws IOException {
		final CsvToSqlConverter converter = new CsvToSqlConverter("my_table", "prefix_", "timestamp", "response_time_ms", 20, ";", "dd/MM/yyyy HH:mm:ss.SSS");
		final String converted = convert(converter, "test_ok.csv");
		verify(converted, "test_ok.sql");
	}

	@Test
	public void testException() {
		final CsvToSqlConverter converter = new CsvToSqlConverter("my_table", "prefix_", "timestamp", "response_time_ms", 20, ";", "yyyy/MM/dd");
		try {
			convert(converter, "test_ok.csv");
			Assert.assertTrue(false);
		}
		catch (final IOException e) {
			Assert.assertEquals(ParseException.class, e.getCause().getClass());
		}
	}

	private String convert(final CsvToSqlConverter converter, final String csvFileName) throws IOException {
		InputStream r1 = null;
		InputStreamReader r2 = null;
		LineNumberReader r3 = null;

		StringWriter w1 = null;
		BufferedWriter w2 = null;

		try {
			r1 = getClass().getResourceAsStream(csvFileName);
			r2 = new InputStreamReader(r1, Charset.forName("UTF-8"));
			r3 = new LineNumberReader(r2);

			w1 = new StringWriter();
			w2 = new BufferedWriter(w1);

			converter.convert(csvFileName, r3, w2);
		}
		finally {
			IOUtils.closeQuietly(w2, w1, r3, r2, r1);
		}

		String sql = w1.toString();
		return sql;
	}

	private void verify(final String sql, final String sqlFileName) throws IOException {
		InputStream e1 = null;
		Reader e2 = null;
		BufferedReader e3 = null;

		Reader a1 = null;
		BufferedReader a2 = null;

		try {
			e1 = getClass().getResourceAsStream(sqlFileName);
			e2 = new InputStreamReader(e1, Charset.forName("UTF-8"));
			e3 = new BufferedReader(e2);

			a1 = new StringReader(sql);
			a2 = new BufferedReader(a1);

			String line;
			while ((line = e3.readLine()) != null) {
				Assert.assertEquals(a2.readLine(), line);
			}
		}
		finally {
			IOUtils.closeQuietly(a2, a1, e3, e2, e1);
		}
	}

}
