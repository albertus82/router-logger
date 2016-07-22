package it.albertus.router.gui.preference.page;

import it.albertus.jface.TextFormatter;
import it.albertus.jface.preference.LocalizedComboEntryNamesAndValues;
import it.albertus.router.reader.AsusDslN12EReader;
import it.albertus.router.reader.AsusDslN14UReader;
import it.albertus.router.reader.DLinkDsl2750Reader;
import it.albertus.router.reader.Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.resources.Resources;
import it.albertus.util.Localized;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class ReaderPreferencePage extends BasePreferencePage {

	@Override
	protected Control createHeader() {
		final Label header = new Label(getFieldEditorParent(), SWT.WRAP);
		TextFormatter.setBoldFontStyle(header);
		header.setText(Resources.get("lbl.preferences.reader.header"));
		return header;
	}

	protected enum ReaderComboData {
		TPLINK_8970(TpLink8970Reader.DEVICE_MODEL_KEY, TpLink8970Reader.class),
		ASUS_N12E(AsusDslN12EReader.DEVICE_MODEL_KEY, AsusDslN12EReader.class),
		ASUS_N14U(AsusDslN14UReader.DEVICE_MODEL_KEY, AsusDslN14UReader.class),
		DLINK_2750(DLinkDsl2750Reader.DEVICE_MODEL_KEY, DLinkDsl2750Reader.class);

		private final String resourceKey;
		private final Class<? extends Reader> readerClass;

		private ReaderComboData(final String resourceKey, final Class<? extends Reader> readerClass) {
			this.resourceKey = resourceKey;
			this.readerClass = readerClass;
		}

		public String getResourceKey() {
			return resourceKey;
		}

		public Class<? extends Reader> getReaderClass() {
			return readerClass;
		}
	}

	public static LocalizedComboEntryNamesAndValues getReaderComboOptions() {
		final ReaderComboData[] values = ReaderComboData.values();
		final LocalizedComboEntryNamesAndValues options = new LocalizedComboEntryNamesAndValues(values.length);
		for (final ReaderComboData comboData : values) {
			final String value = comboData.readerClass.getSimpleName();
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
