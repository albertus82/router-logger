package it.albertus.router.gui.preference.field;

import it.albertus.router.gui.TextFormatter;
import it.albertus.router.resources.Resources;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class FormattedDirectoryFieldEditor extends DirectoryFieldEditor {

	private boolean localized; // becomes true only after super constructors!

	public FormattedDirectoryFieldEditor() {
		super();
		localized = true;
		setErrorMessage(Resources.get("err.preferences.directory"));
	}

	public FormattedDirectoryFieldEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent);
		localized = true;
		setErrorMessage(Resources.get("err.preferences.directory"));
	}

	@Override
	protected Button getChangeControl(final Composite parent) {
		final Button browseButton = super.getChangeControl(parent);
		if (!localized) { // enters only when called from super constructors!
			browseButton.setText(Resources.get("lbl.preferences.directory.button.browse"));
		}
		return browseButton;
	}

	@Override
	protected void doLoad() {
		super.doLoad();
		setToolTipText(getPreferenceStore().getDefaultString(getPreferenceName()));
		updateFontStyle();
	}

	@Override
	protected void valueChanged() {
		super.valueChanged();
		updateFontStyle();
	}

	protected void setToolTipText(final String defaultValue) {
		if (getTextControl() != null && !getTextControl().isDisposed() && defaultValue != null && !defaultValue.isEmpty()) {
			getTextControl().setToolTipText(Resources.get("lbl.preferences.default.value", defaultValue));
		}
	}

	protected void updateFontStyle() {
		final String defaultValue = getPreferenceStore().getDefaultString(getPreferenceName());
		if (defaultValue != null && !defaultValue.isEmpty()) {
			TextFormatter.updateFontStyle(getTextControl(), defaultValue);
		}
	}

}
