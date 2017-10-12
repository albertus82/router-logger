package it.albertus.routerlogger.mqtt;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import it.albertus.util.logging.LoggerFactory;

public abstract class BaseMqttClient {

	private static final Logger logger = LoggerFactory.getLogger(BaseMqttClient.class);

	private volatile IMqttClient client;

	protected class MqttClientStartThread extends Thread {

		private final MqttConnectOptions options;
		private final boolean retry;

		protected MqttClientStartThread(final MqttConnectOptions options, final boolean retry) {
			this.setName("mqttClientStartThread");
			this.setDaemon(true);
			this.options = options;
			this.retry = retry;
		}

		@Override
		public void run() {
			try {
				client.connect(options);
			}
			catch (final Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
				if (retry) {
					try {
						client.close();
					}
					catch (final MqttException me) {
						logger.log(Level.SEVERE, me.toString(), me);
					}
					client = null;
				}
			}
		}
	}

	protected abstract void connect();

	public void disconnect() {
		try {
			doDisconnect();
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}

	protected synchronized void doConnect(final String clientId, final MqttConnectOptions options, final MqttClientPersistence persistence, final boolean retry) throws MqttException {
		if (client == null) {
			client = new MqttClient(options.getServerURIs()[0], clientId, persistence);
			client.setCallback(new MqttCallback(clientId));
			final Thread starter = new MqttClientStartThread(options, retry);
			starter.start();
			try {
				starter.join();
			}
			catch (final InterruptedException e) {
				logger.log(Level.FINE, e.toString(), e);
				Thread.currentThread().interrupt();
			}
		}
	}

	protected synchronized void doPublish(final String topic, final MqttMessage message) throws MqttException {
		if (client == null) {
			connect(); // Lazy connection.
		}
		if (client != null && client.isConnected()) {
			client.publish(topic, message);
		}
	}

	protected synchronized void doSubscribe(final String topic, final int qos, final IMqttMessageListener listener) throws MqttException {
		if (client == null) {
			connect(); // Lazy connection.
		}
		if (client != null && client.isConnected()) {
			client.subscribe(topic, qos, listener);
		}
	}

	protected synchronized boolean doDisconnect() throws MqttException {
		if (client != null) {
			if (client.isConnected()) {
				try {
					client.disconnect();
				}
				catch (final Exception e) {
					logger.log(Level.SEVERE, e.toString(), e);
					client.disconnectForcibly();
				}
			}
			client.close();
			client = null;
			return true;
		}
		else {
			return false;
		}
	}

	public IMqttClient getClient() {
		return client;
	}

}
