package it.albertus.gui.preference.page;

public interface IPage {

	String getNodeId();

	String getLabel();

	Class<? extends AbstractPreferencePage> getPageClass();

	IPage getParent();

}
