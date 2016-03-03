package it.albertus.router.gui;

import it.albertus.router.resources.Resources;
import it.albertus.util.Version;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class AboutSelectionListener extends SelectionAdapter {

	private final Gui gui;

	public AboutSelectionListener(final Gui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent event) {
		final AboutDialog aboutDialog = new AboutDialog(gui.getShell());
		aboutDialog.setText(Resources.get("lbl.about.title"));
		aboutDialog.setMessage(Resources.get("msg.application.name") + ' ' + Resources.get("msg.version", Version.getInstance().getNumber(), Version.getInstance().getDate()));
		aboutDialog.setLink(Resources.get("msg.website"));
		aboutDialog.open();
	}

}
