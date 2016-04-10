package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.gui.preference.Preferences;
import it.albertus.router.util.Logger;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class PreferencesSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public PreferencesSelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final Preferences preferences = new Preferences(gui);
		try {
			preferences.open();
		}
		catch (final Exception e) {
			Logger.getInstance().log(e);
		}
	}

}
