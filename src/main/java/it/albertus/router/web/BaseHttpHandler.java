package it.albertus.router.web;

import java.nio.charset.Charset;

import it.albertus.router.engine.RouterLoggerEngine;

import com.sun.net.httpserver.HttpHandler;

public abstract class BaseHttpHandler implements HttpHandler {

	protected final RouterLoggerEngine engine;

	protected BaseHttpHandler(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	public abstract String getPath();

	protected Charset getCharset() {
		try {
			return Charset.forName("UTF-8");
		}
		catch (final Exception e) {
			return Charset.defaultCharset();
		}
	}

}
