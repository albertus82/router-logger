package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.IPreference;
import it.albertus.jface.preference.page.AbstractPreferencePage;
import it.albertus.jface.preference.page.IPage;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.gui.preference.Preference;
import it.albertus.util.Configuration;

public abstract class BasePreferencePage extends AbstractPreferencePage {

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
