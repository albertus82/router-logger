package it.albertus.routerlogger.dto;

import java.io.Serializable;

import it.albertus.routerlogger.engine.Threshold;
import it.albertus.util.Jsonable;

public class ThresholdDto implements Serializable, Jsonable {

	private static final long serialVersionUID = 3881838273700131909L;

	private final String name;
	private final String key;
	private final String type;
	private final String value;
	private final boolean excluded;
	private final String detected;

	public ThresholdDto(final Threshold threshold, final String value) {
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

}
