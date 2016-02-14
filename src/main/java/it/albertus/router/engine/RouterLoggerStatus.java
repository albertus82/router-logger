package it.albertus.router.engine;

import it.albertus.router.resources.Resources;

public enum RouterLoggerStatus {
	STARTING("lbl.status.starting"),
	CONNECTING("lbl.status.connecting"),
	OK("lbl.status.ok"),
	INFO("lbl.status.info"),
	WARNING("lbl.status.warning"),
	DISCONNECTED("lbl.status.disconnected"),
	ERROR("lbl.status.error");

	private final String key;

	private RouterLoggerStatus(String key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return Resources.get(this.key);
	}

}
