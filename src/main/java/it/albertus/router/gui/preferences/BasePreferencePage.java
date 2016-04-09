package it.albertus.router.gui.preferences;

import it.albertus.router.resources.Resources;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public abstract class BasePreferencePage extends FieldEditorPreferencePage {

	protected abstract Page getPage();

	public BasePreferencePage() {
		super(GRID);
	}

	/** Viene aggiunto automaticamente un separatore tra il testo e i campi. */
	protected Control createHeader() {
		return null;
	}

	/** Non viene inserito alcun separatore tra i campi e il testo. */
	protected Control createFooter() {
		return null;
	}

	protected void addSeparator() {
		final Label separator = new Label(getFieldEditorParent(), SWT.HORIZONTAL | SWT.SEPARATOR);
		GridDataFactory.fillDefaults().span(Integer.MAX_VALUE, 1).applyTo(separator);
	}

	@Override
	protected void createFieldEditors() {
		// Header
		final Control header = createHeader();
		if (header != null) {
			GridDataFactory.fillDefaults().span(Integer.MAX_VALUE, 1).applyTo(header);
			addSeparator();
		}

		// Fields
		for (final Preference preference : Preference.values()) {
			if (this.getPage().equals(preference.getPage())) {
				addField(createFieldEditor(preference));
			}
		}

		// Footer
		final Control footer = createFooter();
		if (footer != null) {
			GridDataFactory.fillDefaults().span(Integer.MAX_VALUE, 1).applyTo(footer);
		}
	}

	protected FieldEditor createFieldEditor(final Preference preference) {
		FieldEditor fe = null;
		final String name = preference.getConfigurationKey();
		final String labelText = Resources.get(preference.getResourceKey());
		final Composite parent = getFieldEditorParent();
		final Class<? extends FieldEditor> clazz = preference.getFieldEditorClass();

		if (clazz.equals(StringFieldEditor.class)) {
			if (preference.getFieldEditorData() instanceof Integer) {
				fe = new StringFieldEditor(name, labelText, (Integer) preference.getFieldEditorData(), parent);
			}
			else {
				fe = new StringFieldEditor(name, labelText, parent);
			}
		}
		else if (clazz.equals(IntegerFieldEditor.class)) {
			if (preference.getFieldEditorData() instanceof Integer) {
				fe = new IntegerFieldEditor(name, labelText, parent, (Integer) preference.getFieldEditorData());
			}
			else {
				fe = new IntegerFieldEditor(name, labelText, parent);
			}
		}
		else if (clazz.equals(DirectoryFieldEditor.class)) {
			fe = new DirectoryFieldEditor(name, labelText, parent);
		}
		else if (clazz.equals(BooleanFieldEditor.class)) {
			fe = new BooleanFieldEditor(name, labelText, parent);
		}
		else if (clazz.equals(ScaleFieldEditor.class)) {
			int[] data = (int[]) preference.getFieldEditorData();
			fe = new ScaleFieldEditor(name, labelText, parent, data[0], data[1], data[2], data[3]);
		}
		else if (clazz.equals(ScaleWithTextFieldEditor.class)) {
			int[] data = (int[]) preference.getFieldEditorData();
			fe = new ScaleWithTextFieldEditor(name, labelText, parent, data[0], data[1], data[2], data[3]);
		}

		return fe;
	}

}
