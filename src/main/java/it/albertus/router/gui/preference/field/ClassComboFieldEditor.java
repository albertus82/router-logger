package it.albertus.router.gui.preference.field;

import org.eclipse.swt.widgets.Composite;

public class ClassComboFieldEditor extends EditableComboFieldEditor {

	private boolean valid = false;

	public ClassComboFieldEditor(String name, String labelText, String[][] entryNamesAndValues, Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
	}

	@Override
	protected void updateValue() {
		super.updateValue();
		refreshValidState();
	}

	@Override
	protected void refreshValidState() {
		valid = true; // TODO Check if class exists.
	}

	@Override
	public boolean isValid() {
		return valid;
	}

}
