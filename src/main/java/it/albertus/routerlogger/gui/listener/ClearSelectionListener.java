package it.albertus.routerlogger.gui.listener;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import it.albertus.routerlogger.gui.RouterLoggerGui;

public abstract class ClearSelectionListener implements SelectionListener {

	protected final RouterLoggerGui gui;

	public ClearSelectionListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public final void widgetDefaultSelected(final SelectionEvent e) {/* Ignore */}

}
