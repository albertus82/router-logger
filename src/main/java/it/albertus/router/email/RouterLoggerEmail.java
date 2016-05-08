package it.albertus.router.email;

import java.io.File;
import java.io.Serializable;
import java.util.UUID;

public class RouterLoggerEmail implements Serializable {

	private static final long serialVersionUID = -2852033440131898330L;

	protected final UUID uuid;
	protected final String subject;
	protected final String message;
	protected final File[] attachments;

	protected RouterLoggerEmail(final String subject, final String message, final File[] attachments) {
		this.uuid = UUID.randomUUID();
		this.subject = subject;
		this.message = message;
		this.attachments = attachments;
	}

	public UUID getUuid() {
		return uuid;
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
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		return "RouterLoggerEmail [subject=" + subject + ", uuid=" + uuid + "]";
	}

}
