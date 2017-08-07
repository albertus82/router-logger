package it.albertus.router.gui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.jface.SwtUtils;
import it.albertus.jface.cocoa.CocoaEnhancerException;
import it.albertus.jface.cocoa.CocoaUIEnhancer;
import it.albertus.jface.sysinfo.SystemInformationDialog;
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
import it.albertus.router.gui.listener.HelpMenuListener;
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

	private static final String LBL_MENU_HEADER_FILE = "lbl.menu.header.file";
	private static final String LBL_MENU_ITEM_RESTART = "lbl.menu.item.restart";
	private static final String LBL_MENU_ITEM_EXIT = "lbl.menu.item.exit";
	private static final String LBL_MENU_HEADER_EDIT = "lbl.menu.header.edit";
	private static final String LBL_MENU_ITEM_COPY = "lbl.menu.item.copy";
	private static final String LBL_MENU_ITEM_DELETE = "lbl.menu.item.delete";
	private static final String LBL_MENU_ITEM_DELETE_KEY = "lbl.menu.item.delete.key";
	private static final String LBL_MENU_ITEM_SELECT_ALL = "lbl.menu.item.select.all";
	private static final String LBL_MENU_ITEM_CLEAR = "lbl.menu.item.clear";
	private static final String LBL_MENU_ITEM_CLEAR_TABLE = "lbl.menu.item.clear.table";
	private static final String LBL_MENU_ITEM_CLEAR_CONSOLE = "lbl.menu.item.clear.console";
	private static final String LBL_MENU_HEADER_CONNECTION = "lbl.menu.header.connection";
	private static final String LBL_MENU_ITEM_CONNECT = "lbl.menu.item.connect";
	private static final String LBL_MENU_ITEM_DISCONNECT = "lbl.menu.item.disconnect";
	private static final String LBL_MENU_HEADER_TOOLS = "lbl.menu.header.tools";
	private static final String LBL_MENU_ITEM_PREFERENCES = "lbl.menu.item.preferences";
	private static final String LBL_MENU_HEADER_HELP = "lbl.menu.header.help";
	private static final String LBL_MENU_HEADER_HELP_WINDOWS = "lbl.menu.header.help.windows";
	private static final String LBL_MENU_ITEM_SYSTEM_INFO = "lbl.menu.item.system.info";
	private static final String LBL_MENU_ITEM_ABOUT = "lbl.menu.item.about";

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

	private final Menu helpMenu;
	private final MenuItem helpMenuHeader;
	private final MenuItem helpSystemInfoItem;
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

		// File
		fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		fileMenuHeader = new MenuItem(bar, SWT.CASCADE);
		fileMenuHeader.setText(Messages.get(LBL_MENU_HEADER_FILE));
		fileMenuHeader.setMenu(fileMenu);

		fileRestartItem = new MenuItem(fileMenu, SWT.PUSH);
		fileRestartItem.setText(Messages.get(LBL_MENU_ITEM_RESTART));
		fileRestartItem.addSelectionListener(new RestartSelectionListener(gui));

		if (!cocoaMenuCreated) {
			new MenuItem(fileMenu, SWT.SEPARATOR);

			fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
			fileExitItem.setText(Messages.get(LBL_MENU_ITEM_EXIT));
			fileExitItem.addSelectionListener(new CloseListener(gui));
		}

		// Edit
		editMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editMenuHeader = new MenuItem(bar, SWT.CASCADE);
		editMenuHeader.setText(Messages.get(LBL_MENU_HEADER_EDIT));
		editMenuHeader.setMenu(editMenu);
		final EditMenuListener editMenuListener = new EditMenuListener(gui);
		editMenu.addMenuListener(editMenuListener);
		editMenuHeader.addArmListener(editMenuListener);

		editCopyMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editCopyMenuItem.setText(Messages.get(LBL_MENU_ITEM_COPY) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		editCopyMenuItem.addSelectionListener(new CopyMenuBarSelectionListener(gui));
		editCopyMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_COPY); // Vero!

		editDeleteMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editDeleteMenuItem.setText(Messages.get(LBL_MENU_ITEM_DELETE) + SwtUtils.getShortcutLabel(Messages.get(LBL_MENU_ITEM_DELETE_KEY)));
		editDeleteMenuItem.addSelectionListener(new DeleteDataTableSelectionListener(gui));
		editDeleteMenuItem.setAccelerator(SwtUtils.KEY_DELETE); // Vero!

		new MenuItem(editMenu, SWT.SEPARATOR);

		editSelectAllMenuItem = new MenuItem(editMenu, SWT.PUSH);
		editSelectAllMenuItem.setText(Messages.get(LBL_MENU_ITEM_SELECT_ALL) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SELECT_ALL));
		editSelectAllMenuItem.addSelectionListener(new SelectAllMenuBarSelectionListener(gui));
		editSelectAllMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SELECT_ALL); // Vero!

		new MenuItem(editMenu, SWT.SEPARATOR);

		editClearSubMenuItem = new MenuItem(editMenu, SWT.CASCADE);
		editClearSubMenuItem.setText(Messages.get(LBL_MENU_ITEM_CLEAR));

		editClearSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		editClearSubMenuItem.setMenu(editClearSubMenu);
		final EditClearSubMenuListener editClearSubMenuListener = new EditClearSubMenuListener(gui);
		editClearSubMenu.addMenuListener(editClearSubMenuListener);
		editClearSubMenuItem.addArmListener(editClearSubMenuListener);

		editClearDataTableMenuItem = new MenuItem(editClearSubMenu, SWT.PUSH);
		editClearDataTableMenuItem.setText(Messages.get(LBL_MENU_ITEM_CLEAR_TABLE));
		editClearDataTableMenuItem.addSelectionListener(new ClearDataTableSelectionListener(gui));

		editClearConsoleMenuItem = new MenuItem(editClearSubMenu, SWT.PUSH);
		editClearConsoleMenuItem.setText(Messages.get(LBL_MENU_ITEM_CLEAR_CONSOLE));
		editClearConsoleMenuItem.addSelectionListener(new ClearConsoleSelectionListener(gui));

		// Connection
		connectionMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		connectionMenuHeader = new MenuItem(bar, SWT.CASCADE);
		connectionMenuHeader.setText(Messages.get(LBL_MENU_HEADER_CONNECTION));
		connectionMenuHeader.setMenu(connectionMenu);
		final ConnectionMenuListener connectionMenuListener = new ConnectionMenuListener(gui);
		connectionMenu.addMenuListener(connectionMenuListener);
		connectionMenuHeader.addArmListener(connectionMenuListener);

		connectionConnectItem = new MenuItem(connectionMenu, SWT.PUSH);
		connectionConnectItem.setText(Messages.get(LBL_MENU_ITEM_CONNECT));
		connectionConnectItem.addSelectionListener(new ConnectSelectionListener(gui));

		new MenuItem(connectionMenu, SWT.SEPARATOR);

		connectionDisconnectItem = new MenuItem(connectionMenu, SWT.PUSH);
		connectionDisconnectItem.setText(Messages.get(LBL_MENU_ITEM_DISCONNECT));
		connectionDisconnectItem.addSelectionListener(new DisconnectSelectionListener(gui));

		// Tools
		if (!cocoaMenuCreated) {
			toolsMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
			toolsMenuHeader = new MenuItem(bar, SWT.CASCADE);
			toolsMenuHeader.setText(Messages.get(LBL_MENU_HEADER_TOOLS));
			toolsMenuHeader.setMenu(toolsMenu);

			toolsPreferencesMenuItem = new MenuItem(toolsMenu, SWT.PUSH);
			toolsPreferencesMenuItem.setText(Messages.get(LBL_MENU_ITEM_PREFERENCES));
			toolsPreferencesMenuItem.addSelectionListener(new PreferencesListener(gui));
		}

		// Help
		helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		helpMenuHeader = new MenuItem(bar, SWT.CASCADE);
		helpMenuHeader.setText(Messages.get(Util.isWindows() ? LBL_MENU_HEADER_HELP_WINDOWS : LBL_MENU_HEADER_HELP));
		helpMenuHeader.setMenu(helpMenu);

		helpSystemInfoItem = new MenuItem(helpMenu, SWT.PUSH);
		helpSystemInfoItem.setText(Messages.get(LBL_MENU_ITEM_SYSTEM_INFO));
		helpSystemInfoItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				SystemInformationDialog.open(gui.getShell());
			}
		});

		if (!cocoaMenuCreated) {
			new MenuItem(helpMenu, SWT.SEPARATOR);

			helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
			helpAboutItem.setText(Messages.get(LBL_MENU_ITEM_ABOUT));
			helpAboutItem.addSelectionListener(new AboutListener(gui));
		}

		final HelpMenuListener helpMenuListener = new HelpMenuListener(helpSystemInfoItem);
		helpMenu.addMenuListener(helpMenuListener);
		helpMenuHeader.addArmListener(helpMenuListener);

		gui.getShell().setMenuBar(bar);
	}

	public void updateTexts() {
		fileMenuHeader.setText(Messages.get(LBL_MENU_HEADER_FILE));
		fileRestartItem.setText(Messages.get(LBL_MENU_ITEM_RESTART));
		if (fileExitItem != null && !fileExitItem.isDisposed()) {
			fileExitItem.setText(Messages.get(LBL_MENU_ITEM_EXIT));
		}
		editMenuHeader.setText(Messages.get(LBL_MENU_HEADER_EDIT));
		editCopyMenuItem.setText(Messages.get(LBL_MENU_ITEM_COPY) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY));
		editDeleteMenuItem.setText(Messages.get(LBL_MENU_ITEM_DELETE) + SwtUtils.getShortcutLabel(Messages.get(LBL_MENU_ITEM_DELETE_KEY)));
		editSelectAllMenuItem.setText(Messages.get(LBL_MENU_ITEM_SELECT_ALL) + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SELECT_ALL));
		editClearSubMenuItem.setText(Messages.get(LBL_MENU_ITEM_CLEAR));
		editClearDataTableMenuItem.setText(Messages.get(LBL_MENU_ITEM_CLEAR_TABLE));
		editClearConsoleMenuItem.setText(Messages.get(LBL_MENU_ITEM_CLEAR_CONSOLE));
		connectionMenuHeader.setText(Messages.get(LBL_MENU_HEADER_CONNECTION));
		connectionConnectItem.setText(Messages.get(LBL_MENU_ITEM_CONNECT));
		connectionDisconnectItem.setText(Messages.get(LBL_MENU_ITEM_DISCONNECT));
		if (toolsMenuHeader != null && !toolsMenuHeader.isDisposed()) {
			toolsMenuHeader.setText(Messages.get(LBL_MENU_HEADER_TOOLS));
		}
		if (toolsPreferencesMenuItem != null && !toolsPreferencesMenuItem.isDisposed()) {
			toolsPreferencesMenuItem.setText(Messages.get(LBL_MENU_ITEM_PREFERENCES));
		}
		helpMenuHeader.setText(Messages.get(Util.isWindows() ? LBL_MENU_HEADER_HELP_WINDOWS : LBL_MENU_HEADER_HELP));
		helpSystemInfoItem.setText(Messages.get(LBL_MENU_ITEM_SYSTEM_INFO));
		if (helpAboutItem != null && !helpAboutItem.isDisposed()) {
			helpAboutItem.setText(Messages.get(LBL_MENU_ITEM_ABOUT));
		}
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

	public MenuItem getConnectionConnectItem() {
		return connectionConnectItem;
	}

	public MenuItem getConnectionDisconnectItem() {
		return connectionDisconnectItem;
	}

}
