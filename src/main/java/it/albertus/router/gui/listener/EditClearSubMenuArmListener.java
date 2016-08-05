package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;

/**
 * Attenzione: disabilitando gli elementi dei menu, vengono automaticamente
 * disabilitati anche i relativi acceleratori.
 */
public class EditClearSubMenuArmListener implements ArmListener {

	private final RouterLoggerGui gui;

	public EditClearSubMenuArmListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetArmed(final ArmEvent ae) {
		gui.getMenuBar().getEditClearDataTableMenuItem().setEnabled(gui.getDataTable().canClear());
		gui.getMenuBar().getEditClearConsoleMenuItem().setEnabled(gui.canClearConsole());
	}

}
