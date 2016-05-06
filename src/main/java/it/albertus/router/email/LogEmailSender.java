package it.albertus.router.email;

import it.albertus.router.resources.Resources;

import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.mail.EmailException;

/** Singleton. */
public class LogEmailSender {

	private static class Singleton {
		private static final LogEmailSender instance = new LogEmailSender();
	}

	public static LogEmailSender getInstance() {
		return Singleton.instance;
	}

	private LogEmailSender() {}

	/** Composition over inheritance. */
	private final EmailSender emailSender = EmailSender.getInstance();

	public String send(final String log, final Date date) throws EmailException {
		final String subject = Resources.get("msg.log.email.subject", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Resources.getLanguage().getLocale()).format(date));
		return emailSender.send(subject, log);
	}

	public String send(final String log) throws EmailException {
		return send(log, new Date());
	}

}
