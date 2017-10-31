package it.albertus.routerlogger.gui.csv2sql;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.routerlogger.resources.Messages;
import it.albertus.util.Version;
import it.albertus.util.logging.LoggerFactory;

public class CsvToSqlConverterApp {

	private static final Logger logger = LoggerFactory.getLogger(CsvToSqlConverterApp.class);

	public static void main(final String... args) {
		Display.setAppName(Messages.get("lbl.csv2sql.dialog.title"));
		Display.setAppVersion(Version.getInstance().getNumber());
		final Display display = Display.getDefault();
		final Shell shell = new Shell(display, SWT.RESIZE | SWT.MIN);
		try {
			new CsvToSqlConverterGui(shell).open();
			while (!shell.isDisposed()) {
				if (!display.isDisposed() && !display.readAndDispatch()) {
					display.sleep();
				}
			}
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
			if (!display.isDisposed() && !shell.isDisposed()) {
				EnhancedErrorDialog.openError(shell, shell.getText(), e.toString(), IStatus.ERROR, e, shell.getDisplay().getSystemImage(SWT.ICON_ERROR));
			}
		}
		finally {
			display.dispose();
		}
	}

}
