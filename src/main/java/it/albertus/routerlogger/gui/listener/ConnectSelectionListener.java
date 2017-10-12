package it.albertus.routerlogger.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import it.albertus.routerlogger.gui.RouterLoggerGui;

public class ConnectSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public ConnectSelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (gui.canConnect()) {
			gui.connect();
		}
	}

}
