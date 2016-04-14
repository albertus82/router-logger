package it.albertus.router.gui.preference.field;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class EditableComboFieldEditor extends ComboFieldEditor {

	public EditableComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
	}

	@Override
	protected void doFillIntoGrid(final Composite parent, final int numColumns) {
		int comboColumns = 1;
		if (numColumns > 1) {
			comboColumns = numColumns - 1;
		}
		final Label label = getLabelControl(parent);
		GridDataFactory.swtDefaults().applyTo(label);

		final Combo combo = getComboBoxControl(parent);
		GridDataFactory.swtDefaults().span(comboColumns, 1).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(combo);
		combo.setFont(parent.getFont());
	}

	@Override
	protected Combo getComboBoxControl(final Composite parent) {
		Combo combo = getComboBoxControl();
		if (combo == null) {
			combo = new Combo(parent, SWT.NONE);
			setComboBoxControl(combo);
			combo.setFont(parent.getFont());
			final String[][] entryNamesAndValues = getEntryNamesAndValues();
			for (int i = 0; i < entryNamesAndValues.length; i++) {
				combo.add(entryNamesAndValues[i][0], i);
			}

			combo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					updateValue();
				}
			});

			combo.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					updateValue();
				}
			});
		}
		return combo;
	}

	protected void updateValue() {
		final String oldValue = getValue();
		final String name = getComboBoxControl().getText();
		setValue(getValueForName(name));
		setPresentsDefaultValue(false);
		fireValueChanged(VALUE, oldValue, getValue());
	}

	@Override
	protected String getValueForName(final String name) {
		final String[][] entryNamesAndValues = getEntryNamesAndValues();
		for (int i = 0; i < entryNamesAndValues.length; i++) {
			final String[] entry = entryNamesAndValues[i];
			if (name.equals(entry[0])) {
				return entry[1];
			}
		}
		return name; // Value not present in the array.
	}

	@Override
	protected void updateComboForValue(final String value) {
		setValue(value);
		final Combo combo = getComboBoxControl();
		final String[][] entryNamesAndValues = getEntryNamesAndValues();
		for (int i = 0; i < entryNamesAndValues.length; i++) {
			if (value.equals(entryNamesAndValues[i][1])) {
				combo.setText(entryNamesAndValues[i][0]);
				return;
			}
		}
		combo.setText(value);
	}

}
