package it.albertus.router.gui.listener;

import it.albertus.router.gui.DataTable;
import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.resources.Messages;

import org.eclipse.swt.events.SelectionEvent;

public class ClearDataTableSelectionListener extends ClearSelectionListener {

	public ClearDataTableSelectionListener(final RouterLoggerGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final DataTable dataTable = gui.getDataTable();
		if (dataTable.canClear()) {
			if (confirm(Messages.get("msg.confirm.clear.table.text"), Messages.get("msg.confirm.clear.table.message")) && dataTable.canClear()) {
				dataTable.clear();
			}
		}
	}

}
