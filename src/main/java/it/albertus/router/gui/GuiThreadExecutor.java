package it.albertus.router.gui;

import it.albertus.router.util.Logger;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

/** Executes code in the user-interface thread. */
public abstract class GuiThreadExecutor {

	private final Widget widget;

	public GuiThreadExecutor(final Widget widget) {
		this.widget = widget;
	}

	public final Widget getWidget() {
		return widget;
	}

	protected abstract void run();

	protected void onError(final Exception exception) {
		if (!(exception instanceof SWTException)) {
			final Logger logger = Logger.getInstance();
			if (logger != null && exception != null) {
				logger.log(exception);
			}
		}
	}

	public void start() {
		if (widget != null) {
			try {
				if (widget.getDisplay().equals(Display.getCurrent())) {
					run();
				}
				else {
					widget.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							try {
								GuiThreadExecutor.this.run();
							}
							catch (final Exception exception) {
								onError(exception);
							}
						}
					});
				}
			}
			catch (final Exception exception) {
				onError(exception);
			}
		}
	}

}