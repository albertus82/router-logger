package it.albertus.router.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;

import it.albertus.util.IOUtils;

public class BaseHttpHandlerTest {

	public static class DummyHttpHandler extends BaseHttpHandler {
		@Override
		public void handle(final HttpExchange exchange) {}

		@Override
		public String getPath() {
			return null;
		}

		@Override
		protected void addCommonHeaders(final HttpExchange exchange) {}
	}

	private static BaseHttpHandler handler;

	@BeforeClass
	public static void init() {
		handler = new DummyHttpHandler();
	}

	@Test
	public void generateEtagTest() throws IOException {
		final String str = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
		Assert.assertEquals("98b2c5bd", handler.generateEtag(str.getBytes("UTF-8")));

		final File file = File.createTempFile(BaseHttpHandlerTest.class.getSimpleName() + '-', null);
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			bw.write(str);
			System.out.println("Created temporary file \"" + file + '"');
		}
		finally {
			IOUtils.closeQuietly(bw, fw);
		}
		Assert.assertEquals(str.length(), file.length());

		Assert.assertEquals("98b2c5bd", handler.generateEtag(file));

		if (file.delete()) {
			System.out.println("Deleted temporary file \"" + file + '"');
		}
		else {
			System.err.println("Cannot delete temporary file \"" + file + '"');
			file.deleteOnExit();
		}
	}

}
