package it.albertus.router.engine;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class RouterData implements Serializable {

	private static final long serialVersionUID = 675438334715612374L;

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

}
