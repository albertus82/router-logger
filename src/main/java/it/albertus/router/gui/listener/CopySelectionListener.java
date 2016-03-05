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
		if (gui.getDataTable().getTable() != null && gui.getDataTable().getTable().isFocusControl() && gui.getDataTable().getTable().getSelectionIndex() != -1) {
			gui.getDataTable().copySelection();
		}
		else if (gui.getConsole().getText() != null && gui.getConsole().getText().isFocusControl() && gui.getConsole().getText().getSelectionCount() != 0) {
			gui.getConsole().getText().copy();
		}
	}

}
