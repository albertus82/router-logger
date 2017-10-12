package it.albertus.routerlogger.gui.preference.field;

import java.sql.Driver;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Composite;

import it.albertus.jface.listener.TrimVerifyListener;
import it.albertus.jface.preference.field.ValidatedComboFieldEditor;
import it.albertus.routerlogger.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

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
				logger.log(Level.FINE, e.toString(), e);
				setErrorMessage(Messages.get("err.preferences.combo.class.database.missing"));
				return false;
			}
			catch (final LinkageError e) {
				logger.log(Level.FINE, e.toString(), e);
				setErrorMessage(Messages.get("err.preferences.combo.class.database.missing"));
				return false;
			}
		}
		else {
			return true;
		}
	}

}
