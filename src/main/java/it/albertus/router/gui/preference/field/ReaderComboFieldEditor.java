package it.albertus.router.gui.preference.field;

import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Composite;

import it.albertus.jface.preference.field.ValidatedComboFieldEditor;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.reader.IReader;
import it.albertus.router.resources.Messages;
import it.albertus.util.logging.LoggerFactory;

public class ReaderComboFieldEditor extends ValidatedComboFieldEditor {

	private static final Logger logger = LoggerFactory.getLogger(ReaderComboFieldEditor.class);

	public ReaderComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
		setErrorMessage(Messages.get("err.preferences.combo.class.reader.invalid"));
	}

	@Override
	protected boolean checkState() {
		try {
			final Class<?> readerClass = Class.forName(RouterLoggerEngine.getReaderClassName(getValue()), false, this.getClass().getClassLoader());
			if (IReader.class.isAssignableFrom(readerClass) && !Modifier.isAbstract(readerClass.getModifiers())) {
				return true;
			}
			else {
				setErrorMessage(Messages.get("err.preferences.combo.class.reader.invalid"));
				return false;
			}
		}
		catch (final Exception e) {
			logger.log(Level.FINE, e.toString(), e);
			setErrorMessage(Messages.get("err.preferences.combo.class.reader.missing"));
			return false;
		}
		catch (final LinkageError e) {
			logger.log(Level.FINE, e.toString(), e);
			setErrorMessage(Messages.get("err.preferences.combo.class.reader.missing"));
			return false;
		}
	}

}
