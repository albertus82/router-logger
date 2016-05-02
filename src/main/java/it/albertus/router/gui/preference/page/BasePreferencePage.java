package it.albertus.router.gui.preference.page;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.gui.preference.FieldEditorData;
import it.albertus.router.gui.preference.Preference;
import it.albertus.router.gui.preference.field.ComboFieldEditor;
import it.albertus.router.gui.preference.field.DatabaseComboFieldEditor;
import it.albertus.router.gui.preference.field.EditableComboFieldEditor;
import it.albertus.router.gui.preference.field.FormattedComboFieldEditor;
import it.albertus.router.gui.preference.field.FormattedDirectoryFieldEditor;
import it.albertus.router.gui.preference.field.FormattedIntegerFieldEditor;
import it.albertus.router.gui.preference.field.FormattedStringFieldEditor;
import it.albertus.router.gui.preference.field.IterationsComboFieldEditor;
import it.albertus.router.gui.preference.field.PasswordFieldEditor;
import it.albertus.router.gui.preference.field.ReaderComboFieldEditor;
import it.albertus.router.gui.preference.field.ScaleIntegerFieldEditor;
import it.albertus.router.gui.preference.field.ThresholdsFieldEditor;
import it.albertus.router.gui.preference.field.WrapStringFieldEditor;
import it.albertus.router.gui.preference.field.WriterComboFieldEditor;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.util.NewLine;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public abstract class BasePreferencePage extends FieldEditorPreferencePage {

	private Control header;

	public BasePreferencePage() {
		super(GRID);
	}

	public Page getPage() {
		return Page.forClass(getClass());
	}

	@Override
	protected void performApply() {
		super.performApply();

		// Save configuration file...
		final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

		OutputStream configurationOutputStream = null;
		try {
			configurationOutputStream = configuration.openConfigurationOutputStream();
			((PreferenceStore) getPreferenceStore()).save(configurationOutputStream, null);
		}
		catch (IOException ioe) {
			Logger.getInstance().log(ioe);
		}
		finally {
			try {
				configurationOutputStream.close();
			}
			catch (final Exception exception) {}
		}

		// Reload RouterLogger configuration...
		try {
			configuration.reload();
		}
		catch (final Exception exception) {
			Logger.getInstance().log(exception);
		}
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

	@Override
	protected void createFieldEditors() {
		// Header
		header = createHeader();
		if (header != null) {
			GridDataFactory.fillDefaults().span(Integer.MAX_VALUE, 1).applyTo(header);
			addSeparator();
		}

		// Fields
		for (final Preference preference : Preference.values()) {
			if (getPage().equals(preference.getPage())) {
				addField(createFieldEditor(preference));
			}
		}
	}

	/** Viene aggiunto automaticamente un separatore tra il testo e i campi. */
	protected Control createHeader() {
		return null;
	}

	protected void addSeparator() {
		final Label separator = new Label(getFieldEditorParent(), SWT.HORIZONTAL | SWT.SEPARATOR);
		GridDataFactory.fillDefaults().span(Integer.MAX_VALUE, 1).grab(true, false).applyTo(separator);
	}

	protected FieldEditor createFieldEditor(final Preference preference) {
		final FieldEditor fe;
		final String name = preference.getConfigurationKey();
		final String labelText = Resources.get(preference.getLabelKey());
		final FieldEditorData data = preference.getFieldEditorData();
		final Class<? extends FieldEditor> clazz = preference.getFieldEditorClass();

		if (StringFieldEditor.class.equals(clazz) || FormattedStringFieldEditor.class.equals(clazz)) {
			fe = FormattedStringFieldEditor.newInstance(name, labelText, getFieldEditorParent(), data);
		}

		else if (WrapStringFieldEditor.class.equals(clazz)) {
			fe = WrapStringFieldEditor.newInstance(name, labelText, getFieldEditorParent(), data);
		}

		else if (PasswordFieldEditor.class.equals(clazz)) {
			fe = PasswordFieldEditor.newInstance(name, labelText, getFieldEditorParent(), data);
		}

		else if (IntegerFieldEditor.class.equals(clazz) || FormattedIntegerFieldEditor.class.equals(clazz)) {
			fe = FormattedIntegerFieldEditor.newInstance(name, labelText, getFieldEditorParent(), data);
		}

		else if (DirectoryFieldEditor.class.equals(clazz) || FormattedDirectoryFieldEditor.class.equals(clazz)) {
			fe = FormattedDirectoryFieldEditor.newInstance(name, labelText, getFieldEditorParent(), data);
		}

		else if (BooleanFieldEditor.class.equals(clazz)) {
			fe = new BooleanFieldEditor(name, labelText, getFieldEditorParent());
		}

		else if (ComboFieldEditor.class.equals(clazz)) {
			fe = new ComboFieldEditor(name, labelText, data.getComboEntryNamesAndValues(), getFieldEditorParent());
		}

		else if (FormattedComboFieldEditor.class.equals(clazz)) {
			fe = new FormattedComboFieldEditor(name, labelText, data.getComboEntryNamesAndValues(), getFieldEditorParent());
		}

		else if (EditableComboFieldEditor.class.equals(clazz)) {
			fe = new EditableComboFieldEditor(name, labelText, data.getComboEntryNamesAndValues(), getFieldEditorParent());
		}

		else if (ReaderComboFieldEditor.class.equals(clazz)) {
			fe = new ReaderComboFieldEditor(name, labelText, data.getComboEntryNamesAndValues(), getFieldEditorParent());
		}

		else if (WriterComboFieldEditor.class.equals(clazz)) {
			fe = new WriterComboFieldEditor(name, labelText, data.getComboEntryNamesAndValues(), getFieldEditorParent());
		}

		else if (DatabaseComboFieldEditor.class.equals(clazz)) {
			fe = new DatabaseComboFieldEditor(name, labelText, data.getComboEntryNamesAndValues(), getFieldEditorParent());
		}

		else if (IterationsComboFieldEditor.class.equals(clazz)) {
			fe = new IterationsComboFieldEditor(name, labelText, getFieldEditorParent());
		}

		else if (ScaleFieldEditor.class.equals(clazz) || ScaleIntegerFieldEditor.class.equals(clazz)) {
			fe = ScaleIntegerFieldEditor.newInstance(name, labelText, getFieldEditorParent(), data);
		}

		else if (ThresholdsFieldEditor.class.equals(clazz)) {
			fe = new ThresholdsFieldEditor(name, labelText, getFieldEditorParent());
		}

		else {
			throw new IllegalStateException("Unsupported FieldEditor: " + clazz.getName());
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

	public Control getHeader() {
		return header;
	}

}
