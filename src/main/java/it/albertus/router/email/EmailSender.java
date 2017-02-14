package it.albertus.router.email;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

import it.albertus.jface.JFaceMessages;
import it.albertus.jface.preference.field.EmailAddressesListEditor;
import it.albertus.router.RouterLogger;
import it.albertus.router.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.ConfigurationException;
import it.albertus.util.logging.LoggerFactory;

/** Singleton. */
public class EmailSender {

	private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

	private static final Configuration configuration = RouterLogger.getConfiguration();

	private static final String EMAIL_ADDRESSES_SPLIT_REGEX = EmailAddressesListEditor.EMAIL_ADDRESSES_SPLIT_REGEX;

	private static final String CFG_KEY_EMAIL_HOST = "email.host";
	private static final String CFG_KEY_EMAIL_USERNAME = "email.username";
	private static final String CFG_KEY_EMAIL_PASSWORD = "email.password";
	private static final String CFG_KEY_EMAIL_FROM_NAME = "email.from.name";
	private static final String CFG_KEY_EMAIL_FROM_ADDRESS = "email.from.address";
	private static final String CFG_KEY_EMAIL_TO_ADDRESSES = "email.to.addresses";
	private static final String CFG_KEY_EMAIL_CC_ADDRESSES = "email.cc.addresses";
	private static final String CFG_KEY_EMAIL_BCC_ADDRESSES = "email.bcc.addresses";
	private static final String CFG_KEY_EMAIL_MAX_QUEUE_SIZE = "email.max.queue.size";

	private static final String MSG_KEY_ERR_EMAIL_CFG_ERROR = "err.email.cfg.error";
	private static final String MSG_KEY_ERR_REVIEW_CFG = "err.configuration.review";

	public static class Defaults {
		public static final int PORT = 25;
		public static final int SSL_PORT = 465;
		public static final boolean SSL_CONNECT = false;
		public static final boolean SSL_IDENTITY = false;
		public static final boolean STARTTLS_ENABLED = false;
		public static final boolean STARTTLS_REQUIRED = false;
		public static final int SOCKET_TIMEOUT = EmailConstants.SOCKET_TIMEOUT_MS;
		public static final int SOCKET_CONNECTION_TIMEOUT = EmailConstants.SOCKET_TIMEOUT_MS;
		public static final int RETRY_INTERVAL_SECS = 60;
		public static final int MAX_SENDINGS_PER_CYCLE = 3;
		public static final byte MAX_QUEUE_SIZE = 10;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private static class Singleton {
		private static final EmailSender instance = new EmailSender();

		private Singleton() {
			throw new IllegalAccessError();
		}
	}

	private final Queue<RouterLoggerEmail> queue = new ConcurrentLinkedQueue<RouterLoggerEmail>();
	private volatile Thread daemon;

	private final Object lock = new Object();

	private EmailSender() {}

	public static EmailSender getInstance() {
		return Singleton.instance;
	}

	private class EmailDaemon extends Thread {

		private EmailDaemon() {
			super(EmailDaemon.class.getSimpleName());
			setDaemon(true);
		}

		@Override
		public void run() {
			logger.fine(Messages.get("msg.thread.started", getName()));
			while (!isInterrupted()) {
				final int maxSendingsPerCycle = configuration.getInt("email.max.sendings.per.cycle", Defaults.MAX_SENDINGS_PER_CYCLE);
				final List<RouterLoggerEmail> sent = new ArrayList<RouterLoggerEmail>(Math.min(queue.size(), maxSendingsPerCycle));
				for (final RouterLoggerEmail email : queue) {
					if (maxSendingsPerCycle <= 0 || sent.size() < maxSendingsPerCycle) {
						processQueuedMessage(sent, email);
					}
					else {
						logger.info(Messages.get("msg.email.limit", maxSendingsPerCycle));
						break; // for
					}
				}
				queue.removeAll(sent);

				// Exit if there is nothing to do...
				synchronized (lock) {
					if (queue.isEmpty()) {
						daemon = null;
						break; // while
					}
				}

				try {
					TimeUnit.SECONDS.sleep(configuration.getInt("email.retry.interval.secs", Defaults.RETRY_INTERVAL_SECS));
				}
				catch (final InterruptedException e) {
					logger.log(Level.FINE, e.toString(), e);
					interrupt();
				}
			}
			logger.fine(Messages.get("msg.thread.terminated", getName()));
		}

		private void processQueuedMessage(final Collection<RouterLoggerEmail> sent, final RouterLoggerEmail email) {
			try {
				send(email);
				sent.add(email);
			}
			catch (final Exception e) {
				logger.log(Level.WARNING, e.toString(), e);
			}
		}
	}

	/**
	 * Enqueue the message for sending as soon as possible.
	 * 
	 * @param subject the subject of the email
	 * @param message the body of the email
	 * @param attachments the attachments of the email
	 */
	public void reserve(final String subject, final String message, final File... attachments) {
		final RouterLoggerEmail email = new RouterLoggerEmail(subject, message, attachments);
		synchronized (lock) {
			final byte maxQueueSize = configuration.getByte(CFG_KEY_EMAIL_MAX_QUEUE_SIZE, Defaults.MAX_QUEUE_SIZE);
			if (queue.size() < maxQueueSize) {
				queue.add(email);
				if (this.daemon == null) {
					daemon = new EmailDaemon();
					daemon.start();
				}
			}
			else {
				logger.warning(Messages.get("err.email.max.queue.size", maxQueueSize, subject));
			}
		}
	}

	/**
	 * Send the message immediately. <b>This operation may take a few
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

	private String send(final RouterLoggerEmail rle) throws EmailException {
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
		logger.info(Messages.get("msg.email.sent", rle.getSubject()));
		return mimeMessageId;
	}

	private void checkConfiguration() {
		// Configuration check
		if (configuration.getString(CFG_KEY_EMAIL_HOST, true).isEmpty()) {
			throw new ConfigurationException(Messages.get(MSG_KEY_ERR_EMAIL_CFG_ERROR) + ' ' + JFaceMessages.get(MSG_KEY_ERR_REVIEW_CFG, configuration.getFileName()), CFG_KEY_EMAIL_HOST);
		}
		if (configuration.getString(CFG_KEY_EMAIL_FROM_ADDRESS, true).isEmpty()) {
			throw new ConfigurationException(Messages.get(MSG_KEY_ERR_EMAIL_CFG_ERROR) + ' ' + JFaceMessages.get(MSG_KEY_ERR_REVIEW_CFG, configuration.getFileName()), CFG_KEY_EMAIL_FROM_ADDRESS);
		}
		if (configuration.getString(CFG_KEY_EMAIL_TO_ADDRESSES, true).isEmpty() && configuration.getString(CFG_KEY_EMAIL_CC_ADDRESSES, true).isEmpty() && configuration.getString(CFG_KEY_EMAIL_BCC_ADDRESSES, true).isEmpty()) {
			throw new ConfigurationException(Messages.get(MSG_KEY_ERR_EMAIL_CFG_ERROR) + ' ' + JFaceMessages.get(MSG_KEY_ERR_REVIEW_CFG, configuration.getFileName()), CFG_KEY_EMAIL_TO_ADDRESSES);
		}
	}

	private void addAttachment(final MultiPartEmail email, final File attachment) throws EmailException {
		final EmailAttachment emailAttachment = new EmailAttachment();
		emailAttachment.setPath(attachment.getPath());
		emailAttachment.setDisposition(EmailAttachment.ATTACHMENT);
		emailAttachment.setDescription(attachment.getName());
		emailAttachment.setName(attachment.getName());
		email.attach(emailAttachment);
	}

	private void initializeEmail(final Email email) throws EmailException {
		email.setSocketConnectionTimeout(configuration.getInt("email.connection.timeout", Defaults.SOCKET_CONNECTION_TIMEOUT));
		email.setSocketTimeout(configuration.getInt("email.socket.timeout", Defaults.SOCKET_TIMEOUT));
		email.setStartTLSEnabled(configuration.getBoolean("email.starttls.enabled", Defaults.STARTTLS_ENABLED));
		email.setStartTLSRequired(configuration.getBoolean("email.starttls.required", Defaults.STARTTLS_REQUIRED));
		email.setSSLCheckServerIdentity(configuration.getBoolean("email.ssl.identity", Defaults.SSL_IDENTITY));
		email.setSSLOnConnect(configuration.getBoolean("email.ssl.connect", Defaults.SSL_CONNECT));
		email.setSmtpPort(configuration.getInt("email.port", Defaults.PORT));
		email.setSslSmtpPort(Integer.toString(configuration.getInt("email.ssl.port", Defaults.SSL_PORT)));

		email.setHostName(configuration.getString(CFG_KEY_EMAIL_HOST));

		// Authentication
		if (!configuration.getString(CFG_KEY_EMAIL_USERNAME, true).isEmpty() && !configuration.getString(CFG_KEY_EMAIL_PASSWORD, true).isEmpty()) {
			email.setAuthenticator(new DefaultAuthenticator(configuration.getString(CFG_KEY_EMAIL_USERNAME), configuration.getString(CFG_KEY_EMAIL_PASSWORD)));
		}

		// Sender
		if (configuration.getString(CFG_KEY_EMAIL_FROM_NAME, true).isEmpty()) {
			email.setFrom(configuration.getString(CFG_KEY_EMAIL_FROM_ADDRESS));
		}
		else {
			email.setFrom(configuration.getString(CFG_KEY_EMAIL_FROM_ADDRESS), configuration.getString(CFG_KEY_EMAIL_FROM_NAME));
		}

		// Recipients
		if (!configuration.getString(CFG_KEY_EMAIL_TO_ADDRESSES, true).isEmpty()) {
			email.addTo(configuration.getString(CFG_KEY_EMAIL_TO_ADDRESSES).split(EMAIL_ADDRESSES_SPLIT_REGEX));
		}
		if (!configuration.getString(CFG_KEY_EMAIL_CC_ADDRESSES, true).isEmpty()) {
			email.addCc(configuration.getString(CFG_KEY_EMAIL_CC_ADDRESSES).split(EMAIL_ADDRESSES_SPLIT_REGEX));
		}
		if (!configuration.getString(CFG_KEY_EMAIL_BCC_ADDRESSES, true).isEmpty()) {
			email.addBcc(configuration.getString(CFG_KEY_EMAIL_BCC_ADDRESSES).split(EMAIL_ADDRESSES_SPLIT_REGEX));
		}
	}

	private void createContents(final Email email, final RouterLoggerEmail rle) throws EmailException {
		email.setSubject(rle.getSubject());
		email.setMsg(rle.getMessage());
	}

}
