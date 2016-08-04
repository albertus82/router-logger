package it.albertus.router.mqtt;

import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;

public class MqttCallback extends MqttCallbackAdapter {

	private final String clientId;

	public MqttCallback(final String clientId) {
		this.clientId = clientId;
	}

	@Override
	public void connectionLost(final Throwable cause) {
		Logger.getInstance().log(cause);
	}

	@Override
	public void connectComplete(boolean reconnect, final String serverURI) {
		Logger.getInstance().log(Resources.get("msg.mqtt.connected", serverURI, clientId), Destination.CONSOLE);
	}

}
