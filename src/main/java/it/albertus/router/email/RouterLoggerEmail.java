package it.albertus.router.email;

import java.io.File;
import java.io.Serializable;

public class RouterLoggerEmail implements Serializable {

	private static final long serialVersionUID = 727772177454786813L;

	protected final String subject;
	protected final String message;
	protected final File[] attachments;

	protected RouterLoggerEmail(final String subject, final String message, final File[] attachments) {
		this.subject = subject;
		this.message = message;
		this.attachments = attachments;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RouterLoggerEmail)) {
			return false;
		}
		RouterLoggerEmail other = (RouterLoggerEmail) obj;
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		}
		else if (!message.equals(other.message)) {
			return false;
		}
		if (subject == null) {
			if (other.subject != null) {
				return false;
			}
		}
		else if (!subject.equals(other.subject)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RouterLoggerEmail [subject=" + subject + "]";
	}

}
