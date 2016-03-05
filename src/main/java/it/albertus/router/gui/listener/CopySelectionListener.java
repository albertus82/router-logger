package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CopySelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public CopySelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (gui.canCopyConsole()) {
			gui.getConsole().getText().copy();
		}
		else if (gui.canCopyDataTable()) {
			gui.getDataTable().copySelection();
		}
	}

}
