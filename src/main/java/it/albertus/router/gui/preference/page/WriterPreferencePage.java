package it.albertus.router.gui.preference.page;

import it.albertus.jface.TextFormatter;
import it.albertus.router.resources.Resources;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.DatabaseWriter;
import it.albertus.router.writer.Writer;

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

	public static String[][] getWriterComboOptions() {
		final int length = WriterClass.values().length;
		final String[][] options = new String[length][2];
		for (int index = 0; index < length; index++) {
			options[index][0] = Resources.get(WriterClass.values()[index].getResourceKey());
			options[index][1] = WriterClass.values()[index].getWriterClass().getSimpleName();
		}
		return options;
	}

}
