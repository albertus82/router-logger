package it.albertus.router.email;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.Configuration;
import it.albertus.util.ConfigurationException;
import it.albertus.util.Console;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

/** Singleton. */
public class EmailSender {

	private static class Singleton {
		private static final EmailSender instance = new EmailSender();
	}

	public static EmailSender getInstance() {
		return Singleton.instance;
	}

	public static final String EMAIL_ADDRESSES_SPLIT_REGEX = "[,;\\s]+";

	public static final String CFG_KEY_EMAIL_FROM_ADDRESS = "email.from.address";
	public static final String CFG_KEY_EMAIL_HOST = "email.host";
	public static final String CFG_KEY_EMAIL_TO_ADDRESSES = "email.to.addresses";
	public static final String CFG_KEY_EMAIL_CC_ADDRESSES = "email.cc.addresses";
	public static final String CFG_KEY_EMAIL_BCC_ADDRESSES = "email.bcc.addresses";

	public interface Defaults {
		int PORT = 25;
		String SSL_PORT = "465";
		boolean SSL_CONNECT = false;
		boolean SSL_IDENTITY = false;
		boolean STARTTLS_ENABLED = false;
		boolean STARTTLS_REQUIRED = false;
		long SEND_INTERVAL_IN_MILLIS = 60000L;
	}

	protected class EmailRunnable implements Runnable {

		protected final Queue<RouterLoggerEmail> queue = new LinkedList<RouterLoggerEmail>();

		protected boolean exit = false;

		@Override
		public void run() {
			while (!exit) {
				final Queue<RouterLoggerEmail> tempQueue = new LinkedList<RouterLoggerEmail>(messagesToEnqueue);
				if (!tempQueue.isEmpty()) {
					queue.addAll(tempQueue);
					messagesToEnqueue.removeAll(tempQueue);
				}
				if (!queue.isEmpty()) {
					final List<RouterLoggerEmail> sentItems = new ArrayList<RouterLoggerEmail>();
					for (final RouterLoggerEmail rle : queue) {
						try {
							send(rle);
							sentItems.add(rle);
						}
						catch (final Exception exception) {
							logger.log(exception, Destination.CONSOLE);
						}
					}
					queue.removeAll(sentItems);
				}
				try {
					Thread.sleep(configuration.getLong("email.send.interval.ms", Defaults.SEND_INTERVAL_IN_MILLIS));
				}
				catch (final InterruptedException ie) {
					exit = true;
				}
			}
		}
	}

	protected final Configuration configuration = RouterLoggerConfiguration.getInstance();
	protected final Queue<RouterLoggerEmail> messagesToEnqueue = new LinkedList<RouterLoggerEmail>();
	protected final Thread emailThread;
	protected Logger logger;
	protected Console out;

	protected EmailSender() {
		emailThread = new Thread(new EmailRunnable(), "emailThread");
		emailThread.setDaemon(true);
	}

	public void init(final Console console, final Logger logger) {
		this.out = console;
		this.logger = logger;
		if (!emailThread.isAlive()) {
			emailThread.start();
		}
	}

	/**
	 * Try to send the message immediately. On error, enqueue the message and
	 * try later.
	 * 
	 * @param subject the subject of the email.
	 * @param message the body of the email.
	 * @param attachments the attachments of the email.
	 */
	public void reserve(final String subject, final String message, final File... attachments) {
		final RouterLoggerEmail rle = new RouterLoggerEmail(subject, message, attachments);
		try {
			send(rle);
		}
		catch (final Exception exception) {
			messagesToEnqueue.add(rle);
			logger.log(exception, Destination.CONSOLE);
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
		final RouterLoggerEmail rle = new RouterLoggerEmail(subject, message, attachments);
		return send(rle);
	}

	protected String send(final RouterLoggerEmail rle) throws EmailException {
		checkConfiguration();
		final Email email;
		if (rle.getAttachments() != null && rle.getAttachments().length > 0) {
			final MultiPartEmail multiPartEmail = new MultiPartEmail();
			for (final File attachment : rle.getAttachments()) {
				addAttachment(attachment, multiPartEmail);
			}
			email = multiPartEmail;
		}
		else {
			email = new SimpleEmail();
		}
		initializeEmail(email);
		createContents(email, rle.getSubject(), rle.getMessage(), rle.getAttachments());
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

	protected void addAttachment(final File attachment, final MultiPartEmail email) throws EmailException {
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
		if (!configuration.getString("email.username", "").isEmpty() && !configuration.getString("email.password", "").isEmpty()) {
			email.setAuthenticator(new DefaultAuthenticator(configuration.getString("email.username"), configuration.getString("email.password")));
		}

		// Sender
		if (configuration.getString("email.from.name", "").isEmpty()) {
			email.setFrom(configuration.getString(CFG_KEY_EMAIL_FROM_ADDRESS));
		}
		else {
			email.setFrom(configuration.getString(CFG_KEY_EMAIL_FROM_ADDRESS), configuration.getString("email.from.name"));
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

	protected void createContents(final Email email, final String subject, final String message, final File... attachments) throws EmailException {
		email.setSubject(subject);
		email.setMsg(message);
	}

}
