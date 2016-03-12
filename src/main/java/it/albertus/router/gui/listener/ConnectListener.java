package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ConnectListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public ConnectListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		// TODO
	}

}
