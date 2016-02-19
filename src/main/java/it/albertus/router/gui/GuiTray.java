package it.albertus.router.gui;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.engine.RouterLoggerStatus;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class GuiTray {

	private static class Singleton {
		private static final GuiTray TRAY = new GuiTray();
	}

	public static GuiTray getInstance() {
		return Singleton.TRAY;
	}

	private GuiTray() {}

	private final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	private TrayItem trayItem = null;
	private Menu menu = null;
	private String toolTipText = null;
	private Image trayIcon = null;

	private Image getTrayIcon(RouterLoggerStatus status) {
		switch (status) {
		case STARTING:
		case CONNECTING:
		case DISCONNECTED:
			return GuiImages.TRAY_ICON_ROUTER_GREY;
		case RECONNECTING:
			return GuiImages.TRAY_ICON_ROUTER_GREY_WARNING;
		case INFO:
		case WARNING:
			return GuiImages.TRAY_ICON_ROUTER_BLUE_WARNING;
		case AUTHENTICATING:
			return GuiImages.TRAY_ICON_ROUTER_BLUE_LOCK;
		case ERROR:
			return GuiImages.TRAY_ICON_ROUTER_GREY_ERROR;
		default:
			return GuiImages.TRAY_ICON_ROUTER_BLUE;
		}
	}

	public void init(final Shell shell, final RouterLoggerEngine app) {
		if (this.trayItem == null && menu == null) {
			shell.addShellListener(new ShellAdapter() {
				@Override
				public void shellIconified(ShellEvent e) {
					iconify(shell, app.getStatus());
					shell.setMinimized(false);
				}
			});
		}
		else {
			throw new IllegalStateException(Resources.get("err.already.initialized", this.getClass().getSimpleName()));
		}
	}

	private void iconify(final Shell shell, final RouterLoggerStatus status) {
		Tray tray = shell.getDisplay().getSystemTray();
		if (tray != null) {
			shell.setVisible(false);
			boolean addListeners = false;
			if (trayItem == null) {
				trayItem = new TrayItem(tray, SWT.NONE);
				trayIcon = getTrayIcon(status);
				trayItem.setImage(trayIcon);
				toolTipText = getBaseToolTipText(status);
				trayItem.setToolTipText(toolTipText);
				addListeners = true;
			}
			else {
				trayItem.setVisible(true);
			}

			if (menu == null) {
				menu = new Menu(shell, SWT.POP_UP);
				MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
				menuItem.setText(Resources.get("lbl.tray.show"));

				menuItem.addListener(SWT.Selection, new RestoreListener(shell));

				menuItem = new MenuItem(menu, SWT.SEPARATOR);

				// Tasto "Exit"...
				menuItem = new MenuItem(menu, SWT.PUSH);
				menuItem.setText(Resources.get("lbl.tray.close"));
				menuItem.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						if (!GuiCloseMessageBox.show() || GuiCloseMessageBox.newInstance(shell).open() == SWT.YES) {
							shell.dispose();
						}
					}
				});
			}

			if (addListeners) {
				trayItem.addListener(SWT.MenuDetect, new Listener() {
					@Override
					public void handleEvent(Event event) {
						menu.setVisible(true);
					}
				});

				trayItem.addListener(SWT.DefaultSelection, new RestoreListener(shell));
			}
		}
	}

	public void updateTrayItem(final RouterLoggerStatus status) {
		updateTrayItem(status, null);
	}

	public void updateTrayItem(final RouterLoggerStatus status, final RouterData info) {
		if (trayItem != null && !trayItem.isDisposed()) {
			final StringBuilder sb = new StringBuilder(getBaseToolTipText(status));
			if (!configuration.getGuiImportantKeys().isEmpty() && info != null && info.getData() != null && !info.getData().isEmpty()) {
				for (final String key : configuration.getGuiImportantKeys()) {
					if (info.getData().containsKey(key)) {
						sb.append(NewLine.SYSTEM_LINE_SEPARATOR).append(key).append(": ").append(info.getData().get(key));
					}
				}
			}
			final String updatedToolTipText = sb.toString();
			if (!updatedToolTipText.equals(toolTipText) || (status != null && !getTrayIcon(status).equals(trayIcon))) {
				try {
					trayItem.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							if (!trayItem.isDisposed()) {
								if (!updatedToolTipText.equals(toolTipText)) {
									toolTipText = updatedToolTipText;
									trayItem.setToolTipText(toolTipText);
								}
								if (status != null && !getTrayIcon(status).equals(trayIcon)) {
									trayIcon = getTrayIcon(status);
									trayItem.setImage(trayIcon);
								}
							}
						}
					});
				}
				catch (SWTException se) {}
			}
		}
	}

	private String getBaseToolTipText(final RouterLoggerStatus status) {
		final StringBuilder sb = new StringBuilder(Resources.get("lbl.tray.tooltip"));
		if (status != null) {
			sb.append(" (").append(status.toString()).append(')');
		}
		return sb.toString();
	}

	private final class RestoreListener implements Listener {
		private final Shell shell;

		private RestoreListener(Shell shell) {
			this.shell = shell;
		}

		@Override
		public void handleEvent(Event event) {
			shell.setVisible(true);
			trayItem.setVisible(false);
		}
	}

}
