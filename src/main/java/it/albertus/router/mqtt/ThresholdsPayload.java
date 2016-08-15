package it.albertus.router.mqtt;

import it.albertus.router.engine.Threshold;
import it.albertus.router.util.Jsonable;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

public class ThresholdsPayload implements Serializable, Jsonable {

	private static final long serialVersionUID = -566811797356824879L;

	private class ThresholdItem implements Serializable {

		private static final long serialVersionUID = 1582125718825266945L;

		private final Threshold threshold;

		private final String name;
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
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
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
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

		private ThresholdsPayload getOuterType() {
			return ThresholdsPayload.this;
		}

	}

	private final Date timestamp;
	private final Set<ThresholdItem> thresholds;

	public ThresholdsPayload(final Map<Threshold, String> thresholds, final Date timestamp) {
		if (timestamp != null) {
			this.timestamp = timestamp;
		}
		else {
			this.timestamp = new Date();
		}
		if (thresholds != null) {
			this.thresholds = new LinkedHashSet<ThresholdItem>(thresholds.size());
			for (final Entry<Threshold, String> entry : thresholds.entrySet()) {
				this.thresholds.add(new ThresholdItem(entry.getKey(), entry.getValue()));
			}
		}
		else {
			this.thresholds = Collections.emptySet();
		}
	}

	@Override
	public String toString() {
		return "ThresholdsPayload [timestamp=" + timestamp + ", thresholds=" + thresholds + "]";
	}

	@Override
	public String toJson() {
		final StringBuilder json = new StringBuilder("{\"timestamp\":\"").append(ISO8601Utils.format(timestamp, true, defaultTimeZone)).append("\"");
		if (!thresholds.isEmpty()) {
			json.append(",\"thresholds\":[");
			int index = 0;
			for (final ThresholdItem threshold : thresholds) {
				json.append("{\"name\":\"").append(threshold.name).append("\",\"key\":\"").append(threshold.key).append("\",\"type\":\"").append(threshold.type).append("\",\"value\":\"").append(threshold.value).append("\"");
				// json.append(",\"excluded\":").append(threshold.excluded);
				json.append(",\"detected\":\"").append(threshold.detected).append("\"}");
				if (++index < thresholds.size()) {
					json.append(',');
				}
			}
			json.append("]");
		}
		json.append("}");
		return json.toString();
	}

}
