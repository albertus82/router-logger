package it.albertus.router.gui.preference.field;

import org.eclipse.swt.widgets.Composite;

public class DatabaseComboFieldEditor extends ValidatedComboFieldEditor {

	public DatabaseComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
	}

	@Override
	protected void refreshValidState() {
		try {
			if (getValue() != null && !getValue().isEmpty()) {
				Class.forName(getValue());
			}
			setValid(true);
		}
		catch (final Throwable throwable) {
			setValid(false);
		}
	}

}
