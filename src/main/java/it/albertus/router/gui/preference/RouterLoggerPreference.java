package it.albertus.router.gui.preference;

import it.albertus.jface.TextConsole;
import it.albertus.jface.preference.FieldEditorData;
import it.albertus.jface.preference.FieldEditorData.FieldEditorDataBuilder;
import it.albertus.jface.preference.LocalizedComboEntryNamesAndValues;
import it.albertus.jface.preference.Preference;
import it.albertus.jface.preference.page.AbstractPreferencePage;
import it.albertus.jface.preference.page.Page;
import it.albertus.router.console.RouterLoggerConsole;
import it.albertus.router.email.EmailSender;
import it.albertus.router.email.ThresholdsEmailSender;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.gui.CloseMessageBox;
import it.albertus.router.gui.DataTable;
import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.gui.TrayIcon;
import it.albertus.router.gui.preference.PreferenceData.PreferenceDataBuilder;
import it.albertus.router.gui.preference.page.AdvancedMqttPreferencePage;
import it.albertus.router.gui.preference.page.CsvPreferencePage;
import it.albertus.router.gui.preference.page.DatabasePreferencePage;
import it.albertus.router.gui.preference.page.GeneralPreferencePage;
import it.albertus.router.gui.preference.page.MqttPreferencePage;
import it.albertus.router.gui.preference.page.ReaderPreferencePage;
import it.albertus.router.gui.preference.page.RouterLoggerPage;
import it.albertus.router.gui.preference.page.ServerHttpsPreferencePage;
import it.albertus.router.gui.preference.page.ServerPreferencePage;
import it.albertus.router.gui.preference.page.WriterPreferencePage;
import it.albertus.router.mqtt.RouterLoggerMqttClient;
import it.albertus.router.reader.AsusDslN12EReader;
import it.albertus.router.reader.AsusDslN14UReader;
import it.albertus.router.reader.DLinkDsl2750Reader;
import it.albertus.router.reader.Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.resources.Resources;
import it.albertus.router.server.BaseHttpHandler;
import it.albertus.router.server.BaseHttpServer;
import it.albertus.router.server.CloseHandler;
import it.albertus.router.server.ConnectHandler;
import it.albertus.router.server.DisconnectHandler;
import it.albertus.router.server.RestartHandler;
import it.albertus.router.server.RootHandler;
import it.albertus.router.server.StatusHandler;
import it.albertus.router.server.WebServer;
import it.albertus.router.util.Logger;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.DatabaseWriter;
import it.albertus.util.Localized;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

public enum RouterLoggerPreference implements Preference {

	LANGUAGE(RouterLoggerPage.GENERAL, FieldEditorType.Combo, new PreferenceDataBuilder().defaultValue(Locale.getDefault().getLanguage()).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(GeneralPreferencePage.getLanguageComboOptions()).build()),
	LOGGER_ITERATIONS(RouterLoggerPage.GENERAL, FieldEditorType.IntegerCombo, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.ITERATIONS).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(new LocalizedComboEntryNamesAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.iterations.infinite");
		}
	}, 0)).build()),
	LOGGER_CLOSE_WHEN_FINISHED(RouterLoggerPage.GENERAL, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.CLOSE_WHEN_FINISHED).build()),
	LOGGER_INTERVAL_NORMAL_MS(RouterLoggerPage.GENERAL, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS).build()),
	LOGGER_INTERVAL_FAST_MS(RouterLoggerPage.GENERAL, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.INTERVAL_FAST_IN_MILLIS).build()),
	LOGGER_HYSTERESIS_MS(RouterLoggerPage.GENERAL, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.HYSTERESIS_IN_MILLIS).build()),
	LOGGER_RETRY_COUNT(RouterLoggerPage.GENERAL, FieldEditorType.IntegerCombo, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.RETRIES).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(new LocalizedComboEntryNamesAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.logger.retry.count.infinite");
		}
	}, 0)).build()),
	LOGGER_RETRY_INTERVAL_MS(RouterLoggerPage.GENERAL, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.RETRY_INTERVAL_IN_MILLIS).build()),
	LOGGER_ERROR_LOG_DESTINATION_PATH(RouterLoggerPage.GENERAL, FieldEditorType.FormattedDirectory, new PreferenceDataBuilder().defaultValue(Logger.Defaults.DIRECTORY).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.log");
		}
	}).build()),
	CONSOLE_SHOW_CONFIGURATION(RouterLoggerPage.GENERAL, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.CONSOLE_SHOW_CONFIGURATION).build()),
	CONSOLE_DEBUG(RouterLoggerPage.GENERAL, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(Logger.Defaults.DEBUG).build()),
	LOG_EMAIL(RouterLoggerPage.GENERAL, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(Logger.Defaults.EMAIL).build()),
	LOG_EMAIL_IGNORE_DUPLICATES(RouterLoggerPage.GENERAL, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(Logger.Defaults.EMAIL_IGNORE_DUPLICATES).parent(LOG_EMAIL).build()),

	READER_CLASS_NAME(RouterLoggerPage.READER, FieldEditorType.ReaderCombo, new FieldEditorDataBuilder().comboEntryNamesAndValues(ReaderPreferencePage.getReaderComboOptions()).build()),
	ROUTER_USERNAME(RouterLoggerPage.READER, FieldEditorType.FormattedString),
	ROUTER_PASSWORD(RouterLoggerPage.READER, FieldEditorType.Password),
	ROUTER_ADDRESS(RouterLoggerPage.READER, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(Reader.Defaults.ROUTER_ADDRESS).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	ROUTER_PORT(RouterLoggerPage.READER, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(Reader.Defaults.ROUTER_PORT).build(), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	SOCKET_TIMEOUT_MS(RouterLoggerPage.READER, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(Reader.Defaults.SOCKET_TIMEOUT_IN_MILLIS).build()),
	CONNECTION_TIMEOUT_MS(RouterLoggerPage.READER, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(Reader.Defaults.CONNECTION_TIMEOUT_IN_MILLIS).build()),
	TELNET_NEWLINE_CHARACTERS(RouterLoggerPage.READER, FieldEditorType.FormattedCombo, new PreferenceDataBuilder().defaultValue(Reader.Defaults.TELNET_NEWLINE_CHARACTERS).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(AbstractPreferencePage.getNewLineComboOptions()).build()),
	READER_LOG_CONNECTED(RouterLoggerPage.READER, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.LOG_CONNECTED).build()),
	READER_WAIT_DISCONNECTED(RouterLoggerPage.READER, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED).build()),
	READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD(RouterLoggerPage.READER, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD).parent(READER_WAIT_DISCONNECTED).build()),
	READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD_MS(RouterLoggerPage.READER, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD_IN_MILLIS).parent(READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD).build()),

	TPLINK_8970_COMMAND_INFO_ADSL(RouterLoggerPage.TPLINK_8970, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(TpLink8970Reader.Defaults.COMMAND_INFO_ADSL).build()),
	TPLINK_8970_COMMAND_INFO_WAN(RouterLoggerPage.TPLINK_8970, FieldEditorType.FormattedString),
	ASUS_DSLN12E_COMMAND_INFO_ADSL(RouterLoggerPage.ASUS_N12E, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(AsusDslN12EReader.Defaults.COMMAND_INFO_ADSL).build()),
	ASUS_DSLN12E_COMMAND_INFO_WAN(RouterLoggerPage.ASUS_N12E, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(AsusDslN12EReader.Defaults.COMMAND_INFO_WAN).build()),
	ASUS_DSLN14U_COMMAND_INFO_ADSL(RouterLoggerPage.ASUS_N14U, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(AsusDslN14UReader.Defaults.COMMAND_INFO_ADSL).build()),
	ASUS_DSLN14U_COMMAND_INFO_WAN(RouterLoggerPage.ASUS_N14U, FieldEditorType.FormattedString),
	DLINK_2750_COMMAND_INFO_ADSL_STATUS(RouterLoggerPage.DLINK_2750, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_STATUS).build()),
	DLINK_2750_COMMAND_INFO_ADSL_SNR(RouterLoggerPage.DLINK_2750, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_SNR).build()),

	GUI_TABLE_ITEMS_MAX(RouterLoggerPage.APPEARANCE, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(DataTable.Defaults.MAX_ITEMS).build(), new FieldEditorDataBuilder().textLimit(4).build()),
	GUI_IMPORTANT_KEYS(RouterLoggerPage.APPEARANCE, FieldEditorType.WrapString),
	GUI_IMPORTANT_KEYS_SEPARATOR(RouterLoggerPage.APPEARANCE, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(RouterLoggerConfiguration.Defaults.GUI_IMPORTANT_KEYS_SEPARATOR).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	GUI_TABLE_COLUMNS_PACK(RouterLoggerPage.APPEARANCE, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(DataTable.Defaults.COLUMNS_PACK).build()),
	GUI_TABLE_COLUMNS_PADDING_RIGHT(RouterLoggerPage.APPEARANCE, FieldEditorType.ScaleInteger, new PreferenceDataBuilder().defaultValue(DataTable.Defaults.COLUMNS_PADDING_RIGHT).build(), new FieldEditorDataBuilder().scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	GUI_CONSOLE_MAX_CHARS(RouterLoggerPage.APPEARANCE, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(TextConsole.Defaults.GUI_CONSOLE_MAX_CHARS).build(), new FieldEditorDataBuilder().textLimit(6).build()),
	GUI_CLIPBOARD_MAX_CHARS(RouterLoggerPage.APPEARANCE, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(RouterLoggerGui.Defaults.GUI_CLIPBOARD_MAX_CHARS).build(), new FieldEditorDataBuilder().integerValidRange(0, 128 * 1024).build()),
	GUI_MINIMIZE_TRAY(RouterLoggerPage.APPEARANCE, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(TrayIcon.Defaults.GUI_MINIMIZE_TRAY).build()),
	GUI_TRAY_TOOLTIP(RouterLoggerPage.APPEARANCE, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(TrayIcon.Defaults.GUI_TRAY_TOOLTIP).parent(GUI_MINIMIZE_TRAY).build()),
	GUI_START_MINIMIZED(RouterLoggerPage.APPEARANCE, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerGui.Defaults.GUI_START_MINIMIZED).build()),
	GUI_CONFIRM_CLOSE(RouterLoggerPage.APPEARANCE, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(CloseMessageBox.Defaults.GUI_CONFIRM_CLOSE).build()),

	CONSOLE_ANIMATION(RouterLoggerPage.CONSOLE, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerConsole.Defaults.CONSOLE_ANIMATION).build()),
	CONSOLE_SHOW_KEYS(RouterLoggerPage.CONSOLE, FieldEditorType.WrapString),
	CONSOLE_SHOW_KEYS_SEPARATOR(RouterLoggerPage.CONSOLE, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(RouterLoggerConfiguration.Defaults.CONSOLE_SHOW_KEYS_SEPARATOR).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),

	WRITER_CLASS_NAME(RouterLoggerPage.WRITER, FieldEditorType.WriterCombo, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.WRITER_CLASS.getSimpleName()).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(WriterPreferencePage.getWriterComboOptions()).build()),

	CSV_DESTINATION_PATH(RouterLoggerPage.CSV, FieldEditorType.FormattedDirectory, new PreferenceDataBuilder().defaultValue(CsvWriter.Defaults.DIRECTORY).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.csv");
		}
	}).build()),
	CSV_NEWLINE_CHARACTERS(RouterLoggerPage.CSV, FieldEditorType.FormattedCombo, new PreferenceDataBuilder().defaultValue(CsvWriter.Defaults.NEWLINE.name()).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(AbstractPreferencePage.getNewLineComboOptions()).build()),
	CSV_FIELD_SEPARATOR(RouterLoggerPage.CSV, FieldEditorType.DelimiterCombo, new PreferenceDataBuilder().defaultValue(CsvWriter.Defaults.FIELD_SEPARATOR).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).comboEntryNamesAndValues(CsvPreferencePage.getSeparatorComboOptions()).build()),
	CSV_FIELD_SEPARATOR_REPLACEMENT(RouterLoggerPage.CSV, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(CsvWriter.Defaults.FIELD_SEPARATOR_REPLACEMENT).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	CSV_EMAIL(RouterLoggerPage.CSV, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(CsvWriter.Defaults.EMAIL).build()),

	DATABASE_DRIVER_CLASS_NAME(RouterLoggerPage.DATABASE, FieldEditorType.DatabaseCombo, new FieldEditorDataBuilder().comboEntryNamesAndValues(DatabasePreferencePage.getDatabaseComboOptions()).build()),
	DATABASE_URL(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString),
	DATABASE_USERNAME(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString),
	DATABASE_PASSWORD(RouterLoggerPage.DATABASE, FieldEditorType.Password),
	DATABASE_TABLE_NAME(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.TABLE_NAME).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_CONNECTION_VALIDATION_TIMEOUT_MS(RouterLoggerPage.DATABASE, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS).build(), new FieldEditorDataBuilder().textLimit(5).build()),
	DATABASE_TIMESTAMP_COLUMN_TYPE(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.TIMESTAMP_COLUMN_TYPE).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_RESPONSE_COLUMN_TYPE(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.RESPONSE_TIME_COLUMN_TYPE).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_INFO_COLUMN_TYPE(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.INFO_COLUMN_TYPE).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_PREFIX(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.COLUMN_NAME_PREFIX).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_MAX_LENGTH(RouterLoggerPage.DATABASE, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.COLUMN_NAME_MAX_LENGTH).build(), new FieldEditorDataBuilder().textLimit(2).build()),

	THRESHOLDS_EXPRESSIONS(RouterLoggerPage.THRESHOLDS, FieldEditorType.Thresholds),
	THRESHOLDS_SPLIT(RouterLoggerPage.THRESHOLDS, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerConfiguration.Defaults.THRESHOLDS_SPLIT).build()),
	THRESHOLDS_EMAIL(RouterLoggerPage.THRESHOLDS, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.THRESHOLDS_EMAIL).build()),
	THRESHOLDS_EMAIL_SEND_INTERVAL_SECS(RouterLoggerPage.THRESHOLDS, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(ThresholdsEmailSender.Defaults.THRESHOLDS_EMAIL_SEND_INTERVAL_SECS).parent(THRESHOLDS_EMAIL).build()),
	THRESHOLDS_EMAIL_MAX_ITEMS(RouterLoggerPage.THRESHOLDS, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(ThresholdsEmailSender.Defaults.MAX_ITEMS).parent(THRESHOLDS_EMAIL).build(), new FieldEditorDataBuilder().integerValidRange(1, 1000).build()),
	THRESHOLDS_EXCLUDED(RouterLoggerPage.THRESHOLDS, FieldEditorType.WrapString),
	THRESHOLDS_EXCLUDED_SEPARATOR(RouterLoggerPage.THRESHOLDS, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(RouterLoggerConfiguration.Defaults.THRESHOLDS_EXCLUDED_SEPARATOR).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),

	EMAIL_HOST(RouterLoggerPage.EMAIL, FieldEditorType.FormattedString),
	EMAIL_USERNAME(RouterLoggerPage.EMAIL, FieldEditorType.FormattedString),
	EMAIL_PASSWORD(RouterLoggerPage.EMAIL, FieldEditorType.Password),
	EMAIL_FROM_NAME(RouterLoggerPage.EMAIL, FieldEditorType.FormattedString),
	EMAIL_FROM_ADDRESS(RouterLoggerPage.EMAIL, FieldEditorType.FormattedString),
	EMAIL_TO_ADDRESSES(RouterLoggerPage.EMAIL, FieldEditorType.EmailAddresses, new FieldEditorDataBuilder().horizontalSpan(Short.MAX_VALUE).build()),

	EMAIL_PORT(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.PORT).build(), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	EMAIL_SSL_PORT(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.SSL_PORT).build(), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	EMAIL_CONNECTION_TIMEOUT(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.SOCKET_CONNECTION_TIMEOUT).build()),
	EMAIL_SOCKET_TIMEOUT(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.SOCKET_TIMEOUT).build()),
	EMAIL_RETRY_INTERVAL_SECS(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.RETRY_INTERVAL_SECS).build()),
	EMAIL_MAX_SENDINGS_PER_CYCLE(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.IntegerCombo, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.MAX_SENDINGS_PER_CYCLE).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(new LocalizedComboEntryNamesAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.email.max.sendings.per.cycle.unlimited");
		}
	}, 0)).build()),
	EMAIL_MAX_QUEUE_SIZE(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.ScaleInteger, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.MAX_QUEUE_SIZE).build(), new FieldEditorDataBuilder().scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	EMAIL_SSL_CONNECT(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.SSL_CONNECT).build()),
	EMAIL_SSL_IDENTITY(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.SSL_IDENTITY).build()),
	EMAIL_STARTTLS_ENABLED(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.STARTTLS_ENABLED).build()),
	EMAIL_STARTTLS_REQUIRED(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.STARTTLS_REQUIRED).build()),

	EMAIL_CC_ADDRESSES(RouterLoggerPage.EMAIL_CC_BCC, FieldEditorType.EmailAddresses, new FieldEditorDataBuilder().horizontalSpan(0).build()),
	EMAIL_BCC_ADDRESSES(RouterLoggerPage.EMAIL_CC_BCC, FieldEditorType.EmailAddresses, new FieldEditorDataBuilder().horizontalSpan(0).build()),

	SERVER_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(WebServer.Defaults.ENABLED).build()),
	SERVER_USERNAME(RouterLoggerPage.SERVER, FieldEditorType.FormattedString, new PreferenceDataBuilder().parent(SERVER_ENABLED).build()),
	SERVER_PASSWORD(RouterLoggerPage.SERVER, FieldEditorType.Password, new PreferenceDataBuilder().parent(SERVER_ENABLED).build()),
	SERVER_PORT(RouterLoggerPage.SERVER, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(WebServer.Defaults.PORT).parent(SERVER_ENABLED).build(), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	SERVER_COMPRESS_RESPONSE(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(BaseHttpHandler.Defaults.COMPRESS_RESPONSE).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_ROOT_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RootHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_RESTART_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RestartHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_CONNECT_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(ConnectHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_DISCONNECT_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(DisconnectHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_CLOSE_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(CloseHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_STATUS_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(StatusHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_STATUS_REFRESH(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(StatusHandler.Defaults.REFRESH).parent(SERVER_HANDLER_STATUS_ENABLED).build()),
	SERVER_HANDLER_STATUS_REFRESH_SECS(RouterLoggerPage.SERVER, FieldEditorType.IntegerCombo, new PreferenceDataBuilder().defaultValue(StatusHandler.Defaults.REFRESH_SECS).parent(SERVER_HANDLER_STATUS_REFRESH).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(new LocalizedComboEntryNamesAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.server.handler.status.refresh.auto");
		}
	}, 0)).build()),
	SERVER_LOG_REQUEST(RouterLoggerPage.SERVER, FieldEditorType.FormattedCombo, new PreferenceDataBuilder().defaultValue(BaseHttpHandler.Defaults.LOG_REQUEST).parent(SERVER_ENABLED).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(ServerPreferencePage.getLogComboOptions()).build()),
	SERVER_THREADS(RouterLoggerPage.SERVER, FieldEditorType.ScaleInteger, new PreferenceDataBuilder().defaultValue(BaseHttpServer.Defaults.THREADS).parent(SERVER_ENABLED).build(), new FieldEditorDataBuilder().scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(010).build()),

	SERVER_SSL_ENABLED(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(BaseHttpServer.Defaults.SSL_ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_SSL_KEYSTORE_TYPE(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.ValidatedCombo, new PreferenceDataBuilder().defaultValue(BaseHttpServer.Defaults.SSL_KEYSTORE_TYPE).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(ServerHttpsPreferencePage.getKeyStoreAlgorithmsComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_KEYSTORE_FILE(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.FormattedFile, new PreferenceDataBuilder().parent(SERVER_SSL_ENABLED).build(), new FieldEditorDataBuilder().fileExtensions(ServerHttpsPreferencePage.getKeyStoreFileExtensions()).build()),
	SERVER_SSL_STOREPASS(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.Password, new PreferenceDataBuilder().parent(SERVER_SSL_ENABLED).build()),
	SERVER_SSL_KEYPASS(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.Password, new PreferenceDataBuilder().parent(SERVER_SSL_ENABLED).build()),
	SERVER_SSL_PROTOCOL(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.ValidatedCombo, new PreferenceDataBuilder().defaultValue(BaseHttpServer.Defaults.SSL_PROTOCOL).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(ServerHttpsPreferencePage.getSslContextAlgorithmsComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_KMF_ALGORITHM(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.ValidatedCombo, new PreferenceDataBuilder().defaultValue(BaseHttpServer.Defaults.SSL_KMF_ALGORITHM).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(ServerHttpsPreferencePage.getKeyManagerFactoryComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_TMF_ALGORITHM(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.ValidatedCombo, new PreferenceDataBuilder().defaultValue(BaseHttpServer.Defaults.SSL_TMF_ALGORITHM).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(ServerHttpsPreferencePage.getTrustManagerFactoryComboOptions()).emptyStringAllowed(false).build()),

	MQTT_ACTIVE(RouterLoggerPage.MQTT, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.ACTIVE).build()),
	MQTT_SERVER_URI(RouterLoggerPage.MQTT, FieldEditorType.Uri, new PreferenceDataBuilder().parent(MQTT_ACTIVE).build(), new FieldEditorDataBuilder().horizontalSpan(2).build()),
	MQTT_USERNAME(RouterLoggerPage.MQTT, FieldEditorType.FormattedString, new PreferenceDataBuilder().parent(MQTT_ACTIVE).build()),
	MQTT_PASSWORD(RouterLoggerPage.MQTT, FieldEditorType.Password, new PreferenceDataBuilder().parent(MQTT_ACTIVE).build()),
	MQTT_CLIENT_ID(RouterLoggerPage.MQTT, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.CLIENT_ID).parent(MQTT_ACTIVE).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),

	MQTT_DATA_ENABLED(RouterLoggerPage.MQTT_MESSAGES, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_ENABLED).parent(MQTT_ACTIVE).build()),
	MQTT_DATA_TOPIC(RouterLoggerPage.MQTT_MESSAGES, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_TOPIC).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	MQTT_DATA_QOS(RouterLoggerPage.MQTT_MESSAGES, FieldEditorType.FormattedCombo, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_QOS).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_DATA_THROTTLING_MS(RouterLoggerPage.MQTT_MESSAGES, FieldEditorType.IntegerCombo, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_THROTTLING_IN_MILLIS).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(new LocalizedComboEntryNamesAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.mqtt.data.throttling.disabled");
		}
	}, 0)).build()),
	MQTT_DATA_RETAINED(RouterLoggerPage.MQTT_MESSAGES, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_RETAINED).parent(MQTT_DATA_ENABLED).build()),
	MQTT_STATUS_ENABLED(RouterLoggerPage.MQTT_MESSAGES, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_ENABLED).parent(MQTT_ACTIVE).build()),
	MQTT_STATUS_TOPIC(RouterLoggerPage.MQTT_MESSAGES, FieldEditorType.FormattedString, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_TOPIC).parent(MQTT_STATUS_ENABLED).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	MQTT_STATUS_QOS(RouterLoggerPage.MQTT_MESSAGES, FieldEditorType.FormattedCombo, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_QOS).parent(MQTT_STATUS_ENABLED).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_STATUS_RETAINED(RouterLoggerPage.MQTT_MESSAGES, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_RETAINED).parent(MQTT_STATUS_ENABLED).build()),

	MQTT_CLEAN_SESSION(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.CLEAN_SESSION).parent(MQTT_ACTIVE).build()),
	MQTT_AUTOMATIC_RECONNECT(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.AUTOMATIC_RECONNECT).parent(MQTT_ACTIVE).build()),
	MQTT_CONNECTION_TIMEOUT(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.CONNECTION_TIMEOUT).parent(MQTT_ACTIVE).build()),
	MQTT_KEEP_ALIVE_INTERVAL(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.KEEP_ALIVE_INTERVAL).parent(MQTT_ACTIVE).build()),
	MQTT_MAX_INFLIGHT(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.FormattedInteger, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.MAX_INFLIGHT).parent(MQTT_ACTIVE).build()),
	MQTT_VERSION(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.FormattedCombo, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.MQTT_VERSION).parent(MQTT_ACTIVE).build(), new FieldEditorDataBuilder().comboEntryNamesAndValues(AdvancedMqttPreferencePage.getMqttVersionComboOptions()).build()),
	MQTT_PERSISTENCE_FILE_ACTIVE(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.PERSISTENCE_FILE_ACTIVE).parent(MQTT_ACTIVE).build()),
	MQTT_PERSISTENCE_FILE_CUSTOM(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.DefaultBoolean, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.PERSISTENCE_FILE_CUSTOM).parent(MQTT_PERSISTENCE_FILE_ACTIVE).build()),
	MQTT_PERSISTENCE_FILE_PATH(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.FormattedDirectory, new PreferenceDataBuilder().defaultValue(System.getProperty("user.dir")).parent(MQTT_PERSISTENCE_FILE_CUSTOM).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.mqtt");
		}
	}).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final Page page;
	private final FieldEditorType fieldEditorType;
	private final String defaultValue;
	private final FieldEditorData fieldEditorData;
	private final Preference parent;
	private final String configurationKey;
	private final String labelKey;
	private final boolean restartRequired;

	private RouterLoggerPreference(final Page page, final FieldEditorType fieldEditorType) {
		this(page, fieldEditorType, null, null);
	}

	private RouterLoggerPreference(final Page page, final FieldEditorType fieldEditorType, final PreferenceData preferenceData) {
		this(page, fieldEditorType, preferenceData, null);
	}

	private RouterLoggerPreference(final Page page, final FieldEditorType fieldEditorType, final FieldEditorData fieldEditorData) {
		this(page, fieldEditorType, null, fieldEditorData);
	}

	private RouterLoggerPreference(final Page page, final FieldEditorType fieldEditorType, final PreferenceData preferenceData, final FieldEditorData fieldEditorData) {
		if (preferenceData != null) {
			final String configurationKey = preferenceData.getConfigurationKey();
			if (configurationKey != null && !configurationKey.isEmpty()) {
				this.configurationKey = configurationKey;
			}
			else {
				this.configurationKey = name().toLowerCase().replace('_', '.');
			}
			final String labelKey = preferenceData.getLabelResourceKey();
			if (labelKey != null && !labelKey.isEmpty()) {
				this.labelKey = labelKey;
			}
			else {
				this.labelKey = LABEL_KEY_PREFIX + this.configurationKey;
			}
			this.defaultValue = preferenceData.getDefaultValue();
			this.parent = preferenceData.getParent();
			this.restartRequired = preferenceData.isRestartRequired();
		}
		else {
			this.configurationKey = name().toLowerCase().replace('_', '.');
			this.labelKey = LABEL_KEY_PREFIX + this.configurationKey;
			this.restartRequired = false;
			this.defaultValue = null;
			this.parent = null;
		}
		this.fieldEditorData = fieldEditorData;
		this.fieldEditorType = fieldEditorType;
		this.page = page;
	}

	@Override
	public String getConfigurationKey() {
		return configurationKey;
	}

	@Override
	public String getLabel() {
		return Resources.get(labelKey);
	}

	@Override
	public Page getPage() {
		return page;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public Preference getParent() {
		return parent;
	}

	@Override
	public boolean isRestartRequired() {
		return restartRequired;
	}

	@Override
	public Set<? extends Preference> getChildren() {
		final Set<RouterLoggerPreference> preferences = EnumSet.noneOf(RouterLoggerPreference.class);
		for (final RouterLoggerPreference item : RouterLoggerPreference.values()) {
			if (this.equals(item.getParent())) {
				preferences.add(item);
			}
		}
		return preferences;
	}

	@Override
	public FieldEditor createFieldEditor(final Composite parent) {
		return FieldEditorFactory.createFieldEditor(fieldEditorType, configurationKey, getLabel(), parent, fieldEditorData);
	}

	public static Preference forConfigurationKey(final String configurationKey) {
		if (configurationKey != null) {
			for (final RouterLoggerPreference preference : RouterLoggerPreference.values()) {
				if (configurationKey.equals(preference.configurationKey)) {
					return preference;
				}
			}
		}
		return null;
	}

}
