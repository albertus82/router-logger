package it.albertus.router.mqtt;

import it.albertus.router.engine.Threshold;
import it.albertus.router.engine.Threshold.Type;
import it.albertus.router.util.Jsonable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

public class ThresholdsPayload implements Serializable, Jsonable {

	private static final long serialVersionUID = -7741846352401497855L;

	private class ThresholdItem {
		private final String name;
		private final String key;
		private final String type;
		private final String value;
		private final boolean excluded;
		private final String detected;

		public ThresholdItem(final String name, final String key, final Type type, final String value, final boolean excluded, final String detected) {
			this.name = name;
			this.key = key;
			this.type = type.name();
			this.value = value;
			this.excluded = excluded;
			this.detected = detected;
		}
	}

	private final Date timestamp;
	private final List<ThresholdItem> thresholds = new ArrayList<ThresholdItem>();

	public ThresholdsPayload(final Map<Threshold, String> thresholds, final Date timestamp) {
		if (timestamp == null) {
			this.timestamp = new Date();
		}
		else {
			this.timestamp = timestamp;
		}
		for (final Entry<Threshold, String> threshold : thresholds.entrySet()) {
			this.thresholds.add(new ThresholdItem(threshold.getKey().getName(), threshold.getKey().getKey(), threshold.getKey().getType(), threshold.getKey().getValue(), threshold.getKey().isExcluded(), threshold.getValue()));
		}
	}

	@Override
	public String toJson() {
		final StringBuilder json = new StringBuilder("{\"timestamp\":\"").append(ISO8601Utils.format(timestamp, true, defaultTimeZone)).append("\"");
		if (!thresholds.isEmpty()) {
			json.append(",\"thresholds\":[");
			int index = 0;
			for (final ThresholdItem threshold : thresholds) {
				json.append("{\"name\":\"").append(threshold.name).append("\",\"key\":\"").append(threshold.key).append("\",\"type\":\"").append(threshold.type).append("\",\"value\":\"").append(threshold.value).append("\",\"excluded\":").append(threshold.excluded).append(",\"detected\":\"").append(threshold.detected).append("\"}");
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
