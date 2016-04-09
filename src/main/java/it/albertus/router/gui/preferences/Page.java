package it.albertus.router.gui.preferences;

public enum Page {
	GENERAL(GeneralPreferencePage.class),
	READER(ReaderPreferencePage.class),
	APPEARANCE(AppearancePreferencePage.class),
	CONSOLE(ConsolePreferencePage.class, Page.APPEARANCE),
	TPLINK_8970(TpLink8970PreferencePage.class, Page.READER),
	ASUS_N12E(AsusN12EPreferencePage.class, Page.READER),
	ASUS_N14U(AsusN14UPreferencePage.class, Page.READER),
	DLINK_2750(DLink2750PreferencePage.class, Page.READER),
	WRITER(WriterPreferencePage.class),
	CSV(CsvPreferencePage.class, Page.WRITER),
	DATABASE(DatabasePreferencePage.class, Page.WRITER);

	private static final String RESOURCE_KEY_PREFIX = "lbl.preferences.";

	private final String nodeId;
	private final String resourceKey;
	private final Class<? extends BasePreferencePage> pageClass;
	private final Page parent;

	private Page(final Class<? extends BasePreferencePage> pageClass) {
		this(null, null, pageClass, null);
	}

	private Page(final Class<? extends BasePreferencePage> pageClass, final Page parent) {
		this(null, null, pageClass, parent);
	}

	private Page(final String nodeId, final String resourceKey, final Class<? extends BasePreferencePage> pageClass) {
		this(nodeId, resourceKey, pageClass, null);
	}

	private Page(final String nodeId, final String resourceKey, final Class<? extends BasePreferencePage> pageClass, final Page parent) {
		if (nodeId != null && !nodeId.isEmpty()) {
			this.nodeId = nodeId;
		}
		else {
			this.nodeId = name().toLowerCase().replace('_', '.');
		}
		if (resourceKey != null && !resourceKey.isEmpty()) {
			this.resourceKey = resourceKey;
		}
		else {
			this.resourceKey = RESOURCE_KEY_PREFIX + this.nodeId;
		}
		this.pageClass = pageClass;
		this.parent = parent;
	}

	public String getNodeId() {
		return nodeId;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public Class<? extends BasePreferencePage> getPageClass() {
		return pageClass;
	}

	public Page getParent() {
		return parent;
	}

}
