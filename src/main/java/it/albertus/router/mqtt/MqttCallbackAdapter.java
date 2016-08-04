package it.albertus.router.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public abstract class MqttCallbackAdapter implements MqttCallbackExtended {

	@Override
	public void connectionLost(Throwable cause) {}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {}

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {}

}
