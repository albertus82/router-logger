package it.albertus.router.gui.preference.page;

import it.albertus.router.resources.Resources;

public class GeneralPreferencePage extends BasePreferencePage {

	@Override
	protected Page getPage() {
		return Page.GENERAL;
	}

	public static String[][] getLanguageComboOptions() {
		final int length = Resources.Language.values().length;
		final String[][] options = new String[length][];
		for (int index = 0; index < length; index++) {
			options[index] = new String[] { Resources.Language.values()[index].getLocale().getDisplayLanguage(Resources.Language.values()[index].getLocale()), Resources.Language.values()[index].getLocale().getLanguage() };
		}
		return options;
	}

}
