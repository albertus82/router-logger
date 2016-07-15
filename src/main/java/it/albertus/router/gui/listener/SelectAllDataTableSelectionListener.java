package it.albertus.router.gui.listener;

import it.albertus.router.gui.DataTable;
import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class SelectAllDataTableSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public SelectAllDataTableSelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final DataTable dataTable = gui.getDataTable();
		if (dataTable.canSelectAll()) {
			dataTable.getTable().selectAll();
		}
	}

}
