package it.albertus.routerlogger.gui.listener;

import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;

import it.albertus.routerlogger.gui.DataTable;
import it.albertus.routerlogger.gui.RouterLoggerGui;

public class DataTableContextMenuDetectListener implements MenuDetectListener {

	private final RouterLoggerGui gui;

	public DataTableContextMenuDetectListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void menuDetected(final MenuDetectEvent mde) {
		final DataTable dataTable = gui.getDataTable();
		dataTable.getCopyMenuItem().setEnabled(dataTable.canCopy());
		dataTable.getDeleteMenuItem().setEnabled(dataTable.canDelete());
		dataTable.getSelectAllMenuItem().setEnabled(dataTable.canSelectAll());
		dataTable.getClearMenuItem().setEnabled(dataTable.canClear());
		dataTable.getContextMenu().setVisible(true);
	}

}
