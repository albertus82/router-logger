package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.widgets.MenuItem;

public class EditArmListener implements ArmListener {

	private final RouterLoggerGui gui;

	public EditArmListener(final RouterLoggerGui gui) {
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
		if (gui.getDataTable().getTable() != null && gui.getDataTable().getTable().isFocusControl() && gui.getDataTable().getTable().getSelectionCount() != 0) {
			return true;
		}
		if (gui.getConsole().getText() != null && gui.getConsole().getText().isFocusControl() && gui.getConsole().getText().getSelectionCount() != 0) {
			return true;
		}
		return false;
	}

	private boolean canSelectAll() {
		if (gui.getDataTable().getTable() != null && gui.getDataTable().getTable().isFocusControl()) {
			return true;
		}
		if (gui.getConsole().getText() != null && gui.getConsole().getText().isFocusControl()) {
			return true;
		}
		return false;
	}

}
