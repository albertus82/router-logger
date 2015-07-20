package it.albertus.router.util;

import it.albertus.router.RouterLoggerConfiguration;
import it.albertus.router.gui.GuiConsole;
import it.albertus.util.Configuration;
import it.albertus.util.Console;
import it.albertus.util.TerminalConsole;

public class ConsoleFactory {

	private interface Defaults {
		boolean GUI_ACTIVE = true;
	}

	private static final Configuration configuration = RouterLoggerConfiguration.getInstance();

	public static Console getConsole() {
		return configuration.getBoolean("gui.active", Defaults.GUI_ACTIVE) ? GuiConsole.getInstance() : TerminalConsole.getInstance();
	}

}
