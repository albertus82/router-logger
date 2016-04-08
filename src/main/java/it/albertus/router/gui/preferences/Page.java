package it.albertus.router.gui.preferences;

public enum Page {
	GENERAL("general", "lbl.preferences.general", GeneralPreferencePage.class),
	ROUTER("router", "lbl.preferences.router", RouterPreferencePage.class),
	APPEARANCE("appearance", "lbl.preferences.appearance", AppearancePreferencePage.class);
	// NETWORK("lbl.preferences.network", NetworkPreferencePage.class),
	// CONSOLE("lbl.preferences.console", ConsolePreferencePage.class),
	// GUI("lbl.preferences.gui", GuiPreferencePage.class),
	// SOURCE("lbl.preferences.source", SourcePreferencePage.class),
	// DESTINATION("lbl.preferences.destination",
	// DestinationPreferencePage.class);

	private final String nodeId;
	private final String resourceKey;
	private final Class<? extends RouterLoggerPreferencePage> pageClass;
	private final Page parent;

	private Page(final String nodeId, final String resourceKey, final Class<? extends RouterLoggerPreferencePage> pageClass) {
		this(nodeId, resourceKey, pageClass, null);
	}

	private Page(final String nodeId, final String resourceKey, final Class<? extends RouterLoggerPreferencePage> pageClass, final Page parent) {
		this.nodeId = nodeId;
		this.resourceKey = resourceKey;
		this.pageClass = pageClass;
		this.parent = parent;
	}

	public String getNodeId() {
		return nodeId;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public Class<? extends RouterLoggerPreferencePage> getPageClass() {
		return pageClass;
	}

	public Page getParent() {
		return parent;
	}

}
