package it.albertus.router.gui.preference.field;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.router.writer.Writer;

import org.eclipse.swt.widgets.Composite;

public class WriterComboFieldEditor extends ValidatedComboFieldEditor {

	public WriterComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
		setErrorMessage(Resources.get("err.preferences.combo.class.writer.invalid"));
	}

	@Override
	protected boolean checkState() {
		try {
			if (Writer.class.isAssignableFrom(Class.forName(RouterLoggerEngine.getWriterClassName(getValue())))) {
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
