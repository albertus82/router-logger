package it.albertus.router.email;

import it.albertus.jface.preference.field.EmailAddressesListEditor;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.Configuration;
import it.albertus.util.ConfigurationException;
import it.albertus.util.Console;
import it.albertus.util.SystemConsole;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

/** Singleton. */
public class EmailSender {

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

	public interface Defaults {
		int PORT = 25;
		int SSL_PORT = 465;
		boolean SSL_CONNECT = false;
		boolean SSL_IDENTITY = false;
		boolean STARTTLS_ENABLED = false;
		boolean STARTTLS_REQUIRED = false;
		int SOCKET_TIMEOUT = EmailConstants.SOCKET_TIMEOUT_MS;
		int SOCKET_CONNECTION_TIMEOUT = EmailConstants.SOCKET_TIMEOUT_MS;
		int RETRY_INTERVAL_SECS = 60;
		int MAX_SENDINGS_PER_CYCLE = 3;
		byte MAX_QUEUE_SIZE = 10;
	}

	private static class Singleton {
		private static final EmailSender instance = new EmailSender();
	}

	public static EmailSender getInstance() {
		return Singleton.instance;
	}

	private EmailSender() {}

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();
	private final Queue<RouterLoggerEmail> queue = new ConcurrentLinkedQueue<RouterLoggerEmail>();
	private volatile Thread daemon;
	private Console out = SystemConsole.getInstance();

	private final Object lock = new Object();

	private class EmailRunnable implements Runnable {

		@Override
		public void run() {
			while (true) {
				final int maxSendingsPerCycle = configuration.getInt("email.max.sendings.per.cycle", Defaults.MAX_SENDINGS_PER_CYCLE);
				final List<RouterLoggerEmail> sent = new ArrayList<RouterLoggerEmail>(Math.min(queue.size(), maxSendingsPerCycle));
				for (final RouterLoggerEmail email : queue) {
					if (maxSendingsPerCycle <= 0 || sent.size() < maxSendingsPerCycle) {
						try {
							send(email);
							sent.add(email);
						}
						catch (final Exception exception) {
							Logger.getInstance().log(exception, Destination.CONSOLE);
						}
					}
					else {
						out.println(Resources.get("msg.email.limit", maxSendingsPerCycle), true);
						break;
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

				try {
					Thread.sleep(1000 * configuration.getInt("email.retry.interval.secs", Defaults.RETRY_INTERVAL_SECS));
				}
				catch (final InterruptedException ie) {
					break;
				}
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
					daemon = new Thread(new EmailRunnable(), "emailDaemon");
					daemon.setDaemon(true);
					daemon.start();
				}
			}
			else {
				Logger.getInstance().log(Resources.get("err.email.max.queue.size", maxQueueSize, subject), Destination.CONSOLE, Destination.FILE);
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
		out.println(Resources.get("msg.email.sent", rle.getSubject()), true);
		return mimeMessageId;
	}

	private void checkConfiguration() {
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

	private void createContents(final Email email, final RouterLoggerEmail rle) throws EmailException {
		email.setSubject(rle.getSubject());
		email.setMsg(rle.getMessage());
	}

}
