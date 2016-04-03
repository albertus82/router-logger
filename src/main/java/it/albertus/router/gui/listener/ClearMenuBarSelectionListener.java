package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.resources.Resources;

import org.eclipse.swt.events.SelectionEvent;

public class ClearMenuBarSelectionListener extends ClearSelectionListener {

	public ClearMenuBarSelectionListener(final RouterLoggerGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (gui.canClearConsole()) {
			if (askForClearing(Resources.get("msg.confirm.clear.console.text"), Resources.get("msg.confirm.clear.console.message")) && gui.canClearConsole()) {
				gui.getConsole().getText().setText("");
			}
		}
		else if (gui.canClearDataTable()) {
			if (askForClearing(Resources.get("msg.confirm.clear.table.text"), Resources.get("msg.confirm.clear.table.message")) && gui.canClearDataTable()) {
				gui.getDataTable().clear();
			}
		}
	}

}
