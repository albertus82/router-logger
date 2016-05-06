package it.albertus.router.email;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.util.Configuration;
import it.albertus.util.ConfigurationException;

import java.io.File;

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

	public static final String CFG_KEY_EMAIL_FROM_ADDRESS = "email.from.address";
	public static final String CFG_KEY_EMAIL_HOST = "email.host";

	public interface Defaults {
		int PORT = 25;
		String SSL_PORT = "465";
		boolean SSL_CONNECT = false;
		boolean SSL_IDENTITY = false;
		boolean STARTTLS_ENABLED = false;
		boolean STARTTLS_REQUIRED = false;
	}

	protected final Configuration configuration = RouterLoggerConfiguration.getInstance();

	public String send(final String subject, final String message, final File... attachments) throws EmailException {
		checkConfiguration();
		final Email email;
		if (attachments != null && attachments.length > 0) {
			final MultiPartEmail multiPartEmail = new MultiPartEmail();
			for (final File attachment : attachments) {
				addAttachment(attachment, multiPartEmail);
			}
			email = multiPartEmail;
		}
		else {
			email = new SimpleEmail();
		}
		initializeEmail(email);
		createContents(email, subject, message, attachments);
		return email.send();
	}

	protected void checkConfiguration() {
		// Configuration check
		if (!configuration.contains(CFG_KEY_EMAIL_HOST)) {
			throw new ConfigurationException(Resources.get("err.email.cfg.error") + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), CFG_KEY_EMAIL_HOST);
		}
		if (!configuration.contains(CFG_KEY_EMAIL_FROM_ADDRESS)) {
			throw new ConfigurationException(Resources.get("err.email.cfg.error") + ' ' + Resources.get("err.review.cfg", configuration.getFileName()), CFG_KEY_EMAIL_FROM_ADDRESS);
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
		if (!configuration.getString("email.to.addresses", "").isEmpty()) {
			email.addTo(configuration.getString("email.to.addresses").split("[,;]+"));
		}
		if (!configuration.getString("email.cc.addresses", "").isEmpty()) {
			email.addCc(configuration.getString("email.cc.addresses").split("[,;]+"));
		}
		if (!configuration.getString("email.bcc.addresses", "").isEmpty()) {
			email.addBcc(configuration.getString("email.bcc.addresses").split("[,;]+"));
		}
	}

	protected void createContents(final Email email, final String subject, final String message, final File... attachments) throws EmailException {
		email.setSubject(subject);
		email.setMsg(message);
	}

}
