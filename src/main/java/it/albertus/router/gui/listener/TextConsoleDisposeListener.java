package it.albertus.router.gui.listener;

import java.io.PrintStream;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

public class TextConsoleDisposeListener implements DisposeListener {

	private final PrintStream sysout;
	private final PrintStream syserr;

	public TextConsoleDisposeListener(final PrintStream sysout, final PrintStream syserr) {
		this.sysout = sysout;
		this.syserr = syserr;
	}

	@Override
	public void widgetDisposed(final DisposeEvent de) {
		try {
			System.setOut(sysout);
			System.setErr(syserr);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
