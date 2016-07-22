package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.LocalizedComboEntryNamesAndValues;
import it.albertus.router.resources.Resources;
import it.albertus.util.Localized;

import java.util.Locale;

public class GeneralPreferencePage extends BasePreferencePage {

	public static LocalizedComboEntryNamesAndValues getLanguageComboOptions() {
		final int length = Resources.Language.values().length;
		final LocalizedComboEntryNamesAndValues options = new LocalizedComboEntryNamesAndValues();
		for (int i = 0; i < length; i++) {
			final int index = i;
			final Locale locale = Resources.Language.values()[index].getLocale();
			final String value = locale.getLanguage();
			final Localized name = new Localized() {
				@Override
				public String getString() {
					return locale.getDisplayLanguage(locale);
				}
			};
			options.add(name, value);
		}
		return options;
	}

}
