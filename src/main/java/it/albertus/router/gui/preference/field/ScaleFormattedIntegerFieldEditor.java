package it.albertus.router.gui.preference.field;

import it.albertus.router.resources.Resources;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ScaleFormattedIntegerFieldEditor extends ScaleFieldEditor {

	private final Text text;
	private final TextFormatter formatter;

	public Text getTextControl() {
		return text;
	}

	public ScaleFormattedIntegerFieldEditor(final String name, final String labelText, final Composite parent, final int min, final int max, final int increment, final int pageIncrement) {
		super(name, labelText, parent, min, max, increment, pageIncrement);
		text = createTextControl(parent);
		formatter = new TextFormatter(text);
	}

	public ScaleFormattedIntegerFieldEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent);
		text = createTextControl(parent);
		formatter = new TextFormatter(text);
	}

	protected Text createTextControl(final Composite parent) {
		final Text text = new Text(parent, SWT.BORDER | SWT.TRAIL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(text); // TODO Width
		text.setTextLimit(Integer.toString(getMaximum()).length());
		text.addFocusListener(new TextFocusListener());
		text.addVerifyListener(new TextVerifyListener());
		return text;
	}

	@Override
	protected void adjustForNumColumns(final int numColumns) {
		((GridData) scale.getLayoutData()).horizontalSpan = numColumns - (getNumberOfControls() - 1);
	}

	@Override
	public int getNumberOfControls() {
		return 3;
	}

	@Override
	protected void valueChanged() {
		super.valueChanged();
		updateText();
	}

	@Override
	protected void doLoad() {
		super.doLoad();
		setToolTipText(getPreferenceStore().getDefaultInt(getPreferenceName()));
		updateText();
	}

	protected void setToolTipText(final int defaultValue) {
		if (text != null && !text.isDisposed() && defaultValue != 0) {
			text.setToolTipText(Resources.get("lbl.preferences.default.value", defaultValue));
		}
	}

	protected void updateText() {
		if (scale != null && !scale.isDisposed()) {
			setText(scale.getSelection());
		}
	}

	protected void setText(final int value) {
		if (text != null && !text.isDisposed() && formatter != null) {
			text.setText(Integer.toString(value));
			formatter.updateFontStyle(getPreferenceStore().getDefaultInt(getPreferenceName()));
		}
	}

	protected class TextVerifyListener implements VerifyListener {
		@Override
		public void verifyText(final VerifyEvent ve) {
			final String oldText = text.getText();
			final String newText = oldText.substring(0, ve.start) + ve.text + oldText.substring(ve.end);

			if (!oldText.equals(newText)) {
				if (!isNumeric(newText.trim()) && newText.trim().length() > 0) {
					ve.doit = false;
				}
			}
		}

		private boolean isNumeric(final String string) {
			try {
				Integer.parseInt(string);
				return true;
			}
			catch (final Exception e) {
				return false;
			}
		}
	}

	protected class TextFocusListener extends FocusAdapter {
		@Override
		public void focusLost(final FocusEvent fe) {
			try {
				int textValue = Integer.parseInt(text.getText());
				if (textValue > getMaximum()) {
					textValue = getMaximum();
				}
				if (textValue < getMinimum()) {
					textValue = getMinimum();
				}
				setText(textValue);
				scale.setSelection(textValue);
			}
			catch (final Exception e) {}
		}
	}

}
