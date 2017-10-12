package it.albertus.routerlogger.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.routerlogger.gui.RouterLoggerGui;

public class CopyMenuBarSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public CopyMenuBarSelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (gui.canCopyConsole()) {
			gui.getConsole().getScrollable().copy();
		}
		else if (gui.getDataTable().canCopy()) {
			gui.getDataTable().copy();
		}
	}

}
