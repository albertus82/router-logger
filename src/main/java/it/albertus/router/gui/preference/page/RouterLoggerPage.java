package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.jface.preference.page.IPageDefinition;
import it.albertus.jface.preference.page.PageDefinition;
import it.albertus.jface.preference.page.PageDefinition.PageDefinitionBuilder;
import it.albertus.router.reader.AsusDslN12EReader;
import it.albertus.router.reader.AsusDslN14UReader;
import it.albertus.router.reader.DLinkDsl2750Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.resources.Resources;
import it.albertus.util.Localized;

import org.eclipse.jface.resource.ImageDescriptor;

public enum RouterLoggerPage implements IPageDefinition {
	GENERAL(new PageDefinitionBuilder().pageClass(GeneralPreferencePage.class).build()),
	READER(new PageDefinitionBuilder().pageClass(ReaderPreferencePage.class).build()),
	APPEARANCE(new PageDefinitionBuilder().pageClass(RestartHeaderPreferencePage.class).build()),
	CONSOLE(new PageDefinitionBuilder().parent(APPEARANCE).build()),
	TPLINK_8970(new PageDefinitionBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(TpLink8970Reader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	ASUS_N12E(new PageDefinitionBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(AsusDslN12EReader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	ASUS_N14U(new PageDefinitionBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(AsusDslN14UReader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	DLINK_2750(new PageDefinitionBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(DLinkDsl2750Reader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	WRITER(new PageDefinitionBuilder().pageClass(WriterPreferencePage.class).build()),
	CSV(new PageDefinitionBuilder().pageClass(CsvPreferencePage.class).parent(WRITER).build()),
	DATABASE(new PageDefinitionBuilder().pageClass(DatabasePreferencePage.class).parent(WRITER).build()),
	THRESHOLDS(),
	EMAIL(),
	EMAIL_ADVANCED(new PageDefinitionBuilder().parent(EMAIL).build()),
	EMAIL_CC_BCC(new PageDefinitionBuilder().parent(EMAIL).build()),
	SERVER(new PageDefinitionBuilder().pageClass(ServerPreferencePage.class).build()),
	SERVER_HANDLER(new PageDefinitionBuilder().parent(SERVER).build()),
	SERVER_HTTPS(new PageDefinitionBuilder().pageClass(ServerHttpsPreferencePage.class).parent(SERVER).build()),
	MQTT(new PageDefinitionBuilder().pageClass(MqttPreferencePage.class).build()),
	MQTT_MESSAGES(new PageDefinitionBuilder().pageClass(RestartHeaderPreferencePage.class).parent(MQTT).build()),
	MQTT_ADVANCED(new PageDefinitionBuilder().pageClass(AdvancedMqttPreferencePage.class).parent(MQTT).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final PageDefinition pageDefinition;

	RouterLoggerPage() {
		this(new PageDefinition());
	}

	RouterLoggerPage(final PageDefinition pageDefinition) {
		this.pageDefinition = pageDefinition;
		if (pageDefinition.getNodeId() == null) {
			pageDefinition.setNodeId(name().toLowerCase().replace('_', '.'));
		}
		if (pageDefinition.getLabel() == null) {
			pageDefinition.setLabel(new Localized() {
				@Override
				public String getString() {
					return Resources.get(LABEL_KEY_PREFIX + pageDefinition.getNodeId());
				}
			});
		}
	}

	@Override
	public String getNodeId() {
		return pageDefinition.getNodeId();
	}

	@Override
	public Localized getLabel() {
		return pageDefinition.getLabel();
	}

	@Override
	public Class<? extends BasePreferencePage> getPageClass() {
		return pageDefinition.getPageClass();
	}

	@Override
	public IPageDefinition getParent() {
		return pageDefinition.getParent();
	}

	@Override
	public ImageDescriptor getImage() {
		return pageDefinition.getImage();
	}

}
