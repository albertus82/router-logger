package it.albertus.router.gui;

import it.albertus.util.Console;
import it.albertus.util.NewLine;

import java.util.Locale;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;

public class GuiConsole extends Console {

	private StyledText styledText;
	private boolean initialized = false;

	private static class Singleton {
		private static final GuiConsole console = new GuiConsole();
	}

	public static GuiConsole getInstance() {
		return Singleton.console;
	}

	private GuiConsole() {}

	public void init(StyledText styledText) {
		if (!initialized) {
			this.styledText = styledText;
			initialized = true;
		}
		else {
			throw new IllegalStateException(this.getClass().getSimpleName() + " already initialized.");
		}
	}

	private static final String NEWLINE = NewLine.CRLF.toString();

	public void print(String value) {
		final String toPrint;
		if (value == null) {
			toPrint = String.valueOf(value);
		}
		else {
			toPrint = value;
		}
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (!styledText.isDisposed()) {
					styledText.setText(styledText.getText() + toPrint);
				}
			}
		});
		updatePosition(value);
	}

	public void print(Object value) {
		print(String.valueOf(value));
	}

	public void print(boolean value) {
		print(String.valueOf(value));
	}

	public void print(char value) {
		print(String.valueOf(value));
	}

	public void print(int value) {
		print(String.valueOf(value));
	}

	public void print(long value) {
		print(String.valueOf(value));
	}

	public void print(float value) {
		print(String.valueOf(value));
	}

	public void print(double value) {
		print(String.valueOf(value));
	}

	public void print(char array[]) {
		print(String.valueOf(array));
	}

	public void println() {
		print(NEWLINE);
		newLine();
	}

	public void println(String value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	public void println(Object value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	public void println(boolean value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	public void println(char value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	public void println(int value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	public void println(long value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	public void println(float value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	public void println(double value) {
		print(value);
		print(NEWLINE);
		newLine();
	}

	public void println(char array[]) {
		print(array);
		print(NEWLINE);
		newLine();
	}

	public void format(Locale l, String format, Object... args) {
		print(String.format(l, format, args));
	}

	public void format(String format, Object... args) {
		print(String.format(format, args));
	}

	public void printf(Locale l, String format, Object... args) {
		format(l, format, args);
	}

	public void printf(String format, Object... args) {
		format(format, args);
	}

	public void print(String value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		print(value);
	}

	public void print(Object value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		print(value);
	}

	public void print(boolean value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		print(value);
	}

	public void print(char value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		print(value);
	}

	public void print(int value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		print(value);
	}

	public void print(long value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		print(value);
	}

	public void print(float value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		print(value);
	}

	public void print(double value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		print(value);
	}

	public void print(char array[], boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		print(array);
	}

	public void println(String value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		println(value);
	}

	public void println(Object value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		println(value);
	}

	public void println(boolean value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		println(value);
	}

	public void println(char value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		println(value);
	}

	public void println(int value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		println(value);
	}

	public void println(long value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		println(value);
	}

	public void println(float value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		println(value);
	}

	public void println(double value, boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		println(value);
	}

	public void println(char array[], boolean onNewLine) {
		if (onNewLine && column != 0) {
			println();
		}
		println(array);
	}

}
