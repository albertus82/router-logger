package it.albertus.router.gui;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.util.Console;
import it.albertus.util.NewLine;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;

public class TextConsole extends Console {

	protected interface Defaults {
		int GUI_CONSOLE_MAX_CHARS = 100000;
	}

	private static class Singleton {
		private static final TextConsole CONSOLE = new TextConsole();
	}

	public static TextConsole getInstance() {
		return Singleton.CONSOLE;
	}

	protected Scrollable scrollable = null;

	protected TextConsole() {}

	protected void createText(final Composite container) {
		scrollable = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
	}

	public void init(final IShellProvider gui, final Object layoutData) {
		if (this.scrollable == null) {
			createText(gui.getShell());
			scrollable.setLayoutData(layoutData);
			scrollable.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
			scrollable.setBackground(scrollable.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		}
		else {
			throw new IllegalStateException(Resources.get("err.already.initialized", this.getClass().getSimpleName()));
		}
	}

	protected static final String NEWLINE = NewLine.SYSTEM_LINE_SEPARATOR;

	protected final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

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
		if (scrollable != null && !scrollable.isDisposed()) {
			try {
				scrollable.getDisplay().syncExec(new Runnable() {
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
			catch (SWTException se) {
				failSafePrint(toPrint);
				updatePosition(toPrint);
			}
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

	public Text getText() {
		return (Text) scrollable;
	}

}
