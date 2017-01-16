package it.albertus.router.gui.preference.field;

import java.sql.Driver;

import org.eclipse.swt.widgets.Composite;

import it.albertus.jface.listener.TrimVerifyListener;
import it.albertus.jface.preference.field.ValidatedComboFieldEditor;
import it.albertus.router.resources.Messages;
import it.albertus.router.util.Logger;
import it.albertus.router.util.LoggerFactory;

public class DatabaseComboFieldEditor extends ValidatedComboFieldEditor {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseComboFieldEditor.class);

	public DatabaseComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
		getComboBoxControl().addVerifyListener(new TrimVerifyListener());
		setErrorMessage(Messages.get("err.preferences.combo.class.database.invalid"));
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
					setErrorMessage(Messages.get("err.preferences.combo.class.database.invalid"));
					return false;
				}
			}
			catch (final Exception e) {
				logger.debug(e);
				setErrorMessage(Messages.get("err.preferences.combo.class.database.missing"));
				return false;
			}
			catch (final LinkageError le) {
				logger.debug(le);
				setErrorMessage(Messages.get("err.preferences.combo.class.database.missing"));
				return false;
			}
		}
		else {
			return true;
		}
	}

}
