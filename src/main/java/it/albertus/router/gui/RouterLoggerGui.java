package it.albertus.router.gui;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import it.albertus.jface.DisplayThreadExecutor;
import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.jface.JFaceMessages;
import it.albertus.jface.console.StyledTextConsole;
import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.engine.Status;
import it.albertus.router.engine.Threshold;
import it.albertus.router.gui.listener.CloseListener;
import it.albertus.router.gui.preference.Preference;
import it.albertus.router.gui.preference.RouterLoggerPreferences;
import it.albertus.router.resources.Messages;
import it.albertus.router.util.InitializationException;
import it.albertus.util.ConfigurationException;
import it.albertus.util.Configured;
import it.albertus.util.ExceptionUtils;
import it.albertus.util.Version;
import it.albertus.util.logging.LoggerFactory;

public class RouterLoggerGui extends RouterLoggerEngine implements IShellProvider {

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerGui.class);

	public static final String CFG_KEY_GUI_CLIPBOARD_MAX_CHARS = "gui.clipboard.max.chars";

	private static final float SASH_MAGNIFICATION_FACTOR = 1.5f;

	public static class Defaults extends RouterLoggerEngine.Defaults {
		public static final boolean GUI_START_MINIMIZED = false;
		public static final int GUI_CLIPBOARD_MAX_CHARS = 100000;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private final Shell shell;
	private final DataTable dataTable;
	private final TrayIcon trayIcon;
	private final MenuBar menuBar;
	private final SashForm sashForm;
	private final StyledTextConsole console;

	private RouterLoggerGui(final Display display) {
		shell = new Shell(display);

		// Fix invisible (transparent) shell bug with some Linux distibutions
		if (!Util.isGtk() && configuration.getBoolean("gui.start.minimized", Defaults.GUI_START_MINIMIZED)) {
			shell.setMinimized(true);
		}

		shell.setText(Messages.get("lbl.window.title"));
		shell.setImages(Images.getMainIcons());
		shell.setLayout(new GridLayout());

		trayIcon = new TrayIcon(this);

		menuBar = new MenuBar(this);

		sashForm = new SashForm(shell, SWT.VERTICAL);
		sashForm.setSashWidth((int) (sashForm.getSashWidth() * SASH_MAGNIFICATION_FACTOR));
		sashForm.setLayout(new GridLayout());
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		dataTable = new DataTable(sashForm, new GridData(SWT.FILL, SWT.FILL, true, true), this);

		console = new StyledTextConsole(sashForm, new GridData(SWT.FILL, SWT.FILL, true, true), true);
		final String fontDataString = configuration.getString("gui.console.font", true);
		if (!fontDataString.isEmpty()) {
			console.setFont(PreferenceConverter.readFontData(fontDataString));
		}
		console.setLimit(new Configured<Integer>() {
			@Override
			public Integer getValue() {
				return configuration.getInt("gui.console.max.chars");
			}
		});

		shell.addListener(SWT.Close, new CloseListener(this));
	}

	/** Entry point for GUI version */
	public static void start(final InitializationException ie) {
		Display.setAppName(Messages.get("msg.application.name"));
		Display.setAppVersion(Version.getInstance().getNumber());
		final Display display = Display.getDefault();

		if (ie != null) { // Display error dialog and exit.
			EnhancedErrorDialog.openError(null, Messages.get("lbl.window.title"), ie.getLocalizedMessage() != null ? ie.getLocalizedMessage() : ie.getMessage(), IStatus.ERROR, ie.getCause() != null ? ie.getCause() : ie, Images.getMainIcons());
			display.dispose();
		}
		else {
			// Creazione finestra applicazione...
			final RouterLoggerGui routerLogger = newInstance(display);
			if (routerLogger == null) {
				display.dispose();
				return;
			}
			final Shell shell = routerLogger.getShell();
			try {
				shell.open();

				// Fix invisible (transparent) shell bug with some Linux distibutions
				if (Util.isGtk() && routerLogger.configuration.getBoolean("gui.start.minimized", Defaults.GUI_START_MINIMIZED)) {
					shell.setMinimized(true);
				}

				routerLogger.addShutdownHook();
				routerLogger.beforeConnect();
				routerLogger.connect();
				while (!shell.isDisposed()) {
					if (!display.isDisposed() && !display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
			catch (final Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
				openErrorMessageBox(shell != null && !shell.isDisposed() ? shell : new Shell(display), e);
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
		logger.log(Level.SEVERE, throwable.toString(), throwable);
		final Shell shell = new Shell(display);
		final int buttonId = openErrorMessageBox(shell, throwable);
		if (buttonId == SWT.OK || buttonId == SWT.NO || new RouterLoggerPreferences().openDialog(shell, Preference.forName(((ConfigurationException) throwable).getKey()).getPageDefinition()) != Window.OK) {
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
				propertyName = Preference.forName(ce.getKey()).getLabel();
			}
			catch (final Exception e) {
				logger.log(Level.FINE, e.toString(), e);
				propertyName = ce.getKey();
			}
			message = JFaceMessages.get("err.configuration.invalid", propertyName) + ' ' + Messages.get("lbl.preferences.edit");
		}
		else {
			style = SWT.ICON_ERROR;
			message = ExceptionUtils.getUIMessage(throwable);
		}
		final MessageBox messageBox = new MessageBox(shell, style);
		messageBox.setText(Messages.get("lbl.window.title"));
		messageBox.setMessage(message);
		return messageBox.open();
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
		catch (final ConfigurationException e) {
			logger.log(Level.SEVERE, e.toString(), e);
		}

		// Aggiornamento icona e tooltip nella barra di notifica (se necessario)
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
			for (final Entry<Threshold, String> entry : thresholdsReached.entrySet()) {
				final Threshold threshold = entry.getKey();
				message.put(threshold.getKey(), entry.getValue());
				if (!threshold.isExcluded()) {
					print = true;
				}
			}
			if (print) {
				logger.log(Level.INFO, Messages.get("msg.thresholds.reached"), message);
				if (trayIcon != null) {
					trayIcon.showBalloonToolTip(thresholdsReached);
				}
			}
		}
	}

	public boolean canCopyConsole() {
		return console.hasSelection() && (console.getScrollable().isFocusControl() || !dataTable.canCopy());
	}

	public boolean canSelectAllConsole() {
		return !console.isEmpty() && (console.getScrollable().isFocusControl() || !dataTable.canSelectAll());
	}

	public boolean canClearConsole() {
		return !console.isEmpty();
	}

	@Override
	public void restart() {
		disconnect(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					httpServer.stop();
					getShutdownDaemon().interrupt();
					joinPollingThread();
					configuration.reload();
					mqttClient.disconnect();
					setIteration(FIRST_ITERATION);
					new DisplayThreadExecutor(shell).execute(new Runnable() {
						@Override
						public void run() {
							if (!logger.isLoggable(Level.FINE)) {
								console.clear();
							}
							dataTable.reset();
							beforeConnect();
							connect();
						}
					});
				}
				catch (final Exception e) {
					logger.log(Level.SEVERE, e.toString(), e);
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
			catch (final ConfigurationException e) {
				// Reset Reader & Writer...
				setReader(null);
				setWriter(null);

				// Open Preferences dialog...
				final int buttonId = openErrorMessageBox(shell, e);
				if (buttonId == SWT.OK || buttonId == SWT.NO || new RouterLoggerPreferences().openDialog(shell, Preference.forName((e).getKey()).getPageDefinition()) != Window.OK) {
					logger.log(Level.SEVERE, e.toString(), e);
					return;
				}
			}
		}
		while (getReader() == null || getWriter() == null);
	}

	@Override
	public void close() {
		new DisplayThreadExecutor(shell).execute(new Runnable() {
			@Override
			public void run() {
				shell.dispose();
			}
		});
	}

	@Override
	protected boolean setStatus(Status status) {
		final boolean update = super.setStatus(status);
		if (update && trayIcon != null) {
			trayIcon.updateTrayItem(status);
			if (Status.WARNING.equals(getCurrentStatus().getStatus())) {
				trayIcon.setShowToolTip(true);
			}
		}
		return update;
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

	public StyledTextConsole getConsole() {
		return console;
	}

	public TrayIcon getTrayIcon() {
		return trayIcon;
	}

}
