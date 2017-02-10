package it.albertus.router.gui.preference;

import java.io.IOException;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import it.albertus.jface.preference.Preferences;
import it.albertus.jface.preference.page.IPageDefinition;
import it.albertus.router.RouterLogger;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.gui.Images;
import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.router.gui.preference.page.PageDefinition;
import it.albertus.router.resources.Messages;
import it.albertus.router.resources.Messages.Language;
import it.albertus.router.util.Logger;
import it.albertus.router.util.LoggerFactory;
import it.albertus.util.Configuration;

public class RouterLoggerPreferences extends Preferences {

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerPreferences.class);

	private static final Configuration configuration = RouterLogger.getConfiguration();

	private final RouterLoggerGui gui;

	public RouterLoggerPreferences(final RouterLoggerGui gui) {
		super(PageDefinition.values(), Preference.values(), configuration, Images.getMainIcons());
		this.gui = gui;
	}

	public RouterLoggerPreferences() {
		this(null);
	}

	@Override
	public int openDialog(final Shell parentShell, final IPageDefinition selectedPage) {
		final Language language = Messages.getLanguage();

		int returnCode;
		try {
			returnCode = super.openDialog(parentShell, selectedPage);
		}
		catch (final IOException ioe) {
			logger.error(ioe);
			returnCode = Window.CANCEL;
		}

		// Update console font...
		final String fontDataString = configuration.getString("gui.console.font", true);
		if (!fontDataString.isEmpty()) {
			gui.getConsole().setFont(PreferenceConverter.readFontData(fontDataString));
		}

		// Check if must update texts...
		if (gui != null && !language.equals(Messages.getLanguage())) {
			gui.getMenuBar().updateTexts();
			gui.getDataTable().updateTexts();
		}

		if (!isRestartRequired()) {
			// Check if restart is required...
			final String configuredReaderClassName = RouterLoggerEngine.getReaderClassName(getPreferenceStore().getString(Preference.READER_CLASS_NAME.getName()));
			final String configuredWriterClassName = RouterLoggerEngine.getWriterClassName(getPreferenceStore().getString(Preference.WRITER_CLASS_NAME.getName()));
			if (gui != null && (gui.getReader() == null || !gui.getReader().getClass().getName().equals(configuredReaderClassName) || gui.getWriter() == null || !gui.getWriter().getClass().getName().equals(configuredWriterClassName))) {
				try {
					// Check if configured classes are valid...
					Class.forName(configuredReaderClassName, false, this.getClass().getClassLoader());
					Class.forName(configuredWriterClassName, false, this.getClass().getClassLoader());
					setRestartRequired(true); // Restart dialog will be shown.
				}
				catch (final Exception e) {
					logger.debug(e);
				}
				catch (final LinkageError le) {
					logger.debug(le);
				}
			}
		}
		return returnCode;
	}

}
