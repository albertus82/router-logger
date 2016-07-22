package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.LocalizedComboEntryNamesAndValues;
import it.albertus.router.resources.Resources;
import it.albertus.router.resources.Resources.Language;
import it.albertus.util.Localized;

import java.util.Locale;

public class GeneralPreferencePage extends BasePreferencePage {

	public static LocalizedComboEntryNamesAndValues getLanguageComboOptions() {
		final Language[] values = Resources.Language.values();
		final LocalizedComboEntryNamesAndValues options = new LocalizedComboEntryNamesAndValues(values.length);
		for (final Language language : values) {
			final Locale locale = language.getLocale();
			final String value = locale.getLanguage();
			final Localized name = new Localized() {
				@Override
				public String getString() {
					return locale.getDisplayLanguage(locale);
				}
			};
			options.put(name, value);
		}
		return options;
	}

}
