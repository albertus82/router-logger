package it.albertus.router.writer;

import it.albertus.router.RouterLogger;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.util.Console;
import it.albertus.util.SystemConsole;

public abstract class Writer implements IWriter {

	protected static final RouterLoggerConfiguration configuration = RouterLogger.getConfiguration();

	protected final Console out = SystemConsole.getInstance();

}
