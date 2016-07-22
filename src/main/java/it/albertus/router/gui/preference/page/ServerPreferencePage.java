package it.albertus.router.gui.preference.page;

import it.albertus.jface.TextFormatter;
import it.albertus.jface.preference.LocalizedComboEntryNamesAndValues;
import it.albertus.router.resources.Resources;
import it.albertus.util.Localized;

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

	public static LocalizedComboEntryNamesAndValues getLogComboOptions() {
		final int length = 4;
		final LocalizedComboEntryNamesAndValues options = new LocalizedComboEntryNamesAndValues(length);
		for (int index = 0; index < length; index++) {
			final int value = index;
			final Localized name = new Localized() {
				@Override
				public String getString() {
					return Resources.get("lbl.preferences.server.log.request." + value);
				}
			};
			options.put(name, value);
		}
		return options;
	}

}
