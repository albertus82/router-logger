package it.albertus.routerlogger.gui.listener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

import it.albertus.jface.preference.Preferences;
import it.albertus.routerlogger.gui.RouterLoggerGui;
import it.albertus.routerlogger.gui.preference.RouterLoggerPreferences;
import it.albertus.routerlogger.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class PreferencesListener extends SelectionAdapter implements Listener {

	private static final Logger logger = LoggerFactory.getLogger(PreferencesListener.class);

	private final RouterLoggerGui gui;

	public PreferencesListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final Preferences preferences = new RouterLoggerPreferences(gui);
		try {
			preferences.openDialog(gui.getShell());
		}
		catch (final IOException e) {
			logger.log(Level.WARNING, e.toString(), e);
		}
		if (preferences.isRestartRequired()) {
			final MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			messageBox.setText(Messages.get("lbl.window.title"));
			messageBox.setMessage(Messages.get("lbl.preferences.restart"));
			if (messageBox.open() == SWT.YES) {
				gui.restart();
			}
		}
	}

	@Override
	public void handleEvent(final Event event) {
		widgetSelected(null);
	}

}
