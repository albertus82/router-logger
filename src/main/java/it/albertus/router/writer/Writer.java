package it.albertus.router.writer;

import it.albertus.router.RouterLogger;
import it.albertus.router.engine.RouterLoggerConfiguration;

public abstract class Writer implements IWriter {

	protected static final RouterLoggerConfiguration configuration = RouterLogger.getConfiguration();

}
