/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - Bug 214392 missing implementation of ComboFieldEditor.setEnabled
 *     Albertus82 (http://github.com/Albertus82) - Editable Combo (could not subclass due to some private fields)
 *******************************************************************************/
package it.albertus.router.gui.preference.field;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class EditableComboFieldEditor extends FieldEditor {

	private Combo combo;
	private String value;
	private final String[][] entryNamesAndValues;

	public EditableComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		init(name, labelText);
		Assert.isTrue(checkArray(entryNamesAndValues));
		this.entryNamesAndValues = entryNamesAndValues;
		createControl(parent);
	}

	protected boolean checkArray(final String[][] table) {
		if (table == null) {
			return false;
		}
		for (int i = 0; i < table.length; i++) {
			final String[] array = table[i];
			if (array == null || array.length != 2) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void adjustForNumColumns(final int numColumns) {
		final Label label = getLabelControl();
		if (numColumns > 1) {
			int left = numColumns;
			if (label != null) {
				((GridData) label.getLayoutData()).horizontalSpan = 1;
				left = left - 1;
			}
			((GridData) combo.getLayoutData()).horizontalSpan = left;
		}
		else {
			if (label != null) {
				((GridData) label.getLayoutData()).horizontalSpan = 1;
			}
			((GridData) combo.getLayoutData()).horizontalSpan = 1;
		}
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
	protected void doLoad() {
		updateComboForValue(getPreferenceStore().getString(getPreferenceName()));
	}

	@Override
	protected void doLoadDefault() {
		updateComboForValue(getPreferenceStore().getDefaultString(getPreferenceName()));
	}

	@Override
	protected void doStore() {
		if (value == null) {
			getPreferenceStore().setToDefault(getPreferenceName());
			return;
		}
		getPreferenceStore().setValue(getPreferenceName(), value);
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	protected Combo getComboBoxControl(final Composite parent) {
		if (combo == null) {
			combo = new Combo(parent, SWT.NONE);
			combo.setFont(parent.getFont());
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

	protected Combo getComboBoxControl() {
		return combo;
	}

	protected void updateValue() {
		final String oldValue = value;
		final String name = combo.getText();
		value = getValueForName(name);
		setPresentsDefaultValue(false);
		fireValueChanged(VALUE, oldValue, value);
	}

	protected String getValueForName(final String name) {
		for (int i = 0; i < entryNamesAndValues.length; i++) {
			final String[] entry = entryNamesAndValues[i];
			if (name.equals(entry[0])) {
				return entry[1];
			}
		}
		return name; // Value not present in the array.
	}

	protected void updateComboForValue(final String value) {
		this.value = value;
		for (int i = 0; i < entryNamesAndValues.length; i++) {
			if (value.equals(entryNamesAndValues[i][1])) {
				combo.setText(entryNamesAndValues[i][0]);
				return;
			}
		}
		combo.setText(value);
	}

	@Override
	public void setEnabled(final boolean enabled, final Composite parent) {
		super.setEnabled(enabled, parent);
		getComboBoxControl(parent).setEnabled(enabled);
	}

	protected String getValue() {
		return value;
	}

	protected String[][] getEntryNamesAndValues() {
		return entryNamesAndValues;
	}

}
