package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class RestoreShellListener implements Listener {

	private final RouterLoggerGui gui;

	public RestoreShellListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void handleEvent(Event event) {
		gui.getShell().setVisible(true);
		gui.getTrayIcon().getTrayItem().setVisible(false);
	}

}
