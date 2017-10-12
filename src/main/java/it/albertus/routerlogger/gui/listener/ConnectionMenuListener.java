package it.albertus.routerlogger.gui.listener;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.routerlogger.gui.RouterLoggerGui;
import it.albertus.util.ConfigurationException;
import it.albertus.util.logging.LoggerFactory;

/**
 * Attenzione: disabilitando gli elementi dei menu, vengono automaticamente
 * disabilitati anche i relativi acceleratori.
 */
public class ConnectionMenuListener implements ArmListener, MenuListener {

	private static final Logger logger = LoggerFactory.getLogger(ConnectionMenuListener.class);

	private final RouterLoggerGui gui;

	public ConnectionMenuListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetArmed(final ArmEvent event) {
		execute();
	}

	@Override
	public void menuShown(final MenuEvent e) {
		execute();
	}

	@Override
	public void menuHidden(final MenuEvent e) {/* Ignore */}

	private void execute() {
		final MenuItem connectMenuItem = gui.getMenuBar().getConnectionConnectItem();
		boolean connectEnabled;
		try {
			connectEnabled = gui.canConnect();
		}
		catch (final ConfigurationException e) {
			logger.log(Level.FINE, e.toString(), e);
			connectEnabled = false;
		}
		connectMenuItem.setEnabled(connectEnabled);

		final MenuItem disconnectMenuItem = gui.getMenuBar().getConnectionDisconnectItem();
		disconnectMenuItem.setEnabled(gui.canDisconnect());
	}

}
