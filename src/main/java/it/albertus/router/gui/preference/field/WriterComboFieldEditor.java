package it.albertus.router.gui.preference.field;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;

import org.eclipse.swt.widgets.Composite;

public class WriterComboFieldEditor extends ValidatedComboFieldEditor {

	public WriterComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
	}

	@Override
	protected boolean checkState() {
		try {
			Class.forName(RouterLoggerEngine.getWriterClassName(getValue()));
			return true;
		}
		catch (final Throwable throwable) {
			return false;
		}
	}

	@Override
	public String getErrorMessage() {
		return Resources.get("err.preferences.combo.class.writer");
	}

}
