package it.albertus.router;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.util.TerminalConsole;

import java.util.Map;

public class RouterLoggerCon extends RouterLoggerEngine {

	protected interface Defaults extends RouterLoggerEngine.Defaults {
		boolean CONSOLE_ANIMATION = true;
	}

	private static final char[] ANIMATION = { '-', '\\', '|', '/' };

	public static void main(String args[]) {
		new RouterLoggerCon().run();
	}

	private int lastLogLength = 0;

	@Override
	protected void showInfo(final RouterData info, final Map<String, String> thresholdsReached) {
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
