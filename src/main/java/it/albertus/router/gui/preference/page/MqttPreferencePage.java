package it.albertus.router.gui.preference.page;

import it.albertus.jface.TextFormatter;
import it.albertus.jface.preference.LocalizedComboEntryNamesAndValues;
import it.albertus.router.mqtt.MqttQos;
import it.albertus.router.resources.Resources;
import it.albertus.util.Localized;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class MqttPreferencePage extends BasePreferencePage {

	@Override
	protected Control createHeader() {
		final Label header = new Label(getFieldEditorParent(), SWT.WRAP);
		TextFormatter.setBoldFontStyle(header);
		header.setText(Resources.get("lbl.preferences.mqtt.header"));
		return header;
	}

	public static LocalizedComboEntryNamesAndValues getMqttQosComboOptions() {
		final MqttQos[] values = MqttQos.values();
		final LocalizedComboEntryNamesAndValues options = new LocalizedComboEntryNamesAndValues(values.length);
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
