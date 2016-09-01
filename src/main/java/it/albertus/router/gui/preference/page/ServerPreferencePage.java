package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.page.RestartHeaderPreferencePage;
import it.albertus.router.resources.Resources;
import it.albertus.util.Localized;

public class ServerPreferencePage extends RestartHeaderPreferencePage {

	public static LocalizedLabelsAndValues getLogComboOptions() {
		final int length = 4;
		final LocalizedLabelsAndValues options = new LocalizedLabelsAndValues(length);
		for (int index = 0; index < length; index++) {
			final int value = index;
			final Localized name = new Localized() {
				@Override
				public String getString() {
					return Resources.get("lbl.preferences.server.log.request." + value);
				}
			};
			options.put(name, value);
		}
		return options;
	}

}
