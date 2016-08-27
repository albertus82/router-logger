package it.albertus.router.gui.preference;

import it.albertus.jface.preference.FieldEditorData;
import it.albertus.jface.preference.FieldEditorFactory;
import it.albertus.router.gui.preference.field.DatabaseComboFieldEditor;
import it.albertus.router.gui.preference.field.ReaderComboFieldEditor;
import it.albertus.router.gui.preference.field.ThresholdsFieldEditor;
import it.albertus.router.gui.preference.field.WriterComboFieldEditor;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

public class RouterLoggerFieldEditorFactory extends FieldEditorFactory {

	@Override
	public FieldEditor createFieldEditor(final Class<? extends FieldEditor> type, final String name, final String label, final Composite parent, final FieldEditorData data) {
		if (DatabaseComboFieldEditor.class.equals(type)) {
			return new DatabaseComboFieldEditor(name, label, data.getLabelsAndValues().toArray(), parent);
		}
		if (ReaderComboFieldEditor.class.equals(type)) {
			return new ReaderComboFieldEditor(name, label, data.getLabelsAndValues().toArray(), parent);
		}
		if (ThresholdsFieldEditor.class.equals(type)) {
			return new ThresholdsFieldEditor(name, label, parent);
		}
		if (WriterComboFieldEditor.class.equals(type)) {
			return new WriterComboFieldEditor(name, label, data.getLabelsAndValues().toArray(), parent);
		}
		return super.createFieldEditor(type, name, label, parent, data);
	}

}
