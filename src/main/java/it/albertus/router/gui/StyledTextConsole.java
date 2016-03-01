package it.albertus.router.gui;

import it.albertus.router.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
		text = new StyledText(container, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
	}

	@Override
	protected void addSelectAllKeyListener() {
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// Supporto CTRL+A per "Seleziona tutto"...
				if (e.stateMask == SWT.MOD1 && e.keyCode == GuiUtils.KEY_SELECT_ALL) {
					getStyledText().selectAll();
				}
			}
		});
	}

	private Menu createContextMenu() {
		final Menu menu = new Menu(text);

		// Copia...
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(Resources.get("lbl.copy") + '\t' + GuiUtils.getMod1KeyLabel() + '+' + Character.toUpperCase(GuiUtils.KEY_COPY));
		menuItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				getStyledText().copy();
			}
		});

		menuItem = new MenuItem(menu, SWT.SEPARATOR);

		// Azzera...
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(Resources.get("lbl.clear"));
		menuItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				getStyledText().setText("");
			}
		});

		menuItem = new MenuItem(menu, SWT.SEPARATOR);

		// Seleziona tutto...
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(Resources.get("lbl.select.all") + '\t' + GuiUtils.getMod1KeyLabel() + '+' + Character.toUpperCase(GuiUtils.KEY_SELECT_ALL));
		menuItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				getStyledText().selectAll();
			}
		});

		text.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				menu.setVisible(true);
			}
		});

		return menu;
	}

	private StyledText getStyledText() {
		return (StyledText) text;
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
