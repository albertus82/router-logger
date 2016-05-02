package it.albertus.router.gui.preference.page;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.gui.preference.Preference;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.util.NewLine;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
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
				addField(preference.createFieldEditor(getFieldEditorParent()));
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
