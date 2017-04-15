package it.albertus.router.gui.preference;

import static it.albertus.router.gui.preference.page.PageDefinition.APPEARANCE;
import static it.albertus.router.gui.preference.page.PageDefinition.APPEARANCE_TABLE;
import static it.albertus.router.gui.preference.page.PageDefinition.CONSOLE;
import static it.albertus.router.gui.preference.page.PageDefinition.CSV;
import static it.albertus.router.gui.preference.page.PageDefinition.DATABASE;
import static it.albertus.router.gui.preference.page.PageDefinition.EMAIL;
import static it.albertus.router.gui.preference.page.PageDefinition.EMAIL_ADVANCED;
import static it.albertus.router.gui.preference.page.PageDefinition.EMAIL_CC_BCC;
import static it.albertus.router.gui.preference.page.PageDefinition.GENERAL;
import static it.albertus.router.gui.preference.page.PageDefinition.LOGGING;
import static it.albertus.router.gui.preference.page.PageDefinition.MQTT;
import static it.albertus.router.gui.preference.page.PageDefinition.MQTT_ADVANCED;
import static it.albertus.router.gui.preference.page.PageDefinition.MQTT_MESSAGES;
import static it.albertus.router.gui.preference.page.PageDefinition.READER;
import static it.albertus.router.gui.preference.page.PageDefinition.READER_ASUS_N12E;
import static it.albertus.router.gui.preference.page.PageDefinition.READER_ASUS_N14U;
import static it.albertus.router.gui.preference.page.PageDefinition.READER_DLINK_2750;
import static it.albertus.router.gui.preference.page.PageDefinition.READER_TPLINK_8970;
import static it.albertus.router.gui.preference.page.PageDefinition.SERVER;
import static it.albertus.router.gui.preference.page.PageDefinition.SERVER_HANDLER;
import static it.albertus.router.gui.preference.page.PageDefinition.SERVER_HTTPS;
import static it.albertus.router.gui.preference.page.PageDefinition.THRESHOLDS;
import static it.albertus.router.gui.preference.page.PageDefinition.WRITER;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;

import it.albertus.jface.console.StyledTextConsole;
import it.albertus.jface.preference.FieldEditorDetails;
import it.albertus.jface.preference.FieldEditorDetails.FieldEditorDetailsBuilder;
import it.albertus.jface.preference.FieldEditorFactory;
import it.albertus.jface.preference.IPreference;
import it.albertus.jface.preference.LocalizedLabelsAndValues;
import it.albertus.jface.preference.PreferenceDetails;
import it.albertus.jface.preference.PreferenceDetails.PreferenceDetailsBuilder;
import it.albertus.jface.preference.field.DefaultBooleanFieldEditor;
import it.albertus.jface.preference.field.DefaultComboFieldEditor;
import it.albertus.jface.preference.field.DelimiterComboFieldEditor;
import it.albertus.jface.preference.field.EmailAddressesListEditor;
import it.albertus.jface.preference.field.EnhancedDirectoryFieldEditor;
import it.albertus.jface.preference.field.EnhancedFileFieldEditor;
import it.albertus.jface.preference.field.EnhancedIntegerFieldEditor;
import it.albertus.jface.preference.field.EnhancedStringFieldEditor;
import it.albertus.jface.preference.field.IntegerComboFieldEditor;
import it.albertus.jface.preference.field.PasswordFieldEditor;
import it.albertus.jface.preference.field.ScaleIntegerFieldEditor;
import it.albertus.jface.preference.field.ShortComboFieldEditor;
import it.albertus.jface.preference.field.ShortFieldEditor;
import it.albertus.jface.preference.field.UriListEditor;
import it.albertus.jface.preference.field.ValidatedComboFieldEditor;
import it.albertus.jface.preference.field.WrapStringFieldEditor;
import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.jface.preference.page.IPageDefinition;
import it.albertus.router.console.RouterLoggerConsole;
import it.albertus.router.email.EmailSender;
import it.albertus.router.email.ThresholdsEmailSender;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.gui.CloseDialog;
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
import it.albertus.router.gui.preference.page.ServerHttpsPreferencePage;
import it.albertus.router.gui.preference.page.WriterPreferencePage;
import it.albertus.router.http.HttpServerConfiguration;
import it.albertus.router.http.html.BaseHtmlHandler;
import it.albertus.router.http.html.CloseHandler;
import it.albertus.router.http.html.ConfigurationHandler;
import it.albertus.router.http.html.ConnectHandler;
import it.albertus.router.http.html.DisconnectHandler;
import it.albertus.router.http.html.LogsHandler;
import it.albertus.router.http.html.RestartHandler;
import it.albertus.router.http.html.RootHtmlHandler;
import it.albertus.router.http.html.StatusHtmlHandler;
import it.albertus.router.http.json.BaseJsonHandler;
import it.albertus.router.mqtt.MqttClient;
import it.albertus.router.reader.AsusDslN12EReader;
import it.albertus.router.reader.AsusDslN14UReader;
import it.albertus.router.reader.DLinkDsl2750Reader;
import it.albertus.router.reader.Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.resources.Messages;
import it.albertus.router.util.logging.EmailHandler;
import it.albertus.router.writer.CsvWriter;
import it.albertus.router.writer.DatabaseWriter;
import it.albertus.util.Configuration;
import it.albertus.util.Localized;

public enum Preference implements IPreference {

	LANGUAGE(new PreferenceDetailsBuilder(GENERAL).defaultValue(Messages.Defaults.LANGUAGE).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(GeneralPreferencePage.getLanguageComboOptions()).build()),
	LOGGER_ITERATIONS(new PreferenceDetailsBuilder(GENERAL).separate().defaultValue(RouterLoggerEngine.Defaults.ITERATIONS).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Messages.get("lbl.preferences.iterations.infinite");
		}
	}, 0)).build()),
	LOGGER_CLOSE_WHEN_FINISHED(new PreferenceDetailsBuilder(GENERAL).defaultValue(RouterLoggerEngine.Defaults.CLOSE_WHEN_FINISHED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	LOGGER_INTERVAL_NORMAL_MS(new PreferenceDetailsBuilder(GENERAL).defaultValue(RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	LOGGER_INTERVAL_FAST_MS(new PreferenceDetailsBuilder(GENERAL).defaultValue(RouterLoggerEngine.Defaults.INTERVAL_FAST_IN_MILLIS).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	LOGGER_HYSTERESIS_MS(new PreferenceDetailsBuilder(GENERAL).defaultValue(RouterLoggerEngine.Defaults.HYSTERESIS_IN_MILLIS).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	LOGGER_RETRY_COUNT(new PreferenceDetailsBuilder(GENERAL).defaultValue(RouterLoggerEngine.Defaults.RETRIES).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Messages.get("lbl.preferences.logger.retry.count.infinite");
		}
	}, 0)).build()),
	LOGGER_RETRY_INTERVAL_MS(new PreferenceDetailsBuilder(GENERAL).defaultValue(RouterLoggerEngine.Defaults.RETRY_INTERVAL_IN_MILLIS).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),

	READER_CLASS_NAME(new PreferenceDetailsBuilder(READER).build(), new FieldEditorDetailsBuilder(ReaderComboFieldEditor.class).labelsAndValues(ReaderPreferencePage.getReaderComboOptions()).build()),
	ROUTER_USERNAME(new PreferenceDetailsBuilder(READER).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	ROUTER_PASSWORD(new PreferenceDetailsBuilder(READER).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	ROUTER_ADDRESS(new PreferenceDetailsBuilder(READER).defaultValue(Reader.Defaults.ROUTER_ADDRESS).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	ROUTER_PORT(new PreferenceDetailsBuilder(READER).defaultValue(Reader.Defaults.ROUTER_PORT).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).numberValidRange(1, 65535).build()),
	SOCKET_TIMEOUT_MS(new PreferenceDetailsBuilder(READER).defaultValue(Reader.Defaults.SOCKET_TIMEOUT_IN_MILLIS).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	CONNECTION_TIMEOUT_MS(new PreferenceDetailsBuilder(READER).defaultValue(Reader.Defaults.CONNECTION_TIMEOUT_IN_MILLIS).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	TELNET_NEWLINE_CHARACTERS(new PreferenceDetailsBuilder(READER).defaultValue(Reader.Defaults.TELNET_NEWLINE_CHARACTERS).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(BasePreferencePage.getNewLineComboOptions()).build()),
	READER_LOG_CONNECTED(new PreferenceDetailsBuilder(READER).defaultValue(RouterLoggerEngine.Defaults.LOG_CONNECTED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	READER_WAIT_DISCONNECTED(new PreferenceDetailsBuilder(READER).defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD(new PreferenceDetailsBuilder(READER).defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD).parent(READER_WAIT_DISCONNECTED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD_MS(new PreferenceDetailsBuilder(READER).defaultValue(RouterLoggerEngine.Defaults.WAIT_DISCONNECTED_INTERVAL_THRESHOLD_IN_MILLIS).parent(READER_WAIT_DISCONNECTED_INTERVAL_THRESHOLD).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),

	TPLINK_8970_COMMAND_INFO_ADSL(new PreferenceDetailsBuilder(READER_TPLINK_8970).defaultValue(TpLink8970Reader.Defaults.COMMAND_INFO_ADSL).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	TPLINK_8970_COMMAND_INFO_WAN(new PreferenceDetailsBuilder(READER_TPLINK_8970).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	ASUS_DSLN12E_COMMAND_INFO_ADSL(new PreferenceDetailsBuilder(READER_ASUS_N12E).defaultValue(AsusDslN12EReader.Defaults.COMMAND_INFO_ADSL).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	ASUS_DSLN12E_COMMAND_INFO_WAN(new PreferenceDetailsBuilder(READER_ASUS_N12E).defaultValue(AsusDslN12EReader.Defaults.COMMAND_INFO_WAN).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	ASUS_DSLN14U_COMMAND_INFO_ADSL(new PreferenceDetailsBuilder(READER_ASUS_N14U).defaultValue(AsusDslN14UReader.Defaults.COMMAND_INFO_ADSL).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	ASUS_DSLN14U_COMMAND_INFO_WAN(new PreferenceDetailsBuilder(READER_ASUS_N14U).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	DLINK_2750_COMMAND_INFO_ADSL_STATUS(new PreferenceDetailsBuilder(READER_DLINK_2750).defaultValue(DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_STATUS).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	DLINK_2750_COMMAND_INFO_ADSL_SNR(new PreferenceDetailsBuilder(READER_DLINK_2750).defaultValue(DLinkDsl2750Reader.Defaults.COMMAND_INFO_ADSL_SNR).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),

	GUI_CONSOLE_FONT(new PreferenceDetailsBuilder(APPEARANCE).defaultValue(JFaceResources.getTextFont().getFontData()).build(), new FieldEditorDetailsBuilder(FontFieldEditor.class).build()),
	GUI_CONSOLE_MAX_CHARS(new PreferenceDetailsBuilder(APPEARANCE).defaultValue(StyledTextConsole.DEFAULT_LIMIT).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).textLimit(6).build()),
	GUI_CLIPBOARD_MAX_CHARS(new PreferenceDetailsBuilder(APPEARANCE).defaultValue(RouterLoggerGui.Defaults.GUI_CLIPBOARD_MAX_CHARS).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).numberValidRange(0, 128 * 1024).build()),
	GUI_MINIMIZE_TRAY(new PreferenceDetailsBuilder(APPEARANCE).separate().defaultValue(TrayIcon.Defaults.GUI_MINIMIZE_TRAY).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	GUI_TRAY_TOOLTIP(new PreferenceDetailsBuilder(APPEARANCE).defaultValue(TrayIcon.Defaults.GUI_TRAY_TOOLTIP).parent(GUI_MINIMIZE_TRAY).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	GUI_START_MINIMIZED(new PreferenceDetailsBuilder(APPEARANCE).defaultValue(RouterLoggerGui.Defaults.GUI_START_MINIMIZED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	GUI_CONFIRM_CLOSE(new PreferenceDetailsBuilder(APPEARANCE).defaultValue(CloseDialog.Defaults.CONFIRM_CLOSE).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	GUI_TABLE_ITEMS_MAX(new PreferenceDetailsBuilder(APPEARANCE_TABLE).defaultValue(DataTable.Defaults.MAX_ITEMS).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).textLimit(4).build()),
	GUI_IMPORTANT_KEYS(new PreferenceDetailsBuilder(APPEARANCE_TABLE).build(), new FieldEditorDetailsBuilder(WrapStringFieldEditor.class).height(4).build()),
	GUI_IMPORTANT_KEYS_SEPARATOR(new PreferenceDetailsBuilder(APPEARANCE_TABLE).defaultValue(RouterLoggerConfiguration.Defaults.GUI_IMPORTANT_KEYS_SEPARATOR).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	GUI_IMPORTANT_KEYS_COLOR_BACKGROUND(new PreferenceDetailsBuilder(APPEARANCE_TABLE).defaultValue(DataTable.Defaults.IMPORTANT_KEYS_COLOR_BACKGROUND).build(), new FieldEditorDetailsBuilder(ColorFieldEditor.class).build()),
	GUI_TABLE_COLUMNS_PACK(new PreferenceDetailsBuilder(APPEARANCE_TABLE).separate().restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PACK).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	GUI_TABLE_COLUMNS_PADDING_RIGHT(new PreferenceDetailsBuilder(APPEARANCE_TABLE).restartRequired().defaultValue(DataTable.Defaults.COLUMNS_PADDING_RIGHT).build(), new FieldEditorDetailsBuilder(ScaleIntegerFieldEditor.class).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	GUI_THRESHOLDS_REACHED_COLOR_FOREGROUND(new PreferenceDetailsBuilder(APPEARANCE_TABLE).separate().defaultValue(DataTable.Defaults.THRESHOLDS_REACHED_COLOR_FOREGROUND).build(), new FieldEditorDetailsBuilder(ColorFieldEditor.class).build()),

	CONSOLE_ANIMATION(new PreferenceDetailsBuilder(CONSOLE).defaultValue(RouterLoggerConsole.Defaults.CONSOLE_ANIMATION).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	CONSOLE_SHOW_KEYS(new PreferenceDetailsBuilder(CONSOLE).build(), new FieldEditorDetailsBuilder(WrapStringFieldEditor.class).build()),
	CONSOLE_SHOW_KEYS_SEPARATOR(new PreferenceDetailsBuilder(CONSOLE).defaultValue(RouterLoggerConfiguration.Defaults.CONSOLE_SHOW_KEYS_SEPARATOR).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),

	WRITER_CLASS_NAME(new PreferenceDetailsBuilder(WRITER).defaultValue(RouterLoggerEngine.Defaults.WRITER_CLASS.getSimpleName()).build(), new FieldEditorDetailsBuilder(WriterComboFieldEditor.class).labelsAndValues(WriterPreferencePage.getWriterComboOptions()).build()),

	CSV_DESTINATION_PATH(new PreferenceDetailsBuilder(CSV).defaultValue(CsvWriter.Defaults.DIRECTORY).build(), new FieldEditorDetailsBuilder(EnhancedDirectoryFieldEditor.class).emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Messages.get("msg.preferences.directory.dialog.message.csv");
		}
	}).build()),
	CSV_NEWLINE_CHARACTERS(new PreferenceDetailsBuilder(CSV).defaultValue(CsvWriter.Defaults.NEWLINE.name()).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(BasePreferencePage.getNewLineComboOptions()).build()),
	CSV_FIELD_SEPARATOR(new PreferenceDetailsBuilder(CSV).defaultValue(CsvWriter.Defaults.FIELD_SEPARATOR).build(), new FieldEditorDetailsBuilder(DelimiterComboFieldEditor.class).emptyStringAllowed(false).labelsAndValues(CsvPreferencePage.getSeparatorComboOptions()).build()),
	CSV_FIELD_SEPARATOR_REPLACEMENT(new PreferenceDetailsBuilder(CSV).defaultValue(CsvWriter.Defaults.FIELD_SEPARATOR_REPLACEMENT).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	CSV_EMAIL(new PreferenceDetailsBuilder(CSV).defaultValue(CsvWriter.Defaults.EMAIL).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	DATABASE_DRIVER_CLASS_NAME(new PreferenceDetailsBuilder(DATABASE).build(), new FieldEditorDetailsBuilder(DatabaseComboFieldEditor.class).labelsAndValues(DatabasePreferencePage.getDatabaseComboOptions()).build()),
	DATABASE_URL(new PreferenceDetailsBuilder(DATABASE).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	DATABASE_USERNAME(new PreferenceDetailsBuilder(DATABASE).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	DATABASE_PASSWORD(new PreferenceDetailsBuilder(DATABASE).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	DATABASE_TABLE_NAME(new PreferenceDetailsBuilder(DATABASE).defaultValue(DatabaseWriter.Defaults.TABLE_NAME).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	DATABASE_CONNECTION_VALIDATION_TIMEOUT_MS(new PreferenceDetailsBuilder(DATABASE).defaultValue(DatabaseWriter.Defaults.CONNECTION_VALIDATION_TIMEOUT_IN_MILLIS).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).textLimit(5).build()),
	DATABASE_TIMESTAMP_COLUMN_TYPE(new PreferenceDetailsBuilder(DATABASE).defaultValue(DatabaseWriter.Defaults.TIMESTAMP_COLUMN_TYPE).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	DATABASE_RESPONSE_COLUMN_TYPE(new PreferenceDetailsBuilder(DATABASE).defaultValue(DatabaseWriter.Defaults.RESPONSE_TIME_COLUMN_TYPE).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	DATABASE_INFO_COLUMN_TYPE(new PreferenceDetailsBuilder(DATABASE).defaultValue(DatabaseWriter.Defaults.INFO_COLUMN_TYPE).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_PREFIX(new PreferenceDetailsBuilder(DATABASE).defaultValue(DatabaseWriter.Defaults.COLUMN_NAME_PREFIX).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	DATABASE_COLUMN_NAME_MAX_LENGTH(new PreferenceDetailsBuilder(DATABASE).defaultValue(DatabaseWriter.Defaults.COLUMN_NAME_MAX_LENGTH).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).textLimit(2).build()),

	THRESHOLDS_EXPRESSIONS(new PreferenceDetailsBuilder(THRESHOLDS).build(), new FieldEditorDetailsBuilder(ThresholdsListEditor.class).build()),
	THRESHOLDS_SPLIT(new PreferenceDetailsBuilder(THRESHOLDS).defaultValue(RouterLoggerConfiguration.Defaults.THRESHOLDS_SPLIT).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	THRESHOLDS_EMAIL(new PreferenceDetailsBuilder(THRESHOLDS).defaultValue(RouterLoggerEngine.Defaults.THRESHOLDS_EMAIL).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	THRESHOLDS_EMAIL_SEND_INTERVAL_SECS(new PreferenceDetailsBuilder(THRESHOLDS).defaultValue(ThresholdsEmailSender.Defaults.THRESHOLDS_EMAIL_SEND_INTERVAL_SECS).parent(THRESHOLDS_EMAIL).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	THRESHOLDS_EMAIL_MAX_ITEMS(new PreferenceDetailsBuilder(THRESHOLDS).defaultValue(ThresholdsEmailSender.Defaults.MAX_ITEMS).parent(THRESHOLDS_EMAIL).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).numberValidRange(1, 1000).build()),
	THRESHOLDS_EXCLUDED(new PreferenceDetailsBuilder(THRESHOLDS).build(), new FieldEditorDetailsBuilder(WrapStringFieldEditor.class).build()),
	THRESHOLDS_EXCLUDED_SEPARATOR(new PreferenceDetailsBuilder(THRESHOLDS).defaultValue(RouterLoggerConfiguration.Defaults.THRESHOLDS_EXCLUDED_SEPARATOR).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),

	EMAIL_HOST(new PreferenceDetailsBuilder(EMAIL).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	EMAIL_USERNAME(new PreferenceDetailsBuilder(EMAIL).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	EMAIL_PASSWORD(new PreferenceDetailsBuilder(EMAIL).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	EMAIL_FROM_NAME(new PreferenceDetailsBuilder(EMAIL).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	EMAIL_FROM_ADDRESS(new PreferenceDetailsBuilder(EMAIL).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	EMAIL_TO_ADDRESSES(new PreferenceDetailsBuilder(EMAIL).build(), new FieldEditorDetailsBuilder(EmailAddressesListEditor.class).horizontalSpan(Short.MAX_VALUE).icons(Images.getMainIcons()).build()),

	EMAIL_PORT(new PreferenceDetailsBuilder(EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.PORT).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).numberValidRange(1, 65535).build()),
	EMAIL_SSL_PORT(new PreferenceDetailsBuilder(EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.SSL_PORT).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).numberValidRange(1, 65535).build()),
	EMAIL_CONNECTION_TIMEOUT(new PreferenceDetailsBuilder(EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.SOCKET_CONNECTION_TIMEOUT).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	EMAIL_SOCKET_TIMEOUT(new PreferenceDetailsBuilder(EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.SOCKET_TIMEOUT).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	EMAIL_RETRY_INTERVAL_SECS(new PreferenceDetailsBuilder(EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.RETRY_INTERVAL_SECS).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	EMAIL_MAX_SENDINGS_PER_CYCLE(new PreferenceDetailsBuilder(EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.MAX_SENDINGS_PER_CYCLE).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Messages.get("lbl.preferences.email.max.sendings.per.cycle.unlimited");
		}
	}, 0)).build()),
	EMAIL_MAX_QUEUE_SIZE(new PreferenceDetailsBuilder(EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.MAX_QUEUE_SIZE).build(), new FieldEditorDetailsBuilder(ScaleIntegerFieldEditor.class).scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(10).build()),
	EMAIL_SSL_CONNECT(new PreferenceDetailsBuilder(EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.SSL_CONNECT).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	EMAIL_SSL_IDENTITY(new PreferenceDetailsBuilder(EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.SSL_IDENTITY).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	EMAIL_STARTTLS_ENABLED(new PreferenceDetailsBuilder(EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.STARTTLS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	EMAIL_STARTTLS_REQUIRED(new PreferenceDetailsBuilder(EMAIL_ADVANCED).defaultValue(EmailSender.Defaults.STARTTLS_REQUIRED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	EMAIL_CC_ADDRESSES(new PreferenceDetailsBuilder(EMAIL_CC_BCC).build(), new FieldEditorDetailsBuilder(EmailAddressesListEditor.class).horizontalSpan(0).icons(Images.getMainIcons()).build()),
	EMAIL_BCC_ADDRESSES(new PreferenceDetailsBuilder(EMAIL_CC_BCC).build(), new FieldEditorDetailsBuilder(EmailAddressesListEditor.class).horizontalSpan(0).icons(Images.getMainIcons()).build()),

	SERVER_ENABLED(new PreferenceDetailsBuilder(SERVER).defaultValue(HttpServerConfiguration.Defaults.ENABLED).restartRequired().build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_PORT(new PreferenceDetailsBuilder(SERVER).defaultValue(HttpServerConfiguration.Defaults.PORT).restartRequired().parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).numberValidRange(1, 65535).build()),
	SERVER_AUTHENTICATION(new PreferenceDetailsBuilder(SERVER).parent(SERVER_ENABLED).defaultValue(HttpServerConfiguration.Defaults.AUTHENTICATION).restartRequired().build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_USERNAME(new PreferenceDetailsBuilder(SERVER).parent(SERVER_AUTHENTICATION).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	SERVER_PASSWORD(new PreferenceDetailsBuilder(SERVER).parent(SERVER_AUTHENTICATION).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).hashAlgorithm(HttpServerConfiguration.Defaults.PASSWORD_HASH_ALGORITHM).build()),
	SERVER_LOG_REQUEST(new PreferenceDetailsBuilder(SERVER).defaultValue(HttpServerConfiguration.Defaults.REQUEST_LOGGING_LEVEL.getName()).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(GeneralPreferencePage.getLoggingLevelComboOptions(new Level[] { Level.OFF, Level.FINEST, Level.FINER, Level.FINE, Level.CONFIG, Level.INFO, Level.WARNING })).build()),
	SERVER_THREADS(new PreferenceDetailsBuilder(SERVER).defaultValue(HttpServerConfiguration.Defaults.THREADS).restartRequired().parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(ScaleIntegerFieldEditor.class).scaleMinimum(1).scaleMaximum(Byte.MAX_VALUE).scalePageIncrement(010).build()),
	SERVER_MAXREQTIME(new PreferenceDetailsBuilder(SERVER).defaultValue(HttpServerConfiguration.Defaults.MAX_REQ_TIME).restartRequired().parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(ShortComboFieldEditor.class).numberValidRange(-1, Short.MAX_VALUE).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Messages.get("lbl.preferences.server.maxreqtime.infinite");
		}
	}, -1)).build()),
	SERVER_MAXRSPTIME(new PreferenceDetailsBuilder(SERVER).defaultValue(HttpServerConfiguration.Defaults.MAX_RSP_TIME).restartRequired().parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(ShortComboFieldEditor.class).numberValidRange(-1, Short.MAX_VALUE).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Messages.get("lbl.preferences.server.maxrsptime.infinite");
		}
	}, -1)).build()),

	SERVER_HANDLER_ROOT_ENABLED(new PreferenceDetailsBuilder(SERVER_HANDLER).defaultValue(RootHtmlHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_LOGS_ENABLED(new PreferenceDetailsBuilder(SERVER_HANDLER).defaultValue(LogsHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_CONFIGURATION_ENABLED(new PreferenceDetailsBuilder(SERVER_HANDLER).defaultValue(ConfigurationHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_STATUS_ENABLED(new PreferenceDetailsBuilder(SERVER_HANDLER).defaultValue(StatusHtmlHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_STATUS_REFRESH(new PreferenceDetailsBuilder(SERVER_HANDLER).defaultValue(StatusHtmlHandler.Defaults.REFRESH).parent(SERVER_HANDLER_STATUS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_STATUS_REFRESH_SECS(new PreferenceDetailsBuilder(SERVER_HANDLER).defaultValue(StatusHtmlHandler.Defaults.REFRESH_SECS).parent(SERVER_HANDLER_STATUS_REFRESH).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Messages.get("lbl.preferences.server.handler.refresh.auto");
		}
	}, 0)).build()),
	SERVER_HANDLER_RESTART_ENABLED(new PreferenceDetailsBuilder(SERVER_HANDLER).separate().defaultValue(RestartHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_CONNECT_ENABLED(new PreferenceDetailsBuilder(SERVER_HANDLER).defaultValue(ConnectHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_DISCONNECT_ENABLED(new PreferenceDetailsBuilder(SERVER_HANDLER).defaultValue(DisconnectHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_CLOSE_ENABLED(new PreferenceDetailsBuilder(SERVER_HANDLER).defaultValue(CloseHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_JSON_ENABLED(new PreferenceDetailsBuilder(SERVER_HANDLER).separate().defaultValue(BaseJsonHandler.Defaults.ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_JSON_REFRESH(new PreferenceDetailsBuilder(SERVER_HANDLER).defaultValue(BaseJsonHandler.Defaults.REFRESH).parent(SERVER_HANDLER_JSON_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_HANDLER_JSON_REFRESH_SECS(new PreferenceDetailsBuilder(SERVER_HANDLER).defaultValue(BaseJsonHandler.Defaults.REFRESH_SECS).parent(SERVER_HANDLER_JSON_REFRESH).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Messages.get("lbl.preferences.server.handler.refresh.auto");
		}
	}, 0)).build()),
	SERVER_COMPRESS_RESPONSE(new PreferenceDetailsBuilder(SERVER_HANDLER).defaultValue(BaseHtmlHandler.Defaults.COMPRESS_RESPONSE).parent(SERVER_ENABLED).separate().build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_COMPRESS_RESPONSE_JSON(new PreferenceDetailsBuilder(SERVER_HANDLER).defaultValue(BaseJsonHandler.Defaults.COMPRESS_RESPONSE).parent(SERVER_HANDLER_JSON_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	SERVER_SSL_ENABLED(new PreferenceDetailsBuilder(SERVER_HTTPS).restartRequired().defaultValue(HttpServerConfiguration.Defaults.SSL_ENABLED).parent(SERVER_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	SERVER_SSL_KEYSTORE_TYPE(new PreferenceDetailsBuilder(SERVER_HTTPS).restartRequired().defaultValue(HttpServerConfiguration.Defaults.SSL_KEYSTORE_TYPE).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(ValidatedComboFieldEditor.class).labelsAndValues(ServerHttpsPreferencePage.getKeyStoreAlgorithmsComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_KEYSTORE_FILE(new PreferenceDetailsBuilder(SERVER_HTTPS).restartRequired().parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(EnhancedFileFieldEditor.class).fileExtensions(ServerHttpsPreferencePage.getKeyStoreFileExtensions()).build()),
	SERVER_SSL_STOREPASS(new PreferenceDetailsBuilder(SERVER_HTTPS).restartRequired().parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	SERVER_SSL_KEYPASS(new PreferenceDetailsBuilder(SERVER_HTTPS).restartRequired().parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	SERVER_SSL_PROTOCOL(new PreferenceDetailsBuilder(SERVER_HTTPS).restartRequired().defaultValue(HttpServerConfiguration.Defaults.SSL_PROTOCOL).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(ValidatedComboFieldEditor.class).labelsAndValues(ServerHttpsPreferencePage.getSslContextAlgorithmsComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_KMF_ALGORITHM(new PreferenceDetailsBuilder(SERVER_HTTPS).restartRequired().defaultValue(HttpServerConfiguration.Defaults.SSL_KMF_ALGORITHM).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(ValidatedComboFieldEditor.class).labelsAndValues(ServerHttpsPreferencePage.getKeyManagerFactoryComboOptions()).emptyStringAllowed(false).build()),
	SERVER_SSL_TMF_ALGORITHM(new PreferenceDetailsBuilder(SERVER_HTTPS).restartRequired().defaultValue(HttpServerConfiguration.Defaults.SSL_TMF_ALGORITHM).parent(SERVER_SSL_ENABLED).build(), new FieldEditorDetailsBuilder(ValidatedComboFieldEditor.class).labelsAndValues(ServerHttpsPreferencePage.getTrustManagerFactoryComboOptions()).emptyStringAllowed(false).build()),

	MQTT_ENABLED(new PreferenceDetailsBuilder(MQTT).defaultValue(MqttClient.Defaults.ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_SERVER_URI(new PreferenceDetailsBuilder(MQTT).restartRequired().parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(UriListEditor.class).horizontalSpan(2).icons(Images.getMainIcons()).build()),
	MQTT_USERNAME(new PreferenceDetailsBuilder(MQTT).restartRequired().parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).build()),
	MQTT_PASSWORD(new PreferenceDetailsBuilder(MQTT).restartRequired().parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(PasswordFieldEditor.class).build()),
	MQTT_CLIENT_ID(new PreferenceDetailsBuilder(MQTT).restartRequired().defaultValue(MqttClient.Defaults.CLIENT_ID).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	MQTT_CONNECT_RETRY(new PreferenceDetailsBuilder(MQTT).separate().restartRequired().defaultValue(MqttClient.Defaults.CONNECT_RETRY).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	MQTT_DATA_ENABLED(new PreferenceDetailsBuilder(MQTT_MESSAGES).defaultValue(MqttClient.Defaults.DATA_ENABLED).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_DATA_TOPIC(new PreferenceDetailsBuilder(MQTT_MESSAGES).defaultValue(MqttClient.Defaults.DATA_TOPIC).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	MQTT_DATA_QOS(new PreferenceDetailsBuilder(MQTT_MESSAGES).defaultValue(MqttClient.Defaults.DATA_QOS).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_DATA_THROTTLING_MS(new PreferenceDetailsBuilder(MQTT_MESSAGES).defaultValue(MqttClient.Defaults.DATA_THROTTLING_IN_MILLIS).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Messages.get("lbl.preferences.mqtt.data.throttling.disabled");
		}
	}, 0)).build()),
	MQTT_DATA_RETAINED(new PreferenceDetailsBuilder(MQTT_MESSAGES).defaultValue(MqttClient.Defaults.DATA_RETAINED).parent(MQTT_DATA_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	MQTT_STATUS_ENABLED(new PreferenceDetailsBuilder(MQTT_MESSAGES).separate().restartRequired().defaultValue(MqttClient.Defaults.STATUS_ENABLED).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_STATUS_TOPIC(new PreferenceDetailsBuilder(MQTT_MESSAGES).restartRequired().defaultValue(MqttClient.Defaults.STATUS_TOPIC).parent(MQTT_STATUS_ENABLED).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	MQTT_STATUS_QOS(new PreferenceDetailsBuilder(MQTT_MESSAGES).restartRequired().defaultValue(MqttClient.Defaults.STATUS_QOS).parent(MQTT_STATUS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_STATUS_RETAINED(new PreferenceDetailsBuilder(MQTT_MESSAGES).restartRequired().defaultValue(MqttClient.Defaults.STATUS_RETAINED).parent(MQTT_STATUS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	MQTT_THRESHOLDS_ENABLED(new PreferenceDetailsBuilder(MQTT_MESSAGES).separate().defaultValue(MqttClient.Defaults.THRESHOLDS_ENABLED).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_THRESHOLDS_TOPIC(new PreferenceDetailsBuilder(MQTT_MESSAGES).defaultValue(MqttClient.Defaults.THRESHOLDS_TOPIC).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDetailsBuilder(EnhancedStringFieldEditor.class).emptyStringAllowed(false).build()),
	MQTT_THRESHOLDS_QOS(new PreferenceDetailsBuilder(MQTT_MESSAGES).defaultValue(MqttClient.Defaults.THRESHOLDS_QOS).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(MqttPreferencePage.getMqttQosComboOptions()).build()),
	MQTT_THRESHOLDS_THROTTLING_MS(new PreferenceDetailsBuilder(MQTT_MESSAGES).defaultValue(MqttClient.Defaults.THRESHOLDS_THROTTLING_IN_MILLIS).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDetailsBuilder(IntegerComboFieldEditor.class).labelsAndValues(new LocalizedLabelsAndValues(new Localized() {
		@Override
		public String getString() {
			return Messages.get("lbl.preferences.mqtt.thresholds.throttling.disabled");
		}
	}, 0)).build()),
	MQTT_THRESHOLDS_RETAINED(new PreferenceDetailsBuilder(MQTT_MESSAGES).defaultValue(MqttClient.Defaults.THRESHOLDS_RETAINED).parent(MQTT_THRESHOLDS_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),

	MQTT_CLEAN_SESSION(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(MqttClient.Defaults.CLEAN_SESSION).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_AUTOMATIC_RECONNECT(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(MqttClient.Defaults.AUTOMATIC_RECONNECT).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_CONNECTION_TIMEOUT(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(MqttClient.Defaults.CONNECTION_TIMEOUT).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	MQTT_KEEP_ALIVE_INTERVAL(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(MqttClient.Defaults.KEEP_ALIVE_INTERVAL).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	MQTT_MAX_INFLIGHT(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(MqttClient.Defaults.MAX_INFLIGHT).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(EnhancedIntegerFieldEditor.class).build()),
	MQTT_VERSION(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(MqttClient.Defaults.MQTT_VERSION).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(AdvancedMqttPreferencePage.getMqttVersionComboOptions()).build()),
	MQTT_PERSISTENCE_FILE_ENABLED(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(MqttClient.Defaults.PERSISTENCE_FILE_ENABLED).parent(MQTT_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_PERSISTENCE_FILE_CUSTOM(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(MqttClient.Defaults.PERSISTENCE_FILE_CUSTOM).parent(MQTT_PERSISTENCE_FILE_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	MQTT_PERSISTENCE_FILE_PATH(new PreferenceDetailsBuilder(MQTT_ADVANCED).restartRequired().defaultValue(Configuration.getOsSpecificConfigurationDir() + File.separator + Messages.get("msg.application.name")).parent(MQTT_PERSISTENCE_FILE_CUSTOM).build(), new FieldEditorDetailsBuilder(EnhancedDirectoryFieldEditor.class).emptyStringAllowed(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Messages.get("msg.preferences.directory.dialog.message.mqtt");
		}
	}).build()),

	LOGGING_LEVEL(new PreferenceDetailsBuilder(LOGGING).defaultValue(RouterLoggerConfiguration.Defaults.LOGGING_LEVEL.getName()).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(GeneralPreferencePage.getLoggingLevelComboOptions()).build()),
	LOGGING_FILES_ENABLED(new PreferenceDetailsBuilder(LOGGING).separate().defaultValue(RouterLoggerConfiguration.Defaults.LOGGING_FILES_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	LOGGING_FILES_PATH(new PreferenceDetailsBuilder(LOGGING).parent(LOGGING_FILES_ENABLED).defaultValue(RouterLoggerConfiguration.Defaults.LOGGING_FILES_PATH).build(), new FieldEditorDetailsBuilder(EnhancedDirectoryFieldEditor.class).emptyStringAllowed(false).directoryMustExist(false).directoryDialogMessage(new Localized() {
		@Override
		public String getString() {
			return Messages.get("msg.preferences.directory.dialog.message.log");
		}
	}).build()),
	LOGGING_FILES_AUTOCLEAN_ENABLED(new PreferenceDetailsBuilder(LOGGING).parent(LOGGING_FILES_ENABLED).defaultValue(RouterLoggerConfiguration.Defaults.LOGGING_FILES_AUTOCLEAN_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	LOGGING_FILES_AUTOCLEAN_KEEP(new PreferenceDetailsBuilder(LOGGING).parent(LOGGING_FILES_AUTOCLEAN_ENABLED).defaultValue(RouterLoggerConfiguration.Defaults.LOGGING_FILES_AUTOCLEAN_KEEP).build(), new FieldEditorDetailsBuilder(ShortFieldEditor.class).numberMinimum(1).build()),
	LOGGING_EMAIL_ENABLED(new PreferenceDetailsBuilder(LOGGING).separate().defaultValue(EmailHandler.Defaults.ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build()),
	LOGGING_EMAIL_LEVEL(new PreferenceDetailsBuilder(LOGGING).defaultValue(EmailHandler.Defaults.LEVEL.getName()).parent(LOGGING_EMAIL_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultComboFieldEditor.class).labelsAndValues(GeneralPreferencePage.getLoggingLevelComboOptions(EmailHandler.MIN_LEVEL, EmailHandler.MAX_LEVEL)).build()),
	LOGGING_EMAIL_IGNORE_DUPLICATES(new PreferenceDetailsBuilder(LOGGING).defaultValue(EmailHandler.Defaults.IGNORE_DUPLICATES).parent(LOGGING_EMAIL_ENABLED).build(), new FieldEditorDetailsBuilder(DefaultBooleanFieldEditor.class).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private static final FieldEditorFactory fieldEditorFactory = new RouterLoggerFieldEditorFactory();

	private final PreferenceDetails preferenceDetails;
	private final FieldEditorDetails fieldEditorDetails;

	Preference(final PreferenceDetails preferenceDetails, final FieldEditorDetails fieldEditorDetails) {
		this.preferenceDetails = preferenceDetails;
		this.fieldEditorDetails = fieldEditorDetails;
		if (preferenceDetails.getName() == null) {
			preferenceDetails.setName(name().toLowerCase().replace('_', '.'));
		}
		if (preferenceDetails.getLabel() == null) {
			preferenceDetails.setLabel(new Localized() {
				@Override
				public String getString() {
					return Messages.get(LABEL_KEY_PREFIX + preferenceDetails.getName());
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
	public IPageDefinition getPageDefinition() {
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
		final Set<Preference> preferences = EnumSet.noneOf(Preference.class);
		for (final Preference item : Preference.values()) {
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
			for (final IPreference preference : Preference.values()) {
				if (name.equals(preference.getName())) {
					return preference;
				}
			}
		}
		return null;
	}

}
