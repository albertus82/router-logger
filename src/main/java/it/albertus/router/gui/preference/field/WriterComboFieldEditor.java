package it.albertus.router.gui.preference.field;

import java.lang.reflect.Modifier;

import org.eclipse.swt.widgets.Composite;

import it.albertus.jface.preference.field.ValidatedComboFieldEditor;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Messages;
import it.albertus.router.util.Logger;
import it.albertus.router.util.LoggerFactory;
import it.albertus.router.writer.Writer;

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
			if (Writer.class.isAssignableFrom(writerClass) && !Modifier.isAbstract(writerClass.getModifiers())) {
				return true;
			}
			else {
				setErrorMessage(Messages.get("err.preferences.combo.class.writer.invalid"));
				return false;
			}
		}
		catch (final Exception e) {
			logger.debug(e);
			setErrorMessage(Messages.get("err.preferences.combo.class.writer.missing"));
			return false;
		}
		catch (final LinkageError le) {
			logger.debug(le);
			setErrorMessage(Messages.get("err.preferences.combo.class.writer.missing"));
			return false;
		}
	}

}
