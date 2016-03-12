package it.albertus.router.engine;

import it.albertus.router.resources.Resources;

public enum RouterLoggerStatus {
	STARTING("lbl.status.starting", false),
	CONNECTING("lbl.status.connecting", false),
	AUTHENTICATING("lbl.status.authenticating", false),
	OK("lbl.status.ok", true),
	INFO("lbl.status.info", true),
	WARNING("lbl.status.warning", true),
	DISCONNECTED("lbl.status.disconnected", false),
	RECONNECTING("lbl.status.reconnecting", false),
	ERROR("lbl.status.error", false);

	private final String key;
	private final boolean running;

	private RouterLoggerStatus(String key, boolean running) {
		this.key = key;
		this.running = running;
	}

	public boolean isRunning() {
		return running;
	}

	@Override
	public String toString() {
		return Resources.get(this.key);
	}

}
