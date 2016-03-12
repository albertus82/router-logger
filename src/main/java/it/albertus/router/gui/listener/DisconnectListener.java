package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class DisconnectListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public DisconnectListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		// TODO MessageBox YES|NO
		gui.disconnect();
	}

}
