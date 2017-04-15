package it.albertus.router.gui.preference.page;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
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

	public static StaticLabelsAndValues getLoggingLevelComboOptions(final Level min, final Level max) {
		final Map<Integer, Level> levels = LoggingSupport.getLevels();
		final StaticLabelsAndValues options = new StaticLabelsAndValues(levels.size());
		for (final Entry<Integer, Level> entry : levels.entrySet()) {
			if ((min == null || entry.getKey().intValue() >= min.intValue()) && (max == null || entry.getKey().intValue() <= max.intValue())) {
				options.put(entry.getValue().getName(), entry.getValue().getName());
			}
		}
		return options;
	}

	public static StaticLabelsAndValues getLoggingLevelComboOptions(final Level[] levels) {
		final StaticLabelsAndValues options = new StaticLabelsAndValues(levels.length);
		final List<Level> levelsList = Arrays.asList(levels);
		for (final Level level : LoggingSupport.getLevels().values()) {
			if (levelsList.contains(level)) {
				options.put(level.getName(), level.getName());
			}
		}
		return options;
	}

	public static StaticLabelsAndValues getLoggingLevelComboOptions() {
		return getLoggingLevelComboOptions(null, null);
	}

}
