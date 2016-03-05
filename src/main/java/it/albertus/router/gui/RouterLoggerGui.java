package it.albertus.router.gui;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.engine.RouterLoggerStatus;
import it.albertus.router.engine.Threshold;
import it.albertus.router.gui.listener.CloseListener;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.ExceptionUtils;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class RouterLoggerGui extends RouterLoggerEngine implements Gui {

	protected interface Defaults extends RouterLoggerEngine.Defaults {
		boolean GUI_MINIMIZE_TRAY = true;
		boolean GUI_START_MINIMIZED = false;
	}

	private final DataTable dataTable = DataTable.getInstance();
	private TrayIcon trayIcon;
	private MenuBar menuBar;
	private Shell shell;

	/** Entry point for GUI version */
	public static void start() {
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
		messageBox.setText(Resources.get("lbl.window.title"));
		messageBox.setMessage(ExceptionUtils.getUIMessage(e));
		messageBox.open();
		shell.dispose();
		display.dispose();
		System.exit(1);
	}

	private void configureShell() {
		shell.setText(Resources.get("lbl.window.title"));
		shell.setImages(Images.ICONS_ROUTER_BLUE);
		if (configuration.getBoolean("gui.minimize.tray", Defaults.GUI_MINIMIZE_TRAY)) {
			trayIcon = TrayIcon.getInstance();
			trayIcon.init(this);
		}

		// Listener sul pulsante di chiusura dell'applicazione...
		if (CloseMessageBox.show()) {
			shell.addListener(SWT.Close, new CloseListener(this));
		}
	}

	private Point getInitialSize() {
		return new Point(750, 580);
	}

	private Shell createShell(Display display) {
		shell = new Shell(display);
		shell.setSize(getInitialSize());
		shell.setMinimized(configuration.getBoolean("gui.start.minimized", Defaults.GUI_START_MINIMIZED));
		configureShell();
		createContents();
		return shell;
	}

	private void createContents() {
		GridLayout layout = new GridLayout(1, true);
		shell.setLayout(layout);

		// Menu
		menuBar = new MenuBar(this);

		// Tabella
		final GridData tableLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		tableLayoutData.minimumHeight = 200;
		tableLayoutData.heightHint = 200;
		dataTable.init(this, tableLayoutData);

		// Console
		final GridData consoleLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		consoleLayoutData.minimumHeight = 200;
		consoleLayoutData.heightHint = 200;
		getConsole().init(shell, consoleLayoutData);
	}

	@Override
	protected void showInfo(final RouterData info, final Map<Threshold, String> thresholdsReached) {
		/* Aggiunta riga nella tabella a video */
		dataTable.addRow(info, thresholdsReached, getIteration());

		/* Aggiornamento icona e tooltip nella barra di notifica (se necessario) */
		if (trayIcon != null) {
			trayIcon.updateTrayItem(getStatus(), info);
		}

		/* Stampa eventuali soglie raggiunte in console */
		printThresholdsReached(thresholdsReached);
	}

	private void printThresholdsReached(final Map<Threshold, String> thresholdsReached) {
		if (thresholdsReached != null && !thresholdsReached.isEmpty()) {
			final Map<String, String> message = new TreeMap<String, String>();
			boolean print = false;
			for (final Threshold threshold : thresholdsReached.keySet()) {
				message.put(threshold.getKey(), thresholdsReached.get(threshold));
				if (!threshold.isExcluded()) {
					print = true;
				}
			}
			if (print) {
				logger.log(Resources.get("msg.thresholds.reached", message), Destination.CONSOLE);
			}
		}
	}

	public boolean canCopyDataTable() {
		return this.getDataTable().getTable() != null && this.getDataTable().getTable().getSelectionCount() > 0;
	}

	public boolean canCopyConsole() {
		return this.getConsole().getText() != null && this.getConsole().getText().isFocusControl() && this.getConsole().getText().getSelectionCount() > 0;
	}

	public boolean canSelectAllDataTable() {
		return this.getDataTable().getTable() != null && this.getDataTable().getTable().getItemCount() > 0;
	}

	public boolean canSelectAllConsole() {
		return this.getConsole().getText() != null && this.getConsole().getText().isFocusControl();
	}

	@Override
	protected void setStatus(RouterLoggerStatus status) {
		super.setStatus(status);
		trayIcon.updateTrayItem(status);
	}

	@Override
	public Shell getShell() {
		return shell;
	}

	@Override
	public TextConsole getConsole() {
		return TextConsole.getInstance();
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public DataTable getDataTable() {
		return dataTable;
	}

	public TrayIcon getTrayIcon() {
		return trayIcon;
	}

}
