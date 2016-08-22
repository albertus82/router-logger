package it.albertus.router.dto;

import it.albertus.router.engine.RouterData;
import it.albertus.util.Jsonable;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

public class RouterDataDto implements Serializable, Jsonable {

	private static final long serialVersionUID = -5176963886161068204L;

	private final Date timestamp;
	private final Integer responseTime;
	private final Map<String, String> data;

	public RouterDataDto(final RouterData routerData) {
		if (routerData != null) {
			this.timestamp = routerData.getTimestamp();
			this.responseTime = routerData.getResponseTime();
			this.data = routerData.getData();
		}
		else {
			this.timestamp = null;
			this.responseTime = null;
			this.data = null;
		}
	}

	@Override
	public String toString() {
		return "RouterDataDto [timestamp=" + timestamp + ", responseTime=" + responseTime + ", data=" + data + "]";
	}

	@Override
	public String toJson() {
		final StringBuilder json = new StringBuilder();
		if (data == null) {
			json.append("null");
		}
		else {
			json.append("{\"timestamp\":\"").append(ISO8601Utils.format(timestamp, true, defaultTimeZone)).append("\",\"responseTime\":").append(responseTime);
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
		}
		return json.toString();
	}

}
