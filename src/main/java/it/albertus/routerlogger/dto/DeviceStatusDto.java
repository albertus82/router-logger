package it.albertus.routerlogger.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import it.albertus.routerlogger.engine.RouterData;
import it.albertus.routerlogger.engine.Threshold;
import it.albertus.routerlogger.engine.ThresholdsReached;
import it.albertus.util.Jsonable;

public class DeviceStatusDto implements Serializable, Jsonable {

	private static final long serialVersionUID = -3532671488236848582L;

	private final Date timestamp;
	private final Integer responseTime;
	private final Map<String, String> data;
	private final Set<ThresholdDto> thresholds;

	public DeviceStatusDto(final RouterData routerData, final ThresholdsReached thresholds) {
		if (routerData != null) {
			this.timestamp = routerData.getTimestamp();
			this.responseTime = routerData.getResponseTime();
			this.data = routerData.getData();
			this.thresholds = new LinkedHashSet<ThresholdDto>(thresholds.getReached().size());
			for (final Entry<Threshold, String> entry : thresholds.getReached().entrySet()) {
				this.thresholds.add(new ThresholdDto(entry.getKey(), entry.getValue()));
			}
		}
		else {
			this.timestamp = null;
			this.responseTime = null;
			this.data = null;
			this.thresholds = null;
		}
	}

	@Override
	public String toJson() {
		final StringBuilder json = new StringBuilder("{");
		if (data != null) {
			json.append("\"timestamp\":\"").append(ISO8601Utils.format(timestamp, true, defaultTimeZone)).append("\",\"responseTime\":").append(responseTime);
			if (!data.isEmpty()) {
				json.append(',').append(jsonifyMap("data", data));
			}
			json.append(',').append(jsonifyCollection("thresholds", thresholds));
		}
		json.append('}');
		return json.toString();
	}

	@Override
	public String toString() {
		return toJson();
	}

	private static String jsonifyCollection(final String name, final Collection<? extends Jsonable> collection) {
		final StringBuilder json = new StringBuilder();
		json.append('"').append(name).append("\":[");
		if (collection != null) {
			int index = 0;
			for (final Jsonable threshold : collection) {
				json.append(threshold.toJson());
				if (++index < collection.size()) {
					json.append(',');
				}
			}
		}
		json.append(']');
		return json.toString();
	}

	private static String jsonifyMap(final String name, final Map<String, String> map) {
		final StringBuilder json = new StringBuilder();
		json.append('"').append(name).append("\":{");
		int index = 0;
		for (final Entry<String, String> entry : map.entrySet()) {
			json.append('"').append(entry.getKey()).append("\":\"").append(entry.getValue()).append('"');
			if (++index < map.size()) {
				json.append(',');
			}
		}
		json.append('}');
		return json.toString();
	}

	private class ThresholdDto implements Serializable, Jsonable {

		private static final long serialVersionUID = 3881838273700131909L;

		private final String name;
		private final String key;
		private final String type;
		private final String value;
		private final boolean excluded;
		private final String detected;

		private ThresholdDto(final Threshold threshold, final String value) {
			this.name = threshold.getName();
			this.key = threshold.getKey();
			this.type = threshold.getType().name();
			this.value = threshold.getValue();
			this.excluded = threshold.isExcluded();
			this.detected = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
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
			if (!(obj instanceof ThresholdDto)) {
				return false;
			}
			ThresholdDto other = (ThresholdDto) obj;
			if (key == null) {
				if (other.key != null) {
					return false;
				}
			}
			else if (!key.equals(other.key)) {
				return false;
			}
			if (type == null) {
				if (other.type != null) {
					return false;
				}
			}
			else if (!type.equals(other.type)) {
				return false;
			}
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			}
			else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}

		@Override
		public String toJson() {
			final StringBuilder json = new StringBuilder();
			json.append("{\"name\":\"").append(name).append("\",\"key\":\"").append(key).append("\",\"type\":\"").append(type).append("\",\"value\":\"").append(value).append('"');
			json.append(",\"excluded\":").append(excluded);
			json.append(",\"detected\":\"").append(detected).append("\"}");
			return json.toString();
		}

		@Override
		public String toString() {
			return toJson();
		}

	}

}
