package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.LocalizedNamesAndValues;
import it.albertus.router.resources.Resources;
import it.albertus.util.Localized;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class AdvancedMqttPreferencePage extends MqttPreferencePage {

	public static LocalizedNamesAndValues getMqttVersionComboOptions() {
		final LocalizedNamesAndValues options = new LocalizedNamesAndValues(3);
		Localized name = new Localized() {
			@Override
			public String getString() {
				return Resources.get("lbl.mqtt.version.default");
			}
		};
		options.put(name, MqttConnectOptions.MQTT_VERSION_DEFAULT);
		name = new Localized() {
			@Override
			public String getString() {
				return "3.1";
			}
		};
		options.put(name, MqttConnectOptions.MQTT_VERSION_3_1);
		name = new Localized() {
			@Override
			public String getString() {
				return "3.1.1";
			}
		};
		options.put(name, MqttConnectOptions.MQTT_VERSION_3_1_1);
		return options;
	}

}
