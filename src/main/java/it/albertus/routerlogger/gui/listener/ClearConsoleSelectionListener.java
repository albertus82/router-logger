package it.albertus.routerlogger.gui.listener;

import org.eclipse.swt.events.SelectionEvent;

import it.albertus.routerlogger.gui.RouterLoggerGui;

public class ClearConsoleSelectionListener extends ClearSelectionListener {

	public ClearConsoleSelectionListener(final RouterLoggerGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (gui.canClearConsole()) {
			gui.getConsole().clear();
		}
	}

}
