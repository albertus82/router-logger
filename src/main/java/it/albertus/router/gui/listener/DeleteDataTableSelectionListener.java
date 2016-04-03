package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class DeleteDataTableSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public DeleteDataTableSelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (gui.canDeleteDataTable() && gui.getDataTable().getTable().isFocusControl()) {
			gui.getDataTable().delete();
		}
	}

}
