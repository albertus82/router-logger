package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.resources.Resources;

import org.eclipse.swt.events.SelectionEvent;

public class ClearDataTableSelectionListener extends ClearSelectionListener {

	public ClearDataTableSelectionListener(final RouterLoggerGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (gui.canClearDataTable()) {
			if (confirm(Resources.get("msg.confirm.clear.table.text"), Resources.get("msg.confirm.clear.table.message")) && gui.canClearDataTable()) {
				gui.getDataTable().clear();
			}
		}
	}

}
