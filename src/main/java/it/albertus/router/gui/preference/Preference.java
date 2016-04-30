package it.albertus.router.gui.preference;

import it.albertus.router.console.RouterLoggerConsole;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.gui.CloseMessageBox;
import it.albertus.router.gui.DataTable;
import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.gui.TextConsole;
import it.albertus.router.gui.TrayIcon;
import it.albertus.router.gui.preference.FieldEditorData.FieldEditorDataBuilder;
import it.albertus.router.gui.preference.field.ComboFieldEditor;
import it.albertus.router.gui.preference.field.DatabaseComboFieldEditor;
import it.albertus.router.gui.preference.field.FormattedComboFieldEditor;
import it.albertus.router.gui.preference.field.FormattedDirectoryFieldEditor;
import it.albertus.router.gui.preference.field.FormattedIntegerFieldEditor;
import it.albertus.router.gui.preference.field.FormattedStringFieldEditor;
import it.albertus.router.gui.preference.field.IterationsComboFieldEditor;
import it.albertus.router.gui.preference.field.PasswordFieldEditor;
import it.albertus.router.gui.preference.field.ReaderComboFieldEditor;
import it.albertus.router.gui.preference.field.ThresholdsFieldEditor;
import it.albertus.router.gui.preference.field.WrapStringFieldEditor;
import it.albertus.router.gui.preference.field.WriterComboFieldEditor;
import it.albertus.router.gui.preference.page.BasePreferencePage;
import it.albertus.router.gui.preference.page.DatabasePreferencePage;
import it.albertus.router.gui.preference.page.GeneralPreferencePage;
import it.albertus.router.gui.preference.page.Page;
import it.albertus.router.gui.preference.page.ReaderPreferencePage;
import it.albertus.router.gui.preference.page.WriterPreferencePage;
import it.albertus.router.reader.AsusDslN12EReader;
import it.albertus.router.reader.AsusDslN14UReader;
import it.albertus.router.reader.DLinkDsl2750Reader;
import it.albertus.router.reader.Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.util.Logger;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.DatabaseWriter;

import java.util.Locale;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;

public enum Preference {
	LANGUAGE(Page.GENERAL, ComboFieldEditor.class, Locale.getDefault().getLanguage(), new FieldEditorDataBuilder().comboEntryNamesAndValues(GeneralPreferencePage.getLanguageComboOptions()).build()),

	READER_CLASS_NAME(Page.READER, ReaderComboFieldEditor.class, null, new FieldEditorDataBuilder().comboEntryNamesAndValues(ReaderPreferencePage.getReaderComboOptions()).build()),
	ROUTER_USERNAME(Page.READER, FormattedStringFieldEditor.class),
	ROUTER_PASSWORD(Page.READER, PasswordFieldEditor.class),
	ROUTER_ADDRESS(Page.READER, FormattedStringFieldEditor.class, Reader.Defaults.ROUTER_ADDRESS, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	ROUTER_PORT(Page.READER, FormattedIntegerFieldEditor.class, Integer.toString(Reader.Defaults.ROUTER_PORT), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),

	SOCKET_TIMEOUT_MS(Page.READER, FormattedIntegerFieldEditor.class, Integer.toString(Reader.Defaults.SOCKET_TIMEOUT_IN_MILLIS)),
	CONNECTION_TIMEOUT_MS(Page.READER, FormattedIntegerFieldEditor.class, Integer.toString(Reader.Defaults.CONNECTION_TIMEOUT_IN_MILLIS)),
	TELNET_NEWLINE_CHARACTERS(Page.READER, FormattedComboFieldEditor.class, Reader.Defaults.TELNET_NEWLINE_CHARACTERS, new FieldEditorDataBuilder().comboEntryNamesAndValues(BasePreferencePage.getNewLineComboOptions()).build()),

	LOGGER_ITERATIONS(Page.GENERAL, IterationsComboFieldEditor.class, Integer.toString(RouterLoggerEngine.Defaults.ITERATIONS)),
	LOGGER_INTERVAL_NORMAL_MS(Page.GENERAL, FormattedIntegerFieldEditor.class, Long.toString(RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS)),
	LOGGER_INTERVAL_FAST_MS(Page.GENERAL, FormattedIntegerFieldEditor.class, Long.toString(RouterLoggerEngine.Defaults.INTERVAL_FAST_IN_MILLIS)),
	LOGGER_HYSTERESIS_MS(Page.GENERAL, FormattedIntegerFieldEditor.class, Long.toString(RouterLoggerEngine.Defaults.HYSTERESIS_IN_MILLIS)),
	LOGGER_RETRY_COUNT(Page.GENERAL, FormattedIntegerFieldEditor.class, Integer.toString(RouterLoggerEngine.Defaults.RETRIES)),
	LOGGER_RETRY_INTERVAL_MS(Page.GENERAL, FormattedIntegerFieldEditor.class, Long.toString(RouterLoggerEngine.Defaults.RETRY_INTERVAL_IN_MILLIS)),
	LOGGER_ERROR_LOG_DESTINATION_PATH(Page.GENERAL, FormattedDirectoryFieldEditor.class, Logger.Defaults.DIRECTORY, new FieldEditorDataBuilder().emptyStringAllowed(false).directoryDialogMessageKey("msg.preferences.directory.dialog.message.log").build()),

	TPLINK_8970_COMMAND_INFO_ADSL(Page.TPLINK_8970, FormattedStringFieldEditor.class, TpLink8970Reader.Defaults.COMMAND_INFO_ADSL),
	TPLINK_8970_COMMAND_INFO_WAN(Page.TPLINK_8970, FormattedStringFieldEditor.class),
	ASUS_DSLN12E_COMMAND_INFO_ADSL(Page.ASUS_N12E, FormattedStringFieldEditor.class, AsusDslN12EReader.Defaults.COMMAND_INFO_ADSL),
	ASUS_DSLN12E_COMMAND_INFO_WAN(Page.ASUS_N12E, FormattedStringFieldEditor.class, AsusDslN12EReader.Defaults.COMMAND_INFO_WAN),
	ASUS_DSLN14U_COMMAND_INFO_ADSL(Page.ASUS_N14U, FormattedStringFieldEditor.class, AsusDslN14UReader.Defaults.COMMAND_INFO_ADSL),
	ASUS_DSLN14U_COMMAND_INFO_WAN(Page.ASUS_N14U, FormattedStringFieldEditor.class),
	DLINK_2750_COMMAND_INFO_ADSL_STATUS(Page.DLINK_2750, FormattedStringFieldEditor.class, DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_STATUS),
	DLINK_2750_COMMAND_INFO_ADSL_SNR(Page.DLINK_2750, FormattedStringFieldEditor.class, DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_SNR),

	GUI_TABLE_ITEMS_MAX(Page.APPEARANCE, FormattedIntegerFieldEditor.class, Integer.toString(DataTable.Defaults.GUI_TABLE_MAX_ITEMS), new FieldEditorDataBuilder().textLimit(4).build()),
	GUI_CONSOLE_MAX_CHARS(Page.APPEARANCE, FormattedIntegerFieldEditor.class, Integer.toString(TextConsole.Defaults.GUI_CONSOLE_MAX_CHARS), new FieldEditorDataBuilder().textLimit(6).build()),
	GUI_TABLE_COLUMNS_PACK(Page.APPEARANCE, BooleanFieldEditor.class, Boolean.toString(DataTable.Defaults.GUI_TABLE_COLUMNS_PACK)),
	GUI_MINIMIZE_TRAY(Page.APPEARANCE, BooleanFieldEditor.class, Boolean.toString(TrayIcon.Defaults.GUI_MINIMIZE_TRAY)),
	GUI_START_MINIMIZED(Page.APPEARANCE, BooleanFieldEditor.class, Boolean.toString(RouterLoggerGui.Defaults.GUI_START_MINIMIZED)),
	GUI_TRAY_TOOLTIP(Page.APPEARANCE, BooleanFieldEditor.class, Boolean.toString(TrayIcon.Defaults.GUI_TRAY_TOOLTIP)),
	GUI_IMPORTANT_KEYS(Page.APPEARANCE, WrapStringFieldEditor.class),
	GUI_IMPORTANT_KEYS_SEPARATOR(Page.APPEARANCE, FormattedStringFieldEditor.class, RouterLoggerConfiguration.Defaults.GUI_IMPORTANT_KEYS_SEPARATOR, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),

	CONSOLE_ANIMATION(Page.CONSOLE, BooleanFieldEditor.class, Boolean.toString(RouterLoggerConsole.Defaults.CONSOLE_ANIMATION)),
	CONSOLE_SHOW_CONFIGURATION(Page.GENERAL, BooleanFieldEditor.class, Boolean.toString(RouterLoggerEngine.Defaults.CONSOLE_SHOW_CONFIGURATION)),
	CONSOLE_SHOW_KEYS(Page.CONSOLE, WrapStringFieldEditor.class),
	CONSOLE_SHOW_KEYS_SEPARATOR(Page.CONSOLE, FormattedStringFieldEditor.class, RouterLoggerConfiguration.Defaults.CONSOLE_SHOW_KEYS_SEPARATOR, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	CONSOLE_DEBUG(Page.GENERAL, BooleanFieldEditor.class, Boolean.toString(Logger.Defaults.DEBUG)),

	GUI_CONFIRM_CLOSE(Page.GENERAL, BooleanFieldEditor.class, Boolean.toString(CloseMessageBox.Defaults.GUI_CONFIRM_CLOSE)),

	WRITER_CLASS_NAME(Page.WRITER, WriterComboFieldEditor.class, RouterLoggerEngine.Defaults.WRITER_CLASS.getSimpleName(), new FieldEditorDataBuilder().comboEntryNamesAndValues(WriterPreferencePage.getWriterComboOptions()).build()),

	CSV_DESTINATION_PATH(Page.CSV, FormattedDirectoryFieldEditor.class, CsvWriter.Defaults.DIRECTORY, new FieldEditorDataBuilder().emptyStringAllowed(false).directoryDialogMessageKey("msg.preferences.directory.dialog.message.csv").build()),
	CSV_NEWLINE_CHARACTERS(Page.CSV, FormattedComboFieldEditor.class, CsvWriter.Defaults.NEWLINE.name(), new FieldEditorDataBuilder().comboEntryNamesAndValues(BasePreferencePage.getNewLineComboOptions()).build()),
	CSV_FIELD_SEPARATOR(Page.CSV, FormattedStringFieldEditor.class, CsvWriter.Defaults.FIELD_SEPARATOR, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	CSV_FIELD_SEPARATOR_REPLACEMENT(Page.CSV, FormattedStringFieldEditor.class, CsvWriter.Defaults.FIELD_SEPARATOR_REPLACEMENT, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),

	DATABASE_DRIVER_CLASS_NAME(Page.DATABASE, DatabaseComboFieldEditor.class, null, new FieldEditorDataBuilder().comboEntryNamesAndValues(DatabasePreferencePage.getDatabaseComboOptions()).build()),
	DATABASE_URL(Page.DATABASE, FormattedStringFieldEditor.class),
	DATABASE_USERNAME(Page.DATABASE, FormattedStringFieldEditor.class),
	DATABASE_PASSWORD(Page.DATABASE, FormattedStringFieldEditor.class),
	DATABASE_TABLE_NAME(Page.DATABASE, FormattedStringFieldEditor.class, DatabaseWriter.Defaults.TABLE_NAME, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_CONNECTION_VALIDATION_TIMEOUT_MS(Page.DATABASE, FormattedIntegerFieldEditor.class, Integer.toString(DatabaseWriter.Defaults.CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS), new FieldEditorDataBuilder().textLimit(5).build()),
	DATABASE_TIMESTAMP_COLUMN_TYPE(Page.DATABASE, FormattedStringFieldEditor.class, DatabaseWriter.Defaults.TIMESTAMP_COLUMN_TYPE, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_RESPONSE_COLUMN_TYPE(Page.DATABASE, FormattedStringFieldEditor.class, DatabaseWriter.Defaults.RESPONSE_TIME_COLUMN_TYPE, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_INFO_COLUMN_TYPE(Page.DATABASE, FormattedStringFieldEditor.class, DatabaseWriter.Defaults.INFO_COLUMN_TYPE, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_PREFIX(Page.DATABASE, FormattedStringFieldEditor.class, DatabaseWriter.Defaults.COLUMN_NAME_PREFIX, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_MAX_LENGTH(Page.DATABASE, FormattedIntegerFieldEditor.class, Integer.toString(DatabaseWriter.Defaults.COLUMN_NAME_MAX_LENGTH), new FieldEditorDataBuilder().textLimit(2).build()),

	THRESHOLDS_SPLIT(Page.THRESHOLDS, BooleanFieldEditor.class, Boolean.toString(RouterLoggerConfiguration.Defaults.THRESHOLDS_SPLIT)),
	THRESHOLDS_EXCLUDED(Page.THRESHOLDS, WrapStringFieldEditor.class),
	THRESHOLDS_EXCLUDED_SEPARATOR(Page.THRESHOLDS, FormattedStringFieldEditor.class, RouterLoggerConfiguration.Defaults.THRESHOLDS_EXCLUDED_SEPARATOR, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),

	THRESHOLDS_EXPRESSIONS(Page.EXPRESSIONS, ThresholdsFieldEditor.class);

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final String configurationKey;
	private final String labelKey;
	private final Page page;
	private final Class<? extends FieldEditor> fieldEditorClass;
	private final String defaultValue;
	private final FieldEditorData fieldEditorData;

	private Preference(final Page page, final Class<? extends FieldEditor> fieldEditorClass) {
		this(page, fieldEditorClass, null, null, null, null);
	}

	private Preference(final Page page, final Class<? extends FieldEditor> fieldEditorClass, final String defaultValue) {
		this(page, fieldEditorClass, defaultValue, null, null, null);
	}

	private Preference(final Page page, final Class<? extends FieldEditor> fieldEditorClass, final String defaultValue, final FieldEditorData fieldEditorData) {
		this(page, fieldEditorClass, defaultValue, fieldEditorData, null, null);
	}

	private Preference(final Page page, final Class<? extends FieldEditor> fieldEditorClass, final String defaultValue, final FieldEditorData fieldEditorData, final String configurationKey) {
		this(page, fieldEditorClass, defaultValue, fieldEditorData, configurationKey, null);
	}

	private Preference(final Page page, final Class<? extends FieldEditor> fieldEditorClass, final String defaultValue, final FieldEditorData fieldEditorData, final String configurationKey, final String labelKey) {
		// Configuration key...
		if (configurationKey != null && !configurationKey.isEmpty()) {
			this.configurationKey = configurationKey;
		}
		else {
			this.configurationKey = name().toLowerCase().replace('_', '.');
		}

		// Label key...
		if (labelKey != null && !labelKey.isEmpty()) {
			this.labelKey = labelKey;
		}
		else {
			this.labelKey = LABEL_KEY_PREFIX + this.configurationKey;
		}

		this.fieldEditorData = fieldEditorData;
		this.defaultValue = defaultValue;
		this.fieldEditorClass = fieldEditorClass;
		this.page = page;
	}

	public String getConfigurationKey() {
		return configurationKey;
	}

	public String getLabelKey() {
		return labelKey;
	}

	public Page getPage() {
		return page;
	}

	public Class<? extends FieldEditor> getFieldEditorClass() {
		return fieldEditorClass;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public FieldEditorData getFieldEditorData() {
		return fieldEditorData;
	}

	public static Preference forConfigurationKey(final String configurationKey) {
		if (configurationKey != null) {
			for (final Preference preference : Preference.values()) {
				if (configurationKey.equals(preference.configurationKey)) {
					return preference;
				}
			}
		}
		return null;
	}

}
