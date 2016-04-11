package it.albertus.router.gui.preference.field;

import it.albertus.router.resources.Resources;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class LocalizedDirectoryFieldEditor extends DirectoryFieldEditor {

	private boolean localized = false;

	public LocalizedDirectoryFieldEditor() {
		super();
		setErrorMessage(Resources.get("err.preferences.directory"));
	}

	public LocalizedDirectoryFieldEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent);
		setErrorMessage(Resources.get("err.preferences.directory"));
	}

	@Override
	protected Button getChangeControl(final Composite parent) {
		final Button browseButton = super.getChangeControl(parent);
		if (!localized) {
			browseButton.setText(Resources.get("lbl.preferences.directory.button.browse"));
			localized = true;
		}
		return browseButton;
	}

}
