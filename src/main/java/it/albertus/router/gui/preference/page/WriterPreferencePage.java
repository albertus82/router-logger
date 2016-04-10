package it.albertus.router.gui.preference.page;

import it.albertus.router.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class WriterPreferencePage extends BasePreferencePage {

	@Override
	protected Control createHeader() {
		final Label header = new Label(getFieldEditorParent(), SWT.WRAP);
		header.setText(Resources.get("lbl.preferences.writer.header"));
		return header;
	}

	@Override
	protected Page getPage() {
		return Page.WRITER;
	}

}
