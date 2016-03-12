package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ConnectSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public ConnectSelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		gui.connect();
	}

}
