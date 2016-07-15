package it.albertus.router.gui.listener;

import it.albertus.router.gui.DataTable;
import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CopyDataTableSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public CopyDataTableSelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final DataTable dataTable = gui.getDataTable();
		if (dataTable.canCopy()) {
			dataTable.copy();
		}
	}

}
