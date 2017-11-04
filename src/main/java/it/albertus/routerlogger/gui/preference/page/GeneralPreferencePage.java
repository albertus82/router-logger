package it.albertus.routerlogger.gui.preference.page;

import java.util.Locale;

import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.routerlogger.resources.Messages;
import it.albertus.routerlogger.resources.Messages.Language;
import it.albertus.util.Localized;

public class GeneralPreferencePage extends BasePreferencePage {

	public static LocalizedLabelsAndValues getLanguageComboOptions() {
		final Language[] values = Messages.Language.values();
		final LocalizedLabelsAndValues options = new LocalizedLabelsAndValues(values.length);
		for (final Language language : values) {
			final Locale locale = language.getLocale();
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
