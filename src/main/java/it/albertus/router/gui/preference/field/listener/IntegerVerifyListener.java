package it.albertus.router.gui.preference.field.listener;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;

public class IntegerVerifyListener implements VerifyListener {

	@Override
	public void verifyText(final VerifyEvent ve) {
		final Text text = (Text) ve.widget;
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
