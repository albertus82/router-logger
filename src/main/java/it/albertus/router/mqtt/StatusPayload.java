package it.albertus.router.mqtt;

import it.albertus.router.engine.RouterLoggerStatus;
import it.albertus.router.util.Jsonable;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

public class StatusPayload implements Serializable, Jsonable {

	private static final long serialVersionUID = -4177252476439888471L;

	private final Date timestamp;
	private final String status;
	private final String description;

	protected StatusPayload(final RouterLoggerStatus status) {
		this.status = status.toString();
		this.description = status.getDescription();
		if (!RouterLoggerStatus.ABEND.equals(status)) {
			this.timestamp = new Date();
		}
		else {
			this.timestamp = null;
		}
	}

	@Override
	public String toString() {
		return "StatusPayload [timestamp=" + timestamp + ", status=" + status + ", description=" + description + "]";
	}

	@Override
	public String toJson() {
		final StringBuilder json = new StringBuilder("{");
		if (timestamp != null) {
			json.append("\"timestamp\":\"").append(ISO8601Utils.format(timestamp, true, defaultTimeZone)).append("\",");
		}
		json.append("\"status\":\"").append(status).append("\",\"description\":\"").append(description).append("\"}");
		return json.toString();
	}

}
