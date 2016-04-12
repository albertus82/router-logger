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

}
