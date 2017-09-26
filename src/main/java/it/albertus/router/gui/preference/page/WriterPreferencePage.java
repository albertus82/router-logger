package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.page.RestartHeaderPreferencePage;
import it.albertus.router.resources.Messages;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.DatabaseWriter;
import it.albertus.router.writer.DummyWriter;
import it.albertus.router.writer.IWriter;
import it.albertus.util.Localized;

public class WriterPreferencePage extends RestartHeaderPreferencePage {

	protected enum WriterComboData {
		CSV(CsvWriter.DESTINATION_KEY, CsvWriter.class),
		DATABASE(DatabaseWriter.DESTINATION_KEY, DatabaseWriter.class),
		DUMMY(DummyWriter.DESTINATION_KEY, DummyWriter.class);

		private final String resourceKey;
		private final Class<? extends IWriter> writerClass;

		private WriterComboData(final String resourceKey, final Class<? extends IWriter> writerClass) {
			this.resourceKey = resourceKey;
			this.writerClass = writerClass;
		}

		public String getResourceKey() {
			return resourceKey;
		}

		public Class<? extends IWriter> getWriterClass() {
			return writerClass;
		}
	}

	public static LocalizedLabelsAndValues getWriterComboOptions() {
		final WriterComboData[] values = WriterComboData.values();
		final LocalizedLabelsAndValues options = new LocalizedLabelsAndValues(values.length);
		for (final WriterComboData comboData : values) {
			final String value = comboData.writerClass.getSimpleName();
			final Localized name = new Localized() {
				@Override
				public String getString() {
					return Messages.get(comboData.resourceKey);
				}
			};
			options.add(name, value);
		}
		return options;
	}

}
