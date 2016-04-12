package it.albertus.router.gui.preference.field;

import org.eclipse.swt.widgets.Composite;

public abstract class ValidatedComboFieldEditor extends EditableComboFieldEditor {

	private boolean valid = true;
	private String errorMessage = null;

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

	protected void setValid(boolean valid) {
		this.valid = valid;
	}

	@Override
	protected void refreshValidState() {
		setValid(checkState());
		if (getErrorMessage() != null && !getErrorMessage().isEmpty()) {
			if (isValid()) {
				clearErrorMessage();
			}
			else {
				showErrorMessage(getErrorMessage());
			}
		}
	}

	protected abstract boolean checkState();

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
