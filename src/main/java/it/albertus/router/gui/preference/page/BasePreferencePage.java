package it.albertus.router.gui.preference.page;

import it.albertus.gui.preference.IPreference;
import it.albertus.gui.preference.page.APreferencePage;
import it.albertus.gui.preference.page.IPage;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.gui.preference.Preference;
import it.albertus.util.Configuration;

public abstract class BasePreferencePage extends APreferencePage {

	public BasePreferencePage() {
		super();
	}

	protected BasePreferencePage(final int style) {
		super(style);
	}

	@Override
	public IPage getPage() {
		return Page.forClass(getClass());
	}

	@Override
	protected IPreference[] getPreferences() {
		return Preference.values();
	}

	@Override
	protected Configuration getConfiguration() {
		return RouterLoggerConfiguration.getInstance();
	}

}
