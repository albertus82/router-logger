package it.albertus.routerlogger.gui.preference.page;

import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.page.RestartHeaderPreferencePage;
import it.albertus.routerlogger.reader.AsusDslN12EReader;
import it.albertus.routerlogger.reader.AsusDslN14UReader;
import it.albertus.routerlogger.reader.DLinkDsl2750Reader;
import it.albertus.routerlogger.reader.IReader;
import it.albertus.routerlogger.reader.TpLink8970Reader;
import it.albertus.routerlogger.resources.Messages;
import it.albertus.util.Localized;

public class ReaderPreferencePage extends RestartHeaderPreferencePage {

	protected enum ReaderComboData {
		TPLINK_8970(TpLink8970Reader.DEVICE_MODEL_KEY, TpLink8970Reader.class),
		ASUS_N12E(AsusDslN12EReader.DEVICE_MODEL_KEY, AsusDslN12EReader.class),
		ASUS_N14U(AsusDslN14UReader.DEVICE_MODEL_KEY, AsusDslN14UReader.class),
		DLINK_2750(DLinkDsl2750Reader.DEVICE_MODEL_KEY, DLinkDsl2750Reader.class);

		private final String resourceKey;
		private final Class<? extends IReader> readerClass;

		private ReaderComboData(final String resourceKey, final Class<? extends IReader> readerClass) {
			this.resourceKey = resourceKey;
			this.readerClass = readerClass;
		}

		public String getResourceKey() {
			return resourceKey;
		}

		public Class<? extends IReader> getReaderClass() {
			return readerClass;
		}
	}

	public static LocalizedLabelsAndValues getReaderComboOptions() {
		final ReaderComboData[] values = ReaderComboData.values();
		final LocalizedLabelsAndValues options = new LocalizedLabelsAndValues(values.length);
		for (final ReaderComboData comboData : values) {
			final String value = comboData.readerClass.getSimpleName();
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
