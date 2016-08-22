package it.albertus.router.dto;

import it.albertus.router.engine.Threshold;
import it.albertus.router.engine.ThresholdsReached;
import it.albertus.util.Jsonable;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

public class ThresholdsDto implements Serializable, Jsonable {

	private static final long serialVersionUID = -566811797356824879L;

	private final Date timestamp;
	private final Set<ThresholdItem> thresholds;

	public ThresholdsDto(final ThresholdsReached thresholdsReached) {
		if (thresholdsReached != null) {
			this.timestamp = thresholdsReached.getTimestamp();
			this.thresholds = new LinkedHashSet<ThresholdItem>(thresholdsReached.getReached().size());
			for (final Entry<Threshold, String> entry : thresholdsReached.getReached().entrySet()) {
				this.thresholds.add(new ThresholdItem(entry.getKey(), entry.getValue()));
			}
		}
		else {
			this.timestamp = null;
			this.thresholds = null;
		}
	}

	@Override
	public String toString() {
		return "ThresholdsDto [timestamp=" + timestamp + ", thresholds=" + thresholds + "]";
	}

	@Override
	public String toJson() {
		final StringBuilder json = new StringBuilder();
		if (thresholds == null) {
			json.append("null");
		}
		else {
			json.append("{\"timestamp\":\"").append(timestamp != null ? ISO8601Utils.format(timestamp, true, defaultTimeZone) : "null").append("\"");
			json.append(",\"thresholds\":");
			json.append("[");
			int index = 0;
			for (final ThresholdItem threshold : thresholds) {
				json.append("{\"name\":\"").append(threshold.getName()).append("\",\"key\":\"").append(threshold.getKey()).append("\",\"type\":\"").append(threshold.getType()).append("\",\"value\":\"").append(threshold.getValue()).append("\"");
				// json.append(",\"excluded\":").append(threshold.excluded);
				json.append(",\"detected\":\"").append(threshold.getDetected()).append("\"}");
				if (++index < thresholds.size()) {
					json.append(',');
				}
			}
			json.append("]}");
		}
		return json.toString();
	}

}

class ThresholdItem implements Serializable {

	private static final long serialVersionUID = 1582125718825266945L;

	private final Threshold threshold;
	private final String key;
	private final String type;
	private final String value;
	// private final boolean excluded;
	private final String detected;

	public ThresholdItem(final Threshold threshold, final String value) {
		this.threshold = threshold;
		this.name = threshold.getName();
		this.key = threshold.getKey();
		this.type = threshold.getType().name();
		this.value = threshold.getValue();
		// this.excluded = threshold.isExcluded();
		this.detected = value;
	}

	private final String name;

	public Threshold getThreshold() {
		return threshold;
	}

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public String getDetected() {
		return detected;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((threshold == null) ? 0 : threshold.hashCode());
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
		if (!(obj instanceof ThresholdItem)) {
			return false;
		}
		ThresholdItem other = (ThresholdItem) obj;
		if (threshold == null) {
			if (other.threshold != null) {
				return false;
			}
		}
		else if (!threshold.equals(other.threshold)) {
			return false;
		}
		return true;
	}

}
