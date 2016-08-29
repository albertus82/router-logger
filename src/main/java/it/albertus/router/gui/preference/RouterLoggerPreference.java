package it.albertus.router.gui.preference;

import it.albertus.jface.TextConsole;
import it.albertus.jface.preference.FieldEditorData;
import it.albertus.jface.preference.FieldEditorData.FieldEditorDataBuilder;
import it.albertus.jface.preference.FieldEditorFactory;
import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.Preference;
import it.albertus.jface.preference.PreferenceData;
import it.albertus.jface.preference.PreferenceData.PreferenceDataBuilder;
import it.albertus.jface.preference.field.DefaultBooleanFieldEditor;
import it.albertus.jface.preference.field.DefaultComboFieldEditor;
import it.albertus.jface.preference.field.DefaultDirectoryFieldEditor;
import it.albertus.jface.preference.field.DefaultFileFieldEditor;
import it.albertus.jface.preference.field.DefaultIntegerFieldEditor;
import it.albertus.jface.preference.field.DefaultStringFieldEditor;
import it.albertus.jface.preference.field.DelimiterComboFieldEditor;
import it.albertus.jface.preference.field.EmailAddressesListEditor;
import it.albertus.jface.preference.field.IntegerComboFieldEditor;
import it.albertus.jface.preference.field.PasswordFieldEditor;
import it.albertus.jface.preference.field.ScaleIntegerFieldEditor;
import it.albertus.jface.preference.field.UriListEditor;
import it.albertus.jface.preference.field.ValidatedComboFieldEditor;
import it.albertus.jface.preference.field.WrapStringFieldEditor;
import it.albertus.jface.preference.page.AbstractPreferencePage;
import it.albertus.jface.preference.page.Page;
import it.albertus.router.console.RouterLoggerConsole;
import it.albertus.router.email.EmailSender;
import it.albertus.router.email.ThresholdsEmailSender;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.gui.CloseMessageBox;
import it.albertus.router.gui.DataTable;
import it.albertus.router.gui.Images;
import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.gui.TrayIcon;
import it.albertus.router.gui.preference.field.DatabaseComboFieldEditor;
import it.albertus.router.gui.preference.field.ReaderComboFieldEditor;
import it.albertus.router.gui.preference.field.ThresholdsListEditor;
import it.albertus.router.gui.preference.field.WriterComboFieldEditor;
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
import it.albertus.router.server.BaseHttpServer;
import it.albertus.router.server.html.BaseHtmlHandler;
import it.albertus.router.server.html.CloseHandler;
import it.albertus.router.server.html.ConnectHandler;
import it.albertus.router.server.html.DisconnectHandler;
import it.albertus.router.server.html.RestartHandler;
import it.albertus.router.server.html.RootHtmlHandler;
import it.albertus.router.server.html.StatusHtmlHandler;
import it.albertus.router.server.json.BaseJsonHandler;
import it.albertus.router.util.Logger;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.DatabaseWriter;
import it.albertus.util.Localized;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

public enum RouterLoggerPreference implements Preference {

	LANGUAGE(RouterLoggerPage.GENERAL, ComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(Locale.getDefault().getLanguage()).build(), new FieldEditorDataBuilder().labelsAndValues(GeneralPreferencePage.getLanguageComboOptions()).build()),
	LOGGER_ITERATIONS(RouterLoggerPage.GENERAL, IntegerComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.ITERATIONS).build(), new FieldEditorDataBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.iterations.infinite");
		}
	}, 0)).build()),
	LOGGER_CLOSE_WHEN_FINISHED(RouterLoggerPage.GENERAL, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.CLOSE_WHEN_FINISHED).build()),
	LOGGER_INTERVAL_NORMAL_MS(RouterLoggerPage.GENERAL, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS).build()),
	LOGGER_INTERVAL_FAST_MS(RouterLoggerPage.GENERAL, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.INTERVAL_FAST_IN_MILLIS).build()),
	LOGGER_HYSTERESIS_MS(RouterLoggerPage.GENERAL, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.HYSTERESIS_IN_MILLIS).build()),
	LOGGER_RETRY_COUNT(RouterLoggerPage.GENERAL, IntegerComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.RETRIES).build(), new FieldEditorDataBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.logger.retry.count.infinite");
		}
	}, 0)).build()),
	LOGGER_RETRY_INTERVAL_MS(RouterLoggerPage.GENERAL, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.RETRY_INTERVAL_IN_MILLIS).build()),
	LOGGER_ERROR_LOG_DESTINATION_PATH(RouterLoggerPage.GENERAL, DefaultDirectoryFieldEditor.class, new PreferenceDataBuilder().defaultValue(Logger.Defaults.DIRECTORY).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.log");
		}
	}).build()),
	CONSOLE_SHOW_CONFIGURATION(RouterLoggerPage.GENERAL, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.CONSOLE_SHOW_CONFIGURATION).build()),
	CONSOLE_DEBUG(RouterLoggerPage.GENERAL, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(Logger.Defaults.DEBUG).build()),
	LOG_EMAIL(RouterLoggerPage.GENERAL, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(Logger.Defaults.EMAIL).build()),
	LOG_EMAIL_IGNORE_DUPLICATES(RouterLoggerPage.GENERAL, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(Logger.Defaults.EMAIL_IGNORE_DUPLICATES).parent(LOG_EMAIL).build()),

	READER_CLASS_NAME(RouterLoggerPage.READER, ReaderComboFieldEditor.class, new FieldEditorDataBuilder().labelsAndValues(ReaderPreferencePage.getReaderComboOptions()).build()),
	ROUTER_USERNAME(RouterLoggerPage.READER, DefaultStringFieldEditor.class),
	ROUTER_PASSWORD(RouterLoggerPage.READER, PasswordFieldEditor.class),
	ROUTER_ADDRESS(RouterLoggerPage.READER, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(Reader.Defaults.ROUTER_ADDRESS).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	ROUTER_PORT(RouterLoggerPage.READER, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(Reader.Defaults.ROUTER_PORT).build(), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	SOCKET_TIMEOUT_MS(RouterLoggerPage.READER, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(Reader.Defaults.SOCKET_TIMEOUT_IN_MILLIS).build()),
	CONNECTION_TIMEOUT_MS(RouterLoggerPage.READER, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(Reader.Defaults.CONNECTION_TIMEOUT_IN_MILLIS).build()),
	TELNET_NEWLINE_CHARACTERS(RouterLoggerPage.READER, DefaultComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(Reader.Defaults.TELNET_NEWLINE_CHARACTERS).build(), new FieldEditorDataBuilder().labelsAndValues(AbstractPreferencePage.getNewLineComboOptions()).build()),
	READER_LOG_CONNECTED(RouterLoggerPage.READER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.LOG_CONNECTED).build()),
	READER_WAIT_DISCONNECTED(RouterLoggerPage.READER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED).build()),
	READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD(RouterLoggerPage.READER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD).parent(READER_WAIT_DISCONNECTED).build()),
	READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD_MS(RouterLoggerPage.READER, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD_IN_MILLIS).parent(READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD).build()),

	TPLINK_8970_COMMAND_INFO_ADSL(RouterLoggerPage.TPLINK_8970, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(TpLink8970Reader.Defaults.COMMAND_INFO_ADSL).build()),
	TPLINK_8970_COMMAND_INFO_WAN(RouterLoggerPage.TPLINK_8970, DefaultStringFieldEditor.class),
	ASUS_DSLN12E_COMMAND_INFO_ADSL(RouterLoggerPage.ASUS_N12E, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(AsusDslN12EReader.Defaults.COMMAND_INFO_ADSL).build()),
	ASUS_DSLN12E_COMMAND_INFO_WAN(RouterLoggerPage.ASUS_N12E, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(AsusDslN12EReader.Defaults.COMMAND_INFO_WAN).build()),
	ASUS_DSLN14U_COMMAND_INFO_ADSL(RouterLoggerPage.ASUS_N14U, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(AsusDslN14UReader.Defaults.COMMAND_INFO_ADSL).build()),
	ASUS_DSLN14U_COMMAND_INFO_WAN(RouterLoggerPage.ASUS_N14U, DefaultStringFieldEditor.class),
	DLINK_2750_COMMAND_INFO_ADSL_STATUS(RouterLoggerPage.DLINK_2750, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_STATUS).build()),
	DLINK_2750_COMMAND_INFO_ADSL_SNR(RouterLoggerPage.DLINK_2750, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_SNR).build()),

	GUI_TABLE_ITEMS_MAX(RouterLoggerPage.APPEARANCE, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(DataTable.Defaults.MAX_ITEMS).build(), new FieldEditorDataBuilder().textLimit(4).build()),
	GUI_IMPORTANT_KEYS(RouterLoggerPage.APPEARANCE, WrapStringFieldEditor.class, new FieldEditorDataBuilder().textHeight(3).build()),
	GUI_IMPORTANT_KEYS_SEPARATOR(RouterLoggerPage.APPEARANCE, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerConfiguration.Defaults.GUI_IMPORTANT_KEYS_SEPARATOR).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	GUI_IMPORTANT_KEYS_COLOR(RouterLoggerPage.APPEARANCE, ColorFieldEditor.class, new PreferenceDataBuilder().defaultValue(DataTable.Defaults.IMPORTANT_KEY_BACKGROUND_COLOR).build()),
	GUI_THRESHOLDS_REACHED_COLOR(RouterLoggerPage.APPEARANCE, ColorFieldEditor.class, new PreferenceDataBuilder().defaultValue(DataTable.Defaults.THRESHOLDS_REACHED_FOREGROUD_COLOR).build()),
	GUI_TABLE_COLUMNS_PACK(RouterLoggerPage.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PACK).build()),
	GUI_TABLE_COLUMNS_PADDING_RIGHT(RouterLoggerPage.APPEARANCE, ScaleIntegerFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PADDING_RIGHT).build(), new FieldEditorDataBuilder().scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	GUI_CONSOLE_MAX_CHARS(RouterLoggerPage.APPEARANCE, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(TextConsole.Defaults.GUI_CONSOLE_MAX_CHARS).build(), new FieldEditorDataBuilder().textLimit(6).build()),
	GUI_CLIPBOARD_MAX_CHARS(RouterLoggerPage.APPEARANCE, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerGui.Defaults.GUI_CLIPBOARD_MAX_CHARS).build(), new FieldEditorDataBuilder().integerValidRange(0, 128 * 1024).build()),
	GUI_MINIMIZE_TRAY(RouterLoggerPage.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(TrayIcon.Defaults.GUI_MINIMIZE_TRAY).build()),
	GUI_TRAY_TOOLTIP(RouterLoggerPage.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(TrayIcon.Defaults.GUI_TRAY_TOOLTIP).parent(GUI_MINIMIZE_TRAY).build()),
	GUI_START_MINIMIZED(RouterLoggerPage.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerGui.Defaults.GUI_START_MINIMIZED).build()),
	GUI_CONFIRM_CLOSE(RouterLoggerPage.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(CloseMessageBox.Defaults.GUI_CONFIRM_CLOSE).build()),

	CONSOLE_ANIMATION(RouterLoggerPage.CONSOLE, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerConsole.Defaults.CONSOLE_ANIMATION).build()),
	CONSOLE_SHOW_KEYS(RouterLoggerPage.CONSOLE, WrapStringFieldEditor.class),
	CONSOLE_SHOW_KEYS_SEPARATOR(RouterLoggerPage.CONSOLE, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerConfiguration.Defaults.CONSOLE_SHOW_KEYS_SEPARATOR).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),

	WRITER_CLASS_NAME(RouterLoggerPage.WRITER, WriterComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.WRITER_CLASS.getSimpleName()).build(), new FieldEditorDataBuilder().labelsAndValues(WriterPreferencePage.getWriterComboOptions()).build()),

	CSV_DESTINATION_PATH(RouterLoggerPage.CSV, DefaultDirectoryFieldEditor.class, new PreferenceDataBuilder().defaultValue(CsvWriter.Defaults.DIRECTORY).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.csv");
		}
	}).build()),
	CSV_NEWLINE_CHARACTERS(RouterLoggerPage.CSV, DefaultComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(CsvWriter.Defaults.NEWLINE.name()).build(), new FieldEditorDataBuilder().labelsAndValues(AbstractPreferencePage.getNewLineComboOptions()).build()),
	CSV_FIELD_SEPARATOR(RouterLoggerPage.CSV, DelimiterComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(CsvWriter.Defaults.FIELD_SEPARATOR).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).labelsAndValues(CsvPreferencePage.getSeparatorComboOptions()).build()),
	CSV_FIELD_SEPARATOR_REPLACEMENT(RouterLoggerPage.CSV, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(CsvWriter.Defaults.FIELD_SEPARATOR_REPLACEMENT).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	CSV_EMAIL(RouterLoggerPage.CSV, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(CsvWriter.Defaults.EMAIL).build()),

	DATABASE_DRIVER_CLASS_NAME(RouterLoggerPage.DATABASE, DatabaseComboFieldEditor.class, new FieldEditorDataBuilder().labelsAndValues(DatabasePreferencePage.getDatabaseComboOptions()).build()),
	DATABASE_URL(RouterLoggerPage.DATABASE, DefaultStringFieldEditor.class),
	DATABASE_USERNAME(RouterLoggerPage.DATABASE, DefaultStringFieldEditor.class),
	DATABASE_PASSWORD(RouterLoggerPage.DATABASE, PasswordFieldEditor.class),
	DATABASE_TABLE_NAME(RouterLoggerPage.DATABASE, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.TABLE_NAME).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_CONNECTION_VALIDATION_TIMEOUT_MS(RouterLoggerPage.DATABASE, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS).build(), new FieldEditorDataBuilder().textLimit(5).build()),
	DATABASE_TIMESTAMP_COLUMN_TYPE(RouterLoggerPage.DATABASE, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.TIMESTAMP_COLUMN_TYPE).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_RESPONSE_COLUMN_TYPE(RouterLoggerPage.DATABASE, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.RESPONSE_TIME_COLUMN_TYPE).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_INFO_COLUMN_TYPE(RouterLoggerPage.DATABASE, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.INFO_COLUMN_TYPE).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_PREFIX(RouterLoggerPage.DATABASE, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.COLUMN_NAME_PREFIX).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_MAX_LENGTH(RouterLoggerPage.DATABASE, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(DatabaseWriter.Defaults.COLUMN_NAME_MAX_LENGTH).build(), new FieldEditorDataBuilder().textLimit(2).build()),

	THRESHOLDS_EXPRESSIONS(RouterLoggerPage.THRESHOLDS, ThresholdsListEditor.class),
	THRESHOLDS_SPLIT(RouterLoggerPage.THRESHOLDS, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerConfiguration.Defaults.THRESHOLDS_SPLIT).build()),
	THRESHOLDS_EMAIL(RouterLoggerPage.THRESHOLDS, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerEngine.Defaults.THRESHOLDS_EMAIL).build()),
	THRESHOLDS_EMAIL_SEND_INTERVAL_SECS(RouterLoggerPage.THRESHOLDS, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(ThresholdsEmailSender.Defaults.THRESHOLDS_EMAIL_SEND_INTERVAL_SECS).parent(THRESHOLDS_EMAIL).build()),
	THRESHOLDS_EMAIL_MAX_ITEMS(RouterLoggerPage.THRESHOLDS, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(ThresholdsEmailSender.Defaults.MAX_ITEMS).parent(THRESHOLDS_EMAIL).build(), new FieldEditorDataBuilder().integerValidRange(1, 1000).build()),
	THRESHOLDS_EXCLUDED(RouterLoggerPage.THRESHOLDS, WrapStringFieldEditor.class),
	THRESHOLDS_EXCLUDED_SEPARATOR(RouterLoggerPage.THRESHOLDS, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerConfiguration.Defaults.THRESHOLDS_EXCLUDED_SEPARATOR).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),

	EMAIL_HOST(RouterLoggerPage.EMAIL, DefaultStringFieldEditor.class),
	EMAIL_USERNAME(RouterLoggerPage.EMAIL, DefaultStringFieldEditor.class),
	EMAIL_PASSWORD(RouterLoggerPage.EMAIL, PasswordFieldEditor.class),
	EMAIL_FROM_NAME(RouterLoggerPage.EMAIL, DefaultStringFieldEditor.class),
	EMAIL_FROM_ADDRESS(RouterLoggerPage.EMAIL, DefaultStringFieldEditor.class),
	EMAIL_TO_ADDRESSES(RouterLoggerPage.EMAIL, EmailAddressesListEditor.class, new FieldEditorDataBuilder().horizontalSpan(Short.MAX_VALUE).icons(Images.MAIN_ICONS).build()),

	EMAIL_PORT(RouterLoggerPage.EMAIL_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.PORT).build(), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	EMAIL_SSL_PORT(RouterLoggerPage.EMAIL_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.SSL_PORT).build(), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	EMAIL_CONNECTION_TIMEOUT(RouterLoggerPage.EMAIL_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.SOCKET_CONNECTION_TIMEOUT).build()),
	EMAIL_SOCKET_TIMEOUT(RouterLoggerPage.EMAIL_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.SOCKET_TIMEOUT).build()),
	EMAIL_RETRY_INTERVAL_SECS(RouterLoggerPage.EMAIL_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.RETRY_INTERVAL_SECS).build()),
	EMAIL_MAX_SENDINGS_PER_CYCLE(RouterLoggerPage.EMAIL_ADVANCED, IntegerComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.MAX_SENDINGS_PER_CYCLE).build(), new FieldEditorDataBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.email.max.sendings.per.cycle.unlimited");
		}
	}, 0)).build()),
	EMAIL_MAX_QUEUE_SIZE(RouterLoggerPage.EMAIL_ADVANCED, ScaleIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.MAX_QUEUE_SIZE).build(), new FieldEditorDataBuilder().scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	EMAIL_SSL_CONNECT(RouterLoggerPage.EMAIL_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.SSL_CONNECT).build()),
	EMAIL_SSL_IDENTITY(RouterLoggerPage.EMAIL_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.SSL_IDENTITY).build()),
	EMAIL_STARTTLS_ENABLED(RouterLoggerPage.EMAIL_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.STARTTLS_ENABLED).build()),
	EMAIL_STARTTLS_REQUIRED(RouterLoggerPage.EMAIL_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(EmailSender.Defaults.STARTTLS_REQUIRED).build()),

	EMAIL_CC_ADDRESSES(RouterLoggerPage.EMAIL_CC_BCC, EmailAddressesListEditor.class, new FieldEditorDataBuilder().horizontalSpan(0).icons(Images.MAIN_ICONS).build()),
	EMAIL_BCC_ADDRESSES(RouterLoggerPage.EMAIL_CC_BCC, EmailAddressesListEditor.class, new FieldEditorDataBuilder().horizontalSpan(0).icons(Images.MAIN_ICONS).build()),

	SERVER_ENABLED(RouterLoggerPage.SERVER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(BaseHttpServer.Defaults.ENABLED).restartRequired().build()),
	SERVER_PORT(RouterLoggerPage.SERVER, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(BaseHttpServer.Defaults.PORT).restartRequired().parent(SERVER_ENABLED).build(), new FieldEditorDataBuilder().integerValidRange(1, 65535).build()),
	SERVER_AUTHENTICATION(RouterLoggerPage.SERVER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().parent(SERVER_ENABLED).defaultValue(BaseHttpServer.Defaults.AUTHENTICATION).restartRequired().build()),
	SERVER_USERNAME(RouterLoggerPage.SERVER, DefaultStringFieldEditor.class, new PreferenceDataBuilder().parent(SERVER_AUTHENTICATION).build()),
	SERVER_PASSWORD(RouterLoggerPage.SERVER, PasswordFieldEditor.class, new PreferenceDataBuilder().parent(SERVER_AUTHENTICATION).build()),
	SERVER_COMPRESS_RESPONSE(RouterLoggerPage.SERVER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(BaseHtmlHandler.Defaults.COMPRESS_RESPONSE).parent(SERVER_ENABLED).build()),
	SERVER_LOG_REQUEST(RouterLoggerPage.SERVER, DefaultComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(BaseHtmlHandler.Defaults.LOG_REQUEST).parent(SERVER_ENABLED).build(), new FieldEditorDataBuilder().labelsAndValues(ServerPreferencePage.getLogComboOptions()).build()),
	SERVER_THREADS(RouterLoggerPage.SERVER, ScaleIntegerFieldEditor.class, new PreferenceDataBuilder().defaultValue(BaseHttpServer.Defaults.THREADS).restartRequired().parent(SERVER_ENABLED).build(), new FieldEditorDataBuilder().scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(010).build()),

	SERVER_HANDLER_ROOT_ENABLED(RouterLoggerPage.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RootHtmlHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_STATUS_ENABLED(RouterLoggerPage.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(StatusHtmlHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_STATUS_REFRESH(RouterLoggerPage.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(StatusHtmlHandler.Defaults.REFRESH).parent(SERVER_HANDLER_STATUS_ENABLED).build()),
	SERVER_HANDLER_STATUS_REFRESH_SECS(RouterLoggerPage.SERVER_HANDLER, IntegerComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(StatusHtmlHandler.Defaults.REFRESH_SECS).parent(SERVER_HANDLER_STATUS_REFRESH).build(), new FieldEditorDataBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.server.handler.refresh.auto");
		}
	}, 0)).build()),
	SERVER_HANDLER_RESTART_ENABLED(RouterLoggerPage.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().separator().defaultValue(RestartHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_CONNECT_ENABLED(RouterLoggerPage.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(ConnectHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_DISCONNECT_ENABLED(RouterLoggerPage.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(DisconnectHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_CLOSE_ENABLED(RouterLoggerPage.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(CloseHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_JSON_ENABLED(RouterLoggerPage.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().separator().defaultValue(BaseJsonHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_JSON_REFRESH(RouterLoggerPage.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(BaseJsonHandler.Defaults.REFRESH).parent(SERVER_HANDLER_JSON_ENABLED).build()),
	SERVER_HANDLER_JSON_REFRESH_SECS(RouterLoggerPage.SERVER_HANDLER, IntegerComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(BaseJsonHandler.Defaults.REFRESH_SECS).parent(SERVER_HANDLER_JSON_REFRESH).build(), new FieldEditorDataBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.server.handler.refresh.auto");
		}
	}, 0)).build()),

	SERVER_SSL_ENABLED(RouterLoggerPage.SERVER_HTTPS, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_SSL_KEYSTORE_TYPE(RouterLoggerPage.SERVER_HTTPS, ValidatedComboFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_KEYSTORE_TYPE).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDataBuilder().labelsAndValues(ServerHttpsPreferencePage.getKeyStoreAlgorithmsComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_KEYSTORE_FILE(RouterLoggerPage.SERVER_HTTPS, DefaultFileFieldEditor.class, new PreferenceDataBuilder().restartRequired().parent(SERVER_SSL_ENABLED).build(), new FieldEditorDataBuilder().fileExtensions(ServerHttpsPreferencePage.getKeyStoreFileExtensions()).build()),
	SERVER_SSL_STOREPASS(RouterLoggerPage.SERVER_HTTPS, PasswordFieldEditor.class, new PreferenceDataBuilder().restartRequired().parent(SERVER_SSL_ENABLED).build()),
	SERVER_SSL_KEYPASS(RouterLoggerPage.SERVER_HTTPS, PasswordFieldEditor.class, new PreferenceDataBuilder().restartRequired().parent(SERVER_SSL_ENABLED).build()),
	SERVER_SSL_PROTOCOL(RouterLoggerPage.SERVER_HTTPS, ValidatedComboFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_PROTOCOL).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDataBuilder().labelsAndValues(ServerHttpsPreferencePage.getSslContextAlgorithmsComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_KMF_ALGORITHM(RouterLoggerPage.SERVER_HTTPS, ValidatedComboFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_KMF_ALGORITHM).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDataBuilder().labelsAndValues(ServerHttpsPreferencePage.getKeyManagerFactoryComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_TMF_ALGORITHM(RouterLoggerPage.SERVER_HTTPS, ValidatedComboFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_TMF_ALGORITHM).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDataBuilder().labelsAndValues(ServerHttpsPreferencePage.getTrustManagerFactoryComboOptions()).emptyStringAllowed(false).build()),

	MQTT_ENABLED(RouterLoggerPage.MQTT, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.ENABLED).build()),
	MQTT_SERVER_URI(RouterLoggerPage.MQTT, UriListEditor.class, new PreferenceDataBuilder().restartRequired().parent(MQTT_ENABLED).build(), new FieldEditorDataBuilder().horizontalSpan(2).icons(Images.MAIN_ICONS).build()),
	MQTT_USERNAME(RouterLoggerPage.MQTT, DefaultStringFieldEditor.class, new PreferenceDataBuilder().restartRequired().parent(MQTT_ENABLED).build()),
	MQTT_PASSWORD(RouterLoggerPage.MQTT, PasswordFieldEditor.class, new PreferenceDataBuilder().restartRequired().parent(MQTT_ENABLED).build()),
	MQTT_CLIENT_ID(RouterLoggerPage.MQTT, DefaultStringFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.CLIENT_ID).parent(MQTT_ENABLED).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	MQTT_CONNECT_RETRY(RouterLoggerPage.MQTT, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().separator().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.CONNECT_RETRY).parent(MQTT_ENABLED).build()),

	MQTT_DATA_ENABLED(RouterLoggerPage.MQTT_MESSAGES, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_ENABLED).parent(MQTT_ENABLED).build()),
	MQTT_DATA_TOPIC(RouterLoggerPage.MQTT_MESSAGES, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_TOPIC).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	MQTT_DATA_QOS(RouterLoggerPage.MQTT_MESSAGES, DefaultComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_QOS).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDataBuilder().labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_DATA_THROTTLING_MS(RouterLoggerPage.MQTT_MESSAGES, IntegerComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_THROTTLING_IN_MILLIS).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDataBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.mqtt.data.throttling.disabled");
		}
	}, 0)).build()),
	MQTT_DATA_RETAINED(RouterLoggerPage.MQTT_MESSAGES, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_RETAINED).parent(MQTT_DATA_ENABLED).build()),

	MQTT_STATUS_ENABLED(RouterLoggerPage.MQTT_MESSAGES, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().separator().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_ENABLED).parent(MQTT_ENABLED).build()),
	MQTT_STATUS_TOPIC(RouterLoggerPage.MQTT_MESSAGES, DefaultStringFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_TOPIC).parent(MQTT_STATUS_ENABLED).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	MQTT_STATUS_QOS(RouterLoggerPage.MQTT_MESSAGES, DefaultComboFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_QOS).parent(MQTT_STATUS_ENABLED).build(), new FieldEditorDataBuilder().labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_STATUS_RETAINED(RouterLoggerPage.MQTT_MESSAGES, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_RETAINED).parent(MQTT_STATUS_ENABLED).build()),

	MQTT_THRESHOLDS_ENABLED(RouterLoggerPage.MQTT_MESSAGES, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().separator().defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_ENABLED).parent(MQTT_ENABLED).build()),
	MQTT_THRESHOLDS_TOPIC(RouterLoggerPage.MQTT_MESSAGES, DefaultStringFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_TOPIC).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).build()),
	MQTT_THRESHOLDS_QOS(RouterLoggerPage.MQTT_MESSAGES, DefaultComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_QOS).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDataBuilder().labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_THRESHOLDS_THROTTLING_MS(RouterLoggerPage.MQTT_MESSAGES, IntegerComboFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_THROTTLING_IN_MILLIS).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDataBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.mqtt.thresholds.throttling.disabled");
		}
	}, 0)).build()),
	MQTT_THRESHOLDS_RETAINED(RouterLoggerPage.MQTT_MESSAGES, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_RETAINED).parent(MQTT_THRESHOLDS_ENABLED).build()),

	MQTT_CLEAN_SESSION(RouterLoggerPage.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.CLEAN_SESSION).parent(MQTT_ENABLED).build()),
	MQTT_AUTOMATIC_RECONNECT(RouterLoggerPage.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.AUTOMATIC_RECONNECT).parent(MQTT_ENABLED).build()),
	MQTT_CONNECTION_TIMEOUT(RouterLoggerPage.MQTT_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.CONNECTION_TIMEOUT).parent(MQTT_ENABLED).build()),
	MQTT_KEEP_ALIVE_INTERVAL(RouterLoggerPage.MQTT_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.KEEP_ALIVE_INTERVAL).parent(MQTT_ENABLED).build()),
	MQTT_MAX_INFLIGHT(RouterLoggerPage.MQTT_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.MAX_INFLIGHT).parent(MQTT_ENABLED).build()),
	MQTT_VERSION(RouterLoggerPage.MQTT_ADVANCED, DefaultComboFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.MQTT_VERSION).parent(MQTT_ENABLED).build(), new FieldEditorDataBuilder().labelsAndValues(AdvancedMqttPreferencePage.getMqttVersionComboOptions()).build()),
	MQTT_PERSISTENCE_FILE_ENABLED(RouterLoggerPage.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.PERSISTENCE_FILE_ENABLED).parent(MQTT_ENABLED).build()),
	MQTT_PERSISTENCE_FILE_CUSTOM(RouterLoggerPage.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.PERSISTENCE_FILE_CUSTOM).parent(MQTT_PERSISTENCE_FILE_ENABLED).build()),
	MQTT_PERSISTENCE_FILE_PATH(RouterLoggerPage.MQTT_ADVANCED, DefaultDirectoryFieldEditor.class, new PreferenceDataBuilder().restartRequired().defaultValue(System.getProperty("user.dir")).parent(MQTT_PERSISTENCE_FILE_CUSTOM).build(), new FieldEditorDataBuilder().emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.mqtt");
		}
	}).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private static final FieldEditorFactory fieldEditorFactory = new RouterLoggerFieldEditorFactory();

	private final Page page;
	private final Class<? extends FieldEditor> fieldEditorType;
	private final String defaultValue;
	private final FieldEditorData fieldEditorData;
	private final Preference parent;
	private final String configurationKey;
	private final String labelKey;
	private final boolean restartRequired;
	private final boolean separator;

	private RouterLoggerPreference(final Page page, final Class<? extends FieldEditor> fieldEditorType) {
		this(page, fieldEditorType, null, null);
	}

	private RouterLoggerPreference(final Page page, final Class<? extends FieldEditor> fieldEditorType, final PreferenceData preferenceData) {
		this(page, fieldEditorType, preferenceData, null);
	}

	private RouterLoggerPreference(final Page page, final Class<? extends FieldEditor> fieldEditorType, final FieldEditorData fieldEditorData) {
		this(page, fieldEditorType, null, fieldEditorData);
	}

	private RouterLoggerPreference(final Page page, final Class<? extends FieldEditor> fieldEditorType, final PreferenceData preferenceData, final FieldEditorData fieldEditorData) {
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
			this.separator = preferenceData.hasSeparator();
		}
		else {
			this.configurationKey = name().toLowerCase().replace('_', '.');
			this.labelKey = LABEL_KEY_PREFIX + this.configurationKey;
			this.restartRequired = false;
			this.defaultValue = null;
			this.parent = null;
			this.separator = false;
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
	public boolean hasSeparator() {
		return separator;
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
		return fieldEditorFactory.createFieldEditor(fieldEditorType, configurationKey, getLabel(), parent, fieldEditorData);
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
