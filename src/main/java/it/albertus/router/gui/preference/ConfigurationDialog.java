package it.albertus.router.gui.preference;

import it.albertus.router.gui.Images;
import it.albertus.router.resources.Resources;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;

public class ConfigurationDialog extends PreferenceDialog {

	public ConfigurationDialog(final Shell parentShell, final PreferenceManager manager) {
		super(parentShell, manager);
	}

	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Resources.get("lbl.preferences.title"));
		newShell.setImages(Images.MAIN_ICONS);
	}

}
