package it.albertus.router.gui.preference;

import it.albertus.jface.preference.FieldEditorData;
import it.albertus.jface.preference.field.ComboFieldEditor;
import it.albertus.jface.preference.field.DefaultBooleanFieldEditor;
import it.albertus.jface.preference.field.EditableComboFieldEditor;
import it.albertus.jface.preference.field.EmailAddressesListEditor;
import it.albertus.jface.preference.field.FormattedComboFieldEditor;
import it.albertus.jface.preference.field.FormattedDirectoryFieldEditor;
import it.albertus.jface.preference.field.FormattedFileFieldEditor;
import it.albertus.jface.preference.field.FormattedIntegerFieldEditor;
import it.albertus.jface.preference.field.FormattedStringFieldEditor;
import it.albertus.jface.preference.field.IntegerComboFieldEditor;
import it.albertus.jface.preference.field.PasswordFieldEditor;
import it.albertus.jface.preference.field.ScaleIntegerFieldEditor;
import it.albertus.jface.preference.field.UriListEditor;
import it.albertus.jface.preference.field.ValidatedComboFieldEditor;
import it.albertus.jface.preference.field.WrapStringFieldEditor;
import it.albertus.router.gui.Images;
import it.albertus.router.gui.preference.field.DatabaseComboFieldEditor;
import it.albertus.router.gui.preference.field.DelimiterComboFieldEditor;
import it.albertus.router.gui.preference.field.ReaderComboFieldEditor;
import it.albertus.router.gui.preference.field.ThresholdsFieldEditor;
import it.albertus.router.gui.preference.field.WriterComboFieldEditor;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

public final class FieldEditorFactory {

	public static FieldEditor createFieldEditor(final FieldEditorType type, final String name, final String label, final Composite parent, final FieldEditorData data) {
		switch (type) {
		case Boolean:
			return new BooleanFieldEditor(name, label, parent);
		case Combo:
			return new ComboFieldEditor(name, label, data.getComboEntryNamesAndValues().toArray(), parent);
		case DatabaseCombo:
			return new DatabaseComboFieldEditor(name, label, data.getComboEntryNamesAndValues().toArray(), parent);
		case DefaultBoolean:
			return new DefaultBooleanFieldEditor(name, label, parent);
		case DelimiterCombo:
			return createDelimiterComboFieldEditor(name, label, parent, data);
		case Directory:
			return createDirectoryFieldEditor(name, label, parent, data);
		case EditableCombo:
			return new EditableComboFieldEditor(name, label, data.getComboEntryNamesAndValues().toArray(), parent);
		case EmailAddresses:
			return new EmailAddressesListEditor(name, label, parent, data.getHorizontalSpan(), Images.MAIN_ICONS);
		case File:
			return createFileFieldEditor(name, label, parent, data);
		case FormattedCombo:
			return new FormattedComboFieldEditor(name, label, data.getComboEntryNamesAndValues().toArray(), parent);
		case FormattedDirectory:
			return createFormattedDirectoryFieldEditor(name, label, parent, data);
		case FormattedFile:
			return createFormattedFileFieldEditor(name, label, parent, data);
		case FormattedInteger:
			return createFormattedIntegerFieldEditor(name, label, parent, data);
		case FormattedString:
			return createFormattedStringFieldEditor(name, label, parent, data);
		case Integer:
			return createIntegerFieldEditor(name, label, parent, data);
		case IntegerCombo:
			return new IntegerComboFieldEditor(name, label, data.getComboEntryNamesAndValues().toArray(), parent);
		case Password:
			return createPasswordFieldEditor(name, label, parent, data);
		case ReaderCombo:
			return new ReaderComboFieldEditor(name, label, data.getComboEntryNamesAndValues().toArray(), parent);
		case Scale:
			return createScaleFieldEditor(name, label, parent, data);
		case ScaleInteger:
			return createScaleIntegerFieldEditor(name, label, parent, data);
		case String:
			return createStringFieldEditor(name, label, parent, data);
		case Thresholds:
			return new ThresholdsFieldEditor(name, label, parent);
		case Uri:
			return new UriListEditor(name, label, parent, data.getHorizontalSpan(), Images.MAIN_ICONS);
		case ValidatedCombo:
			return createValidatedComboFieldEditor(name, label, parent, data);
		case WrapString:
			return createWrapStringFieldEditor(name, label, parent, data);
		case WriterCombo:
			return new WriterComboFieldEditor(name, label, data.getComboEntryNamesAndValues().toArray(), parent);
		default:
			throw new IllegalStateException("Unsupported FieldEditor: " + type);
		}
	}

	private static FieldEditor createDelimiterComboFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		final DelimiterComboFieldEditor delimiterComboFieldEditor = new DelimiterComboFieldEditor(name, label, data.getComboEntryNamesAndValues().toArray(), parent);
		if (data.getEmptyStringAllowed() != null) {
			delimiterComboFieldEditor.setEmptyStringAllowed(data.getEmptyStringAllowed());
		}
		return delimiterComboFieldEditor;
	}

	private static FieldEditor createDirectoryFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		final DirectoryFieldEditor directoryFieldEditor = new DirectoryFieldEditor(name, label, parent);
		if (data != null) {
			if (data.getTextLimit() != null) {
				directoryFieldEditor.setTextLimit(data.getTextLimit());
			}
			if (data.getEmptyStringAllowed() != null) {
				directoryFieldEditor.setEmptyStringAllowed(data.getEmptyStringAllowed());
			}
		}
		return directoryFieldEditor;
	}

	private static FieldEditor createFileFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		final FileFieldEditor fileFieldEditor;
		if (data != null && data.getFileEnforceAbsolute() != null) {
			fileFieldEditor = new FileFieldEditor(name, label, data.getFileEnforceAbsolute(), parent);
		}
		else {
			fileFieldEditor = new FileFieldEditor(name, label, parent);
		}
		if (data != null) {
			if (data.getTextLimit() != null) {
				fileFieldEditor.setTextLimit(data.getTextLimit());
			}
			if (data.getEmptyStringAllowed() != null) {
				fileFieldEditor.setEmptyStringAllowed(data.getEmptyStringAllowed());
			}
			if (data.getFileExtensions() != null && data.getFileExtensions().length != 0) {
				fileFieldEditor.setFileExtensions(data.getFileExtensions());
			}
		}
		return fileFieldEditor;
	}

	private static FieldEditor createFormattedDirectoryFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		final FormattedDirectoryFieldEditor formattedDirectoryFieldEditor = new FormattedDirectoryFieldEditor(name, label, parent);
		if (data != null) {
			if (data.getTextLimit() != null) {
				formattedDirectoryFieldEditor.setTextLimit(data.getTextLimit());
			}
			if (data.getEmptyStringAllowed() != null) {
				formattedDirectoryFieldEditor.setEmptyStringAllowed(data.getEmptyStringAllowed());
			}
			if (data.getDirectoryDialogMessage() != null) {
				formattedDirectoryFieldEditor.setDialogMessage(data.getDirectoryDialogMessage());
			}
		}
		return formattedDirectoryFieldEditor;
	}

	private static FieldEditor createFormattedFileFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		final FormattedFileFieldEditor formattedFileFieldEditor;
		if (data != null && data.getFileEnforceAbsolute() != null) {
			formattedFileFieldEditor = new FormattedFileFieldEditor(name, label, data.getFileEnforceAbsolute(), parent);
		}
		else {
			formattedFileFieldEditor = new FormattedFileFieldEditor(name, label, parent);
		}
		if (data != null) {
			if (data.getTextLimit() != null) {
				formattedFileFieldEditor.setTextLimit(data.getTextLimit());
			}
			if (data.getEmptyStringAllowed() != null) {
				formattedFileFieldEditor.setEmptyStringAllowed(data.getEmptyStringAllowed());
			}
			if (data.getFileExtensions() != null && data.getFileExtensions().length != 0) {
				formattedFileFieldEditor.setFileExtensions(data.getFileExtensions());
			}
		}
		return formattedFileFieldEditor;
	}

	private static FieldEditor createFormattedIntegerFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		final FormattedIntegerFieldEditor formattedIntegerFieldEditor = new FormattedIntegerFieldEditor(name, label, parent);
		if (data != null) {
			if (data.getEmptyStringAllowed() != null) {
				formattedIntegerFieldEditor.setEmptyStringAllowed(data.getEmptyStringAllowed());
			}
			if (data.getIntegerMinValidValue() != null && data.getIntegerMaxValidValue() != null) {
				formattedIntegerFieldEditor.setValidRange(data.getIntegerMinValidValue(), data.getIntegerMaxValidValue());
				formattedIntegerFieldEditor.setTextLimit(data.getIntegerMaxValidValue().toString().length());
			}
			if (data.getTextLimit() != null) {
				formattedIntegerFieldEditor.setTextLimit(data.getTextLimit());
			}
		}
		return formattedIntegerFieldEditor;
	}

	private static FieldEditor createFormattedStringFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		final FormattedStringFieldEditor formattedStringFieldEditor;
		if (data != null && data.getTextWidth() != null && data.getTextValidateStrategy() != null) {
			formattedStringFieldEditor = new FormattedStringFieldEditor(name, label, data.getTextWidth(), data.getTextValidateStrategy(), parent);
		}
		else if (data != null && data.getTextValidateStrategy() != null) {
			formattedStringFieldEditor = new FormattedStringFieldEditor(name, label, StringFieldEditor.UNLIMITED, data.getTextValidateStrategy(), parent);
		}
		else if (data != null && data.getTextWidth() != null) {
			formattedStringFieldEditor = new FormattedStringFieldEditor(name, label, data.getTextWidth(), parent);
		}
		else {
			formattedStringFieldEditor = new FormattedStringFieldEditor(name, label, parent);
		}
		if (data != null) {
			if (data.getEmptyStringAllowed() != null) {
				formattedStringFieldEditor.setEmptyStringAllowed(data.getEmptyStringAllowed());
			}
			if (data.getTextLimit() != null) {
				formattedStringFieldEditor.setTextLimit(data.getTextLimit());
			}
		}
		return formattedStringFieldEditor;
	}

	private static FieldEditor createIntegerFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		final IntegerFieldEditor integerFieldEditor = new IntegerFieldEditor(name, label, parent);
		if (data != null) {
			if (data.getEmptyStringAllowed() != null) {
				integerFieldEditor.setEmptyStringAllowed(data.getEmptyStringAllowed());
			}
			if (data.getIntegerMinValidValue() != null && data.getIntegerMaxValidValue() != null) {
				integerFieldEditor.setValidRange(data.getIntegerMinValidValue(), data.getIntegerMaxValidValue());
				integerFieldEditor.setTextLimit(data.getIntegerMaxValidValue().toString().length());
			}
			if (data.getTextLimit() != null) {
				integerFieldEditor.setTextLimit(data.getTextLimit());
			}
		}
		return integerFieldEditor;
	}

	private static FieldEditor createPasswordFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		final PasswordFieldEditor passwordFieldEditor;
		if (data != null && data.getTextWidth() != null) {
			passwordFieldEditor = new PasswordFieldEditor(name, label, data.getTextWidth(), parent);
		}
		else {
			passwordFieldEditor = new PasswordFieldEditor(name, label, parent);
		}
		if (data != null) {
			if (data.getEmptyStringAllowed() != null) {
				passwordFieldEditor.setEmptyStringAllowed(data.getEmptyStringAllowed());
			}
			if (data.getTextLimit() != null) {
				passwordFieldEditor.setTextLimit(data.getTextLimit());
			}
		}
		return passwordFieldEditor;
	}

	private static FieldEditor createScaleFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		final ScaleFieldEditor scaleFieldEditor = new ScaleFieldEditor(name, label, parent);
		if (data != null) {
			if (data.getScaleMinimum() != null) {
				scaleFieldEditor.setMinimum(data.getScaleMinimum());
			}
			if (data.getScaleMaximum() != null) {
				scaleFieldEditor.setMaximum(data.getScaleMaximum());
			}
			if (data.getScaleIncrement() != null) {
				scaleFieldEditor.setIncrement(data.getScaleIncrement());
			}
			if (data.getScalePageIncrement() != null) {
				scaleFieldEditor.setPageIncrement(data.getScalePageIncrement());
			}
		}
		return scaleFieldEditor;
	}

	private static FieldEditor createScaleIntegerFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		int min = 0;
		int max = 10;
		int increment = 1;
		int pageIncrement = 1;

		if (data != null) {
			if (data.getScaleMinimum() != null) {
				min = data.getScaleMinimum();
			}
			if (data.getScaleMaximum() != null) {
				max = data.getScaleMaximum();
			}
			if (data.getScaleIncrement() != null) {
				increment = data.getScaleIncrement();
			}
			if (data.getScalePageIncrement() != null) {
				pageIncrement = data.getScalePageIncrement();
			}
		}
		return new ScaleIntegerFieldEditor(name, label, parent, min, max, increment, pageIncrement);
	}

	private static FieldEditor createStringFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		final StringFieldEditor stringFieldEditor;
		if (data != null && data.getTextWidth() != null && data.getTextValidateStrategy() != null) {
			stringFieldEditor = new StringFieldEditor(name, label, data.getTextWidth(), data.getTextValidateStrategy(), parent);
		}
		else if (data != null && data.getTextValidateStrategy() != null) {
			stringFieldEditor = new StringFieldEditor(name, label, StringFieldEditor.UNLIMITED, data.getTextValidateStrategy(), parent);
		}
		else if (data != null && data.getTextWidth() != null) {
			stringFieldEditor = new StringFieldEditor(name, label, data.getTextWidth(), parent);
		}
		else {
			stringFieldEditor = new StringFieldEditor(name, label, parent);
		}
		if (data != null) {
			if (data.getEmptyStringAllowed() != null) {
				stringFieldEditor.setEmptyStringAllowed(data.getEmptyStringAllowed());
			}
			if (data.getTextLimit() != null) {
				stringFieldEditor.setTextLimit(data.getTextLimit());
			}
		}
		return stringFieldEditor;
	}

	private static FieldEditor createValidatedComboFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		final ValidatedComboFieldEditor validatedComboFieldEditor = new ValidatedComboFieldEditor(name, label, data.getComboEntryNamesAndValues().toArray(), parent);
		if (data.getEmptyStringAllowed() != null) {
			validatedComboFieldEditor.setEmptyStringAllowed(data.getEmptyStringAllowed());
		}
		return validatedComboFieldEditor;
	}

	private static FieldEditor createWrapStringFieldEditor(final String name, final String label, final Composite parent, final FieldEditorData data) {
		final WrapStringFieldEditor wrapStringFieldEditor;
		if (data != null && data.getTextHeight() != null) {
			wrapStringFieldEditor = new WrapStringFieldEditor(name, label, parent, data.getTextHeight());
		}
		else {
			wrapStringFieldEditor = new WrapStringFieldEditor(name, label, parent);
		}
		if (data != null && data.getTextLimit() != null) {
			wrapStringFieldEditor.setTextLimit(data.getTextLimit());
		}
		return wrapStringFieldEditor;
	}

	/** Instantiation not permitted. */
	private FieldEditorFactory() {}

}
