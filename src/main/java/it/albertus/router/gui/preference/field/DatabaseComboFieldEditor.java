package it.albertus.router.gui.preference.field;

import it.albertus.jface.preference.field.ValidatedComboFieldEditor;
import it.albertus.jface.preference.field.listener.TrimVerifyListener;
import it.albertus.router.resources.Resources;

import java.sql.Driver;

import org.eclipse.swt.widgets.Composite;

public class DatabaseComboFieldEditor extends ValidatedComboFieldEditor {

	public DatabaseComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
		getComboBoxControl().addVerifyListener(new TrimVerifyListener());
		setErrorMessage(Resources.get("err.preferences.combo.class.database.invalid"));
	}

	@Override
	protected boolean checkState() {
		if (getValue() != null && !getValue().isEmpty()) {
			try {
				final Class<?> driverClass = Class.forName(getValue(), false, this.getClass().getClassLoader());
				if (Driver.class.isAssignableFrom(driverClass) && !Driver.class.equals(driverClass)) {
					return true;
				}
				else {
					setErrorMessage(Resources.get("err.preferences.combo.class.database.invalid"));
					return false;
				}
			}
			catch (final Throwable throwable) {
				setErrorMessage(Resources.get("err.preferences.combo.class.database.missing"));
				return false;
			}
		}
		else {
			return true;
		}
	}

}
