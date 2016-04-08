package it.albertus.router.gui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

/**
 * This class demonstrates field editors
 */

public class FieldEditorPageTwo extends FieldEditorPreferencePage {
	public FieldEditorPageTwo() {
		// Use the "grid" layout
		super(GRID);
	}

	/**
	 * Creates the field editors
	 */
	protected void createFieldEditors() {
		// Add an integer field
		IntegerFieldEditor ife = new IntegerFieldEditor("myInt", "Int:", getFieldEditorParent());
		addField(ife);

		// Add a scale field
		ScaleFieldEditor sfe = new ScaleFieldEditor("myScale", "Scale:", getFieldEditorParent(), 0, 100, 1, 10);
		addField(sfe);

		// Add a string field
		StringFieldEditor stringFe = new StringFieldEditor("myString", "String:", getFieldEditorParent());
		addField(stringFe);
	}
}