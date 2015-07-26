package it.albertus.router;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.gui.GuiConsole;
import it.albertus.router.gui.GuiImages;
import it.albertus.router.gui.GuiTable;
import it.albertus.router.gui.GuiTray;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.ExceptionUtils;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
		catch (ExceptionInInitializerError e) {
			int style = SWT.ICON_ERROR;
			final Display display = new Display();
			final Shell shell = new Shell(display);
			MessageBox messageBox = new MessageBox(shell, style);
			messageBox.setText(Resources.get("lbl.error"));
			messageBox.setMessage(ExceptionUtils.getUIMessage(e.getCause() != null ? e.getCause() : e));
			messageBox.open();
			shell.dispose();
			display.dispose();
			System.exit(1);
		}
		return instance;
	}

	private void configureShell(final Shell shell) {
		shell.setText(Resources.get("lbl.window.title"));
		shell.setImages(new Image[] { GuiImages.ICONS[9], GuiImages.ICONS[10], GuiImages.ICONS[11], GuiImages.ICONS[12] });
		if (configuration.getBoolean("gui.minimize.tray", Defaults.GUI_MINIMIZE_TRAY)) {
			GuiTray.getInstance().init(shell);
		}
	}

	protected Point getInitialSize() {
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
	protected void showInfo(Map<String, String> info) {
		table.addRow(info, iteration);
	}

	@Override
	protected void showThresholdsReached(final Map<String, String> info, final Set<String> thresholdsReached) {
		if (info != null && thresholdsReached != null) {
			Map<String, String> values = new TreeMap<String, String>();
			for (String key : thresholdsReached) {
				values.put(key, info.get(key));
			}
			logger.log(Resources.get("msg.thresholds.reached", values), Destination.CONSOLE);
		}
	}

	@Override
	protected GuiConsole getConsole() {
		return GuiConsole.getInstance();
	}

}
