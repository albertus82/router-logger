package it.albertus.routerlogger.writer;

import it.albertus.routerlogger.engine.RouterLoggerConfig;

public abstract class Writer implements IWriter {

	protected static final RouterLoggerConfig configuration = RouterLoggerConfig.getInstance();

}
