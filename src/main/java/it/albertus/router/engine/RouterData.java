package it.albertus.router.engine;

import it.albertus.util.Jsonable;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

public class RouterData implements Serializable, Jsonable {

	private static final long serialVersionUID = -9084896320968670667L;

	private final Date timestamp;
	private final int responseTime;
	private final Map<String, String> data;

	public RouterData(final Date timestamp, final int responseTime, final Map<String, String> data) {
		this.timestamp = timestamp;
		this.responseTime = responseTime;
		this.data = data;
	}

	public RouterData(final int responseTime, final Map<String, String> data) {
		this(new Date(), responseTime, data);
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public int getResponseTime() {
		return responseTime;
	}

	public Map<String, String> getData() {
		return data;
	}

	@Override
	public String toString() {
		return "RouterData [timestamp=" + timestamp + ", responseTime=" + responseTime + ", data=" + data + "]";
	}

	@Override
	public String toJson() {
		final StringBuilder json = new StringBuilder("{\"timestamp\":\"").append(ISO8601Utils.format(timestamp, true, defaultTimeZone)).append("\",\"responseTime\":").append(responseTime);
		if (data != null && !data.isEmpty()) {
			json.append(",\"data\":{");
			int index = 0;
			for (final Entry<String, String> entry : data.entrySet()) {
				json.append('"').append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
				if (++index < data.size()) {
					json.append(',');
				}
			}
			json.append("}");
		}
		json.append("}");
		return json.toString();
	}

}
