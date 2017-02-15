package it.albertus.router.gui.preference.page;

import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.StaticLabelsAndValues;
import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.router.resources.Messages;
import it.albertus.router.resources.Messages.Language;
import it.albertus.util.Localized;
import it.albertus.util.logging.LoggingSupport;

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
			options.put(name, value);
		}
		return options;
	}

	public static StaticLabelsAndValues getLoggingComboOptions() {
		final Map<Integer, Level> levels = LoggingSupport.getLevels();
		final StaticLabelsAndValues options = new StaticLabelsAndValues(levels.size());
		for (final Level level : levels.values()) {
			options.put(level.getName(), level.getName());
		}
		return options;
	}

}
