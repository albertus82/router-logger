package it.albertus.router.gui;

import it.albertus.router.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class StyledTextConsole extends TextConsole {

	private static class Singleton {
		private static final StyledTextConsole CONSOLE = new StyledTextConsole();
	}

	public static StyledTextConsole getInstance() {
		return Singleton.CONSOLE;
	}

	private StyledTextConsole() {}

	@Override
	public void init(final Composite container, final Object layoutData) {
		super.init(container, layoutData);
		createContextMenu();
	}

	@Override
	protected void createText(final Composite container) {
		scrollable = new StyledText(container, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
	}

	private Menu createContextMenu() {
		final Menu menu = new Menu(scrollable);

		// Copia...
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(Resources.get("lbl.menu.item.copy") + GuiUtils.getMod1ShortcutLabel(GuiUtils.KEY_COPY));
		menuItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				getStyledText().copy();
			}
		});

		menuItem = new MenuItem(menu, SWT.SEPARATOR);

		// Azzera...
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(Resources.get("lbl.menu.item.clear"));
		menuItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				getStyledText().setText("");
			}
		});

		menuItem = new MenuItem(menu, SWT.SEPARATOR);

		// Seleziona tutto...
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(Resources.get("lbl.menu.item.select.all") + GuiUtils.getMod1ShortcutLabel(GuiUtils.KEY_SELECT_ALL));
		menuItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				getStyledText().selectAll();
			}
		});

		scrollable.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				menu.setVisible(true);
			}
		});

		return menu;
	}

	public StyledText getStyledText() {
		return (StyledText) scrollable;
	}

	@Override
	protected void doPrint(final String toPrint) {
		if (getStyledText().getCharCount() < configuration.getInt("gui.console.max.chars", Defaults.GUI_CONSOLE_MAX_CHARS)) {
			getStyledText().append(toPrint);
		}
		else {
			getStyledText().setText(toPrint.startsWith(NEWLINE) ? toPrint.substring(NEWLINE.length()) : toPrint);
		}
		getStyledText().setTopIndex(getStyledText().getLineCount() - 1);
	}

}
