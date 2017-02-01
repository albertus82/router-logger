package it.albertus.router.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Messages;

public class CloseDialog {

	public static class Defaults {
		public static final boolean CONFIRM_CLOSE = false;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private final MessageBox messageBox;

	private CloseDialog(final Shell shell) {
		messageBox = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
		messageBox.setText(Messages.get("msg.confirm.close.text"));
		messageBox.setMessage(Messages.get("msg.confirm.close.message"));
	}

	public static int open(final Shell shell) {
		return new CloseDialog(shell).messageBox.open();
	}

	public static boolean mustShow() {
		return RouterLoggerConfiguration.getInstance().getBoolean("gui.confirm.close", Defaults.CONFIRM_CLOSE);
	}

}
