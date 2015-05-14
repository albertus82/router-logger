package it.albertus.router;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class Configurable {
	
	protected static final String CONFIGURATION_FILE_NAME = "routerlogger.cfg";

	protected final Properties configuration = new Properties();
	
	protected Configurable() {
		try {
			// Caricamento file di configurazione...
			loadConfiguration();
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	private void loadConfiguration() throws IOException {
		final InputStream inputStream;
		final File config = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() + '/' + CONFIGURATION_FILE_NAME);
		if (config.exists()) {
			inputStream = new BufferedInputStream(new FileInputStream(config));
		}
		else {
			inputStream = getClass().getResourceAsStream('/' + CONFIGURATION_FILE_NAME);
		}
		configuration.load(inputStream);
		inputStream.close();
	}
	
}
