package it.albertus.router.gui.preference;

import it.albertus.router.gui.Images;
import it.albertus.router.resources.Resources;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

public class ConfigurationDialog extends PreferenceDialog {

	private final Point minimumPageSize = new Point(425, 400);

	public ConfigurationDialog(final Shell parentShell, final PreferenceManager manager) {
		super(parentShell, manager);
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Resources.get("lbl.preferences.title"));
		newShell.setImages(Images.MAIN_ICONS);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);

		final Button okButton = getButton(IDialogConstants.OK_ID);
		okButton.setText(Resources.get("lbl.button.ok"));

		final Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
		cancelButton.setText(Resources.get("lbl.button.cancel"));
	}

	@Override
	protected Layout getPageLayout() {
		return new PageLayout();
	}

	@Override
	public void setMinimumPageSize(final int minWidth, final int minHeight) {
		super.setMinimumPageSize(minWidth, minHeight);
		minimumPageSize.x = minWidth;
		minimumPageSize.y = minHeight;
	}

	@Override
	public void setMinimumPageSize(final Point size) {
		super.setMinimumPageSize(size);
		minimumPageSize.x = size.x;
		minimumPageSize.y = size.y;
	}

	private class PageLayout extends Layout {
		@Override
		public Point computeSize(final Composite composite, final int wHint, final int hHint, final boolean force) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
				return new Point(wHint, hHint);
			}
			int x = minimumPageSize.x;
			int y = minimumPageSize.y;

			if (getCurrentPage() != null) {
				final Point size = getCurrentPage().computeSize();
				x = Math.max(x, size.x);
				y = Math.max(y, size.y);
			}

			if (wHint != SWT.DEFAULT) {
				x = wHint;
			}
			if (hHint != SWT.DEFAULT) {
				y = hHint;
			}
			return new Point(x, y);
		}

		@Override
		public void layout(final Composite composite, final boolean force) {
			final Rectangle rect = composite.getClientArea();
			final Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++) {
				children[i].setSize(rect.width, rect.height);
			}
		}
	}

}
