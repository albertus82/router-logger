package it.albertus.router.gui.preference.field;

import it.albertus.router.gui.preference.field.listener.LowercaseVerifyListener;
import it.albertus.router.resources.Resources;

import org.eclipse.swt.widgets.Composite;

public class IterationsComboFieldEditor extends ValidatedComboFieldEditor {

	public IterationsComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, new String[][] { { Resources.get("lbl.preferences.iterations.infinite"), "0" } }, parent);
		getComboBoxControl().setTextLimit(Integer.toString(Integer.MAX_VALUE).length() - 1);
		getComboBoxControl().addVerifyListener(new LowercaseVerifyListener());
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
	protected String getDefaultValue() {
		return Integer.toString(getPreferenceStore().getDefaultInt(getPreferenceName()));
	}

	/** Trims value and tries to convert it to integer (removes trailing zeros). */
	@Override
	protected String cleanValue(String value) {
		value = super.cleanValue(value);
		try {
			value = Integer.valueOf(value).toString();
		}
		catch (final Exception exception) {}
		return value;
	}

	/** Trims combo text and converts it to integer (removes trailing zeros). */
	@Override
	protected void cleanComboText() {
		final String oldText = getComboBoxControl().getText();
		String newText = oldText.trim().toLowerCase();
		try {
			newText = getNameForValue(Integer.valueOf(newText).toString());
		}
		catch (final Exception exception) {}
		if (!newText.equals(oldText)) {
			getComboBoxControl().setText(newText);
		}
	}

	@Override
	public String getValue() {
		try {
			return Integer.valueOf(super.getValue()).toString();
		}
		catch (final Exception exception) {
			return super.getValue();
		}
	}

	@Override
	protected void setValue(final String value) {
		try {
			super.setValue(Integer.valueOf(value).toString());
		}
		catch (final Exception exception) {
			super.setValue(value);
		}
	}

}
