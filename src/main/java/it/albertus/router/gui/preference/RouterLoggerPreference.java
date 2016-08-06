package it.albertus.router.gui.preference;

import it.albertus.jface.TextConsole;
import it.albertus.jface.preference.FieldEditorData;
import it.albertus.jface.preference.FieldEditorData.FieldEditorDataBuilder;
import it.albertus.jface.preference.LocalizedComboEntryNamesAndValues;
import it.albertus.jface.preference.Preference;
import it.albertus.jface.preference.page.AbstractPreferencePage;
import it.albertus.router.console.RouterLoggerConsole;
import it.albertus.router.email.EmailSender;
import it.albertus.router.email.ThresholdsEmailSender;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.gui.CloseMessageBox;
import it.albertus.router.gui.DataTable;
import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.gui.TrayIcon;
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

	LANGUAGE(RouterLoggerPage.GENERAL, FieldEditorType.Combo, Locale.getDefault().getLanguage(), new FieldEditorDataBuilder().comboEntryNamesAndValues(GeneralPreferencePage.getLanguageComboOptions()).build()),
	LOGGER_ITERATIONS(RouterLoggerPage.GENERAL, FieldEditorType.IntegerCombo, Integer.toString(RouterLoggerEngine.Defaults.ITERATIONS), new FieldEditorDataBuilder().comboEntryNamesAndValues(new LocalizedComboEntryNamesAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.iterations.infinite");
		}
	}, 0)).build()),
	LOGGER_CLOSE_WHEN_FINISHED(RouterLoggerPage.GENERAL, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerEngine.Defaults.CLOSE_WHEN_FINISHED)),
	LOGGER_INTERVAL_NORMAL_MS(RouterLoggerPage.GENERAL, FieldEditorType.FormattedInteger, Long.toString(RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS)),
	LOGGER_INTERVAL_FAST_MS(RouterLoggerPage.GENERAL, FieldEditorType.FormattedInteger, Long.toString(RouterLoggerEngine.Defaults.INTERVAL_FAST_IN_MILLIS)),
	LOGGER_HYSTERESIS_MS(RouterLoggerPage.GENERAL, FieldEditorType.FormattedInteger, Long.toString(RouterLoggerEngine.Defaults.HYSTERESIS_IN_MILLIS)),
	LOGGER_RETRY_COUNT(RouterLoggerPage.GENERAL, FieldEditorType.IntegerCombo, Integer.toString(RouterLoggerEngine.Defaults.RETRIES), new FieldEditorDataBuilder().comboEntryNamesAndValues(new LocalizedComboEntryNamesAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.logger.retry.count.infinite");
		}
	}, 0)).build()),
	LOGGER_RETRY_INTERVAL_MS(RouterLoggerPage.GENERAL, FieldEditorType.FormattedInteger, Long.toString(RouterLoggerEngine.Defaults.RETRY_INTERVAL_IN_MILLIS)),
	LOGGER_ERROR_LOG_DESTINATION_PATH(RouterLoggerPage.GENERAL, FieldEditorType.FormattedDirectory, Logger.Defaults.DIRECTORY, new FieldEditorDataBuilder().emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.log");
		}
	}).build()),
	CONSOLE_SHOW_CONFIGURATION(RouterLoggerPage.GENERAL, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerEngine.Defaults.CONSOLE_SHOW_CONFIGURATION)),
	CONSOLE_DEBUG(RouterLoggerPage.GENERAL, FieldEditorType.DefaultBoolean, Boolean.toString(Logger.Defaults.DEBUG)),
	LOG_EMAIL(RouterLoggerPage.GENERAL, FieldEditorType.DefaultBoolean, Boolean.toString(Logger.Defaults.EMAIL)),
	LOG_EMAIL_IGNORE_DUPLICATES(RouterLoggerPage.GENERAL, FieldEditorType.DefaultBoolean, Boolean.toString(Logger.Defaults.EMAIL_IGNORE_DUPLICATES), null, LOG_EMAIL),

	READER_CLASS_NAME(RouterLoggerPage.READER, FieldEditorType.ReaderCombo, null, new FieldEditorDataBuilder().comboEntryNamesAndValues(ReaderPreferencePage.getReaderComboOptions()).build()),
	ROUTER_USERNAME(RouterLoggerPage.READER, FieldEditorType.FormattedString),
	ROUTER_PASSWORD(RouterLoggerPage.READER, FieldEditorType.Password),
	ROUTER_ADDRESS(RouterLoggerPage.READER, FieldEditorType.FormattedString, Reader.Defaults.ROUTER_ADDRESS, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	ROUTER_PORT(RouterLoggerPage.READER, FieldEditorType.FormattedInteger, Integer.toString(Reader.Defaults.ROUTER_PORT), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	SOCKET_TIMEOUT_MS(RouterLoggerPage.READER, FieldEditorType.FormattedInteger, Integer.toString(Reader.Defaults.SOCKET_TIMEOUT_IN_MILLIS)),
	CONNECTION_TIMEOUT_MS(RouterLoggerPage.READER, FieldEditorType.FormattedInteger, Integer.toString(Reader.Defaults.CONNECTION_TIMEOUT_IN_MILLIS)),
	TELNET_NEWLINE_CHARACTERS(RouterLoggerPage.READER, FieldEditorType.FormattedCombo, Reader.Defaults.TELNET_NEWLINE_CHARACTERS, new FieldEditorDataBuilder().comboEntryNamesAndValues(AbstractPreferencePage.getNewLineComboOptions()).build()),
	READER_LOG_CONNECTED(RouterLoggerPage.READER, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerEngine.Defaults.LOG_CONNECTED)),
	READER_WAIT_DISCONNECTED(RouterLoggerPage.READER, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED)),
	READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD(RouterLoggerPage.READER, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD), null, READER_WAIT_DISCONNECTED),
	READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD_MS(RouterLoggerPage.READER, FieldEditorType.FormattedInteger, Long.toString(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD_IN_MILLIS), null, READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD),

	TPLINK_8970_COMMAND_INFO_ADSL(RouterLoggerPage.TPLINK_8970, FieldEditorType.FormattedString, TpLink8970Reader.Defaults.COMMAND_INFO_ADSL),
	TPLINK_8970_COMMAND_INFO_WAN(RouterLoggerPage.TPLINK_8970, FieldEditorType.FormattedString),
	ASUS_DSLN12E_COMMAND_INFO_ADSL(RouterLoggerPage.ASUS_N12E, FieldEditorType.FormattedString, AsusDslN12EReader.Defaults.COMMAND_INFO_ADSL),
	ASUS_DSLN12E_COMMAND_INFO_WAN(RouterLoggerPage.ASUS_N12E, FieldEditorType.FormattedString, AsusDslN12EReader.Defaults.COMMAND_INFO_WAN),
	ASUS_DSLN14U_COMMAND_INFO_ADSL(RouterLoggerPage.ASUS_N14U, FieldEditorType.FormattedString, AsusDslN14UReader.Defaults.COMMAND_INFO_ADSL),
	ASUS_DSLN14U_COMMAND_INFO_WAN(RouterLoggerPage.ASUS_N14U, FieldEditorType.FormattedString),
	DLINK_2750_COMMAND_INFO_ADSL_STATUS(RouterLoggerPage.DLINK_2750, FieldEditorType.FormattedString, DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_STATUS),
	DLINK_2750_COMMAND_INFO_ADSL_SNR(RouterLoggerPage.DLINK_2750, FieldEditorType.FormattedString, DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_SNR),

	GUI_TABLE_ITEMS_MAX(RouterLoggerPage.APPEARANCE, FieldEditorType.FormattedInteger, Integer.toString(DataTable.Defaults.MAX_ITEMS), new FieldEditorDataBuilder().textLimit(4).build()),
	GUI_IMPORTANT_KEYS(RouterLoggerPage.APPEARANCE, FieldEditorType.WrapString),
	GUI_IMPORTANT_KEYS_SEPARATOR(RouterLoggerPage.APPEARANCE, FieldEditorType.FormattedString, RouterLoggerConfiguration.Defaults.GUI_IMPORTANT_KEYS_SEPARATOR, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	GUI_TABLE_COLUMNS_PACK(RouterLoggerPage.APPEARANCE, FieldEditorType.DefaultBoolean, Boolean.toString(DataTable.Defaults.COLUMNS_PACK)),
	GUI_TABLE_COLUMNS_PADDING_RIGHT(RouterLoggerPage.APPEARANCE, FieldEditorType.ScaleInteger, Byte.toString(DataTable.Defaults.COLUMNS_PADDING_RIGHT), new FieldEditorDataBuilder().scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	GUI_CONSOLE_MAX_CHARS(RouterLoggerPage.APPEARANCE, FieldEditorType.FormattedInteger, Integer.toString(TextConsole.Defaults.GUI_CONSOLE_MAX_CHARS), new FieldEditorDataBuilder().textLimit(6).build()),
	GUI_CLIPBOARD_MAX_CHARS(RouterLoggerPage.APPEARANCE, FieldEditorType.FormattedInteger, Integer.toString(RouterLoggerGui.Defaults.GUI_CLIPBOARD_MAX_CHARS), new FieldEditorDataBuilder().integerValidRange(0, 128 * 1024).build()),
	GUI_MINIMIZE_TRAY(RouterLoggerPage.APPEARANCE, FieldEditorType.DefaultBoolean, Boolean.toString(TrayIcon.Defaults.GUI_MINIMIZE_TRAY)),
	GUI_TRAY_TOOLTIP(RouterLoggerPage.APPEARANCE, FieldEditorType.DefaultBoolean, Boolean.toString(TrayIcon.Defaults.GUI_TRAY_TOOLTIP), null, GUI_MINIMIZE_TRAY),
	GUI_START_MINIMIZED(RouterLoggerPage.APPEARANCE, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerGui.Defaults.GUI_START_MINIMIZED)),
	GUI_CONFIRM_CLOSE(RouterLoggerPage.APPEARANCE, FieldEditorType.DefaultBoolean, Boolean.toString(CloseMessageBox.Defaults.GUI_CONFIRM_CLOSE)),

	CONSOLE_ANIMATION(RouterLoggerPage.CONSOLE, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerConsole.Defaults.CONSOLE_ANIMATION)),
	CONSOLE_SHOW_KEYS(RouterLoggerPage.CONSOLE, FieldEditorType.WrapString),
	CONSOLE_SHOW_KEYS_SEPARATOR(RouterLoggerPage.CONSOLE, FieldEditorType.FormattedString, RouterLoggerConfiguration.Defaults.CONSOLE_SHOW_KEYS_SEPARATOR, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),

	WRITER_CLASS_NAME(RouterLoggerPage.WRITER, FieldEditorType.WriterCombo, RouterLoggerEngine.Defaults.WRITER_CLASS.getSimpleName(), new FieldEditorDataBuilder().comboEntryNamesAndValues(WriterPreferencePage.getWriterComboOptions()).build()),

	CSV_DESTINATION_PATH(RouterLoggerPage.CSV, FieldEditorType.FormattedDirectory, CsvWriter.Defaults.DIRECTORY, new FieldEditorDataBuilder().emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.csv");
		}
	}).build()),
	CSV_NEWLINE_CHARACTERS(RouterLoggerPage.CSV, FieldEditorType.FormattedCombo, CsvWriter.Defaults.NEWLINE.name(), new FieldEditorDataBuilder().comboEntryNamesAndValues(AbstractPreferencePage.getNewLineComboOptions()).build()),
	CSV_FIELD_SEPARATOR(RouterLoggerPage.CSV, FieldEditorType.DelimiterCombo, CsvWriter.Defaults.FIELD_SEPARATOR, new FieldEditorDataBuilder().emptyStringAllowed(false).comboEntryNamesAndValues(CsvPreferencePage.getSeparatorComboOptions()).build()),
	CSV_FIELD_SEPARATOR_REPLACEMENT(RouterLoggerPage.CSV, FieldEditorType.FormattedString, CsvWriter.Defaults.FIELD_SEPARATOR_REPLACEMENT, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	CSV_EMAIL(RouterLoggerPage.CSV, FieldEditorType.DefaultBoolean, Boolean.toString(CsvWriter.Defaults.EMAIL)),

	DATABASE_DRIVER_CLASS_NAME(RouterLoggerPage.DATABASE, FieldEditorType.DatabaseCombo, null, new FieldEditorDataBuilder().comboEntryNamesAndValues(DatabasePreferencePage.getDatabaseComboOptions()).build()),
	DATABASE_URL(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString),
	DATABASE_USERNAME(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString),
	DATABASE_PASSWORD(RouterLoggerPage.DATABASE, FieldEditorType.Password),
	DATABASE_TABLE_NAME(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString, DatabaseWriter.Defaults.TABLE_NAME, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_CONNECTION_VALIDATION_TIMEOUT_MS(RouterLoggerPage.DATABASE, FieldEditorType.FormattedInteger, Integer.toString(DatabaseWriter.Defaults.CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS), new FieldEditorDataBuilder().textLimit(5).build()),
	DATABASE_TIMESTAMP_COLUMN_TYPE(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString, DatabaseWriter.Defaults.TIMESTAMP_COLUMN_TYPE, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_RESPONSE_COLUMN_TYPE(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString, DatabaseWriter.Defaults.RESPONSE_TIME_COLUMN_TYPE, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_INFO_COLUMN_TYPE(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString, DatabaseWriter.Defaults.INFO_COLUMN_TYPE, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_PREFIX(RouterLoggerPage.DATABASE, FieldEditorType.FormattedString, DatabaseWriter.Defaults.COLUMN_NAME_PREFIX, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_MAX_LENGTH(RouterLoggerPage.DATABASE, FieldEditorType.FormattedInteger, Integer.toString(DatabaseWriter.Defaults.COLUMN_NAME_MAX_LENGTH), new FieldEditorDataBuilder().textLimit(2).build()),

	THRESHOLDS_EXPRESSIONS(RouterLoggerPage.THRESHOLDS, FieldEditorType.Thresholds),
	THRESHOLDS_SPLIT(RouterLoggerPage.THRESHOLDS, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerConfiguration.Defaults.THRESHOLDS_SPLIT)),
	THRESHOLDS_EMAIL(RouterLoggerPage.THRESHOLDS, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerEngine.Defaults.THRESHOLDS_EMAIL)),
	THRESHOLDS_EMAIL_SEND_INTERVAL_SECS(RouterLoggerPage.THRESHOLDS, FieldEditorType.FormattedInteger, Integer.toString(ThresholdsEmailSender.Defaults.THRESHOLDS_EMAIL_SEND_INTERVAL_SECS), null, THRESHOLDS_EMAIL),
	THRESHOLDS_EMAIL_MAX_ITEMS(RouterLoggerPage.THRESHOLDS, FieldEditorType.FormattedInteger, Short.toString(ThresholdsEmailSender.Defaults.MAX_ITEMS), new FieldEditorDataBuilder().integerValidRange(1, 1000).build(), THRESHOLDS_EMAIL),
	THRESHOLDS_EXCLUDED(RouterLoggerPage.THRESHOLDS, FieldEditorType.WrapString),
	THRESHOLDS_EXCLUDED_SEPARATOR(RouterLoggerPage.THRESHOLDS, FieldEditorType.FormattedString, RouterLoggerConfiguration.Defaults.THRESHOLDS_EXCLUDED_SEPARATOR, new FieldEditorDataBuilder().emptyStringAllowed(false).build()),

	EMAIL_HOST(RouterLoggerPage.EMAIL, FieldEditorType.FormattedString),
	EMAIL_USERNAME(RouterLoggerPage.EMAIL, FieldEditorType.FormattedString),
	EMAIL_PASSWORD(RouterLoggerPage.EMAIL, FieldEditorType.Password),
	EMAIL_FROM_NAME(RouterLoggerPage.EMAIL, FieldEditorType.FormattedString),
	EMAIL_FROM_ADDRESS(RouterLoggerPage.EMAIL, FieldEditorType.FormattedString),
	EMAIL_TO_ADDRESSES(RouterLoggerPage.EMAIL, FieldEditorType.EmailAddresses, null, new FieldEditorDataBuilder().horizontalSpan(Short.MAX_VALUE).build()),

	EMAIL_PORT(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, Integer.toString(EmailSender.Defaults.PORT), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	EMAIL_SSL_PORT(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, Integer.toString(EmailSender.Defaults.SSL_PORT), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	EMAIL_CONNECTION_TIMEOUT(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, Integer.toString(EmailSender.Defaults.SOCKET_CONNECTION_TIMEOUT)),
	EMAIL_SOCKET_TIMEOUT(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, Integer.toString(EmailSender.Defaults.SOCKET_TIMEOUT)),
	EMAIL_RETRY_INTERVAL_SECS(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.FormattedInteger, Integer.toString(EmailSender.Defaults.RETRY_INTERVAL_SECS)),
	EMAIL_MAX_SENDINGS_PER_CYCLE(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.IntegerCombo, Integer.toString(EmailSender.Defaults.MAX_SENDINGS_PER_CYCLE), new FieldEditorDataBuilder().comboEntryNamesAndValues(new LocalizedComboEntryNamesAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.email.max.sendings.per.cycle.unlimited");
		}
	}, 0)).build()),
	EMAIL_MAX_QUEUE_SIZE(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.ScaleInteger, Byte.toString(EmailSender.Defaults.MAX_QUEUE_SIZE), new FieldEditorDataBuilder().scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	EMAIL_SSL_CONNECT(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.DefaultBoolean, Boolean.toString(EmailSender.Defaults.SSL_CONNECT)),
	EMAIL_SSL_IDENTITY(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.DefaultBoolean, Boolean.toString(EmailSender.Defaults.SSL_IDENTITY)),
	EMAIL_STARTTLS_ENABLED(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.DefaultBoolean, Boolean.toString(EmailSender.Defaults.STARTTLS_ENABLED)),
	EMAIL_STARTTLS_REQUIRED(RouterLoggerPage.EMAIL_ADVANCED, FieldEditorType.DefaultBoolean, Boolean.toString(EmailSender.Defaults.STARTTLS_REQUIRED)),

	EMAIL_CC_ADDRESSES(RouterLoggerPage.EMAIL_CC_BCC, FieldEditorType.EmailAddresses, null, new FieldEditorDataBuilder().horizontalSpan(0).build()),
	EMAIL_BCC_ADDRESSES(RouterLoggerPage.EMAIL_CC_BCC, FieldEditorType.EmailAddresses, null, new FieldEditorDataBuilder().horizontalSpan(0).build()),

	SERVER_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, Boolean.toString(WebServer.Defaults.ENABLED)),
	SERVER_USERNAME(RouterLoggerPage.SERVER, FieldEditorType.FormattedString, null, null, SERVER_ENABLED),
	SERVER_PASSWORD(RouterLoggerPage.SERVER, FieldEditorType.Password, null, null, SERVER_ENABLED),
	SERVER_PORT(RouterLoggerPage.SERVER, FieldEditorType.FormattedInteger, Integer.toString(WebServer.Defaults.PORT), new FieldEditorDataBuilder().integerValidRange(1, 65535).build(), SERVER_ENABLED),
	SERVER_COMPRESS_RESPONSE(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, Boolean.toString(BaseHttpHandler.Defaults.COMPRESS_RESPONSE), null, SERVER_ENABLED),
	SERVER_HANDLER_ROOT_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, Boolean.toString(RootHandler.Defaults.ENABLED), null, SERVER_ENABLED),
	SERVER_HANDLER_RESTART_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, Boolean.toString(RestartHandler.Defaults.ENABLED), null, SERVER_ENABLED),
	SERVER_HANDLER_CONNECT_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, Boolean.toString(ConnectHandler.Defaults.ENABLED), null, SERVER_ENABLED),
	SERVER_HANDLER_DISCONNECT_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, Boolean.toString(DisconnectHandler.Defaults.ENABLED), null, SERVER_ENABLED),
	SERVER_HANDLER_CLOSE_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, Boolean.toString(CloseHandler.Defaults.ENABLED), null, SERVER_ENABLED),
	SERVER_HANDLER_STATUS_ENABLED(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, Boolean.toString(StatusHandler.Defaults.ENABLED), null, SERVER_ENABLED),
	SERVER_HANDLER_STATUS_REFRESH(RouterLoggerPage.SERVER, FieldEditorType.DefaultBoolean, Boolean.toString(StatusHandler.Defaults.REFRESH), null, SERVER_HANDLER_STATUS_ENABLED),
	SERVER_HANDLER_STATUS_REFRESH_SECS(RouterLoggerPage.SERVER, FieldEditorType.IntegerCombo, Integer.toString(StatusHandler.Defaults.REFRESH_SECS), new FieldEditorDataBuilder().comboEntryNamesAndValues(new LocalizedComboEntryNamesAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.server.handler.status.refresh.auto");
		}
	}, 0)).build(), SERVER_HANDLER_STATUS_REFRESH),
	SERVER_LOG_REQUEST(RouterLoggerPage.SERVER, FieldEditorType.FormattedCombo, Integer.toString(BaseHttpHandler.Defaults.LOG_REQUEST), new FieldEditorDataBuilder().comboEntryNamesAndValues(ServerPreferencePage.getLogComboOptions()).build(), SERVER_ENABLED),
	SERVER_THREADS(RouterLoggerPage.SERVER, FieldEditorType.ScaleInteger, Byte.toString(BaseHttpServer.Defaults.THREADS), new FieldEditorDataBuilder().scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(010).build(), SERVER_ENABLED),

	SERVER_SSL_ENABLED(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.DefaultBoolean, Boolean.toString(BaseHttpServer.Defaults.SSL_ENABLED)),
	SERVER_SSL_KEYSTORE_TYPE(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.ValidatedCombo, BaseHttpServer.Defaults.SSL_KEYSTORE_TYPE, new FieldEditorDataBuilder().comboEntryNamesAndValues(ServerHttpsPreferencePage.getKeyStoreAlgorithmsComboOptions()).emptyStringAllowed(false).build(), SERVER_SSL_ENABLED),
	SERVER_SSL_KEYSTORE_FILE(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.FormattedFile, null, new FieldEditorDataBuilder().fileExtensions(ServerHttpsPreferencePage.getKeyStoreFileExtensions()).build(), SERVER_SSL_ENABLED),
	SERVER_SSL_STOREPASS(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.Password, null, null, SERVER_SSL_ENABLED),
	SERVER_SSL_KEYPASS(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.Password, null, null, SERVER_SSL_ENABLED),
	SERVER_SSL_PROTOCOL(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.ValidatedCombo, BaseHttpServer.Defaults.SSL_PROTOCOL, new FieldEditorDataBuilder().comboEntryNamesAndValues(ServerHttpsPreferencePage.getSslContextAlgorithmsComboOptions()).emptyStringAllowed(false).build(), SERVER_SSL_ENABLED),
	SERVER_SSL_KMF_ALGORITHM(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.ValidatedCombo, BaseHttpServer.Defaults.SSL_KMF_ALGORITHM, new FieldEditorDataBuilder().comboEntryNamesAndValues(ServerHttpsPreferencePage.getKeyManagerFactoryComboOptions()).emptyStringAllowed(false).build(), SERVER_SSL_ENABLED),
	SERVER_SSL_TMF_ALGORITHM(RouterLoggerPage.SERVER_HTTPS, FieldEditorType.ValidatedCombo, BaseHttpServer.Defaults.SSL_TMF_ALGORITHM, new FieldEditorDataBuilder().comboEntryNamesAndValues(ServerHttpsPreferencePage.getTrustManagerFactoryComboOptions()).emptyStringAllowed(false).build(), SERVER_SSL_ENABLED),

	MQTT_ACTIVE(RouterLoggerPage.MQTT, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerMqttClient.Defaults.ACTIVE)),
	MQTT_SERVER_URI(RouterLoggerPage.MQTT, FieldEditorType.Uri, null, new FieldEditorDataBuilder().horizontalSpan(2).build(), MQTT_ACTIVE),
	MQTT_USERNAME(RouterLoggerPage.MQTT, FieldEditorType.FormattedString, null, null, MQTT_ACTIVE),
	MQTT_PASSWORD(RouterLoggerPage.MQTT, FieldEditorType.Password, null, null, MQTT_ACTIVE),
	MQTT_CLIENT_ID(RouterLoggerPage.MQTT, FieldEditorType.FormattedString, RouterLoggerMqttClient.Defaults.CLIENT_ID, new FieldEditorDataBuilder().emptyStringAllowed(false).build(), MQTT_ACTIVE),
	MQTT_TOPIC(RouterLoggerPage.MQTT, FieldEditorType.FormattedString, RouterLoggerMqttClient.Defaults.TOPIC, new FieldEditorDataBuilder().emptyStringAllowed(false).build(), MQTT_ACTIVE),
	MQTT_MESSAGE_QOS(RouterLoggerPage.MQTT, FieldEditorType.FormattedCombo, Byte.toString(RouterLoggerMqttClient.Defaults.MESSAGE_QOS), new FieldEditorDataBuilder().comboEntryNamesAndValues(MqttPreferencePage.getMqttQosComboOptions()).build(), MQTT_ACTIVE),
	MQTT_CLEAN_SESSION(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerMqttClient.Defaults.CLEAN_SESSION)),
	MQTT_AUTOMATIC_RECONNECT(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerMqttClient.Defaults.AUTOMATIC_RECONNECT)),
	MQTT_CONNECTION_TIMEOUT(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.FormattedInteger, Integer.toString(RouterLoggerMqttClient.Defaults.CONNECTION_TIMEOUT)),
	MQTT_KEEP_ALIVE_INTERVAL(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.FormattedInteger, Integer.toString(RouterLoggerMqttClient.Defaults.KEEP_ALIVE_INTERVAL)),
	MQTT_MAX_INFLIGHT(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.FormattedInteger, Integer.toString(RouterLoggerMqttClient.Defaults.MAX_INFLIGHT)),
	MQTT_MESSAGE_RETAINED(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.DefaultBoolean, Boolean.toString(RouterLoggerMqttClient.Defaults.MESSAGE_RETAINED)),
	MQTT_VERSION(RouterLoggerPage.MQTT_ADVANCED, FieldEditorType.FormattedCombo, Byte.toString(RouterLoggerMqttClient.Defaults.MQTT_VERSION), new FieldEditorDataBuilder().comboEntryNamesAndValues(AdvancedMqttPreferencePage.getMqttVersionComboOptions()).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final RouterLoggerPage page;
	private final FieldEditorType fieldEditorType;
	private final String defaultValue;
	private final FieldEditorData fieldEditorData;
	private final RouterLoggerPreference parent;
	private final String configurationKey;
	private final String labelKey;

	private RouterLoggerPreference(final RouterLoggerPage page, final FieldEditorType fieldEditorType) {
		this(page, fieldEditorType, null, null, null, null, null);
	}

	private RouterLoggerPreference(final RouterLoggerPage page, final FieldEditorType fieldEditorType, final String defaultValue) {
		this(page, fieldEditorType, defaultValue, null, null, null, null);
	}

	private RouterLoggerPreference(final RouterLoggerPage page, final FieldEditorType fieldEditorType, final String defaultValue, final FieldEditorData fieldEditorData) {
		this(page, fieldEditorType, defaultValue, fieldEditorData, null, null, null);
	}

	private RouterLoggerPreference(final RouterLoggerPage page, final FieldEditorType fieldEditorType, final String defaultValue, final FieldEditorData fieldEditorData, final RouterLoggerPreference parent) {
		this(page, fieldEditorType, defaultValue, fieldEditorData, parent, null, null);
	}

	private RouterLoggerPreference(final RouterLoggerPage page, final FieldEditorType fieldEditorType, final String defaultValue, final FieldEditorData fieldEditorData, final RouterLoggerPreference parent, final String configurationKey) {
		this(page, fieldEditorType, defaultValue, fieldEditorData, parent, configurationKey, null);
	}

	private RouterLoggerPreference(final RouterLoggerPage page, final FieldEditorType fieldEditorType, final String defaultValue, final FieldEditorData fieldEditorData, final RouterLoggerPreference parent, final String configurationKey, final String labelKey) {
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
		this.parent = parent;
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
	public RouterLoggerPage getPage() {
		return page;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public RouterLoggerPreference getParent() {
		return parent;
	}

	@Override
	public Set<RouterLoggerPreference> getChildren() {
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

	public static RouterLoggerPreference forConfigurationKey(final String configurationKey) {
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
