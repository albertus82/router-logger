package it.albertus.router.mqtt;

import it.albertus.router.resources.Resources;

public enum MqttQos {

	AT_MOST_ONCE(0, "lbl.mqtt.qos.0"),
	AT_LEAST_ONCE(1, "lbl.mqtt.qos.1"),
	EXACTLY_ONCE(2, "lbl.mqtt.qos.2");

	private final byte level;
	private final String resourceKey;

	private MqttQos(final int level, final String resourceKey) {
		this.level = (byte) level;
		this.resourceKey = resourceKey;
	}

	public byte getValue() {
		return level;
	}

	public String getDescription() {
		return Resources.get(resourceKey);
	}

}
