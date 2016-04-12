package it.albertus.router.gui.preference.field;

import it.albertus.router.gui.TextFormatter;
import it.albertus.router.resources.Resources;

import org.eclipse.swt.widgets.Composite;

public class IterationsComboFieldEditor extends ValidatedComboFieldEditor {

	public IterationsComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, new String[][] { { Resources.get("lbl.preferences.iterations.infinite"), "0" } }, parent);
		getComboBoxControl().setTextLimit(Integer.toString(Integer.MAX_VALUE).length() - 1);
	}

	@Override
	protected boolean checkState() {
		if (getValue() != null) {
			try {
				final int number = Integer.parseInt(getValue());
				if (number >= 0 && number <= Integer.MAX_VALUE) {
					return true;
				}
			}
			catch (final NumberFormatException nfe) {}
		}
		return false;
	}

	@Override
	public String getErrorMessage() {
		return Resources.get("err.preferences.iterations");
	}

	@Override
	protected void doLoad() {
		super.doLoad();
		setToolTipText(getNameForValue(Integer.toString(getPreferenceStore().getDefaultInt(getPreferenceName()))));
		updateFontStyle();
	}

	@Override
	protected void updateValue() {
		super.updateValue();
		updateFontStyle();
	}

	protected void setToolTipText(final String defaultValue) {
		if (getComboBoxControl() != null && !getComboBoxControl().isDisposed() && defaultValue != null && !defaultValue.isEmpty()) {
			getComboBoxControl().setToolTipText(Resources.get("lbl.preferences.default.value", defaultValue));
		}
	}

	protected void updateFontStyle() {
		final String defaultValue = Integer.toString(getPreferenceStore().getDefaultInt(getPreferenceName()));
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

}
