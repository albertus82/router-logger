package it.albertus.routerlogger.gui.preference.field;

import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Composite;

import it.albertus.jface.preference.field.ValidatedComboFieldEditor;
import it.albertus.routerlogger.engine.RouterLoggerEngine;
import it.albertus.routerlogger.resources.Messages;
import it.albertus.routerlogger.writer.IWriter;
import it.albertus.util.logging.LoggerFactory;

public class WriterComboFieldEditor extends ValidatedComboFieldEditor {

	private static final Logger logger = LoggerFactory.getLogger(WriterComboFieldEditor.class);

	public WriterComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
		setErrorMessage(Messages.get("err.preferences.combo.class.writer.invalid"));
	}

	@Override
	protected boolean checkState() {
		try {
			final Class<?> writerClass = Class.forName(RouterLoggerEngine.getWriterClassName(getValue()), false, this.getClass().getClassLoader());
			if (IWriter.class.isAssignableFrom(writerClass) && !Modifier.isAbstract(writerClass.getModifiers())) {
				return true;
			}
			else {
				setErrorMessage(Messages.get("err.preferences.combo.class.writer.invalid"));
				return false;
			}
		}
		catch (final Exception e) {
			logger.log(Level.FINE, e.toString(), e);
			setErrorMessage(Messages.get("err.preferences.combo.class.writer.missing"));
			return false;
		}
		catch (final LinkageError e) {
			logger.log(Level.FINE, e.toString(), e);
			setErrorMessage(Messages.get("err.preferences.combo.class.writer.missing"));
			return false;
		}
	}

}
