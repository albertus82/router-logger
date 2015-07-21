package it.albertus.router;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.util.TerminalConsole;

import java.util.Map;

public class RouterLoggerCon extends RouterLoggerEngine {

	private static final char[] ANIMATION = { '-', '\\', '|', '/' };

	public static void main(String args[]) {
		new RouterLoggerCon().run();
	}

	@Override
	protected int log(Map<String, String> info, int iteration, int lastLogLength, int iterations) {
		// Scrittura indice dell'iterazione in console...
		final StringBuilder clean = new StringBuilder();
		while (lastLogLength-- > 0) {
			clean.append('\b').append(' ').append('\b');
		}
		final StringBuilder log = new StringBuilder();
		final boolean animate = configuration.getBoolean("console.animation", Defaults.CONSOLE_ANIMATION);
		if (animate) {
			log.append(ANIMATION[iteration & 3]).append(' ');
		}
		log.append(iteration);
		if (iterations != Integer.MAX_VALUE) {
			log.append('/').append(iterations);
		}
		log.append(' ');
		if (animate) {
			log.append(ANIMATION[iteration & 3]).append(' ');
		}
		// Fine scrittura indice.

		// Scrittura informazioni aggiuntive richieste...
		if (info != null && !info.isEmpty()) {
			final StringBuilder infoToShow = new StringBuilder();
			for (String keyToShow : configuration.getString("console.show.keys", "").split(configuration.getString("console.show.keys.separator", Defaults.CONSOLE_SHOW_KEYS_SEPARATOR).trim())) {
				if (keyToShow != null && !"".equals(keyToShow.trim())) {
					keyToShow = keyToShow.trim();
					for (final String key : info.keySet()) {
						if (key != null && key.trim().equals(keyToShow)) {
							if (infoToShow.length() == 0) {
								infoToShow.append('[');
							}
							else {
								infoToShow.append(", ");
							}
							infoToShow.append(keyToShow + ": " + info.get(key));
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
		return lastLogLength;
	}

	@Override
	protected TerminalConsole getConsole() {
		return TerminalConsole.getInstance();
	}

}
