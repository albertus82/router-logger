package it.albertus.router.gui.preference.field;

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
	protected String getDefaultValue() {
		return Integer.toString(getPreferenceStore().getDefaultInt(getPreferenceName()));
	}

	@Override
	protected void doLoad() {
		super.doLoad();
		updateComboText();
	}

	@Override
	protected void updateValue() {
		updateComboText();
		super.updateValue();
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

	protected void updateComboText() {
		try {
			final String oldText = getComboBoxControl().getText();
			final String newText = getNameForValue(Integer.valueOf(oldText).toString());
			if (!newText.equals(oldText)) {
				getComboBoxControl().setText(newText);
			}
		}
		catch (final Exception exception) {}
	}

}
