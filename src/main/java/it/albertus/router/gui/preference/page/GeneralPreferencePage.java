package it.albertus.router.gui.preference.page;

import java.util.Locale;

import it.albertus.router.resources.Resources;

public class GeneralPreferencePage extends BasePreferencePage {

	public static String[][] getLanguageComboOptions() {
		final int length = Resources.Language.values().length;
		final String[][] options = new String[length][];
		for (int index = 0; index < length; index++) {
			final Locale locale = Resources.Language.values()[index].getLocale();
			options[index] = new String[] { locale.getDisplayLanguage(locale), locale.getLanguage() };
		}
		return options;
	}

}
