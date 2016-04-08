package it.albertus.router.gui.preferences;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.resources.Resources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;

public class Preferences {

	public Preferences(final RouterLoggerGui gui) {
		final PreferenceManager preferenceManager = new PreferenceManager();

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
			configurationInputStream = openConfigurationInputStream();
			preferenceStore.load(configurationInputStream);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				configurationInputStream.close();
			}
			catch (Exception e) {}
		}

		final PreferenceDialog preferenceDialog = new PreferenceDialog(gui.getShell(), preferenceManager);

		preferenceDialog.setPreferenceStore(preferenceStore);

		// Open configuration dialog...
		if (preferenceDialog.open() == 0) {
			// Save configuration file...
			OutputStream configurationOutputStream = null;
			try {
				configurationOutputStream = openConfigurationOutputStream();
				preferenceStore.save(configurationOutputStream, null);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				try {
					configurationOutputStream.close();
				}
				catch (Exception e) {}
			}

			// Reload RouterLogger configuration...
			RouterLoggerConfiguration.getInstance().reload();
		}
	}

	private InputStream openConfigurationInputStream() throws IOException {
		final InputStream inputStream;
		File config = null;
		try {
			config = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart()).getParent() + File.separator + RouterLoggerConfiguration.FILE_NAME);
		}
		catch (URISyntaxException use) {
			throw new IOException(use);
		}
		if (config != null && config.exists()) {
			inputStream = new BufferedInputStream(new FileInputStream(config));
		}
		else {
			inputStream = getClass().getResourceAsStream('/' + RouterLoggerConfiguration.FILE_NAME);
		}
		return inputStream;
	}

	private OutputStream openConfigurationOutputStream() throws IOException {
		final OutputStream outputStream;
		File config = null;
		try {
			config = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart()).getParent() + File.separator + RouterLoggerConfiguration.FILE_NAME);
		}
		catch (URISyntaxException use) {
			throw new IOException(use);
		}
		outputStream = new BufferedOutputStream(new FileOutputStream(config));
		return outputStream;
	}

}
