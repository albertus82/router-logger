package it.albertus.router.email;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.Threshold;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/** Note: this class has a natural ordering that is inconsistent with equals. */

public class ThresholdEmailItem implements Serializable, Comparable<ThresholdEmailItem> {

	private static final long serialVersionUID = -1673604233661066997L;

	protected final Date date;
	protected final Map<String, String> thresholds = new TreeMap<String, String>();
	protected final RouterData routerData;
	protected final UUID uuid;

	public ThresholdEmailItem(final Map<Threshold, String> thresholdsReached, final RouterData routerData) {
		this.routerData = routerData;
		this.date = routerData.getTimestamp();
		this.uuid = UUID.randomUUID();
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

	public UUID getUuid() {
		return uuid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		if (!(obj instanceof ThresholdEmailItem)) {
			return false;
		}
		ThresholdEmailItem other = (ThresholdEmailItem) obj;
		if (uuid == null) {
			if (other.uuid != null) {
				return false;
			}
		}
		else if (!uuid.equals(other.uuid)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder(ThresholdsEmailSender.dateFormat.format(routerData.getTimestamp())).append(" - ").append(Resources.get("msg.thresholds.reached", thresholds)).append(NewLine.CRLF.toString()).append(NewLine.CRLF.toString()).append(routerData).toString();
	}

	@Override
	public int compareTo(ThresholdEmailItem o) {
		return this.date.compareTo(o.date);
	}

}
