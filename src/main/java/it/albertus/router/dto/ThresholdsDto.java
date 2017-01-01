package it.albertus.router.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import it.albertus.router.engine.Threshold;
import it.albertus.router.engine.ThresholdsReached;
import it.albertus.util.Jsonable;

public class ThresholdsDto implements Serializable, Jsonable {

	private static final long serialVersionUID = -7221966615926851395L;

	private final Date timestamp;
	private final Set<ThresholdDto> reached;

	public ThresholdsDto(final ThresholdsReached thresholdsReached) {
		if (thresholdsReached != null) {
			this.timestamp = thresholdsReached.getTimestamp();
			this.reached = new LinkedHashSet<ThresholdDto>(thresholdsReached.getReached().size());
			for (final Entry<Threshold, String> entry : thresholdsReached.getReached().entrySet()) {
				this.reached.add(new ThresholdDto(entry.getKey(), entry.getValue()));
			}
		}
		else {
			this.timestamp = null;
			this.reached = null;
		}
	}

	@Override
	public String toString() {
		return "ThresholdsDto [timestamp=" + timestamp + ", reached=" + reached + "]";
	}

	@Override
	public String toJson() {
		final StringBuilder json = new StringBuilder();
		if (reached == null) {
			json.append("null");
		}
		else {
			json.append("{\"timestamp\":\"").append(timestamp != null ? ISO8601Utils.format(timestamp, true, defaultTimeZone) : "null").append('"');
			json.append(",\"reached\":");
			json.append('[');
			int index = 0;
			for (final ThresholdDto threshold : reached) {
				json.append(threshold.toJson());
				if (++index < reached.size()) {
					json.append(',');
				}
			}
			json.append("]}");
		}
		return json.toString();
	}

}
