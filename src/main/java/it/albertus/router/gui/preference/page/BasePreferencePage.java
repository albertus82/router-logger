package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.page.AbstractPreferencePage;
import it.albertus.jface.preference.page.PageDefinition;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.gui.preference.RouterLoggerPreference;

public abstract class BasePreferencePage extends AbstractPreferencePage {

	public BasePreferencePage() {
		super(RouterLoggerConfiguration.getInstance(), RouterLoggerPreference.values());
	}

	protected BasePreferencePage(final int style) {
		super(RouterLoggerConfiguration.getInstance(), RouterLoggerPreference.values(), style);
	}

	@Override
	public PageDefinition getPageDefinition() {
		return RouterLoggerPage.forClass(getClass());
	}

}
