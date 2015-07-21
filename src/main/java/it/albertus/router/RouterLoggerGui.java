package it.albertus.router;

import it.albertus.router.gui.GuiConsole;
import it.albertus.router.gui.GuiImages;
import it.albertus.router.gui.GuiTable;
import it.albertus.router.gui.GuiTray;

import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class RouterLoggerGui extends RouterLoggerEngine {

	protected interface Defaults extends RouterLoggerEngine.Defaults {
		boolean GUI_MINIMIZE_TRAY = true;
	}

	private static final GuiTable table = GuiTable.getInstance();

	public static void main(String args[]) {
		try {
			// Creazione finestra applicazione...
			final Display display = new Display();
			final RouterLoggerGui routerLogger = new RouterLoggerGui();
			final Shell shell = routerLogger.createShell(display);
			shell.open();

			// Avvio thread di interrogazione router...
			final Thread updateThread = new Thread() {
				@Override
				public void run() {
					routerLogger.run();
				}
			};
			updateThread.start();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

			// Segnala al thread che deve terminare il loop...
			routerLogger.exit = true;

			// Distrugge la GUI...
			display.dispose();
			
			// Attende che il thread abbia completato il rilascio risorse...
			while (!routerLogger.close) {
				Thread.sleep(500);
			}
		}
		catch (Exception e) {
			logger.log(e);
		}
	}

	private void configureShell(final Shell shell) {
		shell.setText("RouterLogger");
		shell.setImages(new Image[] { GuiImages.ICONS[9], GuiImages.ICONS[10], GuiImages.ICONS[11], GuiImages.ICONS[12] });
		if (configuration.getBoolean("gui.minimize.tray", Defaults.GUI_MINIMIZE_TRAY)) {
			new GuiTray(shell);
		}
	}

	protected Point getInitialSize() {
		return new Point(750, 550);
	}

	private Shell createShell(Display display) {
		final Shell shell = new Shell(display);
		configureShell(shell);
		shell.setSize(getInitialSize());
		createContents(shell);
		return shell;
	}

	private Control createContents(Composite parent) {
		Composite container = parent;// new Composite(parent, SWT.NONE);

		// Variare il numero per aumentare o diminuire le colonne del layout.
		// Modificare conseguentemente anche lo span della tabella e della
		// console!
		GridLayout layout = new GridLayout(1, true);
		container.setLayout(layout);

		// Tabella
		table.init(container);

		// Console
		getConsole().init(container);

		return container;
	}

	@Override
	protected void log(Map<String, String> info, int iteration, int lastLogLength, int iterations) {
		table.addRow(info, iteration);
	}

	@Override
	protected GuiConsole getConsole() {
		return GuiConsole.getInstance();
	}

}
