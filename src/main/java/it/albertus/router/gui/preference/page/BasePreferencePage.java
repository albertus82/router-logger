package it.albertus.router.gui.preference.page;

import it.albertus.router.gui.preference.Preference;
import it.albertus.router.gui.preference.field.DatabaseComboFieldEditor;
import it.albertus.router.gui.preference.field.EditableComboFieldEditor;
import it.albertus.router.gui.preference.field.FormattedIntegerFieldEditor;
import it.albertus.router.gui.preference.field.FormattedStringFieldEditor;
import it.albertus.router.gui.preference.field.LocalizedDirectoryFieldEditor;
import it.albertus.router.gui.preference.field.ReaderComboFieldEditor;
import it.albertus.router.gui.preference.field.ScaleFormattedIntegerFieldEditor;
import it.albertus.router.gui.preference.field.ThresholdsFieldEditor;
import it.albertus.router.gui.preference.field.WriterComboFieldEditor;
import it.albertus.router.resources.Resources;
import it.albertus.util.NewLine;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public abstract class BasePreferencePage extends FieldEditorPreferencePage {

	protected abstract Page getPage();

	public BasePreferencePage() {
		super(GRID);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		final Button defaultsButton = getDefaultsButton();
		defaultsButton.setText(Resources.get("lbl.preferences.button.defaults"));
		defaultsButton.setToolTipText(Resources.get("lbl.preferences.button.defaults.tooltip"));

		final Button applyButton = getApplyButton();
		applyButton.setText(Resources.get("lbl.button.apply"));
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
		GridDataFactory.fillDefaults().span(Integer.MAX_VALUE, 1).grab(true, false).applyTo(separator);
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
		else if (IntegerFieldEditor.class.equals(clazz) || FormattedIntegerFieldEditor.class.equals(clazz)) {
			if (preference.getFieldEditorData() instanceof Integer) {
				fe = new FormattedIntegerFieldEditor(name, labelText, parent, (Integer) preference.getFieldEditorData());
			}
			else {
				fe = new FormattedIntegerFieldEditor(name, labelText, parent);
			}
		}
		else if (DirectoryFieldEditor.class.equals(clazz) || LocalizedDirectoryFieldEditor.class.equals(clazz)) {
			fe = new LocalizedDirectoryFieldEditor(name, labelText, parent);
		}
		else if (BooleanFieldEditor.class.equals(clazz)) {
			fe = new BooleanFieldEditor(name, labelText, parent);
		}
		else if (ComboFieldEditor.class.equals(clazz)) {
			fe = new ComboFieldEditor(name, labelText, (String[][]) preference.getFieldEditorData(), parent);
		}
		else if (EditableComboFieldEditor.class.equals(clazz)) {
			fe = new EditableComboFieldEditor(name, labelText, (String[][]) preference.getFieldEditorData(), parent);
		}
		else if (ReaderComboFieldEditor.class.equals(clazz)) {
			fe = new ReaderComboFieldEditor(name, labelText, (String[][]) preference.getFieldEditorData(), parent);
		}
		else if (WriterComboFieldEditor.class.equals(clazz)) {
			fe = new WriterComboFieldEditor(name, labelText, (String[][]) preference.getFieldEditorData(), parent);
		}
		else if (DatabaseComboFieldEditor.class.equals(clazz)) {
			fe = new DatabaseComboFieldEditor(name, labelText, (String[][]) preference.getFieldEditorData(), parent);
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

	public static String[][] getNewLineComboOptions() {
		final int length = NewLine.values().length;
		final String[][] options = new String[length][2];
		for (int index = 0; index < length; index++) {
			options[index][0] = options[index][1] = NewLine.values()[index].name();
		}
		return options;
	}

}
