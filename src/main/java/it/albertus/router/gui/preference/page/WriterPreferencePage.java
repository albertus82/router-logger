package it.albertus.router.gui.preference.page;

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
		header.setText(Resources.get("lbl.preferences.writer.header"));
		return header;
	}

	@Override
	protected Page getPage() {
		return Page.WRITER;
	}

	public enum WriterClass {
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

}
