package it.albertus.routerlogger.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.routerlogger.gui.RouterLoggerGui;

public class SelectAllMenuBarSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public SelectAllMenuBarSelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (gui.canSelectAllConsole()) {
			gui.getConsole().getScrollable().selectAll();
		}
		else if (gui.getDataTable().canSelectAll()) {
			gui.getDataTable().getTable().selectAll();
		}
	}

}
