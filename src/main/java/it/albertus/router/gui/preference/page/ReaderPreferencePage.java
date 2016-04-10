package it.albertus.router.gui.preference.page;

import it.albertus.router.gui.TextFormatter;
import it.albertus.router.reader.AsusDslN12EReader;
import it.albertus.router.reader.AsusDslN14UReader;
import it.albertus.router.reader.DLinkDsl2750Reader;
import it.albertus.router.reader.Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.resources.Resources;

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

	@Override
	protected Page getPage() {
		return Page.READER;
	}

	public enum ReaderClass {
		TPLINK_8970(TpLink8970Reader.DEVICE_MODEL_KEY, TpLink8970Reader.class),
		ASUS_N12E(AsusDslN12EReader.DEVICE_MODEL_KEY, AsusDslN12EReader.class),
		ASUS_N14U(AsusDslN14UReader.DEVICE_MODEL_KEY, AsusDslN14UReader.class),
		DLINK_2750(DLinkDsl2750Reader.DEVICE_MODEL_KEY, DLinkDsl2750Reader.class);

		private final String resourceKey;
		private final Class<? extends Reader> readerClass;

		private ReaderClass(final String resourceKey, final Class<? extends Reader> readerClass) {
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

	public static String[][] getReaderComboOptions() {
		final int length = ReaderClass.values().length;
		final String[][] options = new String[length][2];
		for (int index = 0; index < length; index++) {
			options[index][0] = Resources.get(ReaderClass.values()[index].getResourceKey());
			options[index][1] = ReaderClass.values()[index].getReaderClass().getSimpleName();
		}
		return options;
	}

}
