package it.albertus.router.engine;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class RouterData implements Serializable, Comparable<RouterData> {

	private static final long serialVersionUID = -8826449762497656520L;

	private final Date timestamp;
	private int responseTime = -1;
	private final Map<String, String> data;

	public RouterData(final Map<String, String> data) {
		this(new Date(), data);
	}

	public RouterData(final Date timestamp, final Map<String, String> data) {
		this.timestamp = timestamp;
		this.data = data;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public int getResponseTime() {
		return responseTime;
	}

	void setResponseTime(int responseTime) {
		this.responseTime = responseTime;
	}

	public Map<String, String> getData() {
		return data;
	}

	@Override
	public String toString() {
		return "RouterData [timestamp=" + timestamp + ", responseTime=" + responseTime + ", data=" + data + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (!(obj instanceof RouterData)) {
			return false;
		}
		RouterData other = (RouterData) obj;
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

	@Override
	public int compareTo(final RouterData o) {
		return this.timestamp.compareTo(o.timestamp);
	}

}
