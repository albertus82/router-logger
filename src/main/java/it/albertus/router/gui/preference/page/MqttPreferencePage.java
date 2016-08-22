package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.LocalizedNamesAndValues;
import it.albertus.router.mqtt.MqttQos;
import it.albertus.util.Localized;

public class MqttPreferencePage extends RestartHeaderPreferencePage {

	public static LocalizedNamesAndValues getMqttQosComboOptions() {
		final MqttQos[] values = MqttQos.values();
		final LocalizedNamesAndValues options = new LocalizedNamesAndValues(values.length);
		for (final MqttQos qos : values) {
			final byte value = qos.getValue();
			final Localized name = new Localized() {
				@Override
				public String getString() {
					return qos.getDescription();
				}
			};
			options.put(name, value);
		}
		return options;
	}

}
