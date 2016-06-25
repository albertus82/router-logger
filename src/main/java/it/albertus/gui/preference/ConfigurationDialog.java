package it.albertus.gui.preference;

import it.albertus.router.gui.Images;
import it.albertus.router.resources.Resources;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Button;
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

		final Button okButton = getButton(IDialogConstants.OK_ID);
		okButton.setText(Resources.get("lbl.button.ok"));

		final Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
		cancelButton.setText(Resources.get("lbl.button.cancel"));
	}

}
