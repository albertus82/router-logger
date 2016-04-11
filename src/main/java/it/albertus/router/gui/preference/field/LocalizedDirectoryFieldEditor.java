package it.albertus.router.gui.preference.field;

import it.albertus.router.resources.Resources;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class LocalizedDirectoryFieldEditor extends DirectoryFieldEditor {

	private boolean localized = false;

	public LocalizedDirectoryFieldEditor() {
		super();
	}

	public LocalizedDirectoryFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	protected Button getChangeControl(Composite parent) {
		final Button browseButton = super.getChangeControl(parent);
		if (!localized) {
			browseButton.setText(Resources.get("lbl.preferences.directory.button.browse"));
			localized = true;
		}
		return browseButton;
	}

}
