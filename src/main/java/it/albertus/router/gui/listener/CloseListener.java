package it.albertus.router.gui.listener;

import it.albertus.router.gui.CloseMessageBox;
import it.albertus.router.gui.Gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class CloseListener extends SelectionAdapter implements Listener {

	private final Gui gui;

	public CloseListener(final Gui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (!CloseMessageBox.show() || confirmClose()) {
			gui.getShell().dispose();
		}
	}

	@Override
	public void handleEvent(Event event) {
		event.doit = !CloseMessageBox.show() || confirmClose();
	}

	private boolean confirmClose() {
		return CloseMessageBox.newInstance(gui.getShell()).open() == SWT.YES;
	}

}
