package it.albertus.router.gui.preference.field;

import it.albertus.router.resources.Resources;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class FormattedIntegerFieldEditor extends IntegerFieldEditor {

	public FormattedIntegerFieldEditor(final String name, final String labelText, final Composite parent, final int textLimit) {
		super(name, labelText, parent, textLimit);
	}

	public FormattedIntegerFieldEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	protected void doLoad() {
		super.doLoad();
		setToolTipText(getPreferenceStore().getDefaultInt(getPreferenceName()));
		updateFontStyle();
	}

	protected void setToolTipText(final int defaultValue) {
		if (getTextControl() != null && !getTextControl().isDisposed() && defaultValue != 0) {
			getTextControl().setToolTipText(Resources.get("lbl.preferences.default.value", defaultValue));
		}
	}

	@Override
	protected void valueChanged() {
		super.valueChanged();
		updateFontStyle();
	}

	protected void updateFontStyle() {
		final int defaultValue = getPreferenceStore().getDefaultInt(getPreferenceName());
		TextFormatter.updateFontStyle(getTextControl(), defaultValue);
	}

}
