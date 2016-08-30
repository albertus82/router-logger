package it.albertus.router.gui;

import it.albertus.jface.SwtThreadExecutor;
import it.albertus.jface.TextConsole;
import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.engine.Status;
import it.albertus.router.engine.Threshold;
import it.albertus.router.gui.listener.CloseListener;
import it.albertus.router.gui.preference.RouterLoggerPreference;
import it.albertus.router.gui.preference.RouterLoggerPreferences;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.ConfigurationException;
import it.albertus.util.Configured;
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
import org.eclipse.swt.widgets.Text;

public class RouterLoggerGui extends RouterLoggerEngine implements IShellProvider {

	public static final String CFG_KEY_GUI_CLIPBOARD_MAX_CHARS = "gui.clipboard.max.chars";

	private static final float SASH_MAGNIFICATION_FACTOR = 1.5f;

	public interface Defaults extends RouterLoggerEngine.Defaults {
		boolean GUI_START_MINIMIZED = false;
		int GUI_CLIPBOARD_MAX_CHARS = 100000;
	}

	private final Shell shell;
	private final DataTable dataTable;
	private final TrayIcon trayIcon;
	private final MenuBar menuBar;
	private final SashForm sashForm;
	private final TextConsole textConsole;

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
			routerLogger.addShutdownHook();
			routerLogger.beforeConnect();
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
		catch (final Throwable throwable) {
			display.dispose();
			Logger.getInstance().log(throwable);
		}
		finally {
			routerLogger.disconnect(true);
			display.dispose();
			routerLogger.joinPollingThread();
			routerLogger.stopNetworkServices();
			routerLogger.removeShutdownHook();
		}
		routerLogger.printGoodbye();
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
		try {
			Logger.getInstance().log(throwable);
		}
		catch (final NoClassDefFoundError ncdfe) {
			throwable.printStackTrace();
		}
		final Shell shell = new Shell(display);
		final int buttonId = openErrorMessageBox(shell, throwable);
		if (buttonId == SWT.OK || buttonId == SWT.NO || new RouterLoggerPreferences().openDialog(shell, RouterLoggerPreference.forConfigurationKey(((ConfigurationException) throwable).getKey()).getPageDefinition()) != Window.OK) {
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
				propertyName = RouterLoggerPreference.forConfigurationKey(ce.getKey()).getLabel();
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
		textConsole = new TextConsole(sashForm, new GridData(SWT.FILL, SWT.FILL, true, true), new Configured<Integer>() {
			@Override
			public Integer getValue() {
				return configuration.getInt("gui.console.max.chars");
			}
		});

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
			trayIcon.updateTrayItem(getCurrentStatus().getStatus(), info);
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

	public boolean canCopyConsole() {
		final Text text = textConsole.getText();
		return text != null && text.getSelectionCount() > 0 && (text.isFocusControl() || !dataTable.canCopy());
	}

	public boolean canSelectAllConsole() {
		final Text text = textConsole.getText();
		return text != null && !text.getText().isEmpty() && (text.isFocusControl() || !dataTable.canSelectAll());
	}

	public boolean canClearConsole() {
		final Text text = textConsole.getText();
		return text != null && !text.getText().isEmpty();
	}

	/** Avvia il ciclo. */
	@Override
	public void connect() {
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
			if (connect) {
				exit = false;
				pollingThread = new Thread("pollingThread") {
					@Override
					public void run() {
						try {
							outerLoop();
						}
						catch (final Exception exception) {
							logger.log(exception);
							try {
								getReader().disconnect();
							}
							catch (final Exception e) {/* Ignore */}
							release();
						}
						catch (final Throwable throwable) {
							new SwtThreadExecutor(RouterLoggerGui.this.shell) {
								@Override
								protected void run() {
									getWidget().getDisplay().dispose();
								}
							}.start();
							release();
							logger.log(throwable);
							try {
								getReader().disconnect();
							}
							catch (final Exception e) {/* Ignore */}
						}
					}
				};
				pollingThread.start();
			}
			else {
				logger.log(Resources.get("err.operation.not.allowed", getCurrentStatus().getStatus().getDescription()), Destination.CONSOLE);
			}
		}
	}

	@Override
	public void restart() {
		disconnect(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					httpServer.stop();
					joinPollingThread();
					configuration.reload();
					mqttClient.disconnect();
					setIteration(FIRST_ITERATION);
					new SwtThreadExecutor(shell) {
						@Override
						public void run() {
							textConsole.clear();
							dataTable.reset();
							beforeConnect();
							connect();
						}
					}.start();
				}
				catch (final Exception e) {
					logger.log(e);
				}
			}
		}, "resetThread").start();
	}

	@Override
	protected void initReaderAndWriter() {
		do {
			try {
				super.initReaderAndWriter();
			}
			catch (final ConfigurationException ce) {
				// Reset Reader & Writer...
				setReader(null);
				setWriter(null);

				// Open Preferences dialog...
				final int buttonId = openErrorMessageBox(shell, ce);
				if (buttonId == SWT.OK || buttonId == SWT.NO || new RouterLoggerPreferences().openDialog(shell, RouterLoggerPreference.forConfigurationKey((ce).getKey()).getPageDefinition()) != Window.OK) {
					logger.log(ce, Destination.CONSOLE);
					return;
				}
			}
		}
		while (getReader() == null || getWriter() == null);
	}

	@Override
	public void close() {
		new SwtThreadExecutor(shell) {
			@Override
			protected void run() {
				shell.dispose();
			}
		}.start();
	}

	@Override
	protected void setStatus(Status status) {
		super.setStatus(status);
		if (trayIcon != null) {
			trayIcon.updateTrayItem(status);
		}
	}

	@Override
	public Shell getShell() {
		return shell;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public DataTable getDataTable() {
		return dataTable;
	}

	public TextConsole getTextConsole() {
		return textConsole;
	}

	public TrayIcon getTrayIcon() {
		return trayIcon;
	}

}
