package it.albertus.router.gui;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.engine.RouterLoggerStatus;
import it.albertus.router.engine.Threshold;
import it.albertus.router.gui.listener.CloseListener;
import it.albertus.router.gui.preference.Preference;
import it.albertus.router.gui.preference.Preferences;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.ConfigurationException;
import it.albertus.util.ExceptionUtils;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class RouterLoggerGui extends RouterLoggerEngine implements IShellProvider {

	private static final float SASH_MAGNIFICATION_FACTOR = 1.5f;

	public interface Defaults extends RouterLoggerEngine.Defaults {
		boolean GUI_START_MINIMIZED = false;
	}

	private Thread updateThread;

	private final Shell shell;
	private final DataTable dataTable;
	private final TrayIcon trayIcon;
	private final MenuBar menuBar;
	private final SashForm sashForm;

	/** Entry point for GUI version */
	public static void start() {
		final Display display = Display.getDefault();

		// Creazione finestra applicazione...
		final RouterLoggerGui routerLogger = newInstance(display);
		if (routerLogger == null) {
			display.dispose();
			return;
		}
		final Shell shell = routerLogger.getShell();
		try {
			shell.open();

			routerLogger.connect();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		}
		catch (final Exception exception) {
			Logger.getInstance().log(exception);
			openErrorMessageBox(shell != null && !shell.isDisposed() ? shell : new Shell(display), exception);
		}
		finally {
			// Segnala al thread che deve terminare il loop immediatamente.
			routerLogger.disconnect(true);

			// Distrugge la GUI...
			display.dispose();

			// Attende che il thread completi il rilascio risorse...
			if (routerLogger.updateThread != null) {
				try {
					routerLogger.updateThread.join();
				}
				catch (final InterruptedException ie) {}
				catch (final Exception e) {
					Logger.getInstance().log(e);
				}
			}

			// Stampa del messaggio di commiato...
			routerLogger.afterOuterLoop();
		}
	}

	private static RouterLoggerGui newInstance(final Display display) {
		RouterLoggerGui instance;
		try {
			instance = new RouterLoggerGui(display);
		}
		catch (final Exception exception) {
			instance = showError(display, exception);
		}
		catch (final ExceptionInInitializerError exception) {
			instance = showError(display, exception.getCause() != null ? exception.getCause() : exception);
		}
		return instance;
	}

	private static RouterLoggerGui showError(final Display display, final Throwable throwable) {
		Logger.getInstance().log(throwable);
		final Shell shell = new Shell(display);
		final int buttonId = openErrorMessageBox(shell, throwable);
		if (buttonId == SWT.OK || buttonId == SWT.NO || new Preferences(shell).open(Preference.findByConfigurationKey(((ConfigurationException) throwable).getKey()).getPage()) != Window.OK) {
			shell.dispose();
			return null;
		}
		shell.dispose();
		return newInstance(display); // Retry after configuration reload
	}

	private static int openErrorMessageBox(final Shell shell, final Throwable throwable) {
		final int style;
		final String message;
		if (throwable instanceof ConfigurationException) {
			final ConfigurationException ce = (ConfigurationException) throwable;
			style = SWT.ICON_WARNING | SWT.YES | SWT.NO;
			String propertyName;
			try {
				propertyName = Resources.get(Preference.findByConfigurationKey(ce.getKey()).getResourceKey());
			}
			catch (final Exception e) {
				propertyName = ce.getKey();
			}
			message = Resources.get("err.invalid.cfg", propertyName) + ' ' + Resources.get("lbl.preferences.edit");
		}
		else {
			style = SWT.ICON_ERROR;
			message = ExceptionUtils.getUIMessage(throwable);
		}
		final MessageBox messageBox = new MessageBox(shell, style);
		messageBox.setText(Resources.get("lbl.window.title"));
		messageBox.setMessage(message);
		return messageBox.open();
	}

	private RouterLoggerGui(final Display display) {
		shell = new Shell(display);
		shell.setMinimized(configuration.getBoolean("gui.start.minimized", Defaults.GUI_START_MINIMIZED));
		shell.setText(Resources.get("lbl.window.title"));
		shell.setImages(Images.MAIN_ICONS);
		shell.setLayout(new GridLayout());

		trayIcon = new TrayIcon(this);

		menuBar = new MenuBar(this);

		sashForm = new SashForm(shell, SWT.VERTICAL);
		sashForm.setSashWidth((int) (sashForm.getSashWidth() * SASH_MAGNIFICATION_FACTOR));
		sashForm.setLayout(new GridLayout());
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		dataTable = new DataTable(sashForm, new GridData(SWT.FILL, SWT.FILL, true, true), this);
		getConsole().init(sashForm, new GridData(SWT.FILL, SWT.FILL, true, true));

		shell.addListener(SWT.Close, new CloseListener(this));
	}

	@Override
	protected void innerLoop() throws IOException, InterruptedException {
		if (dataTable != null && dataTable.getTable() != null && !dataTable.getTable().isDisposed()) {
			setIteration(dataTable.getIteration() + 1);
		}
		super.innerLoop();
	}

	@Override
	protected void showInfo(final RouterData info, final Map<Threshold, String> thresholdsReached) {
		/* Aggiunta riga nella tabella a video */
		try {
			dataTable.addRow(getIteration(), info, thresholdsReached);
		}
		catch (ConfigurationException ce) {
			logger.log(ce);
		}

		/* Aggiornamento icona e tooltip nella barra di notifica (se necessario) */
		if (trayIcon != null) {
			trayIcon.updateTrayItem(getCurrentStatus(), info);
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
				if (trayIcon != null) {
					trayIcon.showBalloonToolTip(thresholdsReached);
				}
			}
		}
	}

	public boolean canCopyDataTable() {
		return this.getDataTable().getTable() != null && this.getDataTable().getTable().getSelectionCount() > 0;
	}

	public boolean canCopyConsole() {
		return this.getConsole().getText() != null && this.getConsole().getText().getSelectionCount() > 0 && (this.getConsole().getText().isFocusControl() || !canCopyDataTable());
	}

	public boolean canSelectAllDataTable() {
		return this.getDataTable().getTable() != null && this.getDataTable().getTable().getItemCount() > 0;
	}

	public boolean canSelectAllConsole() {
		return this.getConsole().getText() != null && !this.getConsole().getText().getText().isEmpty() && (this.getConsole().getText().isFocusControl() || !canSelectAllDataTable());
	}

	public boolean canDeleteDataTable() {
		return canCopyDataTable();
	}

	public boolean canClearDataTable() {
		return canSelectAllDataTable();
	}

	public boolean canClearConsole() {
		return canSelectAllConsole();
	}

	public boolean canConnect() {
		return (getReader() != null && getWriter() != null && RouterLoggerStatus.STARTING.equals(getCurrentStatus()) || RouterLoggerStatus.DISCONNECTED.equals(getCurrentStatus()) || RouterLoggerStatus.ERROR.equals(getCurrentStatus())) && (configuration.getInt("logger.iterations", Defaults.ITERATIONS) <= 0 || getIteration() <= configuration.getInt("logger.iterations", Defaults.ITERATIONS));
	}

	public boolean canDisconnect() {
		return !(RouterLoggerStatus.STARTING.equals(getCurrentStatus()) || RouterLoggerStatus.DISCONNECTED.equals(getCurrentStatus()) || RouterLoggerStatus.ERROR.equals(getCurrentStatus()) || RouterLoggerStatus.DISCONNECTING.equals(getCurrentStatus()));
	}

	/** Avvia il ciclo. */
	public void connect() {
		// Reader & Writer initialization...
		initReaderAndWriter();

		// Print welcome message...
		beforeOuterLoop();

		// Avvia thread di interrogazione router...
		if (getReader() != null && getWriter() != null) {
			boolean connect;
			try {
				connect = canConnect();
			}
			catch (final Exception exception) {
				logger.log(exception);
				return;
			}
			finally {
				release();
			}
			if (connect) {
				exit = false;
				updateThread = new Thread("updateThread") {
					@Override
					public void run() {
						try {
							outerLoop();
						}
						catch (final Exception exception) {
							logger.log(exception);
						}
						finally {
							release();
						}
					}
				};
				updateThread.start();
			}
			else {
				logger.log(Resources.get("err.operation.not.allowed", getCurrentStatus().toString()), Destination.CONSOLE);
			}
		}
	}

	private void disconnect(final boolean force) {
		if (canDisconnect() || force) {
			setStatus(RouterLoggerStatus.DISCONNECTING);
			exit = true;
			if (isInterruptible()) {
				try {
					updateThread.interrupt();
				}
				catch (final SecurityException se) {}
			}
		}
		else {
			logger.log(Resources.get("err.operation.not.allowed", getCurrentStatus().toString()), Destination.CONSOLE);
		}
	}

	/** Interrompe il ciclo e forza la disconnessione. */
	public void disconnect() {
		disconnect(false);
	}

	public void reset() {
		disconnect(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (updateThread != null) {
					try {
						updateThread.join();
					}
					catch (final InterruptedException ie) {}
					catch (final Exception e) {
						logger.log(e);
					}
				}
				afterOuterLoop();
				configuration.reload();
				setIteration(FIRST_ITERATION);
				setStatus(RouterLoggerStatus.STARTING);
				new GuiThreadExecutor(shell) {
					@Override
					public void run() {
						getConsole().clear();
						dataTable.reset();
						connect();
					}
				}.start();
			}
		}, "resetThread").start();
	}

	@Override
	protected void initReaderAndWriter() {
		try {
			super.initReaderAndWriter();
		}
		catch (final Throwable throwable) {
			setReader(null);
			setWriter(null);
			final int buttonId = openErrorMessageBox(shell, throwable);
			if (buttonId != SWT.OK && buttonId != SWT.NO && new Preferences(RouterLoggerGui.this).open(Preference.findByConfigurationKey(((ConfigurationException) throwable).getKey()).getPage()) == Window.OK) {
				initReaderAndWriter();
			}
		}
	}

	@Override
	protected void setStatus(RouterLoggerStatus status) {
		super.setStatus(status);
		if (trayIcon != null) {
			trayIcon.updateTrayItem(status);
		}
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
