package it.albertus.router.gui.preference;

import it.albertus.jface.TextConsole;
import it.albertus.jface.preference.FieldEditorDetails;
import it.albertus.jface.preference.FieldEditorDetails.FieldEditorDetailsBuilder;
import it.albertus.jface.preference.FieldEditorFactory;
import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.IPreference;
import it.albertus.jface.preference.PreferenceDetails;
import it.albertus.jface.preference.PreferenceDetails.PreferenceDetailsBuilder;
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
import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.jface.preference.page.IPreferencePageDefinition;
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
import it.albertus.router.gui.preference.page.PageDefinition;
import it.albertus.router.gui.preference.page.ReaderPreferencePage;
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

public enum RouterLoggerPreference implements IPreference {

	LANGUAGE(PageDefinition.GENERAL, ComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(Locale.getDefault().getLanguage()).build(), new FieldEditorDetailsBuilder().labelsAndValues(GeneralPreferencePage.getLanguageComboOptions()).build()),
	LOGGER_ITERATIONS(PageDefinition.GENERAL, IntegerComboFieldEditor.class, new PreferenceDetailsBuilder().separate().defaultValue(RouterLoggerEngine.Defaults.ITERATIONS).build(), new FieldEditorDetailsBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.iterations.infinite");
		}
	}, 0)).build()),
	LOGGER_CLOSE_WHEN_FINISHED(PageDefinition.GENERAL, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerEngine.Defaults.CLOSE_WHEN_FINISHED).build()),
	LOGGER_INTERVAL_NORMAL_MS(PageDefinition.GENERAL, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS).build()),
	LOGGER_INTERVAL_FAST_MS(PageDefinition.GENERAL, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerEngine.Defaults.INTERVAL_FAST_IN_MILLIS).build()),
	LOGGER_HYSTERESIS_MS(PageDefinition.GENERAL, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerEngine.Defaults.HYSTERESIS_IN_MILLIS).build()),
	LOGGER_RETRY_COUNT(PageDefinition.GENERAL, IntegerComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerEngine.Defaults.RETRIES).build(), new FieldEditorDetailsBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.logger.retry.count.infinite");
		}
	}, 0)).build()),
	LOGGER_RETRY_INTERVAL_MS(PageDefinition.GENERAL, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerEngine.Defaults.RETRY_INTERVAL_IN_MILLIS).build()),
	LOGGER_ERROR_LOG_DESTINATION_PATH(PageDefinition.GENERAL, DefaultDirectoryFieldEditor.class, new PreferenceDetailsBuilder().separate().defaultValue(Logger.Defaults.DIRECTORY).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.log");
		}
	}).build()),
	CONSOLE_SHOW_CONFIGURATION(PageDefinition.GENERAL, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().separate().defaultValue(RouterLoggerEngine.Defaults.CONSOLE_SHOW_CONFIGURATION).build()),
	CONSOLE_DEBUG(PageDefinition.GENERAL, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(Logger.Defaults.DEBUG).build()),
	LOG_EMAIL(PageDefinition.GENERAL, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(Logger.Defaults.EMAIL).build()),
	LOG_EMAIL_IGNORE_DUPLICATES(PageDefinition.GENERAL, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(Logger.Defaults.EMAIL_IGNORE_DUPLICATES).parent(LOG_EMAIL).build()),

	READER_CLASS_NAME(PageDefinition.READER, ReaderComboFieldEditor.class, new FieldEditorDetailsBuilder().labelsAndValues(ReaderPreferencePage.getReaderComboOptions()).build()),
	ROUTER_USERNAME(PageDefinition.READER, DefaultStringFieldEditor.class),
	ROUTER_PASSWORD(PageDefinition.READER, PasswordFieldEditor.class),
	ROUTER_ADDRESS(PageDefinition.READER, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(Reader.Defaults.ROUTER_ADDRESS).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	ROUTER_PORT(PageDefinition.READER, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(Reader.Defaults.ROUTER_PORT).build(), new FieldEditorDetailsBuilder().integerValidRange(1, 65535).build()),
	SOCKET_TIMEOUT_MS(PageDefinition.READER, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(Reader.Defaults.SOCKET_TIMEOUT_IN_MILLIS).build()),
	CONNECTION_TIMEOUT_MS(PageDefinition.READER, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(Reader.Defaults.CONNECTION_TIMEOUT_IN_MILLIS).build()),
	TELNET_NEWLINE_CHARACTERS(PageDefinition.READER, DefaultComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(Reader.Defaults.TELNET_NEWLINE_CHARACTERS).build(), new FieldEditorDetailsBuilder().labelsAndValues(BasePreferencePage.getNewLineComboOptions()).build()),
	READER_LOG_CONNECTED(PageDefinition.READER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerEngine.Defaults.LOG_CONNECTED).build()),
	READER_WAIT_DISCONNECTED(PageDefinition.READER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED).build()),
	READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD(PageDefinition.READER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD).parent(READER_WAIT_DISCONNECTED).build()),
	READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD_MS(PageDefinition.READER, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD_IN_MILLIS).parent(READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD).build()),

	TPLINK_8970_COMMAND_INFO_ADSL(PageDefinition.TPLINK_8970, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(TpLink8970Reader.Defaults.COMMAND_INFO_ADSL).build()),
	TPLINK_8970_COMMAND_INFO_WAN(PageDefinition.TPLINK_8970, DefaultStringFieldEditor.class),
	ASUS_DSLN12E_COMMAND_INFO_ADSL(PageDefinition.ASUS_N12E, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(AsusDslN12EReader.Defaults.COMMAND_INFO_ADSL).build()),
	ASUS_DSLN12E_COMMAND_INFO_WAN(PageDefinition.ASUS_N12E, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(AsusDslN12EReader.Defaults.COMMAND_INFO_WAN).build()),
	ASUS_DSLN14U_COMMAND_INFO_ADSL(PageDefinition.ASUS_N14U, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(AsusDslN14UReader.Defaults.COMMAND_INFO_ADSL).build()),
	ASUS_DSLN14U_COMMAND_INFO_WAN(PageDefinition.ASUS_N14U, DefaultStringFieldEditor.class),
	DLINK_2750_COMMAND_INFO_ADSL_STATUS(PageDefinition.DLINK_2750, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_STATUS).build()),
	DLINK_2750_COMMAND_INFO_ADSL_SNR(PageDefinition.DLINK_2750, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_SNR).build()),

	GUI_TABLE_ITEMS_MAX(PageDefinition.APPEARANCE, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DataTable.Defaults.MAX_ITEMS).build(), new FieldEditorDetailsBuilder().textLimit(4).build()),
	GUI_IMPORTANT_KEYS(PageDefinition.APPEARANCE, WrapStringFieldEditor.class, new FieldEditorDetailsBuilder().textHeight(3).build()),
	GUI_IMPORTANT_KEYS_SEPARATOR(PageDefinition.APPEARANCE, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerConfiguration.Defaults.GUI_IMPORTANT_KEYS_SEPARATOR).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	GUI_IMPORTANT_KEYS_COLOR(PageDefinition.APPEARANCE, ColorFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DataTable.Defaults.IMPORTANT_KEY_BACKGROUND_COLOR).build()),
	GUI_THRESHOLDS_REACHED_COLOR(PageDefinition.APPEARANCE, ColorFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DataTable.Defaults.THRESHOLDS_REACHED_FOREGROUD_COLOR).build()),
	GUI_TABLE_COLUMNS_PACK(PageDefinition.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PACK).build()),
	GUI_TABLE_COLUMNS_PADDING_RIGHT(PageDefinition.APPEARANCE, ScaleIntegerFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PADDING_RIGHT).build(), new FieldEditorDetailsBuilder().scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	GUI_CONSOLE_MAX_CHARS(PageDefinition.APPEARANCE, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(TextConsole.Defaults.GUI_CONSOLE_MAX_CHARS).build(), new FieldEditorDetailsBuilder().textLimit(6).build()),
	GUI_CLIPBOARD_MAX_CHARS(PageDefinition.APPEARANCE, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerGui.Defaults.GUI_CLIPBOARD_MAX_CHARS).build(), new FieldEditorDetailsBuilder().integerValidRange(0, 128 * 1024).build()),
	GUI_MINIMIZE_TRAY(PageDefinition.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(TrayIcon.Defaults.GUI_MINIMIZE_TRAY).build()),
	GUI_TRAY_TOOLTIP(PageDefinition.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(TrayIcon.Defaults.GUI_TRAY_TOOLTIP).parent(GUI_MINIMIZE_TRAY).build()),
	GUI_START_MINIMIZED(PageDefinition.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerGui.Defaults.GUI_START_MINIMIZED).build()),
	GUI_CONFIRM_CLOSE(PageDefinition.APPEARANCE, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(CloseMessageBox.Defaults.GUI_CONFIRM_CLOSE).build()),

	CONSOLE_ANIMATION(PageDefinition.CONSOLE, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerConsole.Defaults.CONSOLE_ANIMATION).build()),
	CONSOLE_SHOW_KEYS(PageDefinition.CONSOLE, WrapStringFieldEditor.class),
	CONSOLE_SHOW_KEYS_SEPARATOR(PageDefinition.CONSOLE, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerConfiguration.Defaults.CONSOLE_SHOW_KEYS_SEPARATOR).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),

	WRITER_CLASS_NAME(PageDefinition.WRITER, WriterComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerEngine.Defaults.WRITER_CLASS.getSimpleName()).build(), new FieldEditorDetailsBuilder().labelsAndValues(WriterPreferencePage.getWriterComboOptions()).build()),

	CSV_DESTINATION_PATH(PageDefinition.CSV, DefaultDirectoryFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(CsvWriter.Defaults.DIRECTORY).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.csv");
		}
	}).build()),
	CSV_NEWLINE_CHARACTERS(PageDefinition.CSV, DefaultComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(CsvWriter.Defaults.NEWLINE.name()).build(), new FieldEditorDetailsBuilder().labelsAndValues(BasePreferencePage.getNewLineComboOptions()).build()),
	CSV_FIELD_SEPARATOR(PageDefinition.CSV, DelimiterComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(CsvWriter.Defaults.FIELD_SEPARATOR).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).labelsAndValues(CsvPreferencePage.getSeparatorComboOptions()).build()),
	CSV_FIELD_SEPARATOR_REPLACEMENT(PageDefinition.CSV, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(CsvWriter.Defaults.FIELD_SEPARATOR_REPLACEMENT).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	CSV_EMAIL(PageDefinition.CSV, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(CsvWriter.Defaults.EMAIL).build()),

	DATABASE_DRIVER_CLASS_NAME(PageDefinition.DATABASE, DatabaseComboFieldEditor.class, new FieldEditorDetailsBuilder().labelsAndValues(DatabasePreferencePage.getDatabaseComboOptions()).build()),
	DATABASE_URL(PageDefinition.DATABASE, DefaultStringFieldEditor.class),
	DATABASE_USERNAME(PageDefinition.DATABASE, DefaultStringFieldEditor.class),
	DATABASE_PASSWORD(PageDefinition.DATABASE, PasswordFieldEditor.class),
	DATABASE_TABLE_NAME(PageDefinition.DATABASE, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DatabaseWriter.Defaults.TABLE_NAME).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	DATABASE_CONNECTION_VALIDATION_TIMEOUT_MS(PageDefinition.DATABASE, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DatabaseWriter.Defaults.CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS).build(), new FieldEditorDetailsBuilder().textLimit(5).build()),
	DATABASE_TIMESTAMP_COLUMN_TYPE(PageDefinition.DATABASE, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DatabaseWriter.Defaults.TIMESTAMP_COLUMN_TYPE).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	DATABASE_RESPONSE_COLUMN_TYPE(PageDefinition.DATABASE, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DatabaseWriter.Defaults.RESPONSE_TIME_COLUMN_TYPE).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	DATABASE_INFO_COLUMN_TYPE(PageDefinition.DATABASE, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DatabaseWriter.Defaults.INFO_COLUMN_TYPE).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_PREFIX(PageDefinition.DATABASE, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DatabaseWriter.Defaults.COLUMN_NAME_PREFIX).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_MAX_LENGTH(PageDefinition.DATABASE, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DatabaseWriter.Defaults.COLUMN_NAME_MAX_LENGTH).build(), new FieldEditorDetailsBuilder().textLimit(2).build()),

	THRESHOLDS_EXPRESSIONS(PageDefinition.THRESHOLDS, ThresholdsListEditor.class),
	THRESHOLDS_SPLIT(PageDefinition.THRESHOLDS, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerConfiguration.Defaults.THRESHOLDS_SPLIT).build()),
	THRESHOLDS_EMAIL(PageDefinition.THRESHOLDS, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerEngine.Defaults.THRESHOLDS_EMAIL).build()),
	THRESHOLDS_EMAIL_SEND_INTERVAL_SECS(PageDefinition.THRESHOLDS, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(ThresholdsEmailSender.Defaults.THRESHOLDS_EMAIL_SEND_INTERVAL_SECS).parent(THRESHOLDS_EMAIL).build()),
	THRESHOLDS_EMAIL_MAX_ITEMS(PageDefinition.THRESHOLDS, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(ThresholdsEmailSender.Defaults.MAX_ITEMS).parent(THRESHOLDS_EMAIL).build(), new FieldEditorDetailsBuilder().integerValidRange(1, 1000).build()),
	THRESHOLDS_EXCLUDED(PageDefinition.THRESHOLDS, WrapStringFieldEditor.class),
	THRESHOLDS_EXCLUDED_SEPARATOR(PageDefinition.THRESHOLDS, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerConfiguration.Defaults.THRESHOLDS_EXCLUDED_SEPARATOR).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),

	EMAIL_HOST(PageDefinition.EMAIL, DefaultStringFieldEditor.class),
	EMAIL_USERNAME(PageDefinition.EMAIL, DefaultStringFieldEditor.class),
	EMAIL_PASSWORD(PageDefinition.EMAIL, PasswordFieldEditor.class),
	EMAIL_FROM_NAME(PageDefinition.EMAIL, DefaultStringFieldEditor.class),
	EMAIL_FROM_ADDRESS(PageDefinition.EMAIL, DefaultStringFieldEditor.class),
	EMAIL_TO_ADDRESSES(PageDefinition.EMAIL, EmailAddressesListEditor.class, new FieldEditorDetailsBuilder().horizontalSpan(Short.MAX_VALUE).icons(Images.MAIN_ICONS).build()),

	EMAIL_PORT(PageDefinition.EMAIL_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(EmailSender.Defaults.PORT).build(), new FieldEditorDetailsBuilder().integerValidRange(1, 65535).build()),
	EMAIL_SSL_PORT(PageDefinition.EMAIL_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(EmailSender.Defaults.SSL_PORT).build(), new FieldEditorDetailsBuilder().integerValidRange(1, 65535).build()),
	EMAIL_CONNECTION_TIMEOUT(PageDefinition.EMAIL_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(EmailSender.Defaults.SOCKET_CONNECTION_TIMEOUT).build()),
	EMAIL_SOCKET_TIMEOUT(PageDefinition.EMAIL_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(EmailSender.Defaults.SOCKET_TIMEOUT).build()),
	EMAIL_RETRY_INTERVAL_SECS(PageDefinition.EMAIL_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(EmailSender.Defaults.RETRY_INTERVAL_SECS).build()),
	EMAIL_MAX_SENDINGS_PER_CYCLE(PageDefinition.EMAIL_ADVANCED, IntegerComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(EmailSender.Defaults.MAX_SENDINGS_PER_CYCLE).build(), new FieldEditorDetailsBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.email.max.sendings.per.cycle.unlimited");
		}
	}, 0)).build()),
	EMAIL_MAX_QUEUE_SIZE(PageDefinition.EMAIL_ADVANCED, ScaleIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(EmailSender.Defaults.MAX_QUEUE_SIZE).build(), new FieldEditorDetailsBuilder().scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	EMAIL_SSL_CONNECT(PageDefinition.EMAIL_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(EmailSender.Defaults.SSL_CONNECT).build()),
	EMAIL_SSL_IDENTITY(PageDefinition.EMAIL_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(EmailSender.Defaults.SSL_IDENTITY).build()),
	EMAIL_STARTTLS_ENABLED(PageDefinition.EMAIL_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(EmailSender.Defaults.STARTTLS_ENABLED).build()),
	EMAIL_STARTTLS_REQUIRED(PageDefinition.EMAIL_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(EmailSender.Defaults.STARTTLS_REQUIRED).build()),

	EMAIL_CC_ADDRESSES(PageDefinition.EMAIL_CC_BCC, EmailAddressesListEditor.class, new FieldEditorDetailsBuilder().horizontalSpan(0).icons(Images.MAIN_ICONS).build()),
	EMAIL_BCC_ADDRESSES(PageDefinition.EMAIL_CC_BCC, EmailAddressesListEditor.class, new FieldEditorDetailsBuilder().horizontalSpan(0).icons(Images.MAIN_ICONS).build()),

	SERVER_ENABLED(PageDefinition.SERVER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(BaseHttpServer.Defaults.ENABLED).restartRequired().build()),
	SERVER_PORT(PageDefinition.SERVER, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(BaseHttpServer.Defaults.PORT).restartRequired().parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder().integerValidRange(1, 65535).build()),
	SERVER_AUTHENTICATION(PageDefinition.SERVER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().parent(SERVER_ENABLED).defaultValue(BaseHttpServer.Defaults.AUTHENTICATION).restartRequired().build()),
	SERVER_USERNAME(PageDefinition.SERVER, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().parent(SERVER_AUTHENTICATION).build()),
	SERVER_PASSWORD(PageDefinition.SERVER, PasswordFieldEditor.class, new PreferenceDetailsBuilder().parent(SERVER_AUTHENTICATION).build()),
	SERVER_COMPRESS_RESPONSE(PageDefinition.SERVER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(BaseHtmlHandler.Defaults.COMPRESS_RESPONSE).parent(SERVER_ENABLED).build()),
	SERVER_LOG_REQUEST(PageDefinition.SERVER, DefaultComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(BaseHtmlHandler.Defaults.LOG_REQUEST).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder().labelsAndValues(ServerPreferencePage.getLogComboOptions()).build()),
	SERVER_THREADS(PageDefinition.SERVER, ScaleIntegerFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(BaseHttpServer.Defaults.THREADS).restartRequired().parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder().scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(010).build()),

	SERVER_HANDLER_ROOT_ENABLED(PageDefinition.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RootHtmlHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_STATUS_ENABLED(PageDefinition.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(StatusHtmlHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_STATUS_REFRESH(PageDefinition.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(StatusHtmlHandler.Defaults.REFRESH).parent(SERVER_HANDLER_STATUS_ENABLED).build()),
	SERVER_HANDLER_STATUS_REFRESH_SECS(PageDefinition.SERVER_HANDLER, IntegerComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(StatusHtmlHandler.Defaults.REFRESH_SECS).parent(SERVER_HANDLER_STATUS_REFRESH).build(), new FieldEditorDetailsBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.server.handler.refresh.auto");
		}
	}, 0)).build()),
	SERVER_HANDLER_RESTART_ENABLED(PageDefinition.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().separate().defaultValue(RestartHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_CONNECT_ENABLED(PageDefinition.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(ConnectHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_DISCONNECT_ENABLED(PageDefinition.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(DisconnectHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_CLOSE_ENABLED(PageDefinition.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(CloseHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_JSON_ENABLED(PageDefinition.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().separate().defaultValue(BaseJsonHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_HANDLER_JSON_REFRESH(PageDefinition.SERVER_HANDLER, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(BaseJsonHandler.Defaults.REFRESH).parent(SERVER_HANDLER_JSON_ENABLED).build()),
	SERVER_HANDLER_JSON_REFRESH_SECS(PageDefinition.SERVER_HANDLER, IntegerComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(BaseJsonHandler.Defaults.REFRESH_SECS).parent(SERVER_HANDLER_JSON_REFRESH).build(), new FieldEditorDetailsBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.server.handler.refresh.auto");
		}
	}, 0)).build()),

	SERVER_SSL_ENABLED(PageDefinition.SERVER_HTTPS, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_ENABLED).parent(SERVER_ENABLED).build()),
	SERVER_SSL_KEYSTORE_TYPE(PageDefinition.SERVER_HTTPS, ValidatedComboFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_KEYSTORE_TYPE).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder().labelsAndValues(ServerHttpsPreferencePage.getKeyStoreAlgorithmsComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_KEYSTORE_FILE(PageDefinition.SERVER_HTTPS, DefaultFileFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder().fileExtensions(ServerHttpsPreferencePage.getKeyStoreFileExtensions()).build()),
	SERVER_SSL_STOREPASS(PageDefinition.SERVER_HTTPS, PasswordFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().parent(SERVER_SSL_ENABLED).build()),
	SERVER_SSL_KEYPASS(PageDefinition.SERVER_HTTPS, PasswordFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().parent(SERVER_SSL_ENABLED).build()),
	SERVER_SSL_PROTOCOL(PageDefinition.SERVER_HTTPS, ValidatedComboFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_PROTOCOL).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder().labelsAndValues(ServerHttpsPreferencePage.getSslContextAlgorithmsComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_KMF_ALGORITHM(PageDefinition.SERVER_HTTPS, ValidatedComboFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_KMF_ALGORITHM).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder().labelsAndValues(ServerHttpsPreferencePage.getKeyManagerFactoryComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_TMF_ALGORITHM(PageDefinition.SERVER_HTTPS, ValidatedComboFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_TMF_ALGORITHM).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder().labelsAndValues(ServerHttpsPreferencePage.getTrustManagerFactoryComboOptions()).emptyStringAllowed(false).build()),

	MQTT_ENABLED(PageDefinition.MQTT, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerMqttClient.Defaults.ENABLED).build()),
	MQTT_SERVER_URI(PageDefinition.MQTT, UriListEditor.class, new PreferenceDetailsBuilder().restartRequired().parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder().horizontalSpan(2).icons(Images.MAIN_ICONS).build()),
	MQTT_USERNAME(PageDefinition.MQTT, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().parent(MQTT_ENABLED).build()),
	MQTT_PASSWORD(PageDefinition.MQTT, PasswordFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().parent(MQTT_ENABLED).build()),
	MQTT_CLIENT_ID(PageDefinition.MQTT, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.CLIENT_ID).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	MQTT_CONNECT_RETRY(PageDefinition.MQTT, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().separate().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.CONNECT_RETRY).parent(MQTT_ENABLED).build()),

	MQTT_DATA_ENABLED(PageDefinition.MQTT_MESSAGES, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_ENABLED).parent(MQTT_ENABLED).build()),
	MQTT_DATA_TOPIC(PageDefinition.MQTT_MESSAGES, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_TOPIC).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	MQTT_DATA_QOS(PageDefinition.MQTT_MESSAGES, DefaultComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_QOS).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDetailsBuilder().labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_DATA_THROTTLING_MS(PageDefinition.MQTT_MESSAGES, IntegerComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_THROTTLING_IN_MILLIS).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDetailsBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.mqtt.data.throttling.disabled");
		}
	}, 0)).build()),
	MQTT_DATA_RETAINED(PageDefinition.MQTT_MESSAGES, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerMqttClient.Defaults.DATA_RETAINED).parent(MQTT_DATA_ENABLED).build()),

	MQTT_STATUS_ENABLED(PageDefinition.MQTT_MESSAGES, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().separate().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_ENABLED).parent(MQTT_ENABLED).build()),
	MQTT_STATUS_TOPIC(PageDefinition.MQTT_MESSAGES, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_TOPIC).parent(MQTT_STATUS_ENABLED).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	MQTT_STATUS_QOS(PageDefinition.MQTT_MESSAGES, DefaultComboFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_QOS).parent(MQTT_STATUS_ENABLED).build(), new FieldEditorDetailsBuilder().labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_STATUS_RETAINED(PageDefinition.MQTT_MESSAGES, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_RETAINED).parent(MQTT_STATUS_ENABLED).build()),

	MQTT_THRESHOLDS_ENABLED(PageDefinition.MQTT_MESSAGES, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().separate().defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_ENABLED).parent(MQTT_ENABLED).build()),
	MQTT_THRESHOLDS_TOPIC(PageDefinition.MQTT_MESSAGES, DefaultStringFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_TOPIC).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).build()),
	MQTT_THRESHOLDS_QOS(PageDefinition.MQTT_MESSAGES, DefaultComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_QOS).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDetailsBuilder().labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_THRESHOLDS_THROTTLING_MS(PageDefinition.MQTT_MESSAGES, IntegerComboFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_THROTTLING_IN_MILLIS).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDetailsBuilder().labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.mqtt.thresholds.throttling.disabled");
		}
	}, 0)).build()),
	MQTT_THRESHOLDS_RETAINED(PageDefinition.MQTT_MESSAGES, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_RETAINED).parent(MQTT_THRESHOLDS_ENABLED).build()),

	MQTT_CLEAN_SESSION(PageDefinition.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.CLEAN_SESSION).parent(MQTT_ENABLED).build()),
	MQTT_AUTOMATIC_RECONNECT(PageDefinition.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.AUTOMATIC_RECONNECT).parent(MQTT_ENABLED).build()),
	MQTT_CONNECTION_TIMEOUT(PageDefinition.MQTT_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.CONNECTION_TIMEOUT).parent(MQTT_ENABLED).build()),
	MQTT_KEEP_ALIVE_INTERVAL(PageDefinition.MQTT_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.KEEP_ALIVE_INTERVAL).parent(MQTT_ENABLED).build()),
	MQTT_MAX_INFLIGHT(PageDefinition.MQTT_ADVANCED, DefaultIntegerFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.MAX_INFLIGHT).parent(MQTT_ENABLED).build()),
	MQTT_VERSION(PageDefinition.MQTT_ADVANCED, DefaultComboFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.MQTT_VERSION).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder().labelsAndValues(AdvancedMqttPreferencePage.getMqttVersionComboOptions()).build()),
	MQTT_PERSISTENCE_FILE_ENABLED(PageDefinition.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.PERSISTENCE_FILE_ENABLED).parent(MQTT_ENABLED).build()),
	MQTT_PERSISTENCE_FILE_CUSTOM(PageDefinition.MQTT_ADVANCED, DefaultBooleanFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.PERSISTENCE_FILE_CUSTOM).parent(MQTT_PERSISTENCE_FILE_ENABLED).build()),
	MQTT_PERSISTENCE_FILE_PATH(PageDefinition.MQTT_ADVANCED, DefaultDirectoryFieldEditor.class, new PreferenceDetailsBuilder().restartRequired().defaultValue(System.getProperty("user.dir")).parent(MQTT_PERSISTENCE_FILE_CUSTOM).build(), new FieldEditorDetailsBuilder().emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.mqtt");
		}
	}).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private static final FieldEditorFactory fieldEditorFactory = new RouterLoggerFieldEditorFactory();

	private final PreferenceDetails preferenceDetails;
	private final FieldEditorDetails fieldEditorDetails;
	private final Class<? extends FieldEditor> fieldEditorType;
	private final IPreferencePageDefinition pageDefinition;

	private RouterLoggerPreference(final IPreferencePageDefinition page, final Class<? extends FieldEditor> fieldEditorType) {
		this(page, fieldEditorType, new PreferenceDetails(), null);
	}

	private RouterLoggerPreference(final IPreferencePageDefinition page, final Class<? extends FieldEditor> fieldEditorType, final PreferenceDetails preferenceData) {
		this(page, fieldEditorType, preferenceData, null);
	}

	private RouterLoggerPreference(final IPreferencePageDefinition page, final Class<? extends FieldEditor> fieldEditorType, final FieldEditorDetails fieldEditorData) {
		this(page, fieldEditorType, new PreferenceDetails(), fieldEditorData);
	}

	private RouterLoggerPreference(final IPreferencePageDefinition page, final Class<? extends FieldEditor> fieldEditorType, final PreferenceDetails preferenceDetails, final FieldEditorDetails fieldEditorDetails) {
		this.preferenceDetails = preferenceDetails;
		if (preferenceDetails.getName() == null) {
			preferenceDetails.setName(name().toLowerCase().replace('_', '.'));
		}
		if (preferenceDetails.getLabel() == null) {
			preferenceDetails.setLabel(new Localized() {
				@Override
				public String getString() {
					return Resources.get(LABEL_KEY_PREFIX + preferenceDetails.getName());
				}
			});
		}
		this.fieldEditorDetails = fieldEditorDetails;
		this.pageDefinition = page;
		this.fieldEditorType = fieldEditorType;
	}

	@Override
	public String getName() {
		return preferenceDetails.getName();
	}

	@Override
	public String getLabel() {
		return preferenceDetails.getLabel().getString();
	}

	@Override
	public IPreferencePageDefinition getPageDefinition() {
		return pageDefinition;
	}

	@Override
	public String getDefaultValue() {
		return preferenceDetails.getDefaultValue();
	}

	@Override
	public IPreference getParent() {
		return preferenceDetails.getParent();
	}

	@Override
	public boolean isRestartRequired() {
		return preferenceDetails.isRestartRequired();
	}

	@Override
	public boolean isSeparate() {
		return preferenceDetails.isSeparate();
	}

	@Override
	public Set<? extends IPreference> getChildren() {
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
		return fieldEditorFactory.createFieldEditor(fieldEditorType, getName(), getLabel(), parent, fieldEditorDetails);
	}

	public static IPreference forConfigurationKey(final String configurationKey) {
		if (configurationKey != null) {
			for (final RouterLoggerPreference preference : RouterLoggerPreference.values()) {
				if (configurationKey.equals(preference.getName())) {
					return preference;
				}
			}
		}
		return null;
	}

}
