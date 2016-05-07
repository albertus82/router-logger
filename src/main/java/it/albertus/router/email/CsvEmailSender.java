package it.albertus.router.email;

import it.albertus.router.resources.Resources;
import it.albertus.router.writer.CsvWriter;

import java.io.File;
import java.text.DateFormat;

import org.apache.commons.mail.EmailException;

/** Singleton. */
public class CsvEmailSender {

	private static class Singleton {
		private static final CsvEmailSender instance = new CsvEmailSender();
	}

	public static CsvEmailSender getInstance() {
		return Singleton.instance;
	}

	/** Composition over inheritance. */
	private final EmailSender emailSender = EmailSender.getInstance();

	private CsvEmailSender() {}

	public String send(final File attachment) throws EmailException {
		if (attachment != null) {
			String formattedDate = attachment.getName();
			try {
				formattedDate = DateFormat.getDateInstance(DateFormat.LONG, Resources.getLanguage().getLocale()).format(CsvWriter.dateFormatFileName.parse(formattedDate.substring(0, formattedDate.indexOf('.'))));
			}
			catch (final Exception e) {
				formattedDate = e.getClass().getSimpleName();
			}
			final String subject = Resources.get("msg.writer.csv.email.subject", formattedDate);
			final String message = Resources.get("msg.writer.csv.email.message", attachment.getName());
			return emailSender.send(subject, message, attachment);
		}
		else {
			throw new IllegalStateException("Missing attachment.");
		}
	}

}
