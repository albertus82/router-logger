package it.albertus.router.gui.preference.field;

import it.albertus.router.gui.preference.Preference;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class PasswordFieldEditor extends StringFieldEditor {

	private Text textField; // Do not set any value here!

	public PasswordFieldEditor(final String name, final String labelText, final Composite parent) {
		super(name, labelText, parent);
		init();
	}

	public PasswordFieldEditor(final String name, final String labelText, final int width, final Composite parent) {
		super(name, labelText, width, parent);
		init();
	}

	@Override
	public Text getTextControl(final Composite parent) {
		if (textField == null) {
			textField = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
			textField.setFont(parent.getFont());
			textField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(final KeyEvent ke) {
					valueChanged();
				}
			});
			textField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent fe) {
					valueChanged();
				}
			});
			textField.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(final DisposeEvent de) {
					textField = null;
				}
			});
		}
		else {
			checkParent(textField, parent);
		}
		return textField;
	}

	private void init() {
		setTextLimit(Preference.DEFAULT_TEXT_LIMIT);
	}

	protected Text getTextField() {
		return textField;
	}

	protected void setTextField(final Text textField) {
		this.textField = textField;
	}

}
