package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.widgets.MenuItem;

public class EditMenuBarArmListener implements ArmListener {

	private final RouterLoggerGui gui;

	public EditMenuBarArmListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetArmed(ArmEvent e) {
		final MenuItem copyMenuItem = gui.getMenuBar().getEditCopyMenuItem();
		copyMenuItem.setEnabled(canCopy());

		final MenuItem selectAllMenuItem = gui.getMenuBar().getEditSelectAllMenuItem();
		selectAllMenuItem.setEnabled(canSelectAll());
	}

	private boolean canCopy() {
		if (gui.canCopyConsole()) {
			return true;
		}
		if (gui.canCopyDataTable()) {
			return true;
		}
		return false;
	}

	private boolean canSelectAll() {
		if (gui.canSelectAllConsole()) {
			return true;
		}
		if (gui.canSelectAllDataTable()) {
			return true;
		}
		return false;
	}

}
