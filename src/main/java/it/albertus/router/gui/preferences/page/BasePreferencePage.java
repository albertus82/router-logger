package it.albertus.router.gui.preferences.page;

import it.albertus.router.gui.preferences.Preference;
import it.albertus.router.gui.preferences.field.FormattedStringFieldEditor;
import it.albertus.router.gui.preferences.field.ScaleFormattedIntegerFieldEditor;
import it.albertus.router.gui.preferences.field.ThresholdsFieldEditor;
import it.albertus.router.resources.Resources;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
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

		if (StringFieldEditor.class.equals(clazz) || FormattedStringFieldEditor.class.equals(clazz)) {
			final FormattedStringFieldEditor sfe = new FormattedStringFieldEditor(name, labelText, parent);
			if (preference.getFieldEditorData() instanceof Integer) {
				sfe.setTextLimit((Integer) preference.getFieldEditorData());
			}
			fe = sfe;
		}
		else if (IntegerFieldEditor.class.equals(clazz)) {
			if (preference.getFieldEditorData() instanceof Integer) {
				fe = new IntegerFieldEditor(name, labelText, parent, (Integer) preference.getFieldEditorData());
			}
			else {
				fe = new IntegerFieldEditor(name, labelText, parent);
			}
		}
		else if (DirectoryFieldEditor.class.equals(clazz)) {
			fe = new DirectoryFieldEditor(name, labelText, parent);
		}
		else if (BooleanFieldEditor.class.equals(clazz)) {
			fe = new BooleanFieldEditor(name, labelText, parent);
		}
		else if (ComboFieldEditor.class.equals(clazz)) {
			fe = new ComboFieldEditor(name, labelText, (String[][]) preference.getFieldEditorData(), parent);
		}
		else if (ScaleFieldEditor.class.equals(clazz)) {
			int[] data = (int[]) preference.getFieldEditorData();
			fe = new ScaleFieldEditor(name, labelText, parent, data[0], data[1], data[2], data[3]);
		}
		else if (ScaleFormattedIntegerFieldEditor.class.equals(clazz)) {
			int[] data = (int[]) preference.getFieldEditorData();
			fe = new ScaleFormattedIntegerFieldEditor(name, labelText, parent, data[0], data[1], data[2], data[3]);
		}
		else if (ThresholdsFieldEditor.class.equals(clazz)) {
			fe = new ThresholdsFieldEditor(name, labelText, parent);
		}

		return fe;
	}

}
