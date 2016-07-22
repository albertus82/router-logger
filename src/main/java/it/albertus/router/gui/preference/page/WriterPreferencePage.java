package it.albertus.router.gui.preference.page;

import it.albertus.jface.TextFormatter;
import it.albertus.jface.preference.LocalizedComboEntryNamesAndValues;
import it.albertus.router.resources.Resources;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.DatabaseWriter;
import it.albertus.router.writer.Writer;
import it.albertus.util.Localized;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class WriterPreferencePage extends BasePreferencePage {

	@Override
	protected Control createHeader() {
		final Label header = new Label(getFieldEditorParent(), SWT.WRAP);
		TextFormatter.setBoldFontStyle(header);
		header.setText(Resources.get("lbl.preferences.writer.header"));
		return header;
	}

	protected enum WriterClass {
		CSV(CsvWriter.DESTINATION_KEY, CsvWriter.class),
		DATABASE(DatabaseWriter.DESTINATION_KEY, DatabaseWriter.class);

		private final String resourceKey;
		private final Class<? extends Writer> writerClass;

		private WriterClass(final String resourceKey, final Class<? extends Writer> writerClass) {
			this.resourceKey = resourceKey;
			this.writerClass = writerClass;
		}

		public String getResourceKey() {
			return resourceKey;
		}

		public Class<? extends Writer> getWriterClass() {
			return writerClass;
		}
	}

	public static LocalizedComboEntryNamesAndValues getWriterComboOptions() {
		final int length = WriterClass.values().length;
		final LocalizedComboEntryNamesAndValues options = new LocalizedComboEntryNamesAndValues();
		for (int i = 0; i < length; i++) {
			final int index = i;
			final String value = WriterClass.values()[index].getWriterClass().getSimpleName();
			final Localized name = new Localized() {
				@Override
				public String getString() {
					return Resources.get(WriterClass.values()[index].getResourceKey());
				}
			};
			options.add(name, value);
		}
		return options;
	}

}
