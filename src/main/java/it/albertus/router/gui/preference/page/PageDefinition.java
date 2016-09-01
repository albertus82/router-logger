package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.jface.preference.page.IPageDefinition;
import it.albertus.jface.preference.page.PageDefinitionDetails;
import it.albertus.jface.preference.page.PageDefinitionDetails.PageDefinitionDetailsBuilder;
import it.albertus.jface.preference.page.RestartHeaderPreferencePage;
import it.albertus.router.reader.AsusDslN12EReader;
import it.albertus.router.reader.AsusDslN14UReader;
import it.albertus.router.reader.DLinkDsl2750Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.resources.Resources;
import it.albertus.util.Localized;

import org.eclipse.jface.resource.ImageDescriptor;

public enum PageDefinition implements IPageDefinition {

	GENERAL(new PageDefinitionDetailsBuilder().pageClass(GeneralPreferencePage.class).build()),
	READER(new PageDefinitionDetailsBuilder().pageClass(ReaderPreferencePage.class).build()),
	APPEARANCE(new PageDefinitionDetailsBuilder().pageClass(RestartHeaderPreferencePage.class).build()),
	CONSOLE(new PageDefinitionDetailsBuilder().parent(APPEARANCE).build()),
	READER_TPLINK_8970(new PageDefinitionDetailsBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(TpLink8970Reader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	READER_ASUS_N12E(new PageDefinitionDetailsBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(AsusDslN12EReader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	READER_ASUS_N14U(new PageDefinitionDetailsBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(AsusDslN14UReader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	READER_DLINK_2750(new PageDefinitionDetailsBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(DLinkDsl2750Reader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	WRITER(new PageDefinitionDetailsBuilder().pageClass(WriterPreferencePage.class).build()),
	CSV(new PageDefinitionDetailsBuilder().pageClass(CsvPreferencePage.class).parent(WRITER).build()),
	DATABASE(new PageDefinitionDetailsBuilder().pageClass(DatabasePreferencePage.class).parent(WRITER).build()),
	THRESHOLDS,
	EMAIL,
	EMAIL_ADVANCED(new PageDefinitionDetailsBuilder().parent(EMAIL).build()),
	EMAIL_CC_BCC(new PageDefinitionDetailsBuilder().parent(EMAIL).build()),
	SERVER(new PageDefinitionDetailsBuilder().pageClass(ServerPreferencePage.class).build()),
	SERVER_HANDLER(new PageDefinitionDetailsBuilder().parent(SERVER).build()),
	SERVER_HTTPS(new PageDefinitionDetailsBuilder().pageClass(ServerHttpsPreferencePage.class).parent(SERVER).build()),
	MQTT(new PageDefinitionDetailsBuilder().pageClass(MqttPreferencePage.class).build()),
	MQTT_MESSAGES(new PageDefinitionDetailsBuilder().pageClass(RestartHeaderPreferencePage.class).parent(MQTT).build()),
	MQTT_ADVANCED(new PageDefinitionDetailsBuilder().pageClass(AdvancedMqttPreferencePage.class).parent(MQTT).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final PageDefinitionDetails pageDefinitionDetails;

	PageDefinition() {
		this(new PageDefinitionDetailsBuilder().build());
	}

	PageDefinition(final PageDefinitionDetails pageDefinitionDetails) {
		this.pageDefinitionDetails = pageDefinitionDetails;
		if (pageDefinitionDetails.getNodeId() == null) {
			pageDefinitionDetails.setNodeId(name().toLowerCase().replace('_', '.'));
		}
		if (pageDefinitionDetails.getLabel() == null) {
			pageDefinitionDetails.setLabel(new Localized() {
				@Override
				public String getString() {
					return Resources.get(LABEL_KEY_PREFIX + pageDefinitionDetails.getNodeId());
				}
			});
		}
	}

	@Override
	public String getNodeId() {
		return pageDefinitionDetails.getNodeId();
	}

	@Override
	public String getLabel() {
		return pageDefinitionDetails.getLabel().getString();
	}

	@Override
	public Class<? extends BasePreferencePage> getPageClass() {
		return pageDefinitionDetails.getPageClass();
	}

	@Override
	public IPageDefinition getParent() {
		return pageDefinitionDetails.getParent();
	}

	@Override
	public ImageDescriptor getImage() {
		return pageDefinitionDetails.getImage();
	}

}
