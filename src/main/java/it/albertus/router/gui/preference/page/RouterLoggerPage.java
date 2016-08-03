package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.page.Page;
import it.albertus.router.reader.AsusDslN12EReader;
import it.albertus.router.reader.AsusDslN14UReader;
import it.albertus.router.reader.DLinkDsl2750Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.resources.Resources;

public enum RouterLoggerPage implements Page {
	GENERAL(GeneralPreferencePage.class),
	READER(ReaderPreferencePage.class),
	APPEARANCE(AppearancePreferencePage.class),
	CONSOLE(ConsolePreferencePage.class, APPEARANCE),
	TPLINK_8970(TpLink8970Reader.DEVICE_MODEL_KEY, TpLink8970PreferencePage.class, READER),
	ASUS_N12E(AsusDslN12EReader.DEVICE_MODEL_KEY, AsusN12EPreferencePage.class, READER),
	ASUS_N14U(AsusDslN14UReader.DEVICE_MODEL_KEY, AsusN14UPreferencePage.class, READER),
	DLINK_2750(DLinkDsl2750Reader.DEVICE_MODEL_KEY, DLink2750PreferencePage.class, READER),
	WRITER(WriterPreferencePage.class),
	CSV(CsvPreferencePage.class, WRITER),
	DATABASE(DatabasePreferencePage.class, WRITER),
	THRESHOLDS(ThresholdsPreferencePage.class),
	EMAIL(EmailPreferencePage.class),
	EMAIL_ADVANCED(AdvancedEmailPreferencePage.class, EMAIL),
	EMAIL_CC_BCC(CcBccEmailPreferencePage.class, EMAIL),
	SERVER(ServerPreferencePage.class),
	SERVER_HTTPS(ServerHttpsPreferencePage.class, SERVER),
	MQTT(MqttPreferencePage.class);

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final String nodeId;
	private final String labelKey;
	private final Class<? extends BasePreferencePage> pageClass;
	private final RouterLoggerPage parent;

	private RouterLoggerPage(final Class<? extends BasePreferencePage> pageClass) {
		this(null, null, pageClass, null);
	}

	private RouterLoggerPage(final Class<? extends BasePreferencePage> pageClass, final RouterLoggerPage parent) {
		this(null, null, pageClass, parent);
	}

	private RouterLoggerPage(final String labelKey, final Class<? extends BasePreferencePage> pageClass) {
		this(null, labelKey, pageClass, null);
	}

	private RouterLoggerPage(final String labelKey, final Class<? extends BasePreferencePage> pageClass, final RouterLoggerPage parent) {
		this(null, labelKey, pageClass, parent);
	}

	private RouterLoggerPage(final String nodeId, final String labelKey, final Class<? extends BasePreferencePage> pageClass) {
		this(nodeId, labelKey, pageClass, null);
	}

	private RouterLoggerPage(final String nodeId, final String labelKey, final Class<? extends BasePreferencePage> pageClass, final RouterLoggerPage parent) {
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
	public RouterLoggerPage getParent() {
		return parent;
	}

	public static RouterLoggerPage forClass(final Class<? extends BasePreferencePage> clazz) {
		if (clazz != null) {
			for (final RouterLoggerPage page : RouterLoggerPage.values()) {
				if (clazz.equals(page.pageClass)) {
					return page;
				}
			}
		}
		return null;
	}

}
