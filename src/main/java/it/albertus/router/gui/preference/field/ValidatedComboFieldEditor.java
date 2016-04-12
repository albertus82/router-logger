package it.albertus.router.gui.preference.field;

import it.albertus.router.gui.TextFormatter;
import it.albertus.router.resources.Resources;

import org.eclipse.swt.widgets.Composite;

public abstract class ValidatedComboFieldEditor extends EditableComboFieldEditor {

	private boolean valid = true;
	private String errorMessage = null;

	public ValidatedComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
	}

	protected abstract boolean checkState();

	@Override
	protected void updateValue() {
		super.updateValue();
		boolean oldValue = valid;
		refreshValidState();
		updateFontStyle();
		fireValueChanged(IS_VALID, oldValue, valid);
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	protected void setValid(final boolean valid) {
		this.valid = valid;
	}

	@Override
	protected void refreshValidState() {
		setValid(checkState());
		final String errorMessage = getErrorMessage();
		if (errorMessage != null && !errorMessage.isEmpty()) {
			if (isValid()) {
				clearErrorMessage();
			}
			else {
				showErrorMessage(errorMessage);
			}
		}
	}

	@Override
	protected void doLoad() {
		super.doLoad();
		setToolTipText(getNameForValue(getDefaultValue()));
		updateFontStyle();
	}

	@Override
	protected void doLoadDefault() {
		super.doLoadDefault();
		updateFontStyle();
	}

	protected String getDefaultValue() {
		return getPreferenceStore().getDefaultString(getPreferenceName());
	}

	protected void setToolTipText(final String defaultValue) {
		if (getComboBoxControl() != null && !getComboBoxControl().isDisposed() && defaultValue != null && !defaultValue.isEmpty()) {
			getComboBoxControl().setToolTipText(Resources.get("lbl.preferences.default.value", defaultValue));
		}
	}

	protected void updateFontStyle() {
		final String defaultValue = getDefaultValue();
		if (defaultValue != null && !defaultValue.isEmpty()) {
			TextFormatter.updateFontStyle(getComboBoxControl(), defaultValue, getValue());
		}
	}

	protected String getNameForValue(final String value) {
		for (int i = 0; i < getEntryNamesAndValues().length; i++) {
			final String[] entry = getEntryNamesAndValues()[i];
			if (value.equals(entry[1])) {
				return entry[0];
			}
		}
		return value; // Name not present in the array.
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(final String message) {
		this.errorMessage = message;
	}

}
