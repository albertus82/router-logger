package it.albertus.router.gui.preference.field;

import it.albertus.router.gui.TextFormatter;
import it.albertus.router.gui.preference.FieldEditorData;
import it.albertus.router.resources.Resources;

import java.util.prefs.Preferences;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class FormattedStringFieldEditor extends StringFieldEditor {

	public static FormattedStringFieldEditor newInstance(final String name, final String labelText, final Composite parent, final FieldEditorData data) {
		final FormattedStringFieldEditor sfe;
		if (data != null && data.getTextWidth() != null) {
			sfe = new FormattedStringFieldEditor(name, labelText, data.getTextWidth(), parent);
		}
		else {
			sfe = new FormattedStringFieldEditor(name, labelText, parent);
		}
		if (data != null) {
			if (data.getEmptyStringAllowed() != null) {
				sfe.setEmptyStringAllowed(data.getEmptyStringAllowed());
			}
			if (data.getTextLimit() != null) {
				sfe.setTextLimit(data.getTextLimit());
			}
		}
		return sfe;
	}

	protected FormattedStringFieldEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent);
		init();
	}

	protected FormattedStringFieldEditor(final String name, final String labelText, final int width, final Composite parent) {
		super(name, labelText, width, parent);
		init();
	}

	protected FormattedStringFieldEditor(final String name, final String labelText, final int width, final int strategy, final Composite parent) {
		super(name, labelText, width, strategy, parent);
		init();
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

	protected void init() {
		setErrorMessage(Resources.get("err.preferences.string"));
		setTextLimit(Preferences.MAX_VALUE_LENGTH);
	}

}
