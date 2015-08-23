package it.albertus.router.gui;

import it.albertus.router.resources.Resources;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class GuiStyledConsole extends GuiConsole {

	private static class Singleton {
		private static final GuiStyledConsole CONSOLE = new GuiStyledConsole();
	}

	public static GuiStyledConsole getInstance() {
		return Singleton.CONSOLE;
	}

	private GuiStyledConsole() {}

	@Override
	public void init(final Composite container) {
		if (this.text == null) {
			this.text = (StyledText) createText(container);
			createContextMenu();
		}
		else {
			throw new IllegalStateException(Resources.get("err.already.initialized", this.getClass().getSimpleName()));
		}
	}

	@Override
	protected StyledText createText(final Composite container) {
		final StyledText styledText = new StyledText(container, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.minimumHeight = 200;
		gridData.heightHint = 200;
		styledText.setLayoutData(gridData);
		styledText.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

		styledText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// Supporto CTRL+A per "Seleziona tutto"...
				if (e.stateMask == SWT.MOD1 && e.keyCode == GuiUtils.KEY_SELECT_ALL) {
					styledText.selectAll();
				}
			}
		});

		return styledText;
	}

	private Menu createContextMenu() {
		final Menu menu = new Menu(text);

		// Copia...
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(Resources.get("lbl.copy") + '\t' + GuiUtils.getMod1KeyLabel() + '+' + Character.toUpperCase(GuiUtils.KEY_COPY));
		menuItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				text.copy();
			}
		});

		menuItem = new MenuItem(menu, SWT.SEPARATOR);

		// Azzera...
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(Resources.get("lbl.clear"));
		menuItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				text.setText("");
			}
		});

		menuItem = new MenuItem(menu, SWT.SEPARATOR);

		// Seleziona tutto...
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(Resources.get("lbl.select.all") + '\t' + GuiUtils.getMod1KeyLabel() + '+' + Character.toUpperCase(GuiUtils.KEY_SELECT_ALL));
		menuItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				text.selectAll();
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

	private StyledText text = null;

	@Override
	public void print(final String value) {
		final String toPrint;
		if (value == null) {
			toPrint = String.valueOf(value);
		}
		else {
			toPrint = value;
		}
		if (text != null && !text.isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						if (text.getCharCount() < configuration.getInt("gui.console.max.chars", Defaults.GUI_CONSOLE_MAX_CHARS)) {
							text.append(toPrint);
						}
						else {
							text.setText(toPrint.startsWith(NEWLINE) ? toPrint.substring(NEWLINE.length()) : toPrint);
						}
						text.setTopIndex(text.getLineCount() - 1);
					}
					catch (SWTException se) {
						failSafePrint(toPrint);
					}
				}
			});
			updatePosition(value);
		}
		else {
			failSafePrint(toPrint);
		}
	}

}
