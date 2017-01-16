package it.albertus.router.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import it.albertus.router.resources.Messages;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.router.util.LoggerFactory;

public class MqttCallback implements MqttCallbackExtended {

	private static final Logger logger = LoggerFactory.getLogger(MqttCallback.class);

	private final String clientId;

	public MqttCallback(final String clientId) {
		this.clientId = clientId;
	}

	@Override
	public void connectionLost(final Throwable cause) {
		logger.error(cause);
	}

	@Override
	public void connectComplete(boolean reconnect, final String serverURI) {
		logger.info(Messages.get("msg.mqtt.connected", serverURI, clientId), logger.isDebugEnabled() ? new Destination[] { Destination.CONSOLE, Destination.FILE } : new Destination[] { Destination.CONSOLE });
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) {
		logger.debug(Messages.get("msg.mqtt.message.arrived", topic, message));
	}

	@Override
	public void deliveryComplete(final IMqttDeliveryToken token) {
		logger.debug(Messages.get("msg.mqtt.message.delivered", token));
	}

}
