package it.albertus.router.mqtt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import it.albertus.httpserver.HttpDateGenerator;
import it.albertus.util.IOUtils;
import it.albertus.util.NewLine;
import it.albertus.util.logging.LoggerFactory;

public class MqttPayload implements Serializable {

	private static final long serialVersionUID = -6295841307108552836L;

	private static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
	private static final String HEADER_CONTENT_LENGTH = "Content-Length";
	private static final String HEADER_DATE = "Date";

	private static final String CHARSET = "UTF-8";
	private static final byte[] CRLF;

	static {
		try {
			CRLF = NewLine.CRLF.toString().getBytes(CHARSET);
		}
		catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException(e); // UTF-8 must be supported by any JVM
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(MqttPayload.class);

	private static final ThreadLocal<HttpDateGenerator> httpDateGenerator = new ThreadLocal<HttpDateGenerator>() {
		@Override
		protected HttpDateGenerator initialValue() {
			return new HttpDateGenerator();
		}
	};

	private final Map<String, String> headers = new TreeMap<String, String>();
	private final byte[] body;

	public MqttPayload(final String body, final boolean compress) {
		if (body == null) {
			throw new NullPointerException("body cannot be null");
		}
		this.body = createBody(body, compress);
	}

	private byte[] createBody(final String str, final boolean compress) {
		try {
			return createBody(str.getBytes(CHARSET), compress);
		}
		catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException(e); // UTF-8 must be supported by any JVM
		}
	}

	private byte[] createBody(final byte[] bytes, final boolean compress) {
		if (compress) {
			GZIPOutputStream gzos = null;
			try {
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				gzos = new GZIPOutputStream(baos);
				gzos.write(bytes);
				gzos.close();
				headers.put(HEADER_CONTENT_ENCODING, "gzip");
				return baos.toByteArray();
			}
			catch (final IOException e) {
				logger.log(Level.WARNING, e.toString(), e);
				return bytes;
			}
			finally {
				IOUtils.closeQuietly(gzos);
			}
		}
		else {
			return bytes;
		}
	}

	public byte[] getBytes() {
		headers.put(HEADER_CONTENT_LENGTH, Integer.toString(body.length));
		headers.put(HEADER_DATE, httpDateGenerator.get().format(new Date()));

		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (final Entry<String, String> header : headers.entrySet()) {
				baos.write((header.getKey() + ": " + header.getValue()).getBytes(CHARSET));
				baos.write(CRLF);
			}
			baos.write(CRLF);
			baos.write(body);
			return baos.toByteArray();
		}
		catch (final IOException e) {
			throw new IllegalStateException(e); // UTF-8 must be supported by any JVM
		}
	}

}
