package it.albertus.router.reader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.albertus.router.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class DummyReader extends Reader {

	private static final Logger logger = LoggerFactory.getLogger(DummyReader.class);

	private static final byte CHARACTERS = 15;
	private static final byte COLUMNS = 30;

	private static final short LAG_IN_MILLIS = 100;
	private static final short CONNECTION_TIME_IN_MILLIS = 1000;
	private static final short AUTHENTICATION_TIME_IN_MILLIS = 1000;

	private static final double CONNECTION_ERROR_PERCENTAGE = 0.0;
	private static final double AUTHENTICATION_ERROR_PERCENTAGE = 0.0;
	private static final double READ_ERROR_PERCENTAGE = 0.0;

	@Override
	public boolean connect() {
		logger.info(Messages.get("msg.dummy.connect"));
		if (CONNECTION_TIME_IN_MILLIS > 0) {
			try {
				TimeUnit.MILLISECONDS.sleep(CONNECTION_TIME_IN_MILLIS);
			}
			catch (final InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		if (Math.random() > (100.0 - CONNECTION_ERROR_PERCENTAGE) / 100.0) {
			final Exception e = new ConnectException(Messages.get("msg.dummy.connect.error", CONNECTION_ERROR_PERCENTAGE));
			logger.log(Level.WARNING, e.toString(), e);
			return false;
		}
		return true;
	}

	@Override
	public boolean login(final String username, final char[] password) {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Username: {0}", username);
			logger.log(Level.INFO, "Password: {0}", password != null ? String.valueOf(password) : password);
		}
		if (AUTHENTICATION_TIME_IN_MILLIS > 0) {
			try {
				TimeUnit.MILLISECONDS.sleep(AUTHENTICATION_TIME_IN_MILLIS);
			}
			catch (final InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		if (Math.random() > (100.0 - AUTHENTICATION_ERROR_PERCENTAGE) / 100.0) {
			throw new SecurityException(Messages.get("msg.dummy.authentication.error", AUTHENTICATION_ERROR_PERCENTAGE));
		}
		final String message = getClass().getSimpleName() + " - " + Messages.get("msg.test.purposes.only");
		final StringBuilder separator = new StringBuilder();
		for (int c = 0; c < message.length(); c++) {
			separator.append('-');
		}
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintWriter pw = new PrintWriter(baos);
		pw.println(separator.toString());
		pw.println(message);
		pw.print(separator.toString());
		pw.close();
		logger.log(Level.INFO, LOG_MASK_TELNET, baos);
		return true;
	}

	@Override
	public LinkedHashMap<String, String> readInfo() throws IOException {
		final LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for (byte i = 1; i <= COLUMNS; i++) {
			StringBuilder field = new StringBuilder();
			for (byte j = 1; j <= CHARACTERS; j++) {
				field.append((char) (97 + Math.random() * 25));
			}
			map.put(Messages.get("lbl.column.number", i), field.toString());
		}
		if (LAG_IN_MILLIS != 0) {
			try {
				TimeUnit.MILLISECONDS.sleep(LAG_IN_MILLIS);
			}
			catch (final InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		if (Math.random() > (100.0 - READ_ERROR_PERCENTAGE) / 100.0) {
			throw new IOException(Messages.get("msg.dummy.readinfo.error", READ_ERROR_PERCENTAGE));
		}
		return map;
	}

	@Override
	public void logout() {
		logger.info(Messages.get("msg.dummy.logout"));
	}

	@Override
	public void disconnect() {
		logger.info(Messages.get("msg.dummy.disconnect"));
	}

}
