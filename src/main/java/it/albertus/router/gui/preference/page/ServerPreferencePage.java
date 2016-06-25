package it.albertus.router.gui.preference.page;

import it.albertus.jface.TextFormatter;
import it.albertus.router.resources.Resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class ServerPreferencePage extends BasePreferencePage {

	@Override
	protected Control createHeader() {
		final Label header = new Label(getFieldEditorParent(), SWT.WRAP);
		TextFormatter.setBoldFontStyle(header);
		header.setText(Resources.get("lbl.preferences.server.header"));
		return header;
	}

	public static String[][] getLogComboOptions() {
		final int length = 4;
		final String[][] options = new String[length][2];
		for (int index = 0; index < length; index++) {
			options[index][1] = Integer.toString(index);
			options[index][0] = Resources.get("lbl.preferences.server.log.request." + index);
		}
		return options;
	}

}
