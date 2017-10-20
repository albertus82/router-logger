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

public class RouterDataDto implements Serializable, Jsonable {

	private static final long serialVersionUID = -3532671488236848582L;

	private final Date timestamp;
	private final Integer responseTime;
	private final Map<String, String> data;
	private final Set<ThresholdDto> thresholds;

	public RouterDataDto(final RouterData routerData, final ThresholdsReached thresholds) {
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

}
