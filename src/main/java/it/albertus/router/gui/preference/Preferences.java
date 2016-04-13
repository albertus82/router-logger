package it.albertus.router.gui.preference;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.gui.preference.page.Page;
import it.albertus.router.resources.Resources;
import it.albertus.router.resources.Resources.Language;
import it.albertus.router.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

public class Preferences {

	private final RouterLoggerGui gui;
	private final Shell parentShell;
	private boolean restartRequired = false;

	public Preferences(final RouterLoggerGui gui) {
		this.gui = gui;
		this.parentShell = gui.getShell();
	}

	public Preferences(final Shell parentShell) {
		this.gui = null;
		this.parentShell = parentShell;
	}

	public int open() {
		return open(null);
	}

	public int open(final Page selectedPage) {
		final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

		final PreferenceManager preferenceManager = new PreferenceManager();

		// Pages creation...
		final Map<Page, PreferenceNode> preferenceNodes = new EnumMap<Page, PreferenceNode>(Page.class);
		for (final Page page : Page.values()) {
			final PreferenceNode preferenceNode = new PreferenceNode(page.getNodeId(), Resources.get(page.getResourceKey()), null, page.getPageClass().getName());
			if (page.getParent() != null) {
				preferenceNodes.get(page.getParent()).add(preferenceNode);
			}
			else {
				preferenceManager.addToRoot(preferenceNode);
			}
			preferenceNodes.put(page, preferenceNode);
		}

		final PreferenceStore preferenceStore = new PreferenceStore(RouterLoggerConfiguration.FILE_NAME);

		// Set default values...
		for (final Preference preference : Preference.values()) {
			if (preference.getDefaultValue() != null) {
				preferenceStore.setDefault(preference.getConfigurationKey(), preference.getDefaultValue());
			}
		}

		// Load configuration file...
		InputStream configurationInputStream = null;
		try {
			configurationInputStream = configuration.openConfigurationInputStream();
			if (configurationInputStream != null) {
				preferenceStore.load(configurationInputStream);
			}
		}
		catch (IOException ioe) {
			Logger.getInstance().log(ioe);
		}
		finally {
			try {
				configurationInputStream.close();
			}
			catch (Exception e) {}
		}

		final PreferenceDialog preferenceDialog = new ConfigurationDialog(parentShell, preferenceManager);

		preferenceDialog.setPreferenceStore(preferenceStore);

		if (selectedPage != null) {
			preferenceDialog.setSelectedNode(selectedPage.getNodeId());
		}

		final Language language = Resources.getLanguage();

		// Open configuration dialog...
		final int returnCode = preferenceDialog.open();

		if (returnCode == Window.OK) {
			// Save configuration file...
			OutputStream configurationOutputStream = null;
			try {
				configurationOutputStream = configuration.openConfigurationOutputStream();
				preferenceStore.save(configurationOutputStream, null);
			}
			catch (IOException ioe) {
				Logger.getInstance().log(ioe);
			}
			finally {
				try {
					configurationOutputStream.close();
				}
				catch (final Exception exception) {}
			}

			// Reload RouterLogger configuration...
			try {
				configuration.reload();
			}
			catch (final Exception exception) {
				Logger.getInstance().log(exception);
			}
		}

		// Check if must update texts...
		if (gui != null && !language.equals(Resources.getLanguage())) {
			gui.getMenuBar().updateTexts();
		}

		// Check if restart is required...
		final String configuredReaderClassName = RouterLoggerEngine.getReaderClassName(configuration.getString(Preference.READER_CLASS_NAME.getConfigurationKey()));
		final String configuredWriterClassName = RouterLoggerEngine.getWriterClassName(configuration.getString(Preference.WRITER_CLASS_NAME.getConfigurationKey(), Preference.WRITER_CLASS_NAME.getDefaultValue()));
		if (gui != null && (gui.getReader() == null || !gui.getReader().getClass().getName().equals(configuredReaderClassName) || gui.getWriter() == null || !gui.getWriter().getClass().getName().equals(configuredWriterClassName))) {
			restartRequired = true;
		}

		return returnCode;
	}

	public Shell getParentShell() {
		return parentShell;
	}

	public boolean isRestartRequired() {
		return restartRequired;
	}

}
