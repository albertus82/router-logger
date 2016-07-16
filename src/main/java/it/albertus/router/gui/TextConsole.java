package it.albertus.router.gui;

import it.albertus.jface.SwtThreadExecutor;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.gui.listener.TextConsoleDisposeListener;
import it.albertus.router.resources.Resources;
import it.albertus.util.Configuration;
import it.albertus.util.Console;
import it.albertus.util.NewLine;

import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;

public class TextConsole extends OutputStream {

	protected static final String newLine = NewLine.SYSTEM_LINE_SEPARATOR;

	public interface Defaults {
		int GUI_CONSOLE_MAX_CHARS = 100000;
	}

	protected final Configuration configuration = RouterLoggerConfiguration.getInstance();
	protected final Scrollable scrollable;
	protected StringBuilder buffer = new StringBuilder();

	protected TextConsole(final Composite parent, final Object layoutData) {
		if (this.scrollable == null || this.scrollable.isDisposed()) {
			this.scrollable = createText(parent);
			scrollable.setLayoutData(layoutData);
			scrollable.setFont(JFaceResources.getTextFont());
			if (SWT.getPlatform().toLowerCase().startsWith("win")) {
				scrollable.setBackground(scrollable.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			}
			redirectStreams(); // SystemConsole & System.out will print on this SWT Text Widget. 
		}
		else {
			throw new IllegalStateException(Resources.get("err.already.initialized", this.getClass().getSimpleName()));
		}
	}

	protected Scrollable createText(final Composite parent) {
		return new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
	}

	protected void redirectStreams() {
		scrollable.addDisposeListener(new TextConsoleDisposeListener(Console.sysout, Console.syserr));
		final PrintStream ps = new PrintStream(this);
		try {
			System.setOut(ps);
			System.setErr(ps);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write(final int b) {
		buffer.append((char) b);
		if (b == newLine.charAt(newLine.length() - 1)) {
			flush();
		}
	}

	@Override
	public void flush() {
		if (buffer.length() != 0) {
			print(buffer.toString());
			buffer = new StringBuilder();
		}
	}

	@Override
	public void close() {
		flush();
		buffer = null;
	}

	public Text getText() {
		return (Text) scrollable;
	}

	public void clear() {
		getText().setText("");
	}

	protected void failSafePrint(final String value) {
		Console.sysout.print(value);
	}

	protected void doPrint(final String value, final int maxChars) {
		if (getText().getCharCount() < maxChars) {
			getText().append(value);
		}
		else {
			getText().setText(value.startsWith(newLine) ? value.substring(newLine.length()) : value);
		}
		getText().setTopIndex(getText().getLineCount() - 1);
	}

	protected void print(final String value) {
		// Dealing with null argument...
		final String toPrint;
		if (value == null) {
			toPrint = String.valueOf(value);
		}
		else {
			toPrint = value;
		}

		int mc;
		try {
			mc = configuration.getInt("gui.console.max.chars");
		}
		catch (final Exception exception) {
			mc = Defaults.GUI_CONSOLE_MAX_CHARS;
		}
		final int maxChars = mc;

		// Actual print... (async avoids deadlocks)
		new SwtThreadExecutor(scrollable, true) {
			@Override
			protected void run() {
				doPrint(toPrint, maxChars);
			}

			@Override
			protected void onError(final Exception exception) {
				failSafePrint(toPrint);
			}
		}.start();
	}

}
