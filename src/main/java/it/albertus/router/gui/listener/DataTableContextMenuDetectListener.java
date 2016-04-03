package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;

public class DataTableContextMenuDetectListener implements MenuDetectListener {

	private final RouterLoggerGui gui;

	public DataTableContextMenuDetectListener(RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void menuDetected(MenuDetectEvent e) {
		gui.getDataTable().getCopyMenuItem().setEnabled(gui.canCopyDataTable());
		gui.getDataTable().getDeleteMenuItem().setEnabled(gui.canDeleteDataTable());
		gui.getDataTable().getSelectAllMenuItem().setEnabled(gui.canSelectAllDataTable());
		gui.getDataTable().getClearMenuItem().setEnabled(gui.canClearDataTable());
		gui.getDataTable().getContextMenu().setVisible(true);
	}

}
