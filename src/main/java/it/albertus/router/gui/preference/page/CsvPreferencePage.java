package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.router.resources.Resources;
import it.albertus.util.Localized;

public class CsvPreferencePage extends BasePreferencePage {

	private enum Separator {
		COMMA("lbl.preferences.separator.comma", ","),
		TAB("lbl.preferences.separator.tab", "\t"),
		COLON("lbl.preferences.separator.colon", ":"),
		SEMICOLON("lbl.preferences.separator.semicolon", ";"),
		PIPE("lbl.preferences.separator.pipe", "|");

		private final String resourceKey;
		private final String separator;

		private Separator(final String resourceKey, final String separator) {
			this.resourceKey = resourceKey;
			this.separator = separator;
		}
	}

	public static LocalizedLabelsAndValues getSeparatorComboOptions() {
		final Separator[] values = Separator.values();
		final LocalizedLabelsAndValues options = new LocalizedLabelsAndValues(values.length);
		for (final Separator separator : values) {
			final String value = separator.separator;
			final Localized name = new Localized() {
				@Override
				public String getString() {
					return Resources.get(separator.resourceKey);
				}
			};
			options.put(name, value);
		}
		return options;
	}

}
