package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.jface.preference.page.PageDefinition;
import it.albertus.router.reader.AsusDslN12EReader;
import it.albertus.router.reader.AsusDslN14UReader;
import it.albertus.router.reader.DLinkDsl2750Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.resources.Resources;

public enum RouterLoggerPage implements PageDefinition {
	GENERAL(GeneralPreferencePage.class),
	READER(ReaderPreferencePage.class),
	APPEARANCE(RestartHeaderPreferencePage.class),
	CONSOLE(null, APPEARANCE),
	TPLINK_8970(TpLink8970Reader.DEVICE_MODEL_KEY, null, READER),
	ASUS_N12E(AsusDslN12EReader.DEVICE_MODEL_KEY, null, READER),
	ASUS_N14U(AsusDslN14UReader.DEVICE_MODEL_KEY, null, READER),
	DLINK_2750(DLinkDsl2750Reader.DEVICE_MODEL_KEY, null, READER),
	WRITER(WriterPreferencePage.class),
	CSV(CsvPreferencePage.class, WRITER),
	DATABASE(DatabasePreferencePage.class, WRITER),
	THRESHOLDS(),
	EMAIL(null),
	EMAIL_ADVANCED(null, EMAIL),
	EMAIL_CC_BCC(null, EMAIL),
	SERVER(ServerPreferencePage.class),
	SERVER_HANDLER(null, SERVER),
	SERVER_HTTPS(ServerHttpsPreferencePage.class, SERVER),
	MQTT(MqttPreferencePage.class),
	MQTT_MESSAGES(RestartHeaderPreferencePage.class, MQTT),
	MQTT_ADVANCED(AdvancedMqttPreferencePage.class, MQTT);

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final String nodeId;
	private final String labelKey;
	private final Class<? extends BasePreferencePage> pageClass;
	private final PageDefinition parent;

	private RouterLoggerPage() {
		this(null, null, null, null);
	}

	private RouterLoggerPage(final Class<? extends BasePreferencePage> pageClass) {
		this(null, null, pageClass, null);
	}

	private RouterLoggerPage(final Class<? extends BasePreferencePage> pageClass, final PageDefinition parent) {
		this(null, null, pageClass, parent);
	}

	private RouterLoggerPage(final String labelKey, final Class<? extends BasePreferencePage> pageClass) {
		this(null, labelKey, pageClass, null);
	}

	private RouterLoggerPage(final String labelKey, final Class<? extends BasePreferencePage> pageClass, final PageDefinition parent) {
		this(null, labelKey, pageClass, parent);
	}

	private RouterLoggerPage(final String nodeId, final String labelKey, final Class<? extends BasePreferencePage> pageClass) {
		this(nodeId, labelKey, pageClass, null);
	}

	private RouterLoggerPage(final String nodeId, final String labelKey, final Class<? extends BasePreferencePage> pageClass, final PageDefinition parent) {
		if (nodeId != null && !nodeId.isEmpty()) {
			this.nodeId = nodeId;
		}
		else {
			this.nodeId = name().toLowerCase().replace('_', '.');
		}
		if (labelKey != null && !labelKey.isEmpty()) {
			this.labelKey = labelKey;
		}
		else {
			this.labelKey = LABEL_KEY_PREFIX + this.nodeId;
		}
		this.pageClass = pageClass;
		this.parent = parent;
	}

	@Override
	public String getNodeId() {
		return nodeId;
	}

	@Override
	public String getLabel() {
		return Resources.get(labelKey);
	}

	@Override
	public Class<? extends BasePreferencePage> getPageClass() {
		return pageClass;
	}

	@Override
	public PageDefinition getParent() {
		return parent;
	}

}
