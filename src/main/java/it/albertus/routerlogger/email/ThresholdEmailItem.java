package it.albertus.routerlogger.email;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import it.albertus.routerlogger.engine.RouterData;
import it.albertus.routerlogger.engine.Threshold;
import it.albertus.routerlogger.resources.Messages;
import it.albertus.util.NewLine;

public class ThresholdEmailItem implements Serializable {

	private static final long serialVersionUID = -8330502294900146719L;

	protected static final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
		}
	};

	private final Date date;
	private final Map<String, String> thresholds = new TreeMap<String, String>();
	private final RouterData routerData;

	public ThresholdEmailItem(final Map<Threshold, String> thresholdsReached, final RouterData routerData) {
		this.routerData = routerData;
		this.date = routerData.getTimestamp();
		for (final Entry<Threshold, String> entry : thresholdsReached.entrySet()) {
			thresholds.put(entry.getKey().getKey(), entry.getValue());
		}
	}

	public Date getDate() {
		return date;
	}

	public Map<String, String> getThresholds() {
		return thresholds;
	}

	public RouterData getRouterData() {
		return routerData;
	}

	@Override
	public String toString() {
		return new StringBuilder(dateFormat.get().format(date)).append(" - ").append(Messages.get("msg.thresholds.reached", thresholds)).append(NewLine.CRLF).append(NewLine.CRLF).append(routerData).toString();
	}

}
