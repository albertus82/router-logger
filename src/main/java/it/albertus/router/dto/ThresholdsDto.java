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

	private static final long serialVersionUID = -7221966615926851395L;

	private final Date timestamp;
	private final Set<ThresholdDto> thresholds;

	public ThresholdsDto(final ThresholdsReached thresholdsReached) {
		if (thresholdsReached != null) {
			this.timestamp = thresholdsReached.getTimestamp();
			this.thresholds = new LinkedHashSet<ThresholdDto>(thresholdsReached.getReached().size());
			for (final Entry<Threshold, String> entry : thresholdsReached.getReached().entrySet()) {
				this.thresholds.add(new ThresholdDto(entry.getKey(), entry.getValue()));
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
			for (final ThresholdDto threshold : thresholds) {
				json.append("{\"name\":\"").append(threshold.getName()).append("\",\"key\":\"").append(threshold.getKey()).append("\",\"type\":\"").append(threshold.getType()).append("\",\"value\":\"").append(threshold.getValue()).append("\"");
				json.append(",\"excluded\":").append(threshold.isExcluded());
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
