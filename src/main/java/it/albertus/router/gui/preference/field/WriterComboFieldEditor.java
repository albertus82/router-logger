package it.albertus.router.gui.preference.field;

import it.albertus.router.engine.RouterLoggerEngine;

import org.eclipse.swt.widgets.Composite;

public class WriterComboFieldEditor extends ValidatedComboFieldEditor {

	public WriterComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
	}

	@Override
	protected void refreshValidState() {
		try {
			Class.forName(RouterLoggerEngine.getWriterClassName(getValue()));
			setValid(true);
		}
		catch (final Throwable throwable) {
			setValid(false);
		}
	}

}
