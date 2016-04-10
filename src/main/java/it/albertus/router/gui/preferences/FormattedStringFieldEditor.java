package it.albertus.router.gui.preferences;

import it.albertus.router.resources.Resources;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class FormattedStringFieldEditor extends StringFieldEditor {

	private final TextFormatter formatter;

	public FormattedStringFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
		formatter = new TextFormatter(getTextControl());
	}

	@Override
	protected void doLoad() {
		super.doLoad();
		final String defaultValue = getPreferenceStore().getDefaultString(getPreferenceName());
		setToolTipText(defaultValue);
		updateFontStyle();
	}

	protected void setToolTipText(final String defaultValue) {
		if (getTextControl() != null && defaultValue != null && !defaultValue.isEmpty()) {
			getTextControl().setToolTipText(Resources.get("lbl.preferences.default.value", defaultValue));
		}
	}

	@Override
	protected void valueChanged() {
		super.valueChanged();
		updateFontStyle();
	}

	protected void updateFontStyle() {
		final String defaultValue = getPreferenceStore().getDefaultString(getPreferenceName());
		if (formatter != null && defaultValue != null && !defaultValue.isEmpty()) {
			formatter.updateFontStyle(defaultValue);
		}
	}

}
