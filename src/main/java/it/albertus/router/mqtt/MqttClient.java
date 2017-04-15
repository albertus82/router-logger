package it.albertus.router.mqtt;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import it.albertus.jface.preference.field.UriListEditor;
import it.albertus.router.dto.RouterDataDto;
import it.albertus.router.dto.StatusDto;
import it.albertus.router.dto.ThresholdsDto;
import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerStatus;
import it.albertus.router.engine.Status;
import it.albertus.router.engine.ThresholdsReached;
import it.albertus.router.resources.Messages;
import it.albertus.router.util.Payload;
import it.albertus.util.Configuration;
import it.albertus.util.ConfigurationException;
import it.albertus.util.logging.LoggerFactory;

// @Singleton
public class MqttClient extends BaseMqttClient {

	private static final Logger logger = LoggerFactory.getLogger(MqttClient.class);

	private static final Configuration configuration = RouterLoggerConfiguration.getInstance();

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
	private static final String CFG_KEY_MQTT_CONNECT_RETRY = "mqtt.connect.retry";
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

	public static class Defaults {
		public static final boolean ENABLED = false;
		public static final String CLIENT_ID = "RouterLogger";
		public static final int KEEP_ALIVE_INTERVAL = MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT;
		public static final int CONNECTION_TIMEOUT = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;
		public static final int MAX_INFLIGHT = MqttConnectOptions.MAX_INFLIGHT_DEFAULT;
		public static final boolean CLEAN_SESSION = MqttConnectOptions.CLEAN_SESSION_DEFAULT;
		public static final boolean AUTOMATIC_RECONNECT = true;
		public static final boolean CONNECT_RETRY = true;
		public static final byte MQTT_VERSION = MqttConnectOptions.MQTT_VERSION_DEFAULT;
		public static final boolean PERSISTENCE_FILE_ENABLED = false;
		public static final boolean PERSISTENCE_FILE_CUSTOM = false;

		public static final boolean DATA_ENABLED = true;
		public static final String DATA_TOPIC = "router/logger/data";
		public static final byte DATA_QOS = MqttQos.AT_MOST_ONCE.getValue();
		public static final boolean DATA_RETAINED = true;
		public static final long DATA_THROTTLING_IN_MILLIS = 0;

		public static final boolean THRESHOLDS_ENABLED = true;
		public static final String THRESHOLDS_TOPIC = "router/logger/thresholds";
		public static final byte THRESHOLDS_QOS = MqttQos.AT_MOST_ONCE.getValue();
		public static final boolean THRESHOLDS_RETAINED = true;
		public static final long THRESHOLDS_THROTTLING_IN_MILLIS = 0;

		public static final boolean STATUS_ENABLED = true;
		public static final String STATUS_TOPIC = "router/logger/status";
		public static final byte STATUS_QOS = MqttQos.EXACTLY_ONCE.getValue();
		public static final boolean STATUS_RETAINED = true;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private long lastDataMessageTime;
	private long lastThresholdsMessageTime;

	private MqttClient() {}

	private static class Singleton {
		private static final MqttClient instance = new MqttClient();

		private Singleton() {
			throw new IllegalAccessError();
		}
	}

	public static MqttClient getInstance() {
		return Singleton.instance;
	}

	@Override
	protected void connect() {
		try {
			final MqttConnectOptions options = new MqttConnectOptions();
			final String[] serverURIs = configuration.getString(CFG_KEY_MQTT_SERVER_URI, true).split(UriListEditor.URI_SPLIT_REGEX);
			if (serverURIs == null || serverURIs.length == 0 || serverURIs[0].trim().isEmpty()) {
				throw new ConfigurationException(Messages.get("err.mqtt.cfg.error.uri"), CFG_KEY_MQTT_SERVER_URI);
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
					options.setWill(lwtTopic, Payload.createPayload(new StatusDto(new RouterLoggerStatus(Status.ABEND)).toJson()), configuration.getByte(CFG_KEY_MQTT_STATUS_QOS, Defaults.STATUS_QOS), configuration.getBoolean(CFG_KEY_MQTT_STATUS_RETAINED, Defaults.STATUS_RETAINED));
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

			doConnect(clientId, options, persistence, configuration.getBoolean(CFG_KEY_MQTT_CONNECT_RETRY, Defaults.CONNECT_RETRY));
			if (logger.isLoggable(Level.FINE)) {
				System.out.println(options.toString().trim() + "======");
			}
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}

	@Override
	public void disconnect() {
		try {
			if (doDisconnect()) {
				logger.info(Messages.get("msg.mqtt.disconnected"));
			}
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}
	}

	public void publishData(final RouterData data) {
		if (configuration.getBoolean(CFG_KEY_MQTT_ENABLED, Defaults.ENABLED) && configuration.getBoolean(CFG_KEY_MQTT_DATA_ENABLED, Defaults.DATA_ENABLED) && System.currentTimeMillis() - lastDataMessageTime >= configuration.getLong(CFG_KEY_MQTT_DATA_THROTTLING_MS, Defaults.DATA_THROTTLING_IN_MILLIS)) {
			final String topic = configuration.getString(CFG_KEY_MQTT_DATA_TOPIC, Defaults.DATA_TOPIC);
			final MqttMessage message = new MqttMessage(Payload.createPayload(new RouterDataDto(data).toJson()));
			message.setRetained(configuration.getBoolean(CFG_KEY_MQTT_DATA_RETAINED, Defaults.DATA_RETAINED));
			message.setQos(configuration.getByte(CFG_KEY_MQTT_DATA_QOS, Defaults.DATA_QOS));
			try {
				doPublish(topic, message);
			}
			catch (final Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
			}
			lastDataMessageTime = System.currentTimeMillis();
		}
	}

	public void publishStatus(final RouterLoggerStatus status) {
		if (configuration.getBoolean(CFG_KEY_MQTT_ENABLED, Defaults.ENABLED) && configuration.getBoolean(CFG_KEY_MQTT_STATUS_ENABLED, Defaults.STATUS_ENABLED)) {
			final String topic = configuration.getString(CFG_KEY_MQTT_STATUS_TOPIC, Defaults.STATUS_TOPIC);
			final MqttMessage message = new MqttMessage(Payload.createPayload(new StatusDto(status).toJson()));
			message.setRetained(configuration.getBoolean(CFG_KEY_MQTT_STATUS_RETAINED, Defaults.STATUS_RETAINED));
			message.setQos(configuration.getByte(CFG_KEY_MQTT_STATUS_QOS, Defaults.STATUS_QOS));
			try {
				doPublish(topic, message);
			}
			catch (final Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
			}
		}
	}

	public void publishThresholds(final ThresholdsReached thresholdsReached) {
		if (thresholdsReached != null && thresholdsReached.getReached() != null && !thresholdsReached.getReached().isEmpty() && configuration.getBoolean(CFG_KEY_MQTT_ENABLED, Defaults.ENABLED) && configuration.getBoolean(CFG_KEY_MQTT_THRESHOLDS_ENABLED, Defaults.THRESHOLDS_ENABLED) && System.currentTimeMillis() - lastThresholdsMessageTime >= configuration.getLong(CFG_KEY_MQTT_THRESHOLDS_THROTTLING_MS, Defaults.THRESHOLDS_THROTTLING_IN_MILLIS)) {
			final String topic = configuration.getString(CFG_KEY_MQTT_THRESHOLDS_TOPIC, Defaults.THRESHOLDS_TOPIC);
			final MqttMessage message = new MqttMessage(Payload.createPayload(new ThresholdsDto(thresholdsReached).toJson()));
			message.setRetained(configuration.getBoolean(CFG_KEY_MQTT_THRESHOLDS_RETAINED, Defaults.THRESHOLDS_RETAINED));
			message.setQos(configuration.getByte(CFG_KEY_MQTT_THRESHOLDS_QOS, Defaults.THRESHOLDS_QOS));
			try {
				doPublish(topic, message);
			}
			catch (final Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
			}
			lastThresholdsMessageTime = System.currentTimeMillis();
		}
	}

}
