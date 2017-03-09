package it.albertus.router.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.protocol.HttpDateGenerator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import it.albertus.router.RouterLogger;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.util.CRC32OutputStream;
import it.albertus.util.DigestOutputStream;
import it.albertus.util.IOUtils;
import it.albertus.util.logging.LoggerFactory;

public abstract class BaseHttpHandler implements HttpHandler {

	private static final Logger logger = LoggerFactory.getLogger(BaseHttpHandler.class);

	private static final Map<Integer, String> httpStatusCodes;

	static {
		httpStatusCodes = new HashMap<Integer, String>();
		httpStatusCodes.put(100, "Continue");
		httpStatusCodes.put(101, "Switching Protocols");
		httpStatusCodes.put(102, "Processing");
		httpStatusCodes.put(200, "OK");
		httpStatusCodes.put(201, "Created");
		httpStatusCodes.put(202, "Accepted");
		httpStatusCodes.put(203, "Non-Authoritative Information");
		httpStatusCodes.put(204, "No Content");
		httpStatusCodes.put(205, "Reset Content");
		httpStatusCodes.put(206, "Partial Content");
		httpStatusCodes.put(207, "Multi-Status");
		httpStatusCodes.put(208, "Already Reported");
		httpStatusCodes.put(226, "IM Used");
		httpStatusCodes.put(300, "Multiple Choices");
		httpStatusCodes.put(301, "Moved Permanently");
		httpStatusCodes.put(302, "Found");
		httpStatusCodes.put(303, "See Other");
		httpStatusCodes.put(304, "Not Modified");
		httpStatusCodes.put(305, "Use Proxy");
		httpStatusCodes.put(307, "Temporary Redirect");
		httpStatusCodes.put(308, "Permanent Redirect");
		httpStatusCodes.put(400, "Bad Request");
		httpStatusCodes.put(401, "Unauthorized");
		httpStatusCodes.put(402, "Payment Required");
		httpStatusCodes.put(403, "Forbidden");
		httpStatusCodes.put(404, "Not Found");
		httpStatusCodes.put(405, "Method Not Allowed");
		httpStatusCodes.put(406, "Not Acceptable");
		httpStatusCodes.put(407, "Proxy Authentication Required");
		httpStatusCodes.put(408, "Request Timeout");
		httpStatusCodes.put(409, "Conflict");
		httpStatusCodes.put(410, "Gone");
		httpStatusCodes.put(411, "Length Required");
		httpStatusCodes.put(412, "Precondition Failed");
		httpStatusCodes.put(413, "Request Entity Too Large");
		httpStatusCodes.put(414, "Request-URI Too Long");
		httpStatusCodes.put(415, "Unsupported Media Type");
		httpStatusCodes.put(416, "Requested Range Not Satisfiable");
		httpStatusCodes.put(417, "Expectation Failed");
		httpStatusCodes.put(418, "I'm a teapot");
		httpStatusCodes.put(421, "Misdirected Request");
		httpStatusCodes.put(422, "Unprocessable Entity");
		httpStatusCodes.put(423, "Locked");
		httpStatusCodes.put(424, "Failed Dependency");
		httpStatusCodes.put(426, "Upgrade Required");
		httpStatusCodes.put(428, "Precondition Required");
		httpStatusCodes.put(429, "Too Many Requests");
		httpStatusCodes.put(431, "Request Header Fields Too Large");
		httpStatusCodes.put(451, "Unavailable For Legal Reasons");
		httpStatusCodes.put(500, "Internal Server Error");
		httpStatusCodes.put(501, "Not Implemented");
		httpStatusCodes.put(502, "Bad Gateway");
		httpStatusCodes.put(503, "Service Unavailable");
		httpStatusCodes.put(504, "Gateway Timeout");
		httpStatusCodes.put(505, "HTTP Version Not Supported");
		httpStatusCodes.put(506, "Variant Also Negotiates");
		httpStatusCodes.put(507, "Insufficient Storage");
		httpStatusCodes.put(508, "Loop Detected");
		httpStatusCodes.put(510, "Not Extended");
		httpStatusCodes.put(511, "Network Authentication Required");
	}

	protected static final RouterLoggerConfiguration configuration = RouterLogger.getConfiguration();

	protected static final HttpDateGenerator httpDateGenerator = new HttpDateGenerator();

	public static final String PREFERRED_CHARSET = "UTF-8";

	private static final int BUFFER_SIZE = 4096;

	private static final Charset charset = initCharset();

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
		catch (final RuntimeException e) {
			logger.log(Level.WARNING, e.toString(), e);
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
			catch (final IOException e) {
				logger.log(Level.WARNING, e.toString(), e);
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
			logger.log(Level.WARNING, e.toString(), e);
		}
	}

	protected void addContentMd5Header(final HttpExchange exchange, final byte[] responseBody) {
		try {
			exchange.getResponseHeaders().add("Content-MD5", generateContentMd5(responseBody));
		}
		catch (final Exception e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
	}

	protected void sendResponse(final HttpExchange exchange, final byte[] payload) throws IOException {
		final String currentEtag = generateEtag(payload);
		addEtagHeader(exchange, currentEtag);

		// If-None-Match...
		final String ifNoneMatch = exchange.getRequestHeaders().getFirst("If-None-Match");
		if (ifNoneMatch != null && currentEtag != null && currentEtag.equals(ifNoneMatch)) {
			addDateHeader(exchange);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, -1);
			exchange.getResponseBody().close(); // Needed when no write occurs.
		}
		else {
			addCommonHeaders(exchange);
			final byte[] response = compressResponse(payload, exchange);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
			exchange.getResponseBody().write(response);
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

	public static Map<Integer, String> getHttpStatusCodes() {
		return Collections.unmodifiableMap(httpStatusCodes);
	}

}
