package it.albertus.router.gui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.jface.SwtUtils;
import it.albertus.jface.cocoa.CocoaEnhancerException;
import it.albertus.jface.cocoa.CocoaUIEnhancer;
import it.albertus.router.gui.listener.AboutListener;
import it.albertus.router.gui.listener.ClearConsoleSelectionListener;
import it.albertus.router.gui.listener.ClearDataTableSelectionListener;
import it.albertus.router.gui.listener.CloseListener;
import it.albertus.router.gui.listener.ConnectSelectionListener;
import it.albertus.router.gui.listener.ConnectionMenuListener;
import it.albertus.router.gui.listener.CopyMenuBarSelectionListener;
import it.albertus.router.gui.listener.DeleteDataTableSelectionListener;
import it.albertus.router.gui.listener.DisconnectSelectionListener;
import it.albertus.router.gui.listener.EditClearSubMenuListener;
import it.albertus.router.gui.listener.EditMenuListener;
import it.albertus.router.gui.listener.PreferencesListener;
import it.albertus.router.gui.listener.RestartSelectionListener;
import it.albertus.router.gui.listener.SelectAllMenuBarSelectionListener;
import it.albertus.router.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

/**
 * Solo i <tt>MenuItem</tt> che fanno parte di una barra dei men&ugrave; con
 * stile <tt>SWT.BAR</tt> hanno gli acceleratori funzionanti; negli altri casi
 * (ad es. <tt>SWT.POP_UP</tt>), bench&eacute; vengano visualizzate le
 * combinazioni di tasti, gli acceleratori non funzioneranno e le relative
 * combinazioni di tasti saranno ignorate.
 */
public class MenuBar {

	private static final Logger logger = LoggerFactory.getLogger(MenuBar.class);

	private final Menu bar;

	private final Menu fileMenu;
	private final MenuItem fileMenuHeader;
	private final MenuItem fileRestartItem;
	private MenuItem fileExitItem;

	private final Menu editMenu;
	private final MenuItem editMenuHeader;
	private final MenuItem editCopyMenuItem;
	private final MenuItem editDeleteMenuItem;
	private final MenuItem editSelectAllMenuItem;

	private final Menu editClearSubMenu;
	private final MenuItem editClearSubMenuItem;
	private final MenuItem editClearDataTableMenuItem;
	private final MenuItem editClearConsoleMenuItem;

	private final Menu connectionMenu;
	private final MenuItem connectionMenuHeader;
	private final MenuItem connectionConnectItem;
	private final MenuItem connectionDisconnectItem;

	private Menu toolsMenu;
	private MenuItem toolsMenuHeader;
	private MenuItem toolsPreferencesMenuItem;

	private Menu helpMenu;
	private MenuItem helpMenuHeader;
	private MenuItem helpAboutItem;

	protected MenuBar(final RouterLoggerGui gui) {
		final CloseListener closeListener = new CloseListener(gui);
		final AboutListener aboutListener = new AboutListener(gui);
		final PreferencesListener preferencesListener = new PreferencesListener(gui);

		boolean cocoaMenuCreated = false;
		if (Util.isCocoa()) {
			try {
				new CocoaUIEnhancer(gui.getShell().getDisplay()).hookApplicationMenu(closeListener, aboutListener, preferencesListener);
				cocoaMenuCreated = true;
			}
			catch (final CocoaEnhancerException e) {
				logger.log(Level.WARNING, e.toString(), e);
			}
		}

		bar = new Menu(gui.getShell(), SWT.BAR); // Barra

		/* File */
		fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		fileMenuHeader = new MenuItem(bar, SWT.CASCADE);
		fileMenuHeader.setText(Messages.get("lbl.menu.header.file"));
		fileMenuHeader.setMenu(fileMenu);

		fileRestartItem = new MenuItem(fileMenu, SWT.PUSH);
		fileRestartItem.setText(Messages.get("lbl.menu.item.restart"));
		fileRestartItem.addSelectionListener(new RestartSelectionListener(gui));

		if (!cocoaMenuCreated) {
			new MenuItem(fileMenu, SWT.SEPARATOR);

			fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
			fileExitItem.setText(Messages.get("lbl.menu.item.exit"));
			fileExitItem.addSelectionListener(new CloseListener(gui));
		}

		/* Edit */
		editMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editMenuHeader = new MenuItem(bar, SWT.CASCADE);
		editMenuHeader.setText(Messages.get("lbl.menu.header.edit"));
		editMenuHeader.setMenu(editMenu);
		final EditMenuListener editMenuListener = new EditMenuListener(gui);
		editMenu.addMenuListener(editMenuListener);
		editMenuHeader.addArmListener(editMenuListener);

		editCopyMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editCopyMenuItem.setText(Messages.get("lbl.menu.item.copy") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		editCopyMenuItem.addSelectionListener(new CopyMenuBarSelectionListener(gui));
		editCopyMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_COPY); // Vero!

		editDeleteMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editDeleteMenuItem.setText(Messages.get("lbl.menu.item.delete") + SwtUtils.getShortcutLabel(Messages.get("lbl.menu.item.delete.key")));
		editDeleteMenuItem.addSelectionListener(new DeleteDataTableSelectionListener(gui));
		editDeleteMenuItem.setAccelerator(SwtUtils.KEY_DELETE); // Vero!

		new MenuItem(editMenu, SWT.SEPARATOR);

		editSelectAllMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editSelectAllMenuItem.setText(Messages.get("lbl.menu.item.select.all") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SELECT_ALL));
		editSelectAllMenuItem.addSelectionListener(new SelectAllMenuBarSelectionListener(gui));
		editSelectAllMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SELECT_ALL); // Vero!

		new MenuItem(editMenu, SWT.SEPARATOR);

		editClearSubMenuItem = new MenuItem(editMenu, SWT.CASCADE);
		editClearSubMenuItem.setText(Messages.get("lbl.menu.item.clear"));

		editClearSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editClearSubMenuItem.setMenu(editClearSubMenu);
		final EditClearSubMenuListener editClearSubMenuListener = new EditClearSubMenuListener(gui);
		editClearSubMenu.addMenuListener(editClearSubMenuListener);
		editClearSubMenuItem.addArmListener(editClearSubMenuListener);

		editClearDataTableMenuItem = new MenuItem(editClearSubMenu, SWT.PUSH);
		editClearDataTableMenuItem.setText(Messages.get("lbl.menu.item.clear.table"));
		editClearDataTableMenuItem.addSelectionListener(new ClearDataTableSelectionListener(gui));

		editClearConsoleMenuItem = new MenuItem(editClearSubMenu, SWT.PUSH);
		editClearConsoleMenuItem.setText(Messages.get("lbl.menu.item.clear.console"));
		editClearConsoleMenuItem.addSelectionListener(new ClearConsoleSelectionListener(gui));

		/* Connection */
		connectionMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		connectionMenuHeader = new MenuItem(bar, SWT.CASCADE);
		connectionMenuHeader.setText(Messages.get("lbl.menu.header.connection"));
		connectionMenuHeader.setMenu(connectionMenu);
		final ConnectionMenuListener connectionMenuListener = new ConnectionMenuListener(gui);
		connectionMenu.addMenuListener(connectionMenuListener);
		connectionMenuHeader.addArmListener(connectionMenuListener);

		connectionConnectItem = new MenuItem(connectionMenu, SWT.PUSH);
		connectionConnectItem.setText(Messages.get("lbl.menu.item.connect"));
		connectionConnectItem.addSelectionListener(new ConnectSelectionListener(gui));

		new MenuItem(connectionMenu, SWT.SEPARATOR);

		connectionDisconnectItem = new MenuItem(connectionMenu, SWT.PUSH);
		connectionDisconnectItem.setText(Messages.get("lbl.menu.item.disconnect"));
		connectionDisconnectItem.addSelectionListener(new DisconnectSelectionListener(gui));

		/* Tools */
		if (!cocoaMenuCreated) {
			toolsMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
			toolsMenuHeader = new MenuItem(bar, SWT.CASCADE);
			toolsMenuHeader.setText(Messages.get("lbl.menu.header.tools"));
			toolsMenuHeader.setMenu(toolsMenu);

			toolsPreferencesMenuItem = new MenuItem(toolsMenu, SWT.PUSH);
			toolsPreferencesMenuItem.setText(Messages.get("lbl.menu.item.preferences"));
			toolsPreferencesMenuItem.addSelectionListener(new PreferencesListener(gui));
		}

		/* Help */
		if (!cocoaMenuCreated) {
			helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
			helpMenuHeader = new MenuItem(bar, SWT.CASCADE);
			helpMenuHeader.setText(Messages.get("lbl.menu.header.help"));
			helpMenuHeader.setMenu(helpMenu);

			helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
			helpAboutItem.setText(Messages.get("lbl.menu.item.about"));
			helpAboutItem.addSelectionListener(new AboutListener(gui));
		}

		gui.getShell().setMenuBar(bar);
	}

	public void updateTexts() {
		fileMenuHeader.setText(Messages.get("lbl.menu.header.file"));
		fileRestartItem.setText(Messages.get("lbl.menu.item.restart"));
		if (fileExitItem != null && !fileExitItem.isDisposed()) {
			fileExitItem.setText(Messages.get("lbl.menu.item.exit"));
		}
		editMenuHeader.setText(Messages.get("lbl.menu.header.edit"));
		editCopyMenuItem.setText(Messages.get("lbl.menu.item.copy") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		editDeleteMenuItem.setText(Messages.get("lbl.menu.item.delete") + SwtUtils.getShortcutLabel(Messages.get("lbl.menu.item.delete.key")));
		editSelectAllMenuItem.setText(Messages.get("lbl.menu.item.select.all") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SELECT_ALL));
		editClearSubMenuItem.setText(Messages.get("lbl.menu.item.clear"));
		editClearDataTableMenuItem.setText(Messages.get("lbl.menu.item.clear.table"));
		editClearConsoleMenuItem.setText(Messages.get("lbl.menu.item.clear.console"));
		connectionMenuHeader.setText(Messages.get("lbl.menu.header.connection"));
		connectionConnectItem.setText(Messages.get("lbl.menu.item.connect"));
		connectionDisconnectItem.setText(Messages.get("lbl.menu.item.disconnect"));
		if (toolsMenuHeader != null && !toolsMenuHeader.isDisposed()) {
			toolsMenuHeader.setText(Messages.get("lbl.menu.header.tools"));
		}
		if (toolsPreferencesMenuItem != null && !toolsPreferencesMenuItem.isDisposed()) {
			toolsPreferencesMenuItem.setText(Messages.get("lbl.menu.item.preferences"));
		}
		if (helpMenuHeader != null && !helpMenuHeader.isDisposed()) {
			helpMenuHeader.setText(Messages.get("lbl.menu.header.help"));
		}
		if (helpAboutItem != null && !helpAboutItem.isDisposed()) {
			helpAboutItem.setText(Messages.get("lbl.menu.item.about"));
		}
	}

	public Menu getBar() {
		return bar;
	}

	public Menu getFileMenu() {
		return fileMenu;
	}

	public MenuItem getFileMenuHeader() {
		return fileMenuHeader;
	}

	public MenuItem getFileRestartItem() {
		return fileRestartItem;
	}

	public MenuItem getFileExitItem() {
		return fileExitItem;
	}

	public Menu getEditMenu() {
		return editMenu;
	}

	public MenuItem getEditMenuHeader() {
		return editMenuHeader;
	}

	public MenuItem getEditCopyMenuItem() {
		return editCopyMenuItem;
	}

	public MenuItem getEditDeleteMenuItem() {
		return editDeleteMenuItem;
	}

	public MenuItem getEditSelectAllMenuItem() {
		return editSelectAllMenuItem;
	}

	public Menu getEditClearSubMenu() {
		return editClearSubMenu;
	}

	public MenuItem getEditClearSubMenuItem() {
		return editClearSubMenuItem;
	}

	public MenuItem getEditClearDataTableMenuItem() {
		return editClearDataTableMenuItem;
	}

	public MenuItem getEditClearConsoleMenuItem() {
		return editClearConsoleMenuItem;
	}

	public Menu getConnectionMenu() {
		return connectionMenu;
	}

	public MenuItem getConnectionMenuHeader() {
		return connectionMenuHeader;
	}

	public MenuItem getConnectionConnectItem() {
		return connectionConnectItem;
	}

	public MenuItem getConnectionDisconnectItem() {
		return connectionDisconnectItem;
	}

	public Menu getToolsMenu() {
		return toolsMenu;
	}

	public MenuItem getToolsMenuHeader() {
		return toolsMenuHeader;
	}

	public MenuItem getToolsPreferencesMenuItem() {
		return toolsPreferencesMenuItem;
	}

	public Menu getHelpMenu() {
		return helpMenu;
	}

	public MenuItem getHelpMenuHeader() {
		return helpMenuHeader;
	}

	public MenuItem getHelpAboutItem() {
		return helpAboutItem;
	}

}
