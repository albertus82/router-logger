package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.gui.preferences.Preferences;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class PreferencesSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public PreferencesSelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		new Preferences(gui);
	}

}
