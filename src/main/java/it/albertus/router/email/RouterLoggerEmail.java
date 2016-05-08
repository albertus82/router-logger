package it.albertus.router.email;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/** Note: this class has a natural ordering that is inconsistent with equals. */
public class RouterLoggerEmail implements Serializable, Comparable<RouterLoggerEmail> {

	private static final long serialVersionUID = -2852033440131898330L;

	protected final String subject;
	protected final String message;
	protected final File[] attachments;
	protected final Date date;
	protected final UUID uuid;

	protected RouterLoggerEmail(final String subject, final String message, final File[] attachments) {
		if (message != null && !message.isEmpty()) {
			this.message = message;
		}
		else {
			throw new IllegalArgumentException("Invalid message supplied");
		}
		this.uuid = UUID.randomUUID();
		this.date = new Date();
		this.subject = subject != null ? subject.trim() : null;
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

	public Date getDate() {
		return date;
	}

	public UUID getUuid() {
		return uuid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
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
		if (uuid == null) {
			if (other.uuid != null) {
				return false;
			}
		}
		else if (!uuid.equals(other.uuid)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RouterLoggerEmail [subject=" + subject + ", date=" + date + ", uuid=" + uuid + "]";
	}

	@Override
	public int compareTo(final RouterLoggerEmail o) {
		return this.date.compareTo(o.date);
	}

}
