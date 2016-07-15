package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class SelectAllMenuBarSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public SelectAllMenuBarSelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (gui.canSelectAllConsole()) {
			gui.getTextConsole().getText().selectAll();
		}
		else if (gui.canSelectAllDataTable()) {
			gui.getDataTable().getTable().selectAll();
		}
	}

}
