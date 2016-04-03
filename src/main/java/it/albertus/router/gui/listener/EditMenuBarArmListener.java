package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Attenzione: disabilitando gli elementi dei menu, vengono automaticamente
 * disabilitati anche i relativi acceleratori.
 */
public class EditMenuBarArmListener implements ArmListener {

	private final RouterLoggerGui gui;

	public EditMenuBarArmListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetArmed(ArmEvent e) {
		final MenuItem clearMenuItem = gui.getMenuBar().getEditClearMenuItem();
		clearMenuItem.setEnabled(gui.canClearDataTable() || gui.canClearConsole());
	}

}
