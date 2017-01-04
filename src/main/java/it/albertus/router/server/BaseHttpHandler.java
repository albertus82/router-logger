package it.albertus.router.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.GZIPOutputStream;

import org.apache.http.protocol.HttpDateGenerator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.util.Logger;
import it.albertus.util.CRC32OutputStream;
import it.albertus.util.IOUtils;

public abstract class BaseHttpHandler implements HttpHandler {

	public static final String PREFERRED_CHARSET = "UTF-8";

	private static final int BUFFER_SIZE = 4096;

	protected static final HttpDateGenerator httpDateGenerator = new HttpDateGenerator();

	private static final Charset charset = initCharset();

	protected final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	protected final RouterLoggerEngine engine;

	protected BaseHttpHandler(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	protected BaseHttpHandler() {
		this.engine = null;
	}

	private static Charset initCharset() {
		try {
			return Charset.forName(PREFERRED_CHARSET);
		}
		catch (final RuntimeException re) {
			return Charset.defaultCharset();
		}
	}

	public abstract String getPath();

	protected abstract void addCommonHeaders(HttpExchange exchange);

	/**
	 * Adds {@code Date} header to the provided {@link HttpExchange} object.
	 * 
	 * @param exchange the {@link HttpExchange} to be modified.
	 */
	protected void addDateHeader(final HttpExchange exchange) {
		exchange.getResponseHeaders().add("Date", httpDateGenerator.getCurrentDate());
	}

	/**
	 * Adds {@code Content-Encoding: gzip} header to the provided
	 * {@link HttpExchange} object.
	 * 
	 * @param exchange the {@link HttpExchange} to be modified.
	 */
	protected void addGzipHeader(final HttpExchange exchange) {
		exchange.getResponseHeaders().add("Content-Encoding", "gzip");
	}

	protected boolean canCompressResponse(final HttpExchange exchange) {
		final List<String> headers = exchange.getRequestHeaders().get("Accept-Encoding");
		if (headers != null) {
			for (final String header : headers) {
				if (header != null && header.trim().toLowerCase().contains("gzip")) {
					return true;
				}
			}
		}
		return false;
	}

	protected byte[] compressResponse(final byte[] uncompressed, final HttpExchange exchange) {
		if (canCompressResponse(exchange)) {
			try {
				return doCompressResponse(uncompressed, exchange);
			}
			catch (final IOException ioe) {
				Logger.getInstance().log(ioe);
			}
		}
		return uncompressed;
	}

	protected byte[] doCompressResponse(final byte[] uncompressed, final HttpExchange exchange) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(uncompressed.length / 4);
		GZIPOutputStream gzos = null;
		try {
			gzos = new GZIPOutputStream(baos);
			gzos.write(uncompressed);
		}
		finally {
			IOUtils.closeQuietly(gzos, baos);
		}
		addGzipHeader(exchange);
		return baos.toByteArray();
	}

	protected String generateEtag(final byte[] payload) {
		final CRC32 crc = new CRC32();
		crc.update(payload);
		return Long.toHexString(crc.getValue());
	}

	protected String generateEtag(final File file) throws IOException {
		final CRC32OutputStream os = new CRC32OutputStream();
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			IOUtils.copy(is, os, BUFFER_SIZE);
		}
		finally {
			IOUtils.closeQuietly(os, is);
		}
		return os.toString();
	}

	protected Charset getCharset() {
		return charset;
	}

	/**
	 * Returns if this handler is enabled. <b>Handlers are enabled by
	 * default.</b> Requests to disabled handlers will be bounced with <b>HTTP
	 * Status-Code 403: Forbidden.</b>
	 * 
	 * @return {@code true} if this handler is enabled, otherwise {@code false}.
	 */
	public boolean isEnabled() {
		return true;
	}

}
