package it.albertus.routerlogger.gui.listener;

import org.eclipse.swt.events.SelectionEvent;

import it.albertus.routerlogger.gui.DataTable;
import it.albertus.routerlogger.gui.RouterLoggerGui;
import it.albertus.routerlogger.resources.Messages;

public class ClearDataTableSelectionListener extends ClearSelectionListener {

	public ClearDataTableSelectionListener(final RouterLoggerGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final DataTable dataTable = gui.getDataTable();
		if (dataTable.canClear()) {
			final boolean confirm = confirm(Messages.get("msg.confirm.clear.table.text"), Messages.get("msg.confirm.clear.table.message"));
			if (confirm && dataTable.canClear()) {
				dataTable.clear();
			}
		}
	}

}
