package it.albertus.routerlogger.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.routerlogger.gui.DataTable;
import it.albertus.routerlogger.gui.RouterLoggerGui;

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
