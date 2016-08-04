package it.albertus.router.mqtt;

import it.albertus.jface.preference.field.UriListEditor;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.util.Logger;
import it.albertus.util.Configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/** @Singleton */
public class MqttClient {

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
		private static final MqttClient instance = new MqttClient();
	}

	public static MqttClient getInstance() {
		return Singleton.instance;
	}

	protected volatile org.eclipse.paho.client.mqttv3.MqttClient client;
	protected final Configuration configuration = RouterLoggerConfiguration.getInstance();

	private MqttClient() {}

	public void connect() {
		if (configuration.getBoolean(CFG_KEY_MQTT_ACTIVE, Defaults.ACTIVE)) {
			try {
				final MqttConnectOptions options = new MqttConnectOptions();
				options.setServerURIs(configuration.getString(CFG_KEY_MQTT_SERVER_URI).split(UriListEditor.URI_SPLIT_REGEX));
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
				options.setAutomaticReconnect(configuration.getBoolean(CFG_KEY_MQTT_AUTOMATIC_RECONNECT, Defaults.AUTOMATIC_RECONNECT));
				doConnect(options);
			}
			catch (final Exception e) {
				Logger.getInstance().log(e);
			}
		}
	}

	private synchronized void doConnect(final MqttConnectOptions options) {
		try {
			client = new org.eclipse.paho.client.mqttv3.MqttClient(configuration.getString(CFG_KEY_MQTT_SERVER_URI).split(UriListEditor.URI_SPLIT_REGEX)[0], configuration.getString(CFG_KEY_MQTT_CLIENT_ID, Defaults.CLIENT_ID));
			client.connect(options);
		}
		catch (final Exception e) {
			Logger.getInstance().log(e);
		}
	}

	public void publish(final Object payload) {
		publish(String.valueOf(payload));
	}

	public void publish(final String payload) {
		publish(payload.getBytes());
	}

	public void publish(final byte[] payload) {
		if (configuration.getBoolean(CFG_KEY_MQTT_ACTIVE, Defaults.ACTIVE)) {
			final MqttMessage message = new MqttMessage(payload);
			message.setRetained(configuration.getBoolean(CFG_KEY_MQTT_MESSAGE_RETAINED, Defaults.MESSAGE_RETAINED));
			message.setQos(configuration.getByte(CFG_KEY_MQTT_MESSAGE_QOS, Defaults.MESSAGE_QOS));
			if (client == null || (client != null && !client.isConnected() && configuration.getBoolean(CFG_KEY_MQTT_AUTOMATIC_RECONNECT, Defaults.AUTOMATIC_RECONNECT))) {
				connect();
			}
			try {
				if (client != null && client.isConnected()) {
					client.publish(configuration.getString(CFG_KEY_MQTT_TOPIC, Defaults.TOPIC), message);
				}
			}
			catch (final Exception e) {
				Logger.getInstance().log(e);
			}
		}
	}

	public synchronized void disconnect() {
		if (client != null && client.isConnected()) {
			try {
				client.disconnect();
				client = null;
			}
			catch (final Exception e) {
				Logger.getInstance().log(e);
			}
		}
	}

}
