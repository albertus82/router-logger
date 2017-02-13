package it.albertus.router.console;

import java.util.logging.LogRecord;

import it.albertus.util.NewLine;
import it.albertus.util.logging.CustomFormatter;

public class ConsoleFormatter extends CustomFormatter {

	private boolean printOnNewLine = false;

	public ConsoleFormatter(final String format) {
		super(format);
	}

	@Override
	public synchronized String format(final LogRecord record) {
		String message = super.format(record);
		if (printOnNewLine) {
			message = NewLine.SYSTEM_LINE_SEPARATOR + message;
			printOnNewLine = false;
		}
		return message;
	}

	public boolean isPrintOnNewLine() {
		return printOnNewLine;
	}

	public void setPrintOnNewLine(final boolean printOnNewLine) {
		this.printOnNewLine = printOnNewLine;
	}

}
