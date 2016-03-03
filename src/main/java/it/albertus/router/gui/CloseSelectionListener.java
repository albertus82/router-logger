package it.albertus.router.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CloseSelectionListener extends SelectionAdapter {

	private final RouterLoggerGui gui;

	public CloseSelectionListener(RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (!CloseMessageBox.show() || CloseMessageBox.newInstance(gui.getShell()).open() == SWT.YES) {
			gui.getShell().dispose();
		}
	}

}
