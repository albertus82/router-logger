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

	private Page(String nodeId, String resourceKey, Class<? extends RouterLoggerPreferencePage> pageClass) {
		this.nodeId = nodeId;
		this.resourceKey = resourceKey;
		this.pageClass = pageClass;
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

}
