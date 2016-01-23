package it.albertus.router;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.gui.GuiCloseMessageBox;
import it.albertus.router.gui.GuiConsole;
import it.albertus.router.gui.GuiImages;
import it.albertus.router.gui.GuiTable;
import it.albertus.router.gui.GuiTray;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.ExceptionUtils;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class RouterLoggerGui extends RouterLoggerEngine {

	protected interface Defaults extends RouterLoggerEngine.Defaults {
		boolean GUI_MINIMIZE_TRAY = true;
		boolean GUI_START_MINIMIZED = false;
	}

	private final GuiTable table = GuiTable.getInstance();

	public static void main(String args[]) {
		try {
			final RouterLoggerGui routerLogger = newInstance();

			// Creazione finestra applicazione...
			final Display display = new Display();
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
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}

			// Segnala al thread che deve terminare il loop immediatamente...
			routerLogger.exit = true;
			updateThread.interrupt();

			// Distrugge la GUI...
			display.dispose();

			// Attende che il thread completi il rilascio risorse...
			updateThread.join();
		}
		catch (Exception e) {
			Logger.getInstance().log(e);
		}
	}

	private static RouterLoggerGui newInstance() {
		RouterLoggerGui instance = null;
		try {
			instance = new RouterLoggerGui();
		}
		catch (Exception e) {
			fatalError(e);
		}
		catch (ExceptionInInitializerError e) {
			fatalError(e.getCause() != null ? e.getCause() : e);
		}	
		return instance;
	}

	private static void fatalError(Throwable e) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		final MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
		messageBox.setText(Resources.get("lbl.error"));
		messageBox.setMessage(ExceptionUtils.getUIMessage(e));
		messageBox.open();
		shell.dispose();
		display.dispose();
		System.exit(1);
	}

	private void configureShell(final Shell shell) {
		shell.setText(Resources.get("lbl.window.title"));
		shell.setImages(new Image[] { GuiImages.ICONS[9], GuiImages.ICONS[10], GuiImages.ICONS[11], GuiImages.ICONS[12] });
		if (configuration.getBoolean("gui.minimize.tray", Defaults.GUI_MINIMIZE_TRAY)) {
			GuiTray.getInstance().init(shell);
		}

		// Listener sul pulsante di chiusura dell'applicazione...
		if (GuiCloseMessageBox.show()) {
			shell.addListener(SWT.Close, new Listener() {
				public void handleEvent(Event event) {
					event.doit = GuiCloseMessageBox.newInstance(shell).open() == SWT.YES;
				}
			});
		}
	}

	private Point getInitialSize() {
		return new Point(750, 550);
	}

	private Shell createShell(Display display) {
		final Shell shell = new Shell(display);
		shell.setSize(getInitialSize());
		shell.setMinimized(configuration.getBoolean("gui.start.minimized", Defaults.GUI_START_MINIMIZED));
		configureShell(shell);
		createContents(shell);
		return shell;
	}

	private Control createContents(Composite parent) {
		Composite container = parent;// new Composite(parent, SWT.NONE);

		/*
		 * Variare il numero per aumentare o diminuire le colonne del layout.
		 * Modificare conseguentemente anche lo span della tabella e della
		 * console!
		 */
		GridLayout layout = new GridLayout(1, true);
		container.setLayout(layout);

		// Tabella
		table.init(container);

		// Console
		getConsole().init(container);

		return container;
	}

	@Override
	protected void showInfo(final RouterData info) {
		table.addRow(info, iteration);
	}

	@Override
	protected void showThresholdsReached(final Map<String, String> thresholdsReached) {
		logger.log(Resources.get("msg.thresholds.reached", thresholdsReached), Destination.CONSOLE);
	}

	@Override
	protected GuiConsole getConsole() {
		return GuiConsole.getInstance();
	}

}
