package it.albertus.routerlogger.gui.preference;

import it.albertus.jface.preference.FieldEditorDetails;
import it.albertus.jface.preference.FieldEditorFactory;
import it.albertus.routerlogger.gui.preference.field.DatabaseComboFieldEditor;
import it.albertus.routerlogger.gui.preference.field.ReaderComboFieldEditor;
import it.albertus.routerlogger.gui.preference.field.ThresholdsListEditor;
import it.albertus.routerlogger.gui.preference.field.WriterComboFieldEditor;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

public class RouterLoggerFieldEditorFactory extends FieldEditorFactory {

	@Override
	public FieldEditor createFieldEditor(final String name, final String label, final Composite parent, final FieldEditorDetails details) {
		final Class<? extends FieldEditor> type = details.getFieldEditorClass();
		if (DatabaseComboFieldEditor.class.equals(type)) {
			return new DatabaseComboFieldEditor(name, label, details.getLabelsAndValues().toArray(), parent);
		}
		if (ReaderComboFieldEditor.class.equals(type)) {
			return new ReaderComboFieldEditor(name, label, details.getLabelsAndValues().toArray(), parent);
		}
		if (ThresholdsListEditor.class.equals(type)) {
			return new ThresholdsListEditor(name, label, parent);
		}
		if (WriterComboFieldEditor.class.equals(type)) {
			return new WriterComboFieldEditor(name, label, details.getLabelsAndValues().toArray(), parent);
		}
		return super.createFieldEditor(name, label, parent, details);
	}

}
