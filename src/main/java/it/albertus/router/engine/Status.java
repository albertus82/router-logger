package it.albertus.router.engine;

import it.albertus.router.resources.Resources;

public enum Status {
	STARTING,
	CONNECTING,
	AUTHENTICATING,
	OK,
	INFO,
	WARNING,
	DISCONNECTING,
	DISCONNECTED,
	RECONNECTING,
	ERROR,
	CLOSED,
	ABEND;

	private static final String RESOURCE_KEY_PREFIX = "lbl.status.";

	private final String resourceKey;

	private Status() {
		this.resourceKey = RESOURCE_KEY_PREFIX + name().toLowerCase().replace('_', '.');
	}

	public String getDescription() {
		return Resources.get(resourceKey);
	}

}
