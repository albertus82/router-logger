package it.albertus.router.gui.preference.field;

import it.albertus.jface.preference.field.ValidatedComboFieldEditor;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.reader.Reader;
import it.albertus.router.resources.Messages;

import java.lang.reflect.Modifier;

import org.eclipse.swt.widgets.Composite;

public class ReaderComboFieldEditor extends ValidatedComboFieldEditor {

	public ReaderComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
		setErrorMessage(Messages.get("err.preferences.combo.class.reader.invalid"));
	}

	@Override
	protected boolean checkState() {
		try {
			final Class<?> readerClass = Class.forName(RouterLoggerEngine.getReaderClassName(getValue()), false, this.getClass().getClassLoader());
			if (Reader.class.isAssignableFrom(readerClass) && !Modifier.isAbstract(readerClass.getModifiers())) {
				return true;
			}
			else {
				setErrorMessage(Messages.get("err.preferences.combo.class.reader.invalid"));
				return false;
			}
		}
		catch (final Throwable throwable) {
			setErrorMessage(Messages.get("err.preferences.combo.class.reader.missing"));
			return false;
		}
	}

}
