package it.albertus.router.engine;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class RouterData implements Serializable {

	private static final long serialVersionUID = -3312535039501430502L;

	private final Date timestamp;
	private final Map<String, String> data;
	private int responseTime = -1;

	public RouterData(final Map<String, String> data) {
		this.timestamp = new Date();
		this.data = data;
	}

	public RouterData(final Date timestamp, final Map<String, String> data) {
		this.timestamp = timestamp;
		this.data = data;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Map<String, String> getData() {
		return data;
	}

	public int getResponseTime() {
		return responseTime;
	}

	void setResponseTime(int responseTime) {
		this.responseTime = responseTime;
	}

}
