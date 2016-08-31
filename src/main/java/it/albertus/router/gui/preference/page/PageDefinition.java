package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.page.BasePreferencePage;
import it.albertus.jface.preference.page.IPreferencePageDefinition;
import it.albertus.jface.preference.page.PreferencePageDefinition;
import it.albertus.jface.preference.page.PreferencePageDefinition.PreferencePageDefinitionBuilder;
import it.albertus.router.reader.AsusDslN12EReader;
import it.albertus.router.reader.AsusDslN14UReader;
import it.albertus.router.reader.DLinkDsl2750Reader;
import it.albertus.router.reader.TpLink8970Reader;
import it.albertus.router.resources.Resources;
import it.albertus.util.Localized;

import org.eclipse.jface.resource.ImageDescriptor;

public enum PageDefinition implements IPreferencePageDefinition {

	GENERAL(new PreferencePageDefinitionBuilder().pageClass(GeneralPreferencePage.class).build()),
	READER(new PreferencePageDefinitionBuilder().pageClass(ReaderPreferencePage.class).build()),
	APPEARANCE(new PreferencePageDefinitionBuilder().pageClass(RestartHeaderPreferencePage.class).build()),
	CONSOLE(new PreferencePageDefinitionBuilder().parent(APPEARANCE).build()),
	TPLINK_8970(new PreferencePageDefinitionBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(TpLink8970Reader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	ASUS_N12E(new PreferencePageDefinitionBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(AsusDslN12EReader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	ASUS_N14U(new PreferencePageDefinitionBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(AsusDslN14UReader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	DLINK_2750(new PreferencePageDefinitionBuilder().label(new Localized() {
		@Override
		public String getString() {
			return Resources.get(DLinkDsl2750Reader.DEVICE_MODEL_KEY);
		}
	}).parent(READER).build()),
	WRITER(new PreferencePageDefinitionBuilder().pageClass(WriterPreferencePage.class).build()),
	CSV(new PreferencePageDefinitionBuilder().pageClass(CsvPreferencePage.class).parent(WRITER).build()),
	DATABASE(new PreferencePageDefinitionBuilder().pageClass(DatabasePreferencePage.class).parent(WRITER).build()),
	THRESHOLDS,
	EMAIL,
	EMAIL_ADVANCED(new PreferencePageDefinitionBuilder().parent(EMAIL).build()),
	EMAIL_CC_BCC(new PreferencePageDefinitionBuilder().parent(EMAIL).build()),
	SERVER(new PreferencePageDefinitionBuilder().pageClass(ServerPreferencePage.class).build()),
	SERVER_HANDLER(new PreferencePageDefinitionBuilder().parent(SERVER).build()),
	SERVER_HTTPS(new PreferencePageDefinitionBuilder().pageClass(ServerHttpsPreferencePage.class).parent(SERVER).build()),
	MQTT(new PreferencePageDefinitionBuilder().pageClass(MqttPreferencePage.class).build()),
	MQTT_MESSAGES(new PreferencePageDefinitionBuilder().pageClass(RestartHeaderPreferencePage.class).parent(MQTT).build()),
	MQTT_ADVANCED(new PreferencePageDefinitionBuilder().pageClass(AdvancedMqttPreferencePage.class).parent(MQTT).build());

	private static final String LABEL_KEY_PREFIX = "lbl.preferences.";

	private final IPreferencePageDefinition pageDefinition;

	PageDefinition() {
		this(new PreferencePageDefinition());
	}

	PageDefinition(final PreferencePageDefinition pageDefinition) {
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
	public IPreferencePageDefinition getParent() {
		return pageDefinition.getParent();
	}

	@Override
	public ImageDescriptor getImage() {
		return pageDefinition.getImage();
	}

}
