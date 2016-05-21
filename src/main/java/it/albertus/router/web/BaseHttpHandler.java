package it.albertus.router.web;

import it.albertus.router.engine.RouterLoggerEngine;

import java.nio.charset.Charset;

import com.sun.net.httpserver.HttpHandler;

public abstract class BaseHttpHandler implements HttpHandler {

	public static final String PREFERRED_CHARSET = "UTF-8";

	protected final RouterLoggerEngine engine;

	protected BaseHttpHandler(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	public abstract String getPath();

	protected Charset getCharset() {
		try {
			return Charset.forName(PREFERRED_CHARSET);
		}
		catch (final Exception e) {
			return Charset.defaultCharset();
		}
	}

}
