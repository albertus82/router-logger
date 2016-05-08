package it.albertus.router.email;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.Configuration;
import it.albertus.util.ConfigurationException;
import it.albertus.util.Console;
import it.albertus.util.TerminalConsole;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

/** Singleton. */
public class EmailSender {

	public static final String EMAIL_ADDRESSES_SPLIT_REGEX = "[,;\\s]+";

	protected static final String CFG_KEY_EMAIL_HOST = "email.host";
	protected static final String CFG_KEY_EMAIL_USERNAME = "email.username";
	protected static final String CFG_KEY_EMAIL_PASSWORD = "email.password";
	protected static final String CFG_KEY_EMAIL_FROM_NAME = "email.from.name";
	protected static final String CFG_KEY_EMAIL_FROM_ADDRESS = "email.from.address";
	protected static final String CFG_KEY_EMAIL_TO_ADDRESSES = "email.to.addresses";
	protected static final String CFG_KEY_EMAIL_CC_ADDRESSES = "email.cc.addresses";
	protected static final String CFG_KEY_EMAIL_BCC_ADDRESSES = "email.bcc.addresses";

	public interface Defaults {
		int PORT = 25;
		String SSL_PORT = "465";
		boolean SSL_CONNECT = false;
		boolean SSL_IDENTITY = false;
		boolean STARTTLS_ENABLED = false;
		boolean STARTTLS_REQUIRED = false;
		long SEND_INTERVAL_IN_MILLIS = 60000L;
	}

	private static class Singleton {
		private static final EmailSender instance = new EmailSender();
	}

	public static EmailSender getInstance() {
		return Singleton.instance;
	}

	protected final Configuration configuration = RouterLoggerConfiguration.getInstance();
	protected final Queue<RouterLoggerEmail> queue = new ConcurrentLinkedQueue<RouterLoggerEmail>();
	protected volatile Thread daemon;
	protected Console out = TerminalConsole.getInstance(); // Fail-safe.

	private final Object lock = new Object();

	protected class EmailRunnable implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(configuration.getLong("email.send.interval.ms", Defaults.SEND_INTERVAL_IN_MILLIS));
				}
				catch (final InterruptedException ie) {
					break;
				}
				final List<RouterLoggerEmail> sent = new ArrayList<RouterLoggerEmail>(queue.size());
				for (final RouterLoggerEmail email : queue) {
					try {
						send(email);
						sent.add(email);
					}
					catch (final Exception exception) {
						Logger.getInstance().log(exception, Destination.CONSOLE);
					}
				}
				queue.removeAll(sent);

				// Exit if there is nothing to do...
				synchronized (lock) {
					if (queue.isEmpty()) {
						daemon = null;
						break;
					}
				}
			}
		}
	}

	public void init(final Console console) {
		this.out = console;
	}

	/**
	 * Try to send the message immediately. On error, enqueue the message and
	 * try later.
	 * 
	 * @param subject the subject of the email
	 * @param message the body of the email
	 * @param attachments the attachments of the email
	 */
	public void reserve(final String subject, final String message, final File... attachments) {
		final RouterLoggerEmail email = new RouterLoggerEmail(subject, message, attachments);
		try {
			send(email);
		}
		catch (final Exception exception) {
			synchronized (lock) {
				queue.add(email);
				if (this.daemon == null) {
					daemon = new Thread(new EmailRunnable(), "emailDaemon");
					daemon.setDaemon(true);
					daemon.start();
				}
			}
			Logger.getInstance().log(exception, Destination.CONSOLE);
		}
	}

	/**
	 * Send the message immediately. <b>This operation may take many
	 * seconds</b>, so calling from a separate thread can be appropriate.
	 * 
	 * @param subject the subject of the email
	 * @param message the body of the email
	 * @param attachments the attachments of the email
	 * @return the message id of the underlying MimeMessage
	 * @throws EmailException the sending failed
	 */
	public String send(final String subject, final String message, final File... attachments) throws EmailException {
		final RouterLoggerEmail email = new RouterLoggerEmail(subject, message, attachments);
		return send(email);
	}

	protected String send(final RouterLoggerEmail rle) throws EmailException {
		checkConfiguration();
		final Email email;
		if (rle.getAttachments() != null && rle.getAttachments().length > 0) {
			final MultiPartEmail multiPartEmail = new MultiPartEmail();
			for (final File attachment : rle.getAttachments()) {
				addAttachment(multiPartEmail, attachment);
			}
			email = multiPartEmail;
		}
		else {
			email = new SimpleEmail();
		}
		initializeEmail(email);
		createContents(email, rle);
		final String mimeMessageId = email.send();
		out.println(Resources.get("msg.email.sent", rle.getSubject()), true);
		return mimeMessageId;
	}

	protected void checkConfiguration() {
		// Configuration check
		if (configuration.getString(CFG_KEY_EMAIL_HOST, "").isEmpty()) {
			throw new ConfigurationException(Resources.get("err.email.cfg.error") + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), CFG_KEY_EMAIL_HOST);
		}
		if (configuration.getString(CFG_KEY_EMAIL_FROM_ADDRESS, "").isEmpty()) {
			throw new ConfigurationException(Resources.get("err.email.cfg.error") + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), CFG_KEY_EMAIL_FROM_ADDRESS);
		}
		if (configuration.getString(CFG_KEY_EMAIL_TO_ADDRESSES, "").isEmpty() && configuration.getString(CFG_KEY_EMAIL_CC_ADDRESSES, "").isEmpty() && configuration.getString(CFG_KEY_EMAIL_BCC_ADDRESSES, "").isEmpty()) {
			throw new ConfigurationException(Resources.get("err.email.cfg.error") + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), CFG_KEY_EMAIL_TO_ADDRESSES);
		}
	}

	protected void addAttachment(final MultiPartEmail email, final File attachment) throws EmailException {
		final EmailAttachment emailAttachment = new EmailAttachment();
		emailAttachment.setPath(attachment.getPath());
		emailAttachment.setDisposition(EmailAttachment.ATTACHMENT);
		emailAttachment.setDescription(attachment.getName());
		emailAttachment.setName(attachment.getName());
		email.attach(emailAttachment);
	}

	protected void initializeEmail(final Email email) throws EmailException {
		email.setStartTLSEnabled(configuration.getBoolean("email.starttls.enabled", Defaults.STARTTLS_ENABLED));
		email.setStartTLSRequired(configuration.getBoolean("email.starttls.required", Defaults.STARTTLS_REQUIRED));
		email.setSSLCheckServerIdentity(configuration.getBoolean("email.ssl.identity", Defaults.SSL_IDENTITY));
		email.setSSLOnConnect(configuration.getBoolean("email.ssl.connect", Defaults.SSL_CONNECT));
		email.setSmtpPort(configuration.getInt("email.port", Defaults.PORT));
		email.setSslSmtpPort(configuration.getString("email.ssl.port", Defaults.SSL_PORT));

		email.setHostName(configuration.getString(CFG_KEY_EMAIL_HOST));

		// Authentication
		if (!configuration.getString(CFG_KEY_EMAIL_USERNAME, "").isEmpty() && !configuration.getString(CFG_KEY_EMAIL_PASSWORD, "").isEmpty()) {
			email.setAuthenticator(new DefaultAuthenticator(configuration.getString(CFG_KEY_EMAIL_USERNAME), configuration.getString(CFG_KEY_EMAIL_PASSWORD)));
		}

		// Sender
		if (configuration.getString(CFG_KEY_EMAIL_FROM_NAME, "").isEmpty()) {
			email.setFrom(configuration.getString(CFG_KEY_EMAIL_FROM_ADDRESS));
		}
		else {
			email.setFrom(configuration.getString(CFG_KEY_EMAIL_FROM_ADDRESS), configuration.getString(CFG_KEY_EMAIL_FROM_NAME));
		}

		// Recipients
		if (!configuration.getString(CFG_KEY_EMAIL_TO_ADDRESSES, "").isEmpty()) {
			email.addTo(configuration.getString(CFG_KEY_EMAIL_TO_ADDRESSES).split(EMAIL_ADDRESSES_SPLIT_REGEX));
		}
		if (!configuration.getString(CFG_KEY_EMAIL_CC_ADDRESSES, "").isEmpty()) {
			email.addCc(configuration.getString(CFG_KEY_EMAIL_CC_ADDRESSES).split(EMAIL_ADDRESSES_SPLIT_REGEX));
		}
		if (!configuration.getString(CFG_KEY_EMAIL_BCC_ADDRESSES, "").isEmpty()) {
			email.addBcc(configuration.getString(CFG_KEY_EMAIL_BCC_ADDRESSES).split(EMAIL_ADDRESSES_SPLIT_REGEX));
		}
	}

	protected void createContents(final Email email, final RouterLoggerEmail rle) throws EmailException {
		email.setSubject(rle.getSubject());
		email.setMsg(rle.getMessage());
	}

}
