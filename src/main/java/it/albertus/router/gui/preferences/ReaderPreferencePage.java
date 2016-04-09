package it.albertus.router.gui.preferences;

import it.albertus.router.resources.Resources;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;

public class ReaderPreferencePage extends BasePreferencePage {

	@Override
	protected void createFieldEditors() {
		final Label header = new Label(getFieldEditorParent(), SWT.WRAP);
		header.setText(Resources.get("lbl.preferences.reader.header"));
		GridDataFactory.fillDefaults().span(Integer.MAX_VALUE, 1).applyTo(header);
		final Label separator = new Label(getFieldEditorParent(), SWT.HORIZONTAL | SWT.SEPARATOR);
		GridDataFactory.fillDefaults().span(Integer.MAX_VALUE, 1).applyTo(separator);
		super.createFieldEditors();
	}

	@Override
	protected Page getPage() {
		return Page.READER;
	}

}
