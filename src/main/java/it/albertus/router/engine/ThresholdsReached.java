package it.albertus.router.engine;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class ThresholdsReached implements Serializable {

	private static final long serialVersionUID = -1925093897986070599L;

	private final Date timestamp;
	private final Map<Threshold, String> reached;

	public ThresholdsReached(final Map<Threshold, String> reached, final Date timestamp) {
		this.timestamp = timestamp;
		this.reached = reached;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Map<Threshold, String> getReached() {
		return reached;
	}

	@Override
	public String toString() {
		return "ThresholdsReached [timestamp=" + timestamp + ", reached=" + reached + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reached == null) ? 0 : reached.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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
		if (!(obj instanceof ThresholdsReached)) {
			return false;
		}
		ThresholdsReached other = (ThresholdsReached) obj;
		if (reached == null) {
			if (other.reached != null) {
				return false;
			}
		}
		else if (!reached.equals(other.reached)) {
			return false;
		}
		if (timestamp == null) {
			if (other.timestamp != null) {
				return false;
			}
		}
		else if (!timestamp.equals(other.timestamp)) {
			return false;
		}
		return true;
	}

}
