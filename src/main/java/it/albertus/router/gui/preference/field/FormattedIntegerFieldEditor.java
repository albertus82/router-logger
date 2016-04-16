package it.albertus.router.gui.preference.field;

import it.albertus.router.gui.TextFormatter;
import it.albertus.router.gui.preference.field.listener.IntegerVerifyListener;
import it.albertus.router.resources.Resources;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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

	@Override
	protected void valueChanged() {
		super.valueChanged();
		updateFontStyle();
	}

	protected void init() {
		getTextControl().addVerifyListener(new IntegerVerifyListener());
		getTextControl().addFocusListener(new IntegerFocusListener());
		setErrorMessage(Resources.get("err.preferences.integer"));
	}

	protected void setToolTipText(final int defaultValue) {
		if (defaultValue != 0) {
			getTextControl().setToolTipText(Resources.get("lbl.preferences.default.value", defaultValue));
		}
	}

	protected void updateFontStyle() {
		final int defaultValue = getPreferenceStore().getDefaultInt(getPreferenceName());
		TextFormatter.updateFontStyle(getTextControl(), defaultValue);
	}

	/** Removes trailing zeros when the field loses the focus */
	protected class IntegerFocusListener extends FocusAdapter {
		@Override
		public void focusLost(final FocusEvent fe) {
			final Text text = (Text) fe.widget;
			final String oldText = text.getText();
			try {
				final String newText = Integer.toString(Integer.parseInt(oldText));
				if (!oldText.equals(newText)) {
					text.setText(newText);
				}
				valueChanged();
			}
			catch (final Exception e) {}
		}
	}

}
