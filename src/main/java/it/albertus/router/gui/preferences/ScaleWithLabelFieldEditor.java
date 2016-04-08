package it.albertus.router.gui.preferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ScaleWithLabelFieldEditor extends ScaleFieldEditor {

	private final Text text;

	public Text getTextControl() {
		return text;
	}

	public ScaleWithLabelFieldEditor(final String name, final String labelText, final Composite parent, final int min, final int max, final int increment, final int pageIncrement) {
		super(name, labelText, parent, min, max, increment, pageIncrement);
		text = new Text(parent, SWT.READ_ONLY | SWT.BORDER | SWT.TRAIL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(text);
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

}
