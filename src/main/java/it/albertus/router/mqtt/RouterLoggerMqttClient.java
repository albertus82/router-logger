package it.albertus.router.mqtt;

import it.albertus.jface.preference.field.UriListEditor;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.Configuration;
import it.albertus.util.ConfigurationException;

import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

/** @Singleton */
public class RouterLoggerMqttClient {

	public static final byte QOS_MIN = 0;
	public static final byte QOS_MAX = 2;

	private static final String CFG_KEY_MQTT_TOPIC = "mqtt.topic";
	private static final String CFG_KEY_MQTT_MESSAGE_QOS = "mqtt.message.qos";
	private static final String CFG_KEY_MQTT_MESSAGE_RETAINED = "mqtt.message.retained";
	private static final String CFG_KEY_MQTT_CLEAN_SESSION = "mqtt.clean.session";
	private static final String CFG_KEY_MQTT_MAX_INFLIGHT = "mqtt.max.inflight";
	private static final String CFG_KEY_MQTT_CONNECTION_TIMEOUT = "mqtt.connection.timeout";
	private static final String CFG_KEY_MQTT_KEEP_ALIVE_INTERVAL = "mqtt.keep.alive.interval";
	private static final String CFG_KEY_MQTT_PASSWORD = "mqtt.password";
	private static final String CFG_KEY_MQTT_USERNAME = "mqtt.username";
	private static final String CFG_KEY_MQTT_CLIENT_ID = "mqtt.client.id";
	private static final String CFG_KEY_MQTT_SERVER_URI = "mqtt.server.uri";
	private static final String CFG_KEY_MQTT_ACTIVE = "mqtt.active";
	private static final String CFG_KEY_MQTT_AUTOMATIC_RECONNECT = "mqtt.automatic.reconnect";

	public interface Defaults {
		boolean ACTIVE = false;
		String CLIENT_ID = "RouterLogger";
		String TOPIC = "router/status";
		int KEEP_ALIVE_INTERVAL = MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT;
		int CONNECTION_TIMEOUT = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;
		int MAX_INFLIGHT = MqttConnectOptions.MAX_INFLIGHT_DEFAULT;
		boolean CLEAN_SESSION = MqttConnectOptions.CLEAN_SESSION_DEFAULT;
		boolean AUTOMATIC_RECONNECT = false;
		boolean MESSAGE_RETAINED = false;
		byte MESSAGE_QOS = 1;
	}

	private static class Singleton {
		private static final RouterLoggerMqttClient instance = new RouterLoggerMqttClient();
	}

	public static RouterLoggerMqttClient getInstance() {
		return Singleton.instance;
	}

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();
	private volatile MqttClient client;
	private MqttCallback callback;

	private RouterLoggerMqttClient() {}

	public void connect() {
		if (configuration.getBoolean(CFG_KEY_MQTT_ACTIVE, Defaults.ACTIVE)) {
			if (callback == null) {
				callback = new MqttCallback(); // Lazy initialization.
			}
			final Logger logger = Logger.getInstance();
			try {
				final MqttConnectOptions options = new MqttConnectOptions();
				final String[] serverURIs = configuration.getString(CFG_KEY_MQTT_SERVER_URI).split(UriListEditor.URI_SPLIT_REGEX);
				if (serverURIs == null || serverURIs.length == 0 || serverURIs[0].trim().isEmpty()) {
					throw new ConfigurationException(Resources.get("err.mqtt.cfg.error.uri"), CFG_KEY_MQTT_SERVER_URI);
				}
				options.setServerURIs(serverURIs);
				final String username = configuration.getString(CFG_KEY_MQTT_USERNAME);
				if (username != null && !username.isEmpty()) {
					options.setUserName(username);
				}
				final char[] password = configuration.getCharArray(CFG_KEY_MQTT_PASSWORD);
				if (password != null && password.length > 0) {
					options.setPassword(password);
				}
				options.setKeepAliveInterval(configuration.getInt(CFG_KEY_MQTT_KEEP_ALIVE_INTERVAL, Defaults.KEEP_ALIVE_INTERVAL));
				options.setConnectionTimeout(configuration.getInt(CFG_KEY_MQTT_CONNECTION_TIMEOUT, Defaults.CONNECTION_TIMEOUT));
				options.setMaxInflight(configuration.getInt(CFG_KEY_MQTT_MAX_INFLIGHT, Defaults.MAX_INFLIGHT));
				options.setCleanSession(configuration.getBoolean(CFG_KEY_MQTT_CLEAN_SESSION, Defaults.CLEAN_SESSION));
				options.setAutomaticReconnect(false); // configuration.getBoolean(CFG_KEY_MQTT_AUTOMATIC_RECONNECT, Defaults.AUTOMATIC_RECONNECT));
				final String clientId = configuration.getString(CFG_KEY_MQTT_CLIENT_ID, Defaults.CLIENT_ID);
				doConnect(clientId, options);
				System.out.println(Resources.get("msg.mqtt.connected", Arrays.toString(options.getServerURIs()), clientId));
				if (logger.isDebugEnabled()) {
					System.out.println(options.toString().trim());
				}
			}
			catch (final MqttPersistenceException mpe) {
				logger.log(mpe, Destination.CONSOLE, Destination.FILE);
			}
			catch (final Exception e) {
				logger.log(e);
			}
		}
	}

	private synchronized void doConnect(final String clientId, final MqttConnectOptions options) throws MqttException {
		client = new org.eclipse.paho.client.mqttv3.MqttClient(options.getServerURIs()[0], clientId);
		client.setCallback(callback);
		client.connect(options);
	}

	public void publish(final Object payload) {
		publish(String.valueOf(payload));
	}

	public void publish(final String payload) {
		publish(payload.getBytes());
	}

	public void publish(final byte[] payload) {
		if (configuration.getBoolean(CFG_KEY_MQTT_ACTIVE, Defaults.ACTIVE)) {
			final String topic = configuration.getString(CFG_KEY_MQTT_TOPIC, Defaults.TOPIC);
			final MqttMessage message = new MqttMessage(payload);
			message.setRetained(configuration.getBoolean(CFG_KEY_MQTT_MESSAGE_RETAINED, Defaults.MESSAGE_RETAINED));
			message.setQos(configuration.getByte(CFG_KEY_MQTT_MESSAGE_QOS, Defaults.MESSAGE_QOS));
			final boolean automaticReconnect = configuration.getBoolean(CFG_KEY_MQTT_AUTOMATIC_RECONNECT, Defaults.AUTOMATIC_RECONNECT);
			try {
				doPublish(topic, message, automaticReconnect);
			}
			catch (final Exception e) {
				Logger.getInstance().log(e);
			}
		}
	}

	private synchronized void doPublish(final String topic, final MqttMessage message, final boolean automaticReconnect) throws MqttException {
		if (client == null || (client != null && !client.isConnected() && automaticReconnect)) {
			connect();
		}
		if (client != null && client.isConnected()) {
			client.publish(topic, message);
		}
	}

	public void disconnect() {
		try {
			doDisconnect();
		}
		catch (final Exception e) {
			Logger.getInstance().log(e);
		}
	}

	private synchronized void doDisconnect() throws MqttException {
		if (client != null) {
			if (client.isConnected()) {
				client.disconnect();
			}
			client = null;
		}
	}

}
