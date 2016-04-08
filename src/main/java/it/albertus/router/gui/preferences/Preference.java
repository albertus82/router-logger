package it.albertus.router.gui.preferences;

import it.albertus.router.console.RouterLoggerConsole;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.gui.CloseMessageBox;
import it.albertus.router.gui.DataTable;
import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.gui.TextConsole;
import it.albertus.router.gui.TrayIcon;
import it.albertus.router.reader.Reader;
import it.albertus.router.util.Logger;

import java.util.Locale;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

/*
 ##### RouterLogger ## Uncomment properties to enable custom settings #####

 ### Network ###
 #socket.timeout.ms=30000
 #connection.timeout.ms=20000
 #telnet.newline.characters=CRLF

 ### Source ## TpLink8970Reader: TP-Link TD-W8970 V1 ## AsusDslN12EReader: ASUS DSL-N12E ## AsusDslN14UReader: ASUS DSL-N14U ## DLinkDsl2750Reader: D-Link DSL-2750B ## Specify your Reader's fully qualified class name for customized logging ###
 reader.class.name=TpLink8970Reader
 #reader.class.name=AsusDslN12EReader
 #reader.class.name=AsusDslN14UReader
 #reader.class.name=DLinkDsl2750Reader
 #reader.class.name=

 ### TP-Link TD-W8970 V1 ###
 #tplink.8970.command.info.adsl=adsl show info
 #tplink.8970.command.info.wan=wan show connection info pppoa_8_35_1_d

 ### ASUS DSL-N12E ###
 #asus.dsln12e.command.info.adsl=show wan adsl
 #asus.dsln12e.command.info.wan=show wan interface

 ### ASUS DSL-N14U ###
 #asus.dsln14u.command.info.adsl=tcapi show Info_Adsl
 #asus.dsln14u.command.info.wan=tcapi show Wan_PVC0

 ### D-Link DSL-2750B ###
 #dlink.2750.command.info.adsl.status=adsl status
 #dlink.2750.command.info.adsl.snr=adsl snr

 ### Destination ## CsvWriter: CSV ## DatabaseWriter: database ## Specify your Writer's fully qualified class name for customized logging ###
 #writer.class.name=CsvWriter
 #writer.class.name=DatabaseWriter
 #writer.class.name=

 ### CSV ###
 #csv.destination.path=C:/Router/Logs
 #csv.newline.characters=CRLF
 #csv.field.separator=;
 #csv.field.separator.replacement=,

 ### Database ###
 #database.driver.class.name=oracle.jdbc.OracleDriver
 #database.url=jdbc:oracle:thin:@localhost:1521:XE
 #database.username=routerlogger
 #database.password=routerlogger
 #database.table.name=router_log
 #database.connection.validation.timeout.ms=2000
 #database.timestamp.column.type=TIMESTAMP
 #database.response.column.type=INTEGER
 #database.info.column.type=VARCHAR(250)
 #database.column.name.prefix=rl_
 #database.column.name.max.length=30

 ### Thresholds (key, type, value) ###
 #thresholds.split=false
 #thresholds.excluded=rate.down
 #thresholds.excluded.separator=,
 #threshold.snr.down=downstreamNoiseMargin lt 100
 #threshold.rate.down=downstreamCurrRate lt 2500
 */
	
public enum Preference {
	ROUTER_USERNAME(Page.ROUTER, StringFieldEditor.class),
	ROUTER_PASSWORD(Page.ROUTER, StringFieldEditor.class),
	ROUTER_ADDRESS(Page.ROUTER, StringFieldEditor.class, Reader.Defaults.ROUTER_ADDRESS),
	ROUTER_PORT(Page.ROUTER, IntegerFieldEditor.class, Integer.toString(Reader.Defaults.ROUTER_PORT), 5),

	LOGGER_ITERATIONS(Page.GENERAL, IntegerFieldEditor.class, Integer.toString(RouterLoggerEngine.Defaults.ITERATIONS), Integer.toString(Integer.MAX_VALUE).length() - 1),
	LOGGER_INTERVAL_NORMAL_MS(Page.GENERAL, ScaleWithLabelFieldEditor.class, Long.toString(RouterLoggerEngine.Defaults.INTERVAL_NORMAL_IN_MILLIS), new int[] { 0, 15000, 10, 1000 }),
	LOGGER_INTERVAL_FAST_MS(Page.GENERAL, ScaleWithLabelFieldEditor.class, Long.toString(RouterLoggerEngine.Defaults.INTERVAL_FAST_IN_MILLIS), new int[] { 0, 15000, 10, 1000 }),
	LOGGER_HYSTERESIS_MS(Page.GENERAL, IntegerFieldEditor.class, Long.toString(RouterLoggerEngine.Defaults.HYSTERESIS_IN_MILLIS), Integer.toString(Integer.MAX_VALUE).length() - 1),
	LOGGER_RETRY_COUNT(Page.GENERAL, IntegerFieldEditor.class, Integer.toString(RouterLoggerEngine.Defaults.RETRIES), Integer.toString(Integer.MAX_VALUE).length() - 1),
	LOGGER_RETRY_INTERVAL_MS(Page.GENERAL, IntegerFieldEditor.class, Long.toString(RouterLoggerEngine.Defaults.RETRY_INTERVAL_IN_MILLIS), Integer.toString(Integer.MAX_VALUE).length() - 1),
	LOGGER_ERROR_LOG_DESTINATION_PATH(Page.GENERAL, DirectoryFieldEditor.class),
	LANGUAGE(Page.GENERAL, StringFieldEditor.class, Locale.getDefault().getLanguage()),

	GUI_TABLE_ITEMS_MAX(Page.APPEARANCE, IntegerFieldEditor.class, Integer.toString(DataTable.Defaults.GUI_TABLE_MAX_ITEMS), 4),
	GUI_TABLE_COLUMNS_PACK(Page.APPEARANCE, BooleanFieldEditor.class, Boolean.toString(DataTable.Defaults.GUI_TABLE_COLUMNS_PACK)),
	GUI_MINIMIZE_TRAY(Page.APPEARANCE, BooleanFieldEditor.class, Boolean.toString(TrayIcon.Defaults.GUI_MINIMIZE_TRAY)),
	GUI_START_MINIMIZED(Page.APPEARANCE, BooleanFieldEditor.class, Boolean.toString(RouterLoggerGui.Defaults.GUI_START_MINIMIZED)),
	GUI_TRAY_TOOLTIP(Page.APPEARANCE, BooleanFieldEditor.class, Boolean.toString(TrayIcon.Defaults.GUI_TRAY_TOOLTIP)),
	GUI_CONFIRM_CLOSE(Page.APPEARANCE, BooleanFieldEditor.class, Boolean.toString(CloseMessageBox.Defaults.GUI_CONFIRM_CLOSE)),
	GUI_CONSOLE_MAX_CHARS(Page.APPEARANCE, IntegerFieldEditor.class, Integer.toString(TextConsole.Defaults.GUI_CONSOLE_MAX_CHARS), 6),
	GUI_IMPORTANT_KEYS(Page.APPEARANCE, StringFieldEditor.class),
	GUI_IMPORTANT_KEYS_SEPARATOR(Page.APPEARANCE, StringFieldEditor.class, RouterLoggerConfiguration.Defaults.GUI_IMPORTANT_KEYS_SEPARATOR),

	CONSOLE_ANIMATION(Page.CONSOLE, BooleanFieldEditor.class, Boolean.toString(RouterLoggerConsole.Defaults.CONSOLE_ANIMATION)),
	CONSOLE_SHOW_CONFIGURATION(Page.GENERAL, BooleanFieldEditor.class, Boolean.toString(RouterLoggerEngine.Defaults.CONSOLE_SHOW_CONFIGURATION)),
	CONSOLE_SHOW_KEYS(Page.CONSOLE, StringFieldEditor.class),
	CONSOLE_SHOW_KEYS_SEPARATOR(Page.CONSOLE, StringFieldEditor.class, RouterLoggerConfiguration.Defaults.CONSOLE_SHOW_KEYS_SEPARATOR),
	CONSOLE_DEBUG(Page.GENERAL, BooleanFieldEditor.class, Boolean.toString(Logger.Defaults.DEBUG));

	private static final String RESOURCE_KEY_PREFIX = "lbl.preferences.";

	private final String configurationKey;
	private final String resourceKey;
	private final Page page;
	private final Class<? extends FieldEditor> fieldEditorClass;
	private final String defaultValue;
	private final Object fieldEditorData;

	private Preference(final Page page, final Class<? extends FieldEditor> fieldEditorClass) {
		this(null, null, page, fieldEditorClass, null, null);
	}

	private Preference(final String configurationKey, final Page page, final Class<? extends FieldEditor> fieldEditorClass) {
		this(configurationKey, null, page, fieldEditorClass, null, null);
	}

	private Preference(final String configurationKey, final String resourceKey, final Page page, final Class<? extends FieldEditor> fieldEditorClass) {
		this(configurationKey, resourceKey, page, fieldEditorClass, null, null);
	}

	private Preference(final Page page, final Class<? extends FieldEditor> fieldEditorClass, final String defaultValue) {
		this(null, null, page, fieldEditorClass, defaultValue, null);
	}

	private Preference(final String configurationKey, final Page page, final Class<? extends FieldEditor> fieldEditorClass, final String defaultValue) {
		this(configurationKey, null, page, fieldEditorClass, defaultValue, null);
	}

	private Preference(final String configurationKey, final String resourceKey, final Page page, final Class<? extends FieldEditor> fieldEditorClass, final String defaultValue) {
		this(configurationKey, resourceKey, page, fieldEditorClass, defaultValue, null);
	}

	private Preference(final Page page, final Class<? extends FieldEditor> fieldEditorClass, final String defaultValue, final Object fieldEditorData) {
		this(null, null, page, fieldEditorClass, defaultValue, fieldEditorData);
	}

	private Preference(final String configurationKey, final Page page, final Class<? extends FieldEditor> fieldEditorClass, final String defaultValue, final Object fieldEditorData) {
		this(configurationKey, null, page, fieldEditorClass, defaultValue, fieldEditorData);
	}

	private Preference(final String configurationKey, final String resourceKey, final Page page, final Class<? extends FieldEditor> fieldEditorClass, final String defaultValue, final Object fieldEditorData) {
		if (configurationKey != null && !configurationKey.isEmpty()) {
			this.configurationKey = configurationKey;
		}
		else {
			this.configurationKey = name().toLowerCase().replace('_', '.');
		}
		if (resourceKey != null && !resourceKey.isEmpty()) {
			this.resourceKey = resourceKey;
		}
		else {
			this.resourceKey = RESOURCE_KEY_PREFIX + this.configurationKey;
		}
		this.fieldEditorData = fieldEditorData;
		this.defaultValue = defaultValue;
		this.fieldEditorClass = fieldEditorClass;
		this.page = page;
	}

	public String getConfigurationKey() {
		return configurationKey;
	}

	public String getResourceKey() {
		return resourceKey;
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

	public Object getFieldEditorData() {
		return fieldEditorData;
	}

}
