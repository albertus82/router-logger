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
	ERROR("lbl.status.error"),
	ABEND("lbl.status.abend");

	private final String resourceKey;

	private RouterLoggerStatus(final String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getDescription() {
		return Resources.get(resourceKey);
	}

}
