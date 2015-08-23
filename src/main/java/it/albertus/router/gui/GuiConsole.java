package it.albertus.router.gui;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.util.Console;
import it.albertus.util.NewLine;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;

public class GuiConsole extends Console {

	protected interface Defaults {
		int GUI_CONSOLE_MAX_CHARS = 50000;
	}

	private static class Singleton {
		private static final GuiConsole CONSOLE = new GuiConsole();
	}

	public static GuiConsole getInstance() {
		return Singleton.CONSOLE;
	}

	protected GuiConsole() {}

	public void init(final Composite container) {
		if (this.text == null) {
			this.text = (Text) createText(container);
		}
		else {
			throw new IllegalStateException(Resources.get("err.already.initialized", this.getClass().getSimpleName()));
		}
	}

	protected Scrollable createText(final Composite container) {
		final Text text = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.minimumHeight = 200;
		gridData.heightHint = 200;
		text.setLayoutData(gridData);
		text.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		text.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// Supporto CTRL+A per "Seleziona tutto"...
				if (e.stateMask == SWT.MOD1 && e.keyCode == GuiUtils.KEY_SELECT_ALL) {
					text.selectAll();
				}
			}
		});

		return text;
	}

	protected static final String NEWLINE = NewLine.SYSTEM_LINE_SEPARATOR;

	protected final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	private Text text = null;

	protected void failSafePrint(final String toPrint) {
		System.out.print(toPrint);
	}

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

	@Override
	public void print(char array[]) {
		print(String.valueOf(array));
	}

	@Override
	public void println() {
		print(NEWLINE);
		newLine();
	}

	@Override
	public void println(String value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	@Override
	public void println(Object value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	@Override
	public void println(boolean value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	@Override
	public void println(char value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	@Override
	public void println(int value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	@Override
	public void println(long value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	@Override
	public void println(float value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	@Override
	public void println(double value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	@Override
	public void println(char array[]) {
		print(array);
		print(NEWLINE);
		newLine();
	}

}
