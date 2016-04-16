package it.albertus.router.gui.preference.field;

import it.albertus.router.gui.TextFormatter;
import it.albertus.router.gui.preference.field.listener.IntegerVerifyListener;
import it.albertus.router.resources.Resources;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class FormattedIntegerFieldEditor extends IntegerFieldEditor {

	public FormattedIntegerFieldEditor(final String name, final String labelText, final Composite parent, final int textLimit) {
		super(name, labelText, parent, textLimit);
		init();
	}

	public FormattedIntegerFieldEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent);
		init();
	}

	protected void init() {
		getTextControl().addVerifyListener(new IntegerVerifyListener());
		setErrorMessage(Resources.get("err.preferences.integer"));
	}

	@Override
	public void setValidRange(final int min, final int max) {
		super.setValidRange(min, max);
		setErrorMessage(Resources.get("err.preferences.integer.range", min, max));
	}

	@Override
	protected void doLoad() {
		final Text text = getTextControl();
		if (text != null && !text.isDisposed()) {
			int value;
			try {
				value = Integer.parseInt(getPreferenceStore().getString(getPreferenceName()).trim());
			}
			catch (final Exception e) {
				value = IPreferenceStore.INT_DEFAULT_DEFAULT;
			}
			final String valueText = Integer.toString(value);
			text.setText(valueText);
			oldValue = valueText;
			setToolTipText(getPreferenceStore().getDefaultInt(getPreferenceName()));
		}
		updateFontStyle();
	}

	protected void setToolTipText(final int defaultValue) {
		if (defaultValue != 0) {
			getTextControl().setToolTipText(Resources.get("lbl.preferences.default.value", defaultValue));
		}
	}

	@Override
	protected void valueChanged() {
		super.valueChanged();
		updateFontStyle();
	}

	protected void updateFontStyle() {
		final int defaultValue = getPreferenceStore().getDefaultInt(getPreferenceName());
		TextFormatter.updateFontStyle(getTextControl(), defaultValue);
	}

}
