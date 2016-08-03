package it.albertus.router.mqtt;

import it.albertus.router.engine.RouterData;
import it.albertus.router.util.Logger;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttClient {

	protected volatile org.eclipse.paho.client.mqttv3.MqttClient mqttClient;

	private class MqttClientStartThread extends Thread {
		private final MqttConnectOptions options;

		private MqttClientStartThread(MqttConnectOptions options) {
			this.setName("httpServerStartThread");
						this.setDaemon(true);
			this.options = options;
		}

		public void run() {
			try {
				mqttClient.connect(options);
			}
			catch (final Exception e) {
				Logger.getInstance().log(e);
			}
		}
	}

	private static class Singleton {
		private static final MqttClient instance = new MqttClient();
	}

	public static MqttClient getInstance() {
		return Singleton.instance;
	}

	private MqttClient() {}

	public void connect() {
		if (true/* active */) {
			//		String tmpDir;
			//		try {
			//			tmpDir = File.createTempFile("-----", "-----").getParent();
			//		}
			//		catch (IOException e1) {
			//			tmpDir = System.getProperty("java.io.tmpdir");
			//		}
			//		MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
			try {
				mqttClient = new org.eclipse.paho.client.mqttv3.MqttClient("tcp://192.168.1.5:1883", "RouterLogger");
				final MqttConnectOptions options = new MqttConnectOptions();
				options.setUserName("admin");
				options.setPassword("xx".toCharArray());
				//				new MqttClientStartThread(options).start();
				mqttClient.connect(options);
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void publish(final RouterData routerData) {
		if (true/* active */&& mqttClient != null && mqttClient.isConnected()) {
			final MqttMessage message = new MqttMessage();
			message.setPayload(routerData.toString().getBytes());
			message.setRetained(true);
			message.setQos(1);
			try {
				if (mqttClient.isConnected()) {
					mqttClient.publish("routerlogger/data", message);
				}
			}
			catch (Exception e) {
				Logger.getInstance().log(e);
			}
		}
	}

	public void disconnect() {
		if (true/* active */&& mqttClient != null) {
			try {
				mqttClient.disconnect();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
