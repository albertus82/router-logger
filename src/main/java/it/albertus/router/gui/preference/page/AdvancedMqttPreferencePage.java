package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.page.RestartHeaderPreferencePage;
import it.albertus.router.resources.Messages;
import it.albertus.util.Localized;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class AdvancedMqttPreferencePage extends RestartHeaderPreferencePage {

	public static LocalizedLabelsAndValues getMqttVersionComboOptions() {
		final LocalizedLabelsAndValues options = new LocalizedLabelsAndValues(3);
		Localized name = new Localized() {
			@Override
			public String getString() {
				return Messages.get("lbl.mqtt.version.default");
			}
		};
		options.add(name, MqttConnectOptions.MQTT_VERSION_DEFAULT);
		name = new Localized() {
			@Override
			public String getString() {
				return "3.1";
			}
		};
		options.add(name, MqttConnectOptions.MQTT_VERSION_3_1);
		name = new Localized() {
			@Override
			public String getString() {
				return "3.1.1";
			}
		};
		options.add(name, MqttConnectOptions.MQTT_VERSION_3_1_1);
		return options;
	}

}
