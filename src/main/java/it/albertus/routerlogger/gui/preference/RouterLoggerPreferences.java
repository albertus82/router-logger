package it.albertus.routerlogger.gui.preference;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import it.albertus.jface.preference.Preferences;
import it.albertus.jface.preference.page.IPageDefinition;
import it.albertus.routerlogger.engine.RouterLoggerConfig;
import it.albertus.routerlogger.engine.RouterLoggerEngine;
import it.albertus.routerlogger.gui.Images;
import it.albertus.routerlogger.gui.RouterLoggerGui;
import it.albertus.routerlogger.gui.preference.page.PageDefinition;
import it.albertus.routerlogger.resources.Messages;
import it.albertus.routerlogger.resources.Messages.Language;
import it.albertus.util.Configuration;
import it.albertus.util.logging.LoggerFactory;

public class RouterLoggerPreferences extends Preferences {

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerPreferences.class);

	private static final Configuration configuration = RouterLoggerConfig.getInstance();

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
		catch (final IOException e) {
			logger.log(Level.SEVERE, e.toString(), e);
			returnCode = Window.CANCEL;
		}

		if (gui != null) {
			// Update console font...
			final String fontDataString = configuration.getString("gui.console.font", true);
			if (!fontDataString.isEmpty()) {
				gui.getConsole().setFont(PreferenceConverter.readFontData(fontDataString));
			}

			// Check if must update texts...
			if (!language.equals(Messages.getLanguage())) {
				gui.getMenuBar().updateTexts();
				gui.getDataTable().updateTexts();
			}

			if (!isRestartRequired()) {
				// Check if restart is required...
				final String configuredReaderClassName = RouterLoggerEngine.getReaderClassName(getPreferenceStore().getString(Preference.READER_CLASS_NAME.getName()));
				final String configuredWriterClassName = RouterLoggerEngine.getWriterClassName(getPreferenceStore().getString(Preference.WRITER_CLASS_NAME.getName()));
				if (gui.getReader() == null || !gui.getReader().getClass().getName().equals(configuredReaderClassName) || gui.getWriters().isEmpty() || !gui.getWriters().get(0).getClass().getName().equals(configuredWriterClassName)) {
					try {
						// Check if configured classes are valid...
						Class.forName(configuredReaderClassName, false, this.getClass().getClassLoader());
						Class.forName(configuredWriterClassName, false, this.getClass().getClassLoader());
						setRestartRequired(true); // Restart dialog will be shown.
					}
					catch (final Exception e) {
						logger.log(Level.FINE, e.toString(), e);
					}
					catch (final LinkageError e) {
						logger.log(Level.FINE, e.toString(), e);
					}
				}
			}
		}
		return returnCode;
	}

}
