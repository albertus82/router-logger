package it.albertus.router.gui.preference;

import it.albertus.router.gui.Images;
import it.albertus.router.resources.Resources;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class ConfigurationDialog extends PreferenceDialog {

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
		getButton(IDialogConstants.OK_ID).setText(Resources.get("lbl.button.ok"));
		getButton(IDialogConstants.CANCEL_ID).setText(Resources.get("lbl.button.cancel"));
	}

}
