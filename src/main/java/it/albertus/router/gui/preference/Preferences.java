package it.albertus.router.gui.preference;

import it.albertus.gui.preference.APreferences;
import it.albertus.gui.preference.IPreference;
import it.albertus.gui.preference.page.IPage;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.gui.preference.page.Page;
import it.albertus.router.resources.Resources;
import it.albertus.router.resources.Resources.Language;
import it.albertus.util.Configuration;

import org.eclipse.swt.widgets.Shell;

public class Preferences extends APreferences {

	private final RouterLoggerGui gui;
	private boolean restartRequired = false;

	public Preferences(final RouterLoggerGui gui) {
		super(gui.getShell());
		this.gui = gui;
	}

	public Preferences(final Shell parentShell) {
		super(parentShell);
		this.gui = null;
	}

	@Override
	protected Configuration getConfiguration() {
		return RouterLoggerConfiguration.getInstance();
	}

	@Override
	protected IPage[] getPages() {
		return Page.values();
	}

	@Override
	protected IPreference[] getPreferences() {
		return Preference.values();
	}

	@Override
	public int open(final IPage selectedPage) {
		final Language language = Resources.getLanguage();

		final int returnCode = super.open(selectedPage);

		// Check if must update texts...
		if (gui != null && !language.equals(Resources.getLanguage())) {
			gui.getMenuBar().updateTexts();
		}

		// Check if restart is required...
		final Configuration configuration = getConfiguration();
		final String configuredReaderClassName = RouterLoggerEngine.getReaderClassName(configuration.getString(Preference.READER_CLASS_NAME.getConfigurationKey()));
		final String configuredWriterClassName = RouterLoggerEngine.getWriterClassName(configuration.getString(Preference.WRITER_CLASS_NAME.getConfigurationKey(), Preference.WRITER_CLASS_NAME.getDefaultValue()));
		if (gui != null && (gui.getReader() == null || !gui.getReader().getClass().getName().equals(configuredReaderClassName) || gui.getWriter() == null || !gui.getWriter().getClass().getName().equals(configuredWriterClassName))) {
			try {
				// Check if configured classes are valid...
				Class.forName(configuredReaderClassName, false, this.getClass().getClassLoader());
				Class.forName(configuredWriterClassName, false, this.getClass().getClassLoader());
				restartRequired = true; // Restart dialog will be shown.
			}
			catch (final Throwable t) {}
		}
		return returnCode;
	}

	public boolean isRestartRequired() {
		return restartRequired;
	}

}
