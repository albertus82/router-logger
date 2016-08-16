package it.albertus.router.mqtt;

import it.albertus.jface.preference.field.UriListEditor;
import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerStatus;
import it.albertus.router.engine.Threshold;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.Configuration;
import it.albertus.util.ConfigurationException;

import java.util.Date;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

/** @Singleton */
public class RouterLoggerMqttClient extends BaseMqttClient {

	private static final String CFG_KEY_MQTT_CLEAN_SESSION = "mqtt.clean.session";
	private static final String CFG_KEY_MQTT_MAX_INFLIGHT = "mqtt.max.inflight";
	private static final String CFG_KEY_MQTT_CONNECTION_TIMEOUT = "mqtt.connection.timeout";
	private static final String CFG_KEY_MQTT_KEEP_ALIVE_INTERVAL = "mqtt.keep.alive.interval";
	private static final String CFG_KEY_MQTT_PASSWORD = "mqtt.password";
	private static final String CFG_KEY_MQTT_USERNAME = "mqtt.username";
	private static final String CFG_KEY_MQTT_CLIENT_ID = "mqtt.client.id";
	private static final String CFG_KEY_MQTT_SERVER_URI = "mqtt.server.uri";
	private static final String CFG_KEY_MQTT_ENABLED = "mqtt.enabled";
	private static final String CFG_KEY_MQTT_AUTOMATIC_RECONNECT = "mqtt.automatic.reconnect";
	private static final String CFG_KEY_MQTT_VERSION = "mqtt.version";
	private static final String CFG_KEY_MQTT_PERSISTENCE_FILE_ENABLED = "mqtt.persistence.file.enabled";
	private static final String CFG_KEY_MQTT_PERSISTENCE_FILE_CUSTOM = "mqtt.persistence.file.custom";
	private static final String CFG_KEY_MQTT_PERSISTENCE_FILE_PATH = "mqtt.persistence.file.path";

	private static final String CFG_KEY_MQTT_DATA_ENABLED = "mqtt.data.enabled";
	private static final String CFG_KEY_MQTT_DATA_TOPIC = "mqtt.data.topic";
	private static final String CFG_KEY_MQTT_DATA_QOS = "mqtt.data.qos";
	private static final String CFG_KEY_MQTT_DATA_RETAINED = "mqtt.data.retained";
	private static final String CFG_KEY_MQTT_DATA_THROTTLING_MS = "mqtt.data.throttling.ms";

	private static final String CFG_KEY_MQTT_THRESHOLDS_ENABLED = "mqtt.thresholds.enabled";
	private static final String CFG_KEY_MQTT_THRESHOLDS_TOPIC = "mqtt.thresholds.topic";
	private static final String CFG_KEY_MQTT_THRESHOLDS_QOS = "mqtt.thresholds.qos";
	private static final String CFG_KEY_MQTT_THRESHOLDS_RETAINED = "mqtt.thresholds.retained";
	private static final String CFG_KEY_MQTT_THRESHOLDS_THROTTLING_MS = "mqtt.thresholds.throttling.ms";

	private static final String CFG_KEY_MQTT_STATUS_ENABLED = "mqtt.status.enabled";
	private static final String CFG_KEY_MQTT_STATUS_TOPIC = "mqtt.status.topic";
	private static final String CFG_KEY_MQTT_STATUS_QOS = "mqtt.status.qos";
	private static final String CFG_KEY_MQTT_STATUS_RETAINED = "mqtt.status.retained";

	public interface Defaults {
		boolean ENABLED = false;
		String CLIENT_ID = "RouterLogger";
		int KEEP_ALIVE_INTERVAL = MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT;
		int CONNECTION_TIMEOUT = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;
		int MAX_INFLIGHT = MqttConnectOptions.MAX_INFLIGHT_DEFAULT;
		boolean CLEAN_SESSION = MqttConnectOptions.CLEAN_SESSION_DEFAULT;
		boolean AUTOMATIC_RECONNECT = true;
		byte MQTT_VERSION = MqttConnectOptions.MQTT_VERSION_DEFAULT;
		boolean PERSISTENCE_FILE_ENABLED = false;
		boolean PERSISTENCE_FILE_CUSTOM = false;

		boolean DATA_ENABLED = true;
		String DATA_TOPIC = "router/logger/data";
		byte DATA_QOS = MqttQos.AT_MOST_ONCE.getValue();
		boolean DATA_RETAINED = true;
		long DATA_THROTTLING_IN_MILLIS = 0;

		boolean THRESHOLDS_ENABLED = true;
		String THRESHOLDS_TOPIC = "router/logger/thresholds";
		byte THRESHOLDS_QOS = MqttQos.AT_MOST_ONCE.getValue();
		boolean THRESHOLDS_RETAINED = true;
		long THRESHOLDS_THROTTLING_IN_MILLIS = 0;

		boolean STATUS_ENABLED = true;
		String STATUS_TOPIC = "router/logger/status";
		byte STATUS_QOS = MqttQos.EXACTLY_ONCE.getValue();
		boolean STATUS_RETAINED = true;
	}

	private static class Singleton {
		private static final RouterLoggerMqttClient instance = new RouterLoggerMqttClient();
	}

	public static RouterLoggerMqttClient getInstance() {
		return Singleton.instance;
	}

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();

	private long lastDataMessageTime;
	private long lastThresholdsMessageTime;

	private RouterLoggerMqttClient() {}

	@Override
	protected void connect() {
		try {
			final MqttConnectOptions options = new MqttConnectOptions();
			final String[] serverURIs = configuration.getString(CFG_KEY_MQTT_SERVER_URI, "").split(UriListEditor.URI_SPLIT_REGEX);
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
			options.setAutomaticReconnect(configuration.getBoolean(CFG_KEY_MQTT_AUTOMATIC_RECONNECT, Defaults.AUTOMATIC_RECONNECT));
			options.setMqttVersion(configuration.getByte(CFG_KEY_MQTT_VERSION, Defaults.MQTT_VERSION));
			if (configuration.getBoolean(CFG_KEY_MQTT_STATUS_ENABLED, Defaults.STATUS_ENABLED)) {
				final String lwtTopic = configuration.getString(CFG_KEY_MQTT_STATUS_TOPIC, Defaults.STATUS_TOPIC);
				if (lwtTopic != null && !lwtTopic.isEmpty()) {
					options.setWill(lwtTopic, createPayload(new StatusPayload(RouterLoggerStatus.ABEND).toJson()), configuration.getByte(CFG_KEY_MQTT_STATUS_QOS, Defaults.STATUS_QOS), configuration.getBoolean(CFG_KEY_MQTT_STATUS_RETAINED, Defaults.STATUS_RETAINED));
				}
			}

			final String clientId = configuration.getString(CFG_KEY_MQTT_CLIENT_ID, Defaults.CLIENT_ID);

			final MqttClientPersistence persistence;
			if (configuration.getBoolean(CFG_KEY_MQTT_PERSISTENCE_FILE_ENABLED, Defaults.PERSISTENCE_FILE_ENABLED)) {
				final String directory = configuration.getString(CFG_KEY_MQTT_PERSISTENCE_FILE_PATH);
				if (configuration.getBoolean(CFG_KEY_MQTT_PERSISTENCE_FILE_CUSTOM, Defaults.PERSISTENCE_FILE_CUSTOM) && directory != null && !directory.isEmpty()) {
					persistence = new MqttDefaultFilePersistence(directory);
				}
				else {
					persistence = new MqttDefaultFilePersistence();
				}
			}
			else {
				persistence = new MemoryPersistence();
			}

			doConnect(clientId, options, persistence);
			if (Logger.getInstance().isDebugEnabled()) {
				System.out.println(options.toString().trim());
			}
		}
		catch (final Exception e) {
			Logger.getInstance().log(e);
		}
	}

	@Override
	public void disconnect() {
		final Logger logger = Logger.getInstance();
		try {
			if (doDisconnect()) {
				logger.log(Resources.get("msg.mqtt.disconnected"), Destination.CONSOLE);
			}
		}
		catch (final Exception e) {
			logger.log(e, Destination.CONSOLE, Destination.FILE);
		}
	}

	public void publishData(final RouterData data) {
		if (configuration.getBoolean(CFG_KEY_MQTT_ENABLED, Defaults.ENABLED) && configuration.getBoolean(CFG_KEY_MQTT_DATA_ENABLED, Defaults.DATA_ENABLED) && System.currentTimeMillis() - lastDataMessageTime >= configuration.getLong(CFG_KEY_MQTT_DATA_THROTTLING_MS, Defaults.DATA_THROTTLING_IN_MILLIS)) {
			final String topic = configuration.getString(CFG_KEY_MQTT_DATA_TOPIC, Defaults.DATA_TOPIC);
			final MqttMessage message = new MqttMessage(createPayload(data.toJson()));
			message.setRetained(configuration.getBoolean(CFG_KEY_MQTT_DATA_RETAINED, Defaults.DATA_RETAINED));
			message.setQos(configuration.getByte(CFG_KEY_MQTT_DATA_QOS, Defaults.DATA_QOS));
			try {
				doPublish(topic, message);
			}
			catch (final Exception e) {
				Logger.getInstance().log(e);
			}
			lastDataMessageTime = System.currentTimeMillis();
		}
	}

	public void publishStatus(final RouterLoggerStatus status) {
		if (configuration.getBoolean(CFG_KEY_MQTT_ENABLED, Defaults.ENABLED) && configuration.getBoolean(CFG_KEY_MQTT_STATUS_ENABLED, Defaults.STATUS_ENABLED)) {
			final String topic = configuration.getString(CFG_KEY_MQTT_STATUS_TOPIC, Defaults.STATUS_TOPIC);
			final MqttMessage message = new MqttMessage(createPayload(new StatusPayload(status).toJson()));
			message.setRetained(configuration.getBoolean(CFG_KEY_MQTT_STATUS_RETAINED, Defaults.STATUS_RETAINED));
			message.setQos(configuration.getByte(CFG_KEY_MQTT_STATUS_QOS, Defaults.STATUS_QOS));
			try {
				doPublish(topic, message);
			}
			catch (final Exception e) {
				Logger.getInstance().log(e);
			}
		}
	}

	public void publishThresholds(Map<Threshold, String> thresholdsReached, final Date timestamp) {
		if (thresholdsReached != null && !thresholdsReached.isEmpty() && configuration.getBoolean(CFG_KEY_MQTT_ENABLED, Defaults.ENABLED) && configuration.getBoolean(CFG_KEY_MQTT_THRESHOLDS_ENABLED, Defaults.THRESHOLDS_ENABLED) && System.currentTimeMillis() - lastThresholdsMessageTime >= configuration.getLong(CFG_KEY_MQTT_THRESHOLDS_THROTTLING_MS, Defaults.THRESHOLDS_THROTTLING_IN_MILLIS)) {
			final String topic = configuration.getString(CFG_KEY_MQTT_THRESHOLDS_TOPIC, Defaults.THRESHOLDS_TOPIC);
			final MqttMessage message = new MqttMessage(createPayload(new ThresholdsPayload(thresholdsReached, timestamp).toJson()));
			message.setRetained(configuration.getBoolean(CFG_KEY_MQTT_THRESHOLDS_RETAINED, Defaults.THRESHOLDS_RETAINED));
			message.setQos(configuration.getByte(CFG_KEY_MQTT_THRESHOLDS_QOS, Defaults.THRESHOLDS_QOS));
			try {
				doPublish(topic, message);
			}
			catch (final Exception e) {
				Logger.getInstance().log(e);
			}
			lastThresholdsMessageTime = System.currentTimeMillis();
		}
	}

}
