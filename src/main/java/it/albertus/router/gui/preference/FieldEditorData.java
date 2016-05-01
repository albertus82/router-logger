package it.albertus.router.gui.preference;

import java.util.Arrays;

public class FieldEditorData {

	// ComboFieldEditor
	private final String[][] comboEntryNamesAndValues;

	// ScaleFieldEditor
	private final Integer scaleMinimum;
	private final Integer scaleMaximum;
	private final Integer scaleIncrement;
	private final Integer scalePageIncrement;

	// StringFieldEditor
	private final Integer textLimit;
	private final Integer textWidth;
	private final Integer textHeight;
	private final Boolean emptyStringAllowed;

	// IntegerFieldEditor
	private final Integer integerMinValidValue;
	private final Integer integerMaxValidValue;

	// DirectoryFieldEditor
	private final String directoryDialogMessageKey;

	public String[][] getComboEntryNamesAndValues() {
		return comboEntryNamesAndValues;
	}

	public Integer getScaleMinimum() {
		return scaleMinimum;
	}

	public Integer getScaleMaximum() {
		return scaleMaximum;
	}

	public Integer getScaleIncrement() {
		return scaleIncrement;
	}

	public Integer getScalePageIncrement() {
		return scalePageIncrement;
	}

	public Integer getTextLimit() {
		return textLimit;
	}

	public Integer getTextWidth() {
		return textWidth;
	}

	public Integer getTextHeight() {
		return textHeight;
	}

	public Integer getIntegerMinValidValue() {
		return integerMinValidValue;
	}

	public Integer getIntegerMaxValidValue() {
		return integerMaxValidValue;
	}

	public String getDirectoryDialogMessageKey() {
		return directoryDialogMessageKey;
	}

	public Boolean getEmptyStringAllowed() {
		return emptyStringAllowed;
	}

	@Override
	public String toString() {
		return "FieldEditorData [comboEntryNamesAndValues=" + Arrays.toString(comboEntryNamesAndValues) + ", scaleMinimum=" + scaleMinimum + ", scaleMaximum=" + scaleMaximum + ", scaleIncrement=" + scaleIncrement + ", scalePageIncrement=" + scalePageIncrement + ", textLimit=" + textLimit + ", textWidth=" + textWidth + ", textHeight=" + textHeight + ", emptyStringAllowed=" + emptyStringAllowed + ", integerMinValidValue=" + integerMinValidValue + ", integerMaxValidValue=" + integerMaxValidValue + ", directoryDialogMessageKey=" + directoryDialogMessageKey + "]";
	}

	public static class FieldEditorDataBuilder {
		private String[][] comboEntryNamesAndValues;
		private Integer scaleMinimum;
		private Integer scaleMaximum;
		private Integer scaleIncrement;
		private Integer scalePageIncrement;
		private Integer textLimit;
		private Integer textWidth;
		private Integer textHeight;
		private Integer integerMinValidValue;
		private Integer integerMaxValidValue;
		private String directoryDialogMessageKey;
		private Boolean emptyStringAllowed;

		public FieldEditorDataBuilder comboEntryNamesAndValues(final String[][] comboEntryNamesAndValues) {
			this.comboEntryNamesAndValues = comboEntryNamesAndValues;
			return this;
		}

		public FieldEditorDataBuilder scaleMinimum(final int scaleMinimum) {
			this.scaleMinimum = scaleMinimum;
			return this;
		}

		public FieldEditorDataBuilder scaleMaximum(final int scaleMaximum) {
			this.scaleMaximum = scaleMaximum;
			return this;
		}

		public FieldEditorDataBuilder scaleIncrement(final int scaleIncrement) {
			this.scaleIncrement = scaleIncrement;
			return this;
		}

		public FieldEditorDataBuilder scalePageIncrement(final int scalePageIncrement) {
			this.scalePageIncrement = scalePageIncrement;
			return this;
		}

		public FieldEditorDataBuilder textLimit(final int textLimit) {
			this.textLimit = textLimit;
			return this;
		}

		public FieldEditorDataBuilder textWidth(final int textWidth) {
			this.textWidth = textWidth;
			return this;
		}

		public FieldEditorDataBuilder textHeight(final int textHeight) {
			this.textHeight = textHeight;
			return this;
		}

		public FieldEditorDataBuilder integerValidRange(final int integerMinValidValue, final int integerMaxValidValue) {
			this.integerMinValidValue = integerMinValidValue;
			this.integerMaxValidValue = integerMaxValidValue;
			return this;
		}

		public FieldEditorDataBuilder directoryDialogMessageKey(final String directoryDialogMessageKey) {
			this.directoryDialogMessageKey = directoryDialogMessageKey;
			return this;
		}

		public FieldEditorDataBuilder emptyStringAllowed(final boolean emptyStringAllowed) {
			this.emptyStringAllowed = emptyStringAllowed;
			return this;
		}

		public FieldEditorData build() {
			return new FieldEditorData(this);
		}
	}

	private FieldEditorData(final FieldEditorDataBuilder builder) {
		this.comboEntryNamesAndValues = builder.comboEntryNamesAndValues;
		this.scaleMinimum = builder.scaleMinimum;
		this.scaleMaximum = builder.scaleMaximum;
		this.scaleIncrement = builder.scaleIncrement;
		this.scalePageIncrement = builder.scalePageIncrement;
		this.textLimit = builder.textLimit;
		this.textWidth = builder.textWidth;
		this.textHeight = builder.textHeight;
		this.integerMinValidValue = builder.integerMinValidValue;
		this.integerMaxValidValue = builder.integerMaxValidValue;
		this.directoryDialogMessageKey = builder.directoryDialogMessageKey;
		this.emptyStringAllowed = builder.emptyStringAllowed;
	}

}
