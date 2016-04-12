package it.albertus.router.gui.preference.field;

import org.eclipse.swt.widgets.Composite;

public abstract class ValidatedComboFieldEditor extends EditableComboFieldEditor {

	private boolean valid = true;

	public ValidatedComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
	}

	@Override
	protected void updateValue() {
		super.updateValue();
		boolean oldValue = valid;
		refreshValidState();
		fireValueChanged(IS_VALID, oldValue, valid);
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

}
