package it.albertus.router.console;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.engine.Threshold;
import it.albertus.router.resources.Resources;
import it.albertus.util.TerminalConsole;
import it.albertus.util.Version;

import java.util.Map;

public class RouterLoggerConsole extends RouterLoggerEngine {

	protected interface Defaults extends RouterLoggerEngine.Defaults {
		boolean CONSOLE_ANIMATION = true;
	}

	private static final String ARG_HELP = "--help";
	private static final String ARG_CONSOLE = "-c";

	private static final char[] ANIMATION = { '-', '\\', '|', '/' };

	/** Entry point for console version */
	public static void start(String args[]) {
		/* Controlli sui parametri */
		if (args[0].trim().equalsIgnoreCase(ARG_HELP)) {
			final Version version = Version.getInstance();
			System.out.println(Resources.get("msg.welcome", Resources.get("msg.application.name"), Resources.get("msg.version", version.getNumber(), version.getDate()), Resources.get("msg.website")));
			System.out.println();
			System.out.println(Resources.get("msg.help.usage", ARG_CONSOLE, ARG_HELP));
			System.out.println();
			System.out.println("  " + Resources.get("msg.help.option.console", ARG_CONSOLE));
			System.out.println("  " + Resources.get("msg.help.option.help", ARG_HELP));
		}
		else if (args.length > 1) {
			System.err.println(Resources.get("err.too.many.parameters", args[1]));
			System.out.println(Resources.get("err.try.help", ARG_HELP));
		}
		else if (args[0].trim().equalsIgnoreCase(ARG_CONSOLE)) {
			/* Start RouterLogger in console */
			new RouterLoggerConsole().run();
		}
		else {
			System.err.println(Resources.get("err.unrecognized.option", args[0]));
			System.out.println(Resources.get("err.try.help", ARG_HELP));
		}
	}

	private int lastLogLength = 0;

	@Override
	protected void showInfo(final RouterData info, final Map<Threshold, String> thresholdsReached) {
		// Scrittura indice dell'iterazione in console...
		final StringBuilder clean = new StringBuilder();
		while (lastLogLength-- > 0) {
			clean.append('\b').append(' ').append('\b');
		}
		final StringBuilder log = new StringBuilder();
		final boolean animate = configuration.getBoolean("console.animation", Defaults.CONSOLE_ANIMATION);
		if (animate) {
			log.append(ANIMATION[getIteration() & 3]).append(' ');
		}
		log.append(getIteration());
		final int iterations = configuration.getInt("logger.iterations", Defaults.ITERATIONS);
		if (iterations > 0) {
			log.append('/').append(iterations);
		}
		log.append(' ');
		if (animate) {
			log.append(ANIMATION[getIteration() & 3]).append(' ');
		}
		// Fine scrittura indice.

		// Scrittura informazioni aggiuntive richieste...
		if (info != null && info.getData() != null && !info.getData().isEmpty()) {
			final StringBuilder infoToShow = new StringBuilder();
			for (String keyToShow : configuration.getConsoleKeysToShow()) {
				if (keyToShow != null && !"".equals(keyToShow.trim())) {
					keyToShow = keyToShow.trim();
					for (final String key : info.getData().keySet()) {
						if (key != null && key.trim().equals(keyToShow)) {
							if (infoToShow.length() == 0) {
								infoToShow.append('[');
							}
							else {
								infoToShow.append(", ");
							}
							infoToShow.append(keyToShow + ": " + info.getData().get(key));
						}
					}
				}
			}
			if (infoToShow.length() != 0) {
				infoToShow.append("] ");
			}
			log.append(infoToShow);
		}
		// Fine scrittura informazioni aggiuntive.

		lastLogLength = log.length();
		out.print(clean.toString() + log.toString());
	}

	@Override
	protected TerminalConsole getConsole() {
		return TerminalConsole.getInstance();
	}

}