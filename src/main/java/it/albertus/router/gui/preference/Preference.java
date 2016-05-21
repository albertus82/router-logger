package it.albertus.router.gui.preference;

import it.albertus.router.console.RouterLoggerConsole;
import it.albertus.router.email.EmailSender;
import it.albertus.router.email.ThresholdsEmailSender;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.gui.CloseMessageBox;
import it.albertus.router.gui.DataTable;
import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.gui.TextConsole;
import it.albertus.router.gui.TrayIcon;
import it.albertus.router.gui.preference.FieldEditorData.FieldEditorDataBuilder;
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
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.web.StatusHandler;
import it.albertus.router.web.WebServer;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.DatabaseWriter;

import java.util.Locale;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

public enum Preference {
	LANGUAGE(Page.GENERAL, FieldEditorType.Combo, Locale.getDefault().getLanguage(), new FieldEditorDataBuilder().comboEntryNamesAndValues(GeneralPreferencePage.getLanguageComboOptions()).build()),
	LOGGER_ITERATIONS(Page.GENERAL, FieldEditorType.IterationsCombo, Integer.toString(RouterLoggerEngine.Defaults.ITERATIONS)),
	LOGGER_INTERVAL_NORMAL_MS(Page.GENERAL, FieldEditorType.FormattedInteger, Long.toString(RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS)),
	LOGGER_INTERVAL_FAST_MS(Page.GENERAL, FieldEditorType.FormattedInteger, Long.toString(RouterLoggerEngine.Defaults.INTERVAL_FAST_IN_MILLIS)),
	LOGGER_HYSTERESIS_MS(Page.GENERAL, FieldEditorType.FormattedInteger, Long.toString(RouterLoggerEngine.Defaults.HYSTERESIS_IN_MILLIS)),
	LOGGER_RETRY_COUNT(Page.GENERAL, FieldEditorType.FormattedInteger, Integer.toString(RouterLoggerEngine.Defaults.RETRIES)),
	LOGGER_RETRY_INTERVAL_MS(Page.GENERAL, FieldEditorType.FormattedInteger, Long.toString(RouterLoggerEngine.Defaults.RETRY_INTERVAL_IN_MILLIS)),
	LOGGER_ERROR_LOG_DESTINATION_PATH(Page.GENERAL, FieldEditorType.FormattedDirectory, Logger.Defaults.DIRECTORY, new FieldEditorDataBuilder().emptyStringAllowed(false).directoryDialogMessageKey("msg.preferences.directory.dialog.message.log").build()),
	GUI_MINIMIZE_TRAY(Page.GENERAL, FieldEditorType.DefaultBoolean, Boolean.toString(TrayIcon.Defaults.GUI_MINIMIZE_TRAY)),
	GUI_TRAY_TOOLTIP(Page.GENERAL, FieldEditorType.DefaultBoolean, Boolean.toString(TrayIcon.Defaults.GUI_TRAY_TOOLTIP)),
	GUI_START_MINIMIZED(Page.GENERAL, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerGui.Defaults.GUI_START_MINIMIZED)),
	GUI_CONFIRM_CLOSE(Page.GENERAL, FieldEditorType.DefaultBoolean, Boolean.toString(CloseMessageBox.Defaults.GUI_CONFIRM_CLOSE)),
	CONSOLE_SHOW_CONFIGURATION(Page.GENERAL, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerEngine.Defaults.CONSOLE_SHOW_CONFIGURATION)),
	CONSOLE_DEBUG(Page.GENERAL, FieldEditorType.DefaultBoolean, Boolean.toString(Logger.Defaults.DEBUG)),
	LOG_EMAIL(Page.GENERAL, FieldEditorType.DefaultBoolean, Boolean.toString(Logger.Defaults.EMAIL)),

	READER_CLASS_NAME(Page.READER, FieldEditorType.ReaderCombo, null, new FieldEditorDataBuilder().comboEntryNamesAndValues(ReaderPreferencePage.getReaderComboOptions()).build()),
	ROUTER_USERNAME(Page.READER, FieldEditorType.FormattedString),
	ROUTER_PASSWORD(Page.READER, FieldEditorType.Password),
	ROUTER_ADDRESS(Page.READER, FieldEditorType.FormattedString, Reader.Defaults.ROUTER_ADDRESS, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	ROUTER_PORT(Page.READER, FieldEditorType.FormattedInteger, Integer.toString(Reader.Defaults.ROUTER_PORT), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	SOCKET_TIMEOUT_MS(Page.READER, FieldEditorType.FormattedInteger, Integer.toString(Reader.Defaults.SOCKET_TIMEOUT_IN_MILLIS)),
	CONNECTION_TIMEOUT_MS(Page.READER, FieldEditorType.FormattedInteger, Integer.toString(Reader.Defaults.CONNECTION_TIMEOUT_IN_MILLIS)),
	TELNET_NEWLINE_CHARACTERS(Page.READER, FieldEditorType.FormattedCombo, Reader.Defaults.TELNET_NEWLINE_CHARACTERS, new FieldEditorDataBuilder().comboEntryNamesAndValues(BasePreferencePage.getNewLineComboOptions()).build()),

	TPLINK_8970_COMMAND_INFO_ADSL(Page.TPLINK_8970, FieldEditorType.FormattedString, TpLink8970Reader.Defaults.COMMAND_INFO_ADSL),
	TPLINK_8970_COMMAND_INFO_WAN(Page.TPLINK_8970, FieldEditorType.FormattedString),
	ASUS_DSLN12E_COMMAND_INFO_ADSL(Page.ASUS_N12E, FieldEditorType.FormattedString, AsusDslN12EReader.Defaults.COMMAND_INFO_ADSL),
	ASUS_DSLN12E_COMMAND_INFO_WAN(Page.ASUS_N12E, FieldEditorType.FormattedString, AsusDslN12EReader.Defaults.COMMAND_INFO_WAN),
	ASUS_DSLN14U_COMMAND_INFO_ADSL(Page.ASUS_N14U, FieldEditorType.FormattedString, AsusDslN14UReader.Defaults.COMMAND_INFO_ADSL),
	ASUS_DSLN14U_COMMAND_INFO_WAN(Page.ASUS_N14U, FieldEditorType.FormattedString),
	DLINK_2750_COMMAND_INFO_ADSL_STATUS(Page.DLINK_2750, FieldEditorType.FormattedString, DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_STATUS),
	DLINK_2750_COMMAND_INFO_ADSL_SNR(Page.DLINK_2750, FieldEditorType.FormattedString, DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_SNR),

	GUI_TABLE_ITEMS_MAX(Page.APPEARANCE, FieldEditorType.FormattedInteger, Integer.toString(DataTable.Defaults.GUI_TABLE_MAX_ITEMS), new FieldEditorDataBuilder().textLimit(4).build()),
	GUI_IMPORTANT_KEYS(Page.APPEARANCE, FieldEditorType.WrapString),
	GUI_IMPORTANT_KEYS_SEPARATOR(Page.APPEARANCE, FieldEditorType.FormattedString, RouterLoggerConfiguration.Defaults.GUI_IMPORTANT_KEYS_SEPARATOR, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	GUI_TABLE_COLUMNS_PACK(Page.APPEARANCE, FieldEditorType.DefaultBoolean, Boolean.toString(DataTable.Defaults.GUI_TABLE_COLUMNS_PACK)),
	GUI_CONSOLE_MAX_CHARS(Page.APPEARANCE, FieldEditorType.FormattedInteger, Integer.toString(TextConsole.Defaults.GUI_CONSOLE_MAX_CHARS), new FieldEditorDataBuilder().textLimit(6).build()),

	CONSOLE_ANIMATION(Page.CONSOLE, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerConsole.Defaults.CONSOLE_ANIMATION)),
	CONSOLE_SHOW_KEYS(Page.CONSOLE, FieldEditorType.WrapString),
	CONSOLE_SHOW_KEYS_SEPARATOR(Page.CONSOLE, FieldEditorType.FormattedString, RouterLoggerConfiguration.Defaults.CONSOLE_SHOW_KEYS_SEPARATOR, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),

	WRITER_CLASS_NAME(Page.WRITER, FieldEditorType.WriterCombo, RouterLoggerEngine.Defaults.WRITER_CLASS.getSimpleName(), new FieldEditorDataBuilder().comboEntryNamesAndValues(WriterPreferencePage.getWriterComboOptions()).build()),

	CSV_DESTINATION_PATH(Page.CSV, FieldEditorType.FormattedDirectory, CsvWriter.Defaults.DIRECTORY, new FieldEditorDataBuilder().emptyStringAllowed(false).directoryDialogMessageKey("msg.preferences.directory.dialog.message.csv").build()),
	CSV_NEWLINE_CHARACTERS(Page.CSV, FieldEditorType.FormattedCombo, CsvWriter.Defaults.NEWLINE.name(), new FieldEditorDataBuilder().comboEntryNamesAndValues(BasePreferencePage.getNewLineComboOptions()).build()),
	CSV_FIELD_SEPARATOR(Page.CSV, FieldEditorType.FormattedString, CsvWriter.Defaults.FIELD_SEPARATOR, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	CSV_FIELD_SEPARATOR_REPLACEMENT(Page.CSV, FieldEditorType.FormattedString, CsvWriter.Defaults.FIELD_SEPARATOR_REPLACEMENT, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	CSV_EMAIL(Page.CSV, FieldEditorType.DefaultBoolean, Boolean.toString(CsvWriter.Defaults.EMAIL)),

	DATABASE_DRIVER_CLASS_NAME(Page.DATABASE, FieldEditorType.DatabaseCombo, null, new FieldEditorDataBuilder().comboEntryNamesAndValues(DatabasePreferencePage.getDatabaseComboOptions()).build()),
	DATABASE_URL(Page.DATABASE, FieldEditorType.FormattedString),
	DATABASE_USERNAME(Page.DATABASE, FieldEditorType.FormattedString),
	DATABASE_PASSWORD(Page.DATABASE, FieldEditorType.Password),
	DATABASE_TABLE_NAME(Page.DATABASE, FieldEditorType.FormattedString, DatabaseWriter.Defaults.TABLE_NAME, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_CONNECTION_VALIDATION_TIMEOUT_MS(Page.DATABASE, FieldEditorType.FormattedInteger, Integer.toString(DatabaseWriter.Defaults.CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS), new FieldEditorDataBuilder().textLimit(5).build()),
	DATABASE_TIMESTAMP_COLUMN_TYPE(Page.DATABASE, FieldEditorType.FormattedString, DatabaseWriter.Defaults.TIMESTAMP_COLUMN_TYPE, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_RESPONSE_COLUMN_TYPE(Page.DATABASE, FieldEditorType.FormattedString, DatabaseWriter.Defaults.RESPONSE_TIME_COLUMN_TYPE, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_INFO_COLUMN_TYPE(Page.DATABASE, FieldEditorType.FormattedString, DatabaseWriter.Defaults.INFO_COLUMN_TYPE, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_PREFIX(Page.DATABASE, FieldEditorType.FormattedString, DatabaseWriter.Defaults.COLUMN_NAME_PREFIX, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_MAX_LENGTH(Page.DATABASE, FieldEditorType.FormattedInteger, Integer.toString(DatabaseWriter.Defaults.COLUMN_NAME_MAX_LENGTH), new FieldEditorDataBuilder().textLimit(2).build()),

	THRESHOLDS_EXPRESSIONS(Page.THRESHOLDS, FieldEditorType.Thresholds),
	THRESHOLDS_SPLIT(Page.THRESHOLDS, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerConfiguration.Defaults.THRESHOLDS_SPLIT)),
	THRESHOLDS_EMAIL(Page.THRESHOLDS, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerEngine.Defaults.THRESHOLDS_EMAIL)),
	THRESHOLDS_EMAIL_SEND_INTERVAL_SECS(Page.THRESHOLDS, FieldEditorType.FormattedInteger, Integer.toString(ThresholdsEmailSender.Defaults.THRESHOLDS_EMAIL_SEND_INTERVAL_SECS)),
	THRESHOLDS_EXCLUDED(Page.THRESHOLDS, FieldEditorType.WrapString),
	THRESHOLDS_EXCLUDED_SEPARATOR(Page.THRESHOLDS, FieldEditorType.FormattedString, RouterLoggerConfiguration.Defaults.THRESHOLDS_EXCLUDED_SEPARATOR, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),

	EMAIL_HOST(Page.EMAIL, FieldEditorType.FormattedString),
	EMAIL_USERNAME(Page.EMAIL, FieldEditorType.FormattedString),
	EMAIL_PASSWORD(Page.EMAIL, FieldEditorType.Password),
	EMAIL_FROM_NAME(Page.EMAIL, FieldEditorType.FormattedString),
	EMAIL_FROM_ADDRESS(Page.EMAIL, FieldEditorType.FormattedString),
	EMAIL_TO_ADDRESSES(Page.EMAIL, FieldEditorType.EmailAddresses),
	EMAIL_SSL_CONNECT(Page.EMAIL, FieldEditorType.DefaultBoolean, Boolean.toString(EmailSender.Defaults.SSL_CONNECT)),
	EMAIL_SSL_IDENTITY(Page.EMAIL, FieldEditorType.DefaultBoolean, Boolean.toString(EmailSender.Defaults.SSL_IDENTITY)),
	EMAIL_STARTTLS_ENABLED(Page.EMAIL, FieldEditorType.DefaultBoolean, Boolean.toString(EmailSender.Defaults.STARTTLS_ENABLED)),
	EMAIL_STARTTLS_REQUIRED(Page.EMAIL, FieldEditorType.DefaultBoolean, Boolean.toString(EmailSender.Defaults.STARTTLS_REQUIRED)),

	EMAIL_PORT(Page.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, Integer.toString(EmailSender.Defaults.PORT), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	EMAIL_SSL_PORT(Page.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, EmailSender.Defaults.SSL_PORT, new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	EMAIL_CONNECTION_TIMEOUT(Page.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, Integer.toString(EmailSender.Defaults.SOCKET_CONNECTION_TIMEOUT)),
	EMAIL_SOCKET_TIMEOUT(Page.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, Integer.toString(EmailSender.Defaults.SOCKET_TIMEOUT)),
	EMAIL_RETRY_INTERVAL_SECS(Page.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, Integer.toString(EmailSender.Defaults.RETRY_INTERVAL_SECS)),
	EMAIL_CC_ADDRESSES(Page.EMAIL_ADVANCED, FieldEditorType.EmailAddresses),
	EMAIL_BCC_ADDRESSES(Page.EMAIL_ADVANCED, FieldEditorType.EmailAddresses),

	SERVER_ENABLED(Page.WEBSERVER, FieldEditorType.DefaultBoolean, Boolean.toString(WebServer.Defaults.ENABLED)),
	SERVER_USERNAME(Page.WEBSERVER, FieldEditorType.FormattedString, null, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	SERVER_PASSWORD(Page.WEBSERVER, FieldEditorType.Password, null, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	SERVER_PORT(Page.WEBSERVER, FieldEditorType.FormattedInteger, Integer.toString(WebServer.Defaults.PORT), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	SERVER_STATUS_REFRESH(Page.WEBSERVER, FieldEditorType.DefaultBoolean, Boolean.toString(StatusHandler.Defaults.REFRESH)),
	SERVER_STATUS_REFRESH_SECS(Page.WEBSERVER, FieldEditorType.FormattedInteger, Integer.toString(0), new FieldEditorDataBuilder().integerValidRange(0, 999999999).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final Page page;
	private final FieldEditorType fieldEditorType;
	private final String defaultValue;
	private final FieldEditorData fieldEditorData;
	private final String configurationKey;
	private final String labelKey;

	private Preference(final Page page, final FieldEditorType fieldEditorType) {
		this(page, fieldEditorType, null, null, null, null);
	}

	private Preference(final Page page, final FieldEditorType fieldEditorType, final String defaultValue) {
		this(page, fieldEditorType, defaultValue, null, null, null);
	}

	private Preference(final Page page, final FieldEditorType fieldEditorType, final String defaultValue, final FieldEditorData fieldEditorData) {
		this(page, fieldEditorType, defaultValue, fieldEditorData, null, null);
	}

	private Preference(final Page page, final FieldEditorType fieldEditorType, final String defaultValue, final FieldEditorData fieldEditorData, final String configurationKey) {
		this(page, fieldEditorType, defaultValue, fieldEditorData, configurationKey, null);
	}

	private Preference(final Page page, final FieldEditorType fieldEditorType, final String defaultValue, final FieldEditorData fieldEditorData, final String configurationKey, final String labelKey) {
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
		this.fieldEditorType = fieldEditorType;
		this.page = page;
	}

	public FieldEditor createFieldEditor(final Composite parent) {
		return FieldEditorFactory.createFieldEditor(fieldEditorType, configurationKey, Resources.get(labelKey), parent, fieldEditorData);
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

	public FieldEditorType getFieldEditorType() {
		return fieldEditorType;
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
