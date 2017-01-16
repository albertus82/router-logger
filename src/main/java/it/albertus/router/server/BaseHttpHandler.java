package it.albertus.router.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.protocol.HttpDateGenerator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.util.Logger;
import it.albertus.router.util.LoggerFactory;
import it.albertus.util.CRC32OutputStream;
import it.albertus.util.DigestOutputStream;
import it.albertus.util.IOUtils;

public abstract class BaseHttpHandler implements HttpHandler {

	private static final Logger logger = LoggerFactory.getLogger(BaseHttpHandler.class);

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
			logger.error(re);
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

	protected void addEtagHeader(final HttpExchange exchange, final String eTag) {
		if (eTag != null) {
			exchange.getResponseHeaders().add("ETag", eTag);
		}
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
				logger.error(ioe);
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

	/*
	 * The MD5 digest is computed based on the content of the entity-body,
	 * including any content-coding that has been applied, but not including any
	 * transfer-encoding applied to the message-body. If the message is received
	 * with a transfer-encoding, that encoding MUST be removed prior to checking
	 * the Content-MD5 value against the received entity.
	 */
	protected String generateContentMd5(final File file) throws NoSuchAlgorithmException, IOException {
		FileInputStream fis = null;
		DigestOutputStream dos = null;
		try {
			fis = new FileInputStream(file);
			dos = new DigestOutputStream("MD5");
			IOUtils.copy(fis, dos, BUFFER_SIZE);
		}
		finally {
			IOUtils.closeQuietly(dos, fis);
		}
		return DatatypeConverter.printBase64Binary(dos.getValue());
	}

	protected String generateContentMd5(final byte[] responseBody) throws NoSuchAlgorithmException {
		final MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update(responseBody);
		return DatatypeConverter.printBase64Binary(digest.digest());
	}

	protected void addContentMd5Header(final HttpExchange exchange, final File file) {
		try {
			exchange.getResponseHeaders().add("Content-MD5", generateContentMd5(file));
		}
		catch (final Exception e) {
			logger.error(e);
		}
	}

	protected void addContentMd5Header(final HttpExchange exchange, final byte[] responseBody) {
		try {
			exchange.getResponseHeaders().add("Content-MD5", generateContentMd5(responseBody));
		}
		catch (final Exception e) {
			logger.error(e);
		}
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
