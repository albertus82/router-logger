package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.LocalizedNamesAndValues;
import it.albertus.router.resources.Resources;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.DatabaseWriter;
import it.albertus.router.writer.DummyWriter;
import it.albertus.router.writer.Writer;
import it.albertus.util.Localized;

public class WriterPreferencePage extends RestartHeaderPreferencePage {

	protected enum WriterComboData {
		CSV(CsvWriter.DESTINATION_KEY, CsvWriter.class),
		DATABASE(DatabaseWriter.DESTINATION_KEY, DatabaseWriter.class),
		DUMMY(DummyWriter.DESTINATION_KEY, DummyWriter.class);

		private final String resourceKey;
		private final Class<? extends Writer> writerClass;

		private WriterComboData(final String resourceKey, final Class<? extends Writer> writerClass) {
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

	public static LocalizedNamesAndValues getWriterComboOptions() {
		final WriterComboData[] values = WriterComboData.values();
		final LocalizedNamesAndValues options = new LocalizedNamesAndValues(values.length);
		for (final WriterComboData comboData : values) {
			final String value = comboData.writerClass.getSimpleName();
			final Localized name = new Localized() {
				@Override
				public String getString() {
					return Resources.get(comboData.resourceKey);
				}
			};
			options.put(name, value);
		}
		return options;
	}

}
