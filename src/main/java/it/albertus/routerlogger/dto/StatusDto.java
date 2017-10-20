package it.albertus.routerlogger.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import it.albertus.routerlogger.engine.RouterLoggerStatus;
import it.albertus.routerlogger.engine.Status;
import it.albertus.util.Jsonable;

public class StatusDto implements Serializable, Jsonable {

	private static final long serialVersionUID = -4177252476439888471L;

	private final Date timestamp;
	private final String status;
	private final String description;

	public StatusDto(final RouterLoggerStatus status) {
		if (status != null) {
			this.status = status.getStatus().toString();
			this.description = status.getStatus().getDescription();
			if (!Status.ABEND.equals(status.getStatus())) {
				this.timestamp = status.getTimestamp();
			}
			else {
				this.timestamp = null;
			}
		}
		else {
			this.status = null;
			this.description = null;
			this.timestamp = null;
		}
	}

	@Override
	public String toJson() {
		final StringBuilder json = new StringBuilder();
		if (status == null) {
			json.append("null");
		}
		else {
			json.append('{');
			if (timestamp != null) {
				json.append("\"timestamp\":\"").append(ISO8601Utils.format(timestamp, true, defaultTimeZone)).append("\",");
			}
			json.append("\"status\":\"").append(status).append("\",\"description\":\"").append(description).append("\"}");
		}
		return json.toString();
	}

}
