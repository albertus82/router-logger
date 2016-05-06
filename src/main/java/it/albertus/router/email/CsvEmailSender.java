package it.albertus.router.email;

import it.albertus.router.resources.Resources;
import it.albertus.router.writer.CsvWriter;

import java.io.File;
import java.text.DateFormat;

import org.apache.commons.mail.EmailException;

public class CsvEmailSender extends EmailSender {

	private static class Singleton {
		private static final CsvEmailSender instance = new CsvEmailSender();
	}

	public static CsvEmailSender getInstance() {
		return Singleton.instance;
	}

	private CsvEmailSender() {}

	public String send(final File... attachments) throws EmailException {
		if (attachments != null && attachments.length == 1) {
			String formattedDate = attachments[0].getName();
			try {
				formattedDate = DateFormat.getDateInstance(DateFormat.LONG, Resources.getLanguage().getLocale()).format(CsvWriter.dateFormatFileName.parse(formattedDate.substring(0, formattedDate.indexOf('.'))));
			}
			catch (final Exception e) {
				formattedDate = e.getClass().getSimpleName();
			}
			final String subject = Resources.get("msg.writer.csv.email.subject", formattedDate);
			final String message = Resources.get("msg.writer.csv.email.message", attachments[0].getName());
			return send(subject, message, attachments);
		}
		else {
			throw new IllegalStateException("Illegal attachments count.");
		}
	}

}
