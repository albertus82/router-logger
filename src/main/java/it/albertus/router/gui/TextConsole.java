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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;

public class TextConsole extends Console {

	protected interface Defaults {
		int GUI_CONSOLE_MAX_CHARS = 50000;
	}

	private static class Singleton {
		private static final TextConsole CONSOLE = new TextConsole();
	}

	public static TextConsole getInstance() {
		return Singleton.CONSOLE;
	}

	protected TextConsole() {}

	public void init(final Composite container, final Object layoutData) {
		if (this.text == null) {
			createText(container);
			configureText(layoutData);
			addSelectAllKeyListener();
		}
		else {
			throw new IllegalStateException(Resources.get("err.already.initialized", this.getClass().getSimpleName()));
		}
	}

	protected void createText(final Composite container) {
		text = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
	}

	protected void addSelectAllKeyListener() {
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// Supporto CTRL+A per "Seleziona tutto"...
				if (e.stateMask == SWT.MOD1 && e.keyCode == GuiUtils.KEY_SELECT_ALL) {
					getText().selectAll();
				}
			}
		});
	}

	protected void configureText(Object layoutData) {
		text.setLayoutData(layoutData);
		text.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WHITE));
	}

	protected static final String NEWLINE = NewLine.SYSTEM_LINE_SEPARATOR;

	protected final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

	protected Scrollable text = null;

	protected void failSafePrint(final String toPrint) {
		System.out.print(toPrint);
	}

	protected void doPrint(final String toPrint) {
		if (getText().getCharCount() < configuration.getInt("gui.console.max.chars", Defaults.GUI_CONSOLE_MAX_CHARS)) {
			getText().append(toPrint);
		}
		else {
			getText().setText(toPrint.startsWith(NEWLINE) ? toPrint.substring(NEWLINE.length()) : toPrint);
		}
		getText().setTopIndex(getText().getLineCount() - 1);
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
						doPrint(value);
					}
					catch (SWTException se) {
						failSafePrint(value);
					}
					finally {
						updatePosition(value);
					}
				}
			});
		}
		else {
			failSafePrint(toPrint);
			updatePosition(toPrint);
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

	private Text getText() {
		return (Text) text;
	}

}