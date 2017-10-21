package it.albertus.routerlogger.engine;

import java.io.Serializable;
import java.util.Date;

public class AppStatus implements Serializable {

	private static final long serialVersionUID = -5574131723102221930L;

	private final Date timestamp;
	private final Status status;

	public AppStatus(final Status status) {
		this.timestamp = new Date();
		this.status = status;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Status getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "RouterLoggerStatus [timestamp=" + timestamp + ", status=" + status + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		if (!(obj instanceof AppStatus)) {
			return false;
		}
		AppStatus other = (AppStatus) obj;
		if (status != other.status) {
			return false;
		}
		return true;
	}

}
