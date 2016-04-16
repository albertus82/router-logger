package it.albertus.router.gui.preference.field.listener;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Text;

/** Accepts only numeric inputs and trims automatically. */
public class IntegerVerifyListener extends TrimVerifyListener {

	@Override
	public void verifyText(final VerifyEvent ve) {
		super.verifyText(ve); // Trim
		final Text text = (Text) ve.widget;
		final String oldText = text.getText();
		final String newText = oldText.substring(0, ve.start) + ve.text + oldText.substring(ve.end);

		if (!oldText.equals(newText)) {
			if (!isNumeric(newText) && newText.trim().length() > 0) {
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
