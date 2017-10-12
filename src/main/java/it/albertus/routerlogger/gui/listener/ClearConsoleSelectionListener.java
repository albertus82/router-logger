package it.albertus.routerlogger.gui.listener;

import org.eclipse.swt.events.SelectionEvent;

import it.albertus.routerlogger.gui.RouterLoggerGui;
import it.albertus.routerlogger.resources.Messages;

public class ClearConsoleSelectionListener extends ClearSelectionListener {

	public ClearConsoleSelectionListener(final RouterLoggerGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (gui.canClearConsole() && confirm(Messages.get("msg.confirm.clear.console.text"), Messages.get("msg.confirm.clear.console.message"))) {
			gui.getConsole().clear();
		}
	}

}
