package it.albertus.router.email;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.Threshold;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class ThresholdEmailItem implements Serializable, Comparable<ThresholdEmailItem> {

	private static final long serialVersionUID = -1673604233661066997L;

	protected static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	protected final Date date;
	protected final Map<String, String> thresholds = new TreeMap<String, String>();
	protected final RouterData routerData;

	public ThresholdEmailItem(final Map<Threshold, String> thresholdsReached, final RouterData routerData) {
		this.routerData = routerData;
		this.date = routerData.getTimestamp();
		for (final Threshold threshold : thresholdsReached.keySet()) {
			thresholds.put(threshold.getKey(), thresholdsReached.get(threshold));
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ThresholdEmailItem)) {
			return false;
		}
		ThresholdEmailItem other = (ThresholdEmailItem) obj;
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		}
		else if (!date.equals(other.date)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder(dateFormat.format(date)).append(" - ").append(Resources.get("msg.thresholds.reached", thresholds)).append(NewLine.CRLF.toString()).append(NewLine.CRLF.toString()).append(routerData).toString();
	}

	@Override
	public int compareTo(final ThresholdEmailItem o) {
		return this.date.compareTo(o.date);
	}

}
