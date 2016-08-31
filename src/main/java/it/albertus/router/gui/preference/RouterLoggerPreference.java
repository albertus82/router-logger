package it.albertus.router.gui.preference;

import it.albertus.jface.TextConsole;
import it.albertus.jface.preference.FieldEditorDetails;
import it.albertus.jface.preference.FieldEditorDetails.FieldEditorDetailsBuilder;
import it.albertus.jface.preference.FieldEditorFactory;
import it.albertus.jface.preference.IPreference;
import it.albertus.jface.preference.LocalizedLabelsAndValues;
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

	LANGUAGE(new PreferenceDetailsBuilder(PageDefinition.GENERAL).defaultValue(Locale.getDefault().getLanguage()).build(), new FieldEditorDetailsBuilder(ComboFieldEditor.class).labelsAndValues(GeneralPreferencePage.getLanguageComboOptions()).build()),
	LOGGER_ITERATIONS(new PreferenceDetailsBuilder(PageDefinition.GENERAL).separate().defaultValue(RouterLoggerEngine.Defaults.ITERATIONS).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.iterations.infinite");
		}
	}, 0)).build()),
	LOGGER_CLOSE_WHEN_FINISHED(new PreferenceDetailsBuilder(PageDefinition.GENERAL).defaultValue(RouterLoggerEngine.Defaults.CLOSE_WHEN_FINISHED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	LOGGER_INTERVAL_NORMAL_MS(new PreferenceDetailsBuilder(PageDefinition.GENERAL).defaultValue(RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),
	LOGGER_INTERVAL_FAST_MS(new PreferenceDetailsBuilder(PageDefinition.GENERAL).defaultValue(RouterLoggerEngine.Defaults.INTERVAL_FAST_IN_MILLIS).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),
	LOGGER_HYSTERESIS_MS(new PreferenceDetailsBuilder(PageDefinition.GENERAL).defaultValue(RouterLoggerEngine.Defaults.HYSTERESIS_IN_MILLIS).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),
	LOGGER_RETRY_COUNT(new PreferenceDetailsBuilder(PageDefinition.GENERAL).defaultValue(RouterLoggerEngine.Defaults.RETRIES).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.logger.retry.count.infinite");
		}
	}, 0)).build()),
	LOGGER_RETRY_INTERVAL_MS(new PreferenceDetailsBuilder(PageDefinition.GENERAL).defaultValue(RouterLoggerEngine.Defaults.RETRY_INTERVAL_IN_MILLIS).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),
	LOGGER_ERROR_LOG_DESTINATION_PATH(new PreferenceDetailsBuilder(PageDefinition.GENERAL).separate().defaultValue(Logger.Defaults.DIRECTORY).build(), new FieldEditorDetailsBuilder(DefaultDirectoryFieldEditor.class).emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.log");
		}
	}).build()),
	CONSOLE_SHOW_CONFIGURATION(new PreferenceDetailsBuilder(PageDefinition.GENERAL).separate().defaultValue(RouterLoggerEngine.Defaults.CONSOLE_SHOW_CONFIGURATION).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	CONSOLE_DEBUG(new PreferenceDetailsBuilder(PageDefinition.GENERAL).defaultValue(Logger.Defaults.DEBUG).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	LOG_EMAIL(new PreferenceDetailsBuilder(PageDefinition.GENERAL).defaultValue(Logger.Defaults.EMAIL).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	LOG_EMAIL_IGNORE_DUPLICATES(new PreferenceDetailsBuilder(PageDefinition.GENERAL).defaultValue(Logger.Defaults.EMAIL_IGNORE_DUPLICATES).parent(LOG_EMAIL).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	READER_CLASS_NAME(new PreferenceDetailsBuilder(PageDefinition.READER).build(), new FieldEditorDetailsBuilder(ReaderComboFieldEditor.class).labelsAndValues(ReaderPreferencePage.getReaderComboOptions()).build()),
	ROUTER_USERNAME(new PreferenceDetailsBuilder(PageDefinition.READER).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	ROUTER_PASSWORD(new PreferenceDetailsBuilder(PageDefinition.READER).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	ROUTER_ADDRESS(new PreferenceDetailsBuilder(PageDefinition.READER).defaultValue(Reader.Defaults.ROUTER_ADDRESS).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),
	ROUTER_PORT(new PreferenceDetailsBuilder(PageDefinition.READER).defaultValue(Reader.Defaults.ROUTER_PORT).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).integerValidRange(1, 65535).build()),
	SOCKET_TIMEOUT_MS(new PreferenceDetailsBuilder(PageDefinition.READER).defaultValue(Reader.Defaults.SOCKET_TIMEOUT_IN_MILLIS).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),
	CONNECTION_TIMEOUT_MS(new PreferenceDetailsBuilder(PageDefinition.READER).defaultValue(Reader.Defaults.CONNECTION_TIMEOUT_IN_MILLIS).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),
	TELNET_NEWLINE_CHARACTERS(new PreferenceDetailsBuilder(PageDefinition.READER).defaultValue(Reader.Defaults.TELNET_NEWLINE_CHARACTERS).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(BasePreferencePage.getNewLineComboOptions()).build()),
	READER_LOG_CONNECTED(new PreferenceDetailsBuilder(PageDefinition.READER).defaultValue(RouterLoggerEngine.Defaults.LOG_CONNECTED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	READER_WAIT_DISCONNECTED(new PreferenceDetailsBuilder(PageDefinition.READER).defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD(new PreferenceDetailsBuilder(PageDefinition.READER).defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD).parent(READER_WAIT_DISCONNECTED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD_MS(new PreferenceDetailsBuilder(PageDefinition.READER).defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD_IN_MILLIS).parent(READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),

	TPLINK_8970_COMMAND_INFO_ADSL(new PreferenceDetailsBuilder(PageDefinition.TPLINK_8970).defaultValue(TpLink8970Reader.Defaults.COMMAND_INFO_ADSL).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	TPLINK_8970_COMMAND_INFO_WAN(new PreferenceDetailsBuilder(PageDefinition.TPLINK_8970).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	ASUS_DSLN12E_COMMAND_INFO_ADSL(new PreferenceDetailsBuilder(PageDefinition.ASUS_N12E).defaultValue(AsusDslN12EReader.Defaults.COMMAND_INFO_ADSL).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	ASUS_DSLN12E_COMMAND_INFO_WAN(new PreferenceDetailsBuilder(PageDefinition.ASUS_N12E).defaultValue(AsusDslN12EReader.Defaults.COMMAND_INFO_WAN).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	ASUS_DSLN14U_COMMAND_INFO_ADSL(new PreferenceDetailsBuilder(PageDefinition.ASUS_N14U).defaultValue(AsusDslN14UReader.Defaults.COMMAND_INFO_ADSL).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	ASUS_DSLN14U_COMMAND_INFO_WAN(new PreferenceDetailsBuilder(PageDefinition.ASUS_N14U).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	DLINK_2750_COMMAND_INFO_ADSL_STATUS(new PreferenceDetailsBuilder(PageDefinition.DLINK_2750).defaultValue(DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_STATUS).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	DLINK_2750_COMMAND_INFO_ADSL_SNR(new PreferenceDetailsBuilder(PageDefinition.DLINK_2750).defaultValue(DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_SNR).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),

	GUI_TABLE_ITEMS_MAX(new PreferenceDetailsBuilder(PageDefinition.APPEARANCE).defaultValue(DataTable.Defaults.MAX_ITEMS).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).textLimit(4).build()),
	GUI_IMPORTANT_KEYS(new PreferenceDetailsBuilder(PageDefinition.APPEARANCE).build(), new FieldEditorDetailsBuilder(WrapStringFieldEditor.class).textHeight(3).build()),
	GUI_IMPORTANT_KEYS_SEPARATOR(new PreferenceDetailsBuilder(PageDefinition.APPEARANCE).defaultValue(RouterLoggerConfiguration.Defaults.GUI_IMPORTANT_KEYS_SEPARATOR).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),
	GUI_IMPORTANT_KEYS_COLOR(new PreferenceDetailsBuilder(PageDefinition.APPEARANCE).defaultValue(DataTable.Defaults.IMPORTANT_KEY_BACKGROUND_COLOR).build(), new FieldEditorDetailsBuilder(ColorFieldEditor.class).build()),
	GUI_THRESHOLDS_REACHED_COLOR(new PreferenceDetailsBuilder(PageDefinition.APPEARANCE).defaultValue(DataTable.Defaults.THRESHOLDS_REACHED_FOREGROUD_COLOR).build(), new FieldEditorDetailsBuilder(ColorFieldEditor.class).build()),
	GUI_TABLE_COLUMNS_PACK(new PreferenceDetailsBuilder(PageDefinition.APPEARANCE).restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PACK).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	GUI_TABLE_COLUMNS_PADDING_RIGHT(new PreferenceDetailsBuilder(PageDefinition.APPEARANCE).restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PADDING_RIGHT).build(), new FieldEditorDetailsBuilder(ScaleIntegerFieldEditor.class).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	GUI_CONSOLE_MAX_CHARS(new PreferenceDetailsBuilder(PageDefinition.APPEARANCE).defaultValue(TextConsole.Defaults.GUI_CONSOLE_MAX_CHARS).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).textLimit(6).build()),
	GUI_CLIPBOARD_MAX_CHARS(new PreferenceDetailsBuilder(PageDefinition.APPEARANCE).defaultValue(RouterLoggerGui.Defaults.GUI_CLIPBOARD_MAX_CHARS).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).integerValidRange(0, 128 * 1024).build()),
	GUI_MINIMIZE_TRAY(new PreferenceDetailsBuilder(PageDefinition.APPEARANCE).defaultValue(TrayIcon.Defaults.GUI_MINIMIZE_TRAY).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	GUI_TRAY_TOOLTIP(new PreferenceDetailsBuilder(PageDefinition.APPEARANCE).defaultValue(TrayIcon.Defaults.GUI_TRAY_TOOLTIP).parent(GUI_MINIMIZE_TRAY).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	GUI_START_MINIMIZED(new PreferenceDetailsBuilder(PageDefinition.APPEARANCE).defaultValue(RouterLoggerGui.Defaults.GUI_START_MINIMIZED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	GUI_CONFIRM_CLOSE(new PreferenceDetailsBuilder(PageDefinition.APPEARANCE).defaultValue(CloseMessageBox.Defaults.GUI_CONFIRM_CLOSE).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	CONSOLE_ANIMATION(new PreferenceDetailsBuilder(PageDefinition.CONSOLE).defaultValue(RouterLoggerConsole.Defaults.CONSOLE_ANIMATION).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	CONSOLE_SHOW_KEYS(new PreferenceDetailsBuilder(PageDefinition.CONSOLE).build(), new FieldEditorDetailsBuilder(WrapStringFieldEditor.class).build()),
	CONSOLE_SHOW_KEYS_SEPARATOR(new PreferenceDetailsBuilder(PageDefinition.CONSOLE).defaultValue(RouterLoggerConfiguration.Defaults.CONSOLE_SHOW_KEYS_SEPARATOR).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),

	WRITER_CLASS_NAME(new PreferenceDetailsBuilder(PageDefinition.WRITER).defaultValue(RouterLoggerEngine.Defaults.WRITER_CLASS.getSimpleName()).build(), new FieldEditorDetailsBuilder(WriterComboFieldEditor.class).labelsAndValues(WriterPreferencePage.getWriterComboOptions()).build()),

	CSV_DESTINATION_PATH(new PreferenceDetailsBuilder(PageDefinition.CSV).defaultValue(CsvWriter.Defaults.DIRECTORY).build(), new FieldEditorDetailsBuilder(DefaultDirectoryFieldEditor.class).emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.csv");
		}
	}).build()),
	CSV_NEWLINE_CHARACTERS(new PreferenceDetailsBuilder(PageDefinition.CSV).defaultValue(CsvWriter.Defaults.NEWLINE.name()).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(BasePreferencePage.getNewLineComboOptions()).build()),
	CSV_FIELD_SEPARATOR(new PreferenceDetailsBuilder(PageDefinition.CSV).defaultValue(CsvWriter.Defaults.FIELD_SEPARATOR).build(), new FieldEditorDetailsBuilder(DelimiterComboFieldEditor.class).emptyStringAllowed(false).labelsAndValues(CsvPreferencePage.getSeparatorComboOptions()).build()),
	CSV_FIELD_SEPARATOR_REPLACEMENT(new PreferenceDetailsBuilder(PageDefinition.CSV).defaultValue(CsvWriter.Defaults.FIELD_SEPARATOR_REPLACEMENT).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),
	CSV_EMAIL(new PreferenceDetailsBuilder(PageDefinition.CSV).defaultValue(CsvWriter.Defaults.EMAIL).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	DATABASE_DRIVER_CLASS_NAME(new PreferenceDetailsBuilder(PageDefinition.DATABASE).build(), new FieldEditorDetailsBuilder(DatabaseComboFieldEditor.class).labelsAndValues(DatabasePreferencePage.getDatabaseComboOptions()).build()),
	DATABASE_URL(new PreferenceDetailsBuilder(PageDefinition.DATABASE).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	DATABASE_USERNAME(new PreferenceDetailsBuilder(PageDefinition.DATABASE).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	DATABASE_PASSWORD(new PreferenceDetailsBuilder(PageDefinition.DATABASE).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	DATABASE_TABLE_NAME(new PreferenceDetailsBuilder(PageDefinition.DATABASE).defaultValue(DatabaseWriter.Defaults.TABLE_NAME).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),
	DATABASE_CONNECTION_VALIDATION_TIMEOUT_MS(new PreferenceDetailsBuilder(PageDefinition.DATABASE).defaultValue(DatabaseWriter.Defaults.CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).textLimit(5).build()),
	DATABASE_TIMESTAMP_COLUMN_TYPE(new PreferenceDetailsBuilder(PageDefinition.DATABASE).defaultValue(DatabaseWriter.Defaults.TIMESTAMP_COLUMN_TYPE).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),
	DATABASE_RESPONSE_COLUMN_TYPE(new PreferenceDetailsBuilder(PageDefinition.DATABASE).defaultValue(DatabaseWriter.Defaults.RESPONSE_TIME_COLUMN_TYPE).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),
	DATABASE_INFO_COLUMN_TYPE(new PreferenceDetailsBuilder(PageDefinition.DATABASE).defaultValue(DatabaseWriter.Defaults.INFO_COLUMN_TYPE).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_PREFIX(new PreferenceDetailsBuilder(PageDefinition.DATABASE).defaultValue(DatabaseWriter.Defaults.COLUMN_NAME_PREFIX).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_MAX_LENGTH(new PreferenceDetailsBuilder(PageDefinition.DATABASE).defaultValue(DatabaseWriter.Defaults.COLUMN_NAME_MAX_LENGTH).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).textLimit(2).build()),

	THRESHOLDS_EXPRESSIONS(new PreferenceDetailsBuilder(PageDefinition.THRESHOLDS).build(), new FieldEditorDetailsBuilder(ThresholdsListEditor.class).build()),
	THRESHOLDS_SPLIT(new PreferenceDetailsBuilder(PageDefinition.THRESHOLDS).defaultValue(RouterLoggerConfiguration.Defaults.THRESHOLDS_SPLIT).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	THRESHOLDS_EMAIL(new PreferenceDetailsBuilder(PageDefinition.THRESHOLDS).defaultValue(RouterLoggerEngine.Defaults.THRESHOLDS_EMAIL).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	THRESHOLDS_EMAIL_SEND_INTERVAL_SECS(new PreferenceDetailsBuilder(PageDefinition.THRESHOLDS).defaultValue(ThresholdsEmailSender.Defaults.THRESHOLDS_EMAIL_SEND_INTERVAL_SECS).parent(THRESHOLDS_EMAIL).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),
	THRESHOLDS_EMAIL_MAX_ITEMS(new PreferenceDetailsBuilder(PageDefinition.THRESHOLDS).defaultValue(ThresholdsEmailSender.Defaults.MAX_ITEMS).parent(THRESHOLDS_EMAIL).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).integerValidRange(1, 1000).build()),
	THRESHOLDS_EXCLUDED(new PreferenceDetailsBuilder(PageDefinition.THRESHOLDS).build(), new FieldEditorDetailsBuilder(WrapStringFieldEditor.class).build()),
	THRESHOLDS_EXCLUDED_SEPARATOR(new PreferenceDetailsBuilder(PageDefinition.THRESHOLDS).defaultValue(RouterLoggerConfiguration.Defaults.THRESHOLDS_EXCLUDED_SEPARATOR).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),

	EMAIL_HOST(new PreferenceDetailsBuilder(PageDefinition.EMAIL).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	EMAIL_USERNAME(new PreferenceDetailsBuilder(PageDefinition.EMAIL).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	EMAIL_PASSWORD(new PreferenceDetailsBuilder(PageDefinition.EMAIL).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	EMAIL_FROM_NAME(new PreferenceDetailsBuilder(PageDefinition.EMAIL).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	EMAIL_FROM_ADDRESS(new PreferenceDetailsBuilder(PageDefinition.EMAIL).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	EMAIL_TO_ADDRESSES(new PreferenceDetailsBuilder(PageDefinition.EMAIL).build(), new FieldEditorDetailsBuilder(EmailAddressesListEditor.class).horizontalSpan(Short.MAX_VALUE).icons(Images.MAIN_ICONS).build()),

	EMAIL_PORT(new PreferenceDetailsBuilder(PageDefinition.EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.PORT).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).integerValidRange(1, 65535).build()),
	EMAIL_SSL_PORT(new PreferenceDetailsBuilder(PageDefinition.EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.SSL_PORT).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).integerValidRange(1, 65535).build()),
	EMAIL_CONNECTION_TIMEOUT(new PreferenceDetailsBuilder(PageDefinition.EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.SOCKET_CONNECTION_TIMEOUT).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),
	EMAIL_SOCKET_TIMEOUT(new PreferenceDetailsBuilder(PageDefinition.EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.SOCKET_TIMEOUT).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),
	EMAIL_RETRY_INTERVAL_SECS(new PreferenceDetailsBuilder(PageDefinition.EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.RETRY_INTERVAL_SECS).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),
	EMAIL_MAX_SENDINGS_PER_CYCLE(new PreferenceDetailsBuilder(PageDefinition.EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.MAX_SENDINGS_PER_CYCLE).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.email.max.sendings.per.cycle.unlimited");
		}
	}, 0)).build()),
	EMAIL_MAX_QUEUE_SIZE(new PreferenceDetailsBuilder(PageDefinition.EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.MAX_QUEUE_SIZE).build(), new FieldEditorDetailsBuilder(ScaleIntegerFieldEditor.class).scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	EMAIL_SSL_CONNECT(new PreferenceDetailsBuilder(PageDefinition.EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.SSL_CONNECT).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	EMAIL_SSL_IDENTITY(new PreferenceDetailsBuilder(PageDefinition.EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.SSL_IDENTITY).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	EMAIL_STARTTLS_ENABLED(new PreferenceDetailsBuilder(PageDefinition.EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.STARTTLS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	EMAIL_STARTTLS_REQUIRED(new PreferenceDetailsBuilder(PageDefinition.EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.STARTTLS_REQUIRED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	EMAIL_CC_ADDRESSES(new PreferenceDetailsBuilder(PageDefinition.EMAIL_CC_BCC).build(), new FieldEditorDetailsBuilder(EmailAddressesListEditor.class).horizontalSpan(0).icons(Images.MAIN_ICONS).build()),
	EMAIL_BCC_ADDRESSES(new PreferenceDetailsBuilder(PageDefinition.EMAIL_CC_BCC).build(), new FieldEditorDetailsBuilder(EmailAddressesListEditor.class).horizontalSpan(0).icons(Images.MAIN_ICONS).build()),

	SERVER_ENABLED(new PreferenceDetailsBuilder(PageDefinition.SERVER).defaultValue(BaseHttpServer.Defaults.ENABLED).restartRequired().build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_PORT(new PreferenceDetailsBuilder(PageDefinition.SERVER).defaultValue(BaseHttpServer.Defaults.PORT).restartRequired().parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).integerValidRange(1, 65535).build()),
	SERVER_AUTHENTICATION(new PreferenceDetailsBuilder(PageDefinition.SERVER).parent(SERVER_ENABLED).defaultValue(BaseHttpServer.Defaults.AUTHENTICATION).restartRequired().build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_USERNAME(new PreferenceDetailsBuilder(PageDefinition.SERVER).parent(SERVER_AUTHENTICATION).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	SERVER_PASSWORD(new PreferenceDetailsBuilder(PageDefinition.SERVER).parent(SERVER_AUTHENTICATION).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	SERVER_COMPRESS_RESPONSE(new PreferenceDetailsBuilder(PageDefinition.SERVER).defaultValue(BaseHtmlHandler.Defaults.COMPRESS_RESPONSE).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_LOG_REQUEST(new PreferenceDetailsBuilder(PageDefinition.SERVER).defaultValue(BaseHtmlHandler.Defaults.LOG_REQUEST).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(ServerPreferencePage.getLogComboOptions()).build()),
	SERVER_THREADS(new PreferenceDetailsBuilder(PageDefinition.SERVER).defaultValue(BaseHttpServer.Defaults.THREADS).restartRequired().parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(ScaleIntegerFieldEditor.class).scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(010).build()),

	SERVER_HANDLER_ROOT_ENABLED(new PreferenceDetailsBuilder(PageDefinition.SERVER_HANDLER).defaultValue(RootHtmlHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_STATUS_ENABLED(new PreferenceDetailsBuilder(PageDefinition.SERVER_HANDLER).defaultValue(StatusHtmlHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_STATUS_REFRESH(new PreferenceDetailsBuilder(PageDefinition.SERVER_HANDLER).defaultValue(StatusHtmlHandler.Defaults.REFRESH).parent(SERVER_HANDLER_STATUS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_STATUS_REFRESH_SECS(new PreferenceDetailsBuilder(PageDefinition.SERVER_HANDLER).defaultValue(StatusHtmlHandler.Defaults.REFRESH_SECS).parent(SERVER_HANDLER_STATUS_REFRESH).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.server.handler.refresh.auto");
		}
	}, 0)).build()),
	SERVER_HANDLER_RESTART_ENABLED(new PreferenceDetailsBuilder(PageDefinition.SERVER_HANDLER).separate().defaultValue(RestartHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_CONNECT_ENABLED(new PreferenceDetailsBuilder(PageDefinition.SERVER_HANDLER).defaultValue(ConnectHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_DISCONNECT_ENABLED(new PreferenceDetailsBuilder(PageDefinition.SERVER_HANDLER).defaultValue(DisconnectHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_CLOSE_ENABLED(new PreferenceDetailsBuilder(PageDefinition.SERVER_HANDLER).defaultValue(CloseHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_JSON_ENABLED(new PreferenceDetailsBuilder(PageDefinition.SERVER_HANDLER).separate().defaultValue(BaseJsonHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_JSON_REFRESH(new PreferenceDetailsBuilder(PageDefinition.SERVER_HANDLER).defaultValue(BaseJsonHandler.Defaults.REFRESH).parent(SERVER_HANDLER_JSON_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_JSON_REFRESH_SECS(new PreferenceDetailsBuilder(PageDefinition.SERVER_HANDLER).defaultValue(BaseJsonHandler.Defaults.REFRESH_SECS).parent(SERVER_HANDLER_JSON_REFRESH).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.server.handler.refresh.auto");
		}
	}, 0)).build()),

	SERVER_SSL_ENABLED(new PreferenceDetailsBuilder(PageDefinition.SERVER_HTTPS).restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_SSL_KEYSTORE_TYPE(new PreferenceDetailsBuilder(PageDefinition.SERVER_HTTPS).restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_KEYSTORE_TYPE).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(ValidatedComboFieldEditor.class).labelsAndValues(ServerHttpsPreferencePage.getKeyStoreAlgorithmsComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_KEYSTORE_FILE(new PreferenceDetailsBuilder(PageDefinition.SERVER_HTTPS).restartRequired().parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultFileFieldEditor.class).fileExtensions(ServerHttpsPreferencePage.getKeyStoreFileExtensions()).build()),
	SERVER_SSL_STOREPASS(new PreferenceDetailsBuilder(PageDefinition.SERVER_HTTPS).restartRequired().parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	SERVER_SSL_KEYPASS(new PreferenceDetailsBuilder(PageDefinition.SERVER_HTTPS).restartRequired().parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	SERVER_SSL_PROTOCOL(new PreferenceDetailsBuilder(PageDefinition.SERVER_HTTPS).restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_PROTOCOL).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(ValidatedComboFieldEditor.class).labelsAndValues(ServerHttpsPreferencePage.getSslContextAlgorithmsComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_KMF_ALGORITHM(new PreferenceDetailsBuilder(PageDefinition.SERVER_HTTPS).restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_KMF_ALGORITHM).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(ValidatedComboFieldEditor.class).labelsAndValues(ServerHttpsPreferencePage.getKeyManagerFactoryComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_TMF_ALGORITHM(new PreferenceDetailsBuilder(PageDefinition.SERVER_HTTPS).restartRequired().defaultValue(BaseHttpServer.Defaults.SSL_TMF_ALGORITHM).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(ValidatedComboFieldEditor.class).labelsAndValues(ServerHttpsPreferencePage.getTrustManagerFactoryComboOptions()).emptyStringAllowed(false).build()),

	MQTT_ENABLED(new PreferenceDetailsBuilder(PageDefinition.MQTT).defaultValue(RouterLoggerMqttClient.Defaults.ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_SERVER_URI(new PreferenceDetailsBuilder(PageDefinition.MQTT).restartRequired().parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(UriListEditor.class).horizontalSpan(2).icons(Images.MAIN_ICONS).build()),
	MQTT_USERNAME(new PreferenceDetailsBuilder(PageDefinition.MQTT).restartRequired().parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).build()),
	MQTT_PASSWORD(new PreferenceDetailsBuilder(PageDefinition.MQTT).restartRequired().parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	MQTT_CLIENT_ID(new PreferenceDetailsBuilder(PageDefinition.MQTT).restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.CLIENT_ID).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),
	MQTT_CONNECT_RETRY(new PreferenceDetailsBuilder(PageDefinition.MQTT).separate().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.CONNECT_RETRY).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	MQTT_DATA_ENABLED(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).defaultValue(RouterLoggerMqttClient.Defaults.DATA_ENABLED).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_DATA_TOPIC(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).defaultValue(RouterLoggerMqttClient.Defaults.DATA_TOPIC).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),
	MQTT_DATA_QOS(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).defaultValue(RouterLoggerMqttClient.Defaults.DATA_QOS).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_DATA_THROTTLING_MS(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).defaultValue(RouterLoggerMqttClient.Defaults.DATA_THROTTLING_IN_MILLIS).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.mqtt.data.throttling.disabled");
		}
	}, 0)).build()),
	MQTT_DATA_RETAINED(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).defaultValue(RouterLoggerMqttClient.Defaults.DATA_RETAINED).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	MQTT_STATUS_ENABLED(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).separate().restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_ENABLED).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_STATUS_TOPIC(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_TOPIC).parent(MQTT_STATUS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),
	MQTT_STATUS_QOS(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_QOS).parent(MQTT_STATUS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_STATUS_RETAINED(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.STATUS_RETAINED).parent(MQTT_STATUS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	MQTT_THRESHOLDS_ENABLED(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).separate().defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_ENABLED).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_THRESHOLDS_TOPIC(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_TOPIC).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultStringFieldEditor.class).emptyStringAllowed(false).build()),
	MQTT_THRESHOLDS_QOS(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_QOS).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_THRESHOLDS_THROTTLING_MS(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_THROTTLING_IN_MILLIS).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Resources.get("lbl.preferences.mqtt.thresholds.throttling.disabled");
		}
	}, 0)).build()),
	MQTT_THRESHOLDS_RETAINED(new PreferenceDetailsBuilder(PageDefinition.MQTT_MESSAGES).defaultValue(RouterLoggerMqttClient.Defaults.THRESHOLDS_RETAINED).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	MQTT_CLEAN_SESSION(new PreferenceDetailsBuilder(PageDefinition.MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.CLEAN_SESSION).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_AUTOMATIC_RECONNECT(new PreferenceDetailsBuilder(PageDefinition.MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.AUTOMATIC_RECONNECT).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_CONNECTION_TIMEOUT(new PreferenceDetailsBuilder(PageDefinition.MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.CONNECTION_TIMEOUT).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),
	MQTT_KEEP_ALIVE_INTERVAL(new PreferenceDetailsBuilder(PageDefinition.MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.KEEP_ALIVE_INTERVAL).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),
	MQTT_MAX_INFLIGHT(new PreferenceDetailsBuilder(PageDefinition.MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.MAX_INFLIGHT).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultIntegerFieldEditor.class).build()),
	MQTT_VERSION(new PreferenceDetailsBuilder(PageDefinition.MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.MQTT_VERSION).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(AdvancedMqttPreferencePage.getMqttVersionComboOptions()).build()),
	MQTT_PERSISTENCE_FILE_ENABLED(new PreferenceDetailsBuilder(PageDefinition.MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.PERSISTENCE_FILE_ENABLED).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_PERSISTENCE_FILE_CUSTOM(new PreferenceDetailsBuilder(PageDefinition.MQTT_ADVANCED).restartRequired().defaultValue(RouterLoggerMqttClient.Defaults.PERSISTENCE_FILE_CUSTOM).parent(MQTT_PERSISTENCE_FILE_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_PERSISTENCE_FILE_PATH(new PreferenceDetailsBuilder(PageDefinition.MQTT_ADVANCED).restartRequired().defaultValue(System.getProperty("user.dir")).parent(MQTT_PERSISTENCE_FILE_CUSTOM).build(), new FieldEditorDetailsBuilder(DefaultDirectoryFieldEditor.class).emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Resources.get("msg.preferences.directory.dialog.message.mqtt");
		}
	}).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private static final FieldEditorFactory fieldEditorFactory = new RouterLoggerFieldEditorFactory();

	private final PreferenceDetails preferenceDetails;
	private final FieldEditorDetails fieldEditorDetails;

	RouterLoggerPreference(final PreferenceDetails preferenceDetails, final FieldEditorDetails fieldEditorDetails) {
		this.preferenceDetails = preferenceDetails;
		this.fieldEditorDetails = fieldEditorDetails;
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
		return preferenceDetails.getPageDefinition();
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
		return fieldEditorFactory.createFieldEditor(getName(), getLabel(), parent, fieldEditorDetails);
	}

	public static IPreference forName(final String name) {
		if (name != null) {
			for (final IPreference preference : RouterLoggerPreference.values()) {
				if (name.equals(preference.getName())) {
					return preference;
				}
			}
		}
		return null;
	}

}
