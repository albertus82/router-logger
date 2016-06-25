package it.albertus.gui.preference;

import it.albertus.gui.preference.page.IPage;

import java.util.Set;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

public interface IPreference {

	String getConfigurationKey();

	String getLabel();

	IPage getPage();

	FieldEditorType getFieldEditorType();

	String getDefaultValue();

	FieldEditorData getFieldEditorData();

	IPreference getParent();

	Set<? extends IPreference> getChildren();

	FieldEditor createFieldEditor(Composite parent);

}
