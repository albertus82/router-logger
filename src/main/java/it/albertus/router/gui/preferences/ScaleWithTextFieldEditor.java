package it.albertus.router.gui.preferences;

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

public class ScaleWithTextFieldEditor extends ScaleFieldEditor {

	private final Text text;

	public Text getTextControl() {
		return text;
	}

	public ScaleWithTextFieldEditor(final String name, final String labelText, final Composite parent, final int min, final int max, final int increment, final int pageIncrement) {
		super(name, labelText, parent, min, max, increment, pageIncrement);
		text = new Text(parent, SWT.BORDER | SWT.TRAIL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(text); // TODO
																					// Width
		text.setTextLimit(Integer.toString(max).length());
		text.addFocusListener(new TextFocusListener(min, max));
		text.addVerifyListener(new TextVerifyListener());
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
		updateText();
	}

	private void updateText() {
		if (scale != null && !scale.isDisposed() && text != null && !text.isDisposed()) {
			text.setText(Integer.toString(scale.getSelection()));
		}
	}

	private class TextVerifyListener implements VerifyListener {
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

	private class TextFocusListener extends FocusAdapter {
		private final int min;
		private final int max;

		private TextFocusListener(int min, int max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public void focusLost(final FocusEvent fe) {
			try {
				int textValue = Integer.parseInt(text.getText());
				if (textValue > max) {
					textValue = max;
				}
				if (textValue < min) {
					textValue = min;
				}
				text.setText(Integer.toString(textValue));
				scale.setSelection(textValue);
			}
			catch (final Exception e) {}
		}
	}

}
