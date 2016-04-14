package it.albertus.router.gui;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.util.Console;
import it.albertus.util.NewLine;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;

public class TextConsole extends Console {

	public interface Defaults {
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

	protected void createText(final Composite parent) {
		scrollable = new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
	}

	public void init(final Composite parent, final Object layoutData) {
		if (this.scrollable == null || this.scrollable.isDisposed()) {
			createText(parent);
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

	protected void failSafePrint(final String value) {
		System.out.print(value);
	}

	protected void doPrint(final String value) {
		try {
			int maxChars;
			try {
				maxChars = configuration.getInt("gui.console.max.chars");
			}
			catch (final Exception exception) {
				maxChars = Defaults.GUI_CONSOLE_MAX_CHARS;
			}
			if (getText().getCharCount() < maxChars) {
				getText().append(value);
			}
			else {
				getText().setText(value.startsWith(NEWLINE) ? value.substring(NEWLINE.length()) : value);
			}
			getText().setTopIndex(getText().getLineCount() - 1);
		}
		catch (SWTException se) {
			failSafePrint(value);
		}
		finally {
			updatePosition(value);
		}
	}

	@Override
	public void print(final String value) {
		// Dealing with null argument...
		final String toPrint;
		if (value == null) {
			toPrint = String.valueOf(value);
		}
		else {
			toPrint = value;
		}

		// Actual print...
		if (scrollable != null && !scrollable.isDisposed()) {
			try {
				if (scrollable.getDisplay().equals(Display.getCurrent())) {
					doPrint(toPrint);
				}
				else {
					scrollable.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							doPrint(toPrint);
						}
					});
				}
			}
			catch (final SWTException se) {
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
		print(value + NEWLINE);
		newLine();
	}

	@Override
	public void println(Object value) {
		print(String.valueOf(value) + NEWLINE);
		newLine();
	}

	@Override
	public void println(boolean value) {
		print(String.valueOf(value) + NEWLINE);
		newLine();
	}

	@Override
	public void println(char value) {
		print(String.valueOf(value) + NEWLINE);
		newLine();
	}

	@Override
	public void println(int value) {
		print(String.valueOf(value) + NEWLINE);
		newLine();
	}

	@Override
	public void println(long value) {
		print(String.valueOf(value) + NEWLINE);
		newLine();
	}

	@Override
	public void println(float value) {
		print(String.valueOf(value) + NEWLINE);
		newLine();
	}

	@Override
	public void println(double value) {
		print(String.valueOf(value) + NEWLINE);
		newLine();
	}

	@Override
	public void println(char array[]) {
		print(String.valueOf(array) + NEWLINE);
		newLine();
	}

	public Text getText() {
		return (Text) scrollable;
	}

}
