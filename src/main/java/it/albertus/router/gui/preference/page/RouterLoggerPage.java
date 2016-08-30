package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.jface.preference.page.PageDefinition;
import it.albertus.jface.preference.page.PageDefinitionData;
import it.albertus.jface.preference.page.PageDefinitionData.PageDefinitionDataBuilder;
import it.albertus.router.reader.AsusDslN12EReader;
import it.albertus.router.reader.AsusDslN14UReader;
import it.albertus.router.reader.DLinkDsl2750Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.resources.Resources;
import it.albertus.util.Localized;

import org.eclipse.jface.resource.ImageDescriptor;

public enum RouterLoggerPage implements PageDefinition {
	GENERAL(new PageDefinitionDataBuilder().pageClass(GeneralPreferencePage.class).build()),
	READER(new PageDefinitionDataBuilder().pageClass(ReaderPreferencePage.class).build()),
	APPEARANCE(new PageDefinitionDataBuilder().pageClass(RestartHeaderPreferencePage.class).build()),
	CONSOLE(new PageDefinitionDataBuilder().parent(APPEARANCE).build()),
	TPLINK_8970(new PageDefinitionDataBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(TpLink8970Reader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	ASUS_N12E(new PageDefinitionDataBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(AsusDslN12EReader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	ASUS_N14U(new PageDefinitionDataBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(AsusDslN14UReader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	DLINK_2750(new PageDefinitionDataBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(DLinkDsl2750Reader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	WRITER(new PageDefinitionDataBuilder().pageClass(WriterPreferencePage.class).build()),
	CSV(new PageDefinitionDataBuilder().pageClass(CsvPreferencePage.class).parent(WRITER).build()),
	DATABASE(new PageDefinitionDataBuilder().pageClass(DatabasePreferencePage.class).parent(WRITER).build()),
	THRESHOLDS(),
	EMAIL(),
	EMAIL_ADVANCED(new PageDefinitionDataBuilder().parent(EMAIL).build()),
	EMAIL_CC_BCC(new PageDefinitionDataBuilder().parent(EMAIL).build()),
	SERVER(new PageDefinitionDataBuilder().pageClass(ServerPreferencePage.class).build()),
	SERVER_HANDLER(new PageDefinitionDataBuilder().parent(SERVER).build()),
	SERVER_HTTPS(new PageDefinitionDataBuilder().pageClass(ServerHttpsPreferencePage.class).parent(SERVER).build()),
	MQTT(new PageDefinitionDataBuilder().pageClass(MqttPreferencePage.class).build()),
	MQTT_MESSAGES(new PageDefinitionDataBuilder().pageClass(RestartHeaderPreferencePage.class).parent(MQTT).build()),
	MQTT_ADVANCED(new PageDefinitionDataBuilder().pageClass(AdvancedMqttPreferencePage.class).parent(MQTT).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final String nodeId;
	private Localized label;
	private final Class<? extends BasePreferencePage> pageClass;
	private final ImageDescriptor image;
	private final PageDefinition parent;

	RouterLoggerPage() {
		this(null);
	}

	RouterLoggerPage(final PageDefinitionData data) {
		this(data != null ? data.getNodeId() : null, data != null ? data.getLabel() : null, data != null ? data.getPageClass() : null, data != null ? data.getImage() : null, data != null ? data.getParent() : null);
	}

	RouterLoggerPage(final String nodeId, final Localized label, final Class<? extends BasePreferencePage> pageClass, final ImageDescriptor image, final PageDefinition parent) {
		if (nodeId != null && !nodeId.isEmpty()) {
			this.nodeId = nodeId;
		}
		else {
			this.nodeId = name().toLowerCase().replace('_', '.');
		}
		if (label != null) {
			this.label = label;
		}
		else {
			this.label = new Localized() {
				@Override
				public String getString() {
					return Resources.get(LABEL_KEY_PREFIX + RouterLoggerPage.this.nodeId);
				}
			};
		}
		this.pageClass = pageClass;
		this.parent = parent;
		this.image = image;
	}

	@Override
	public String getNodeId() {
		return nodeId;
	}

	@Override
	public String getLabel() {
		return label.getString();
	}

	@Override
	public Class<? extends BasePreferencePage> getPageClass() {
		return pageClass;
	}

	@Override
	public PageDefinition getParent() {
		return parent;
	}

	@Override
	public ImageDescriptor getImage() {
		return image;
	}

}
