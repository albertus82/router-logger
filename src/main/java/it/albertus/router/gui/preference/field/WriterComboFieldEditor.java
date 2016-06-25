package it.albertus.router.gui.preference.field;

import it.albertus.gui.preference.field.ValidatedComboFieldEditor;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.router.writer.Writer;

import java.lang.reflect.Modifier;

import org.eclipse.swt.widgets.Composite;

public class WriterComboFieldEditor extends ValidatedComboFieldEditor {

	public WriterComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
		setErrorMessage(Resources.get("err.preferences.combo.class.writer.invalid"));
	}

	@Override
	protected boolean checkState() {
		try {
			final Class<?> writerClass = Class.forName(RouterLoggerEngine.getWriterClassName(getValue()), false, this.getClass().getClassLoader());
			if (Writer.class.isAssignableFrom(writerClass) && !Modifier.isAbstract(writerClass.getModifiers())) {
				return true;
			}
			else {
				setErrorMessage(Resources.get("err.preferences.combo.class.writer.invalid"));
				return false;
			}
		}
		catch (final Throwable throwable) {
			setErrorMessage(Resources.get("err.preferences.combo.class.writer.missing"));
			return false;
		}
	}

}
