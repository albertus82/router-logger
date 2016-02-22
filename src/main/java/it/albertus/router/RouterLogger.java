package it.albertus.router;

public class RouterLogger {

	/** Unique entry point */
	public static void main(String args[]) {
		if (args.length != 0) {
			RouterLoggerCon.start(args);
		}
		else {
			RouterLoggerGui.start();
		}
	}

}
