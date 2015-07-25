package it.albertus.router.gui;

import it.albertus.util.Console;
import it.albertus.util.NewLine;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class GuiConsole extends Console {

	private static class Singleton {
		private static final GuiConsole console = new GuiConsole();
	}

	public static GuiConsole getInstance() {
		return Singleton.console;
	}

	private GuiConsole() {}

	public void init(final Composite container) {
		if (this.styledText == null) {
			this.styledText = createStyledText(container);

			this.styledText.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					// Supporto CTRL+A per "Seleziona tutto"...
					if (e.stateMask == SWT.CTRL && e.keyCode == 'a') {
						styledText.selectAll();
					}
				}
			});
		}
		else {
			throw new IllegalStateException(this.getClass().getSimpleName() + " already initialized.");
		}
	}

	private StyledText createStyledText(final Composite container) {
		final StyledText styledText = new StyledText(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.minimumHeight = 200;
		gridData.heightHint = 200;
		styledText.setLayoutData(gridData);
		styledText.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		styledText.setEditable(false);
		return styledText;
	}

	private static final String NEWLINE = NewLine.CRLF.toString();

	private StyledText styledText = null;

	private void failSafePrint(final String toPrint) {
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
		if (styledText != null && !styledText.isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						styledText.append(toPrint);
						styledText.setTopIndex(styledText.getLineCount() - 1);
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
