package it.albertus.router.engine;

import it.albertus.router.resources.Resources;

public enum RouterLoggerStatus {
	STARTING("lbl.status.starting"),
	CONNECTING("lbl.status.connecting"),
	AUTHENTICATING("lbl.status.authenticating"),
	OK("lbl.status.ok"),
	INFO("lbl.status.info"),
	WARNING("lbl.status.warning"),
	DISCONNECTING("lbl.status.disconnecting"),
	DISCONNECTED("lbl.status.disconnected"),
	RECONNECTING("lbl.status.reconnecting"),
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
