package it.albertus.router.gui;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerStatus;
import it.albertus.router.gui.listener.CloseListener;
import it.albertus.router.gui.listener.RestoreShellListener;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class TrayIcon {

	private interface Defaults {
		boolean GUI_MINIMIZE_TRAY = true;
	}

	private final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	private final RouterLoggerGui gui;

	private Tray tray;
	private TrayItem trayItem;
	private String toolTipText;
	private Image trayIcon;

	private Menu trayMenu;
	private MenuItem showMenuItem;
	private MenuItem exitMenuItem;

	public TrayIcon(RouterLoggerGui gui) {
		this.gui = gui;
		gui.getShell().addShellListener(new ShellAdapter() {
			@Override
			public void shellIconified(ShellEvent e) {
				if (configuration.getBoolean("gui.minimize.tray", Defaults.GUI_MINIMIZE_TRAY)) {
					iconify();
				}
			}
		});
	}

	private Image getTrayIcon(RouterLoggerStatus status) {
		switch (status) {
		case STARTING:
		case CONNECTING:
		case DISCONNECTED:
			return Images.TRAY_ICON_ROUTER_GREY;
		case RECONNECTING:
			return Images.TRAY_ICON_ROUTER_GREY_CLOCK;
		case INFO:
		case WARNING:
			return Images.TRAY_ICON_ROUTER_BLUE_WARNING;
		case AUTHENTICATING:
			return Images.TRAY_ICON_ROUTER_BLUE_LOCK;
		case ERROR:
			return Images.TRAY_ICON_ROUTER_GREY_ERROR;
		default:
			return Images.TRAY_ICON_ROUTER_BLUE;
		}
	}

	private void iconify() {
		if (tray == null) {
			/* Inizializzazione */
			tray = gui.getShell().getDisplay().getSystemTray();

			if (tray != null) {
				trayItem = new TrayItem(tray, SWT.NONE);
				trayIcon = getTrayIcon(gui.getStatus());
				trayItem.setImage(trayIcon);
				toolTipText = getBaseToolTipText(gui.getStatus());
				trayItem.setToolTipText(toolTipText);

				trayMenu = new Menu(gui.getShell(), SWT.POP_UP);
				showMenuItem = new MenuItem(trayMenu, SWT.PUSH);
				showMenuItem.setText(Resources.get("lbl.tray.show"));
				showMenuItem.addListener(SWT.Selection, new RestoreShellListener(gui));
				trayMenu.setDefaultItem(showMenuItem);

				new MenuItem(trayMenu, SWT.SEPARATOR);

				exitMenuItem = new MenuItem(trayMenu, SWT.PUSH);
				exitMenuItem.setText(Resources.get("lbl.tray.close"));
				exitMenuItem.addSelectionListener(new CloseListener(gui));
				trayItem.addMenuDetectListener(new MenuDetectListener() {
					@Override
					public void menuDetected(MenuDetectEvent e) {
						trayMenu.setVisible(true);
					}
				});

				trayItem.addListener(SWT.DefaultSelection, new RestoreShellListener(gui));
			}
		}

		if (tray != null && trayItem != null && !trayItem.isDisposed()) {
			gui.getShell().setVisible(false);
			trayItem.setVisible(true);
			gui.getShell().setMinimized(false);
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

	public Tray getTray() {
		return tray;
	}

	public TrayItem getTrayItem() {
		return trayItem;
	}

	public Menu getTrayMenu() {
		return trayMenu;
	}

	public MenuItem getShowMenuItem() {
		return showMenuItem;
	}

	public MenuItem getExitMenuItem() {
		return exitMenuItem;
	}

}
