package it.albertus.router.mqtt;

import it.albertus.router.util.Logger;

public class MqttCallback extends MqttCallbackAdapter {

	@Override
	public void connectionLost(final Throwable cause) {
		Logger.getInstance().log(cause);
	}

}
