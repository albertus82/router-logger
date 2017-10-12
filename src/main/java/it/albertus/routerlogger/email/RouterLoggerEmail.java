package it.albertus.routerlogger.email;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

public class RouterLoggerEmail implements Serializable {

	private static final long serialVersionUID = -5241562797785414380L;

	protected final Date date;
	protected final String subject;
	protected final String message;
	protected final File[] attachments;

	protected RouterLoggerEmail(final String subject, final String message, final File[] attachments) {
		if (message != null && !message.isEmpty()) {
			this.message = message;
		}
		else {
			throw new IllegalArgumentException("Invalid message supplied");
		}
		this.date = new Date();
		this.subject = subject != null ? subject.trim() : null;
		this.attachments = attachments;
	}

	public Date getDate() {
		return date;
	}

	public String getSubject() {
		return subject;
	}

	public String getMessage() {
		return message;
	}

	public File[] getAttachments() {
		return attachments;
	}

	@Override
	public String toString() {
		return "RouterLoggerEmail [date=" + date + ", subject=" + subject + "]";
	}

}
