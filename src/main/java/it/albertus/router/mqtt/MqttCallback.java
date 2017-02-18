package it.albertus.router.mqtt;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttToken;

import it.albertus.router.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class MqttCallback implements MqttCallbackExtended {

	private static final Logger logger = LoggerFactory.getLogger(MqttCallback.class);

	private final String clientId;

	public MqttCallback(final String clientId) {
		this.clientId = clientId;
	}

	@Override
	public void connectionLost(final Throwable e) {
		logger.log(Level.WARNING, e.toString(), e);
	}

	@Override
	public void connectComplete(final boolean reconnect, final String serverURI) {
		logger.log(Level.INFO, Messages.get("msg.mqtt.connected"), new String[] { serverURI, clientId });
	}

	@Override
	public void messageArrived(final String topic, final MqttMessage message) {
		logger.log(Level.FINE, Messages.get("msg.mqtt.message.arrived"), new Object[] { topic, message });
	}

	@Override
	public void deliveryComplete(final IMqttDeliveryToken token) {
		logger.log(Level.FINE, Messages.get("msg.mqtt.message.delivered"), token instanceof MqttToken ? ((MqttToken) token).internalTok : token);
	}

}
