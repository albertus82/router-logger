package it.albertus.routerlogger.writer;

import it.albertus.routerlogger.engine.RouterLoggerConfig;

public abstract class Writer implements IWriter {

	protected static final RouterLoggerConfig configuration = RouterLoggerConfig.getInstance();

//	@Override
//	public boolean equals(final Object obj) {
//		return obj == null ? false : getClass().equals(obj.getClass());
//	}
//
//	@Override
//	public int hashCode() {
//		return getClass().hashCode();
//	}

}
