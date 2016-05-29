package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerEngine;

import java.util.HashSet;
import java.util.Set;

import com.sun.net.httpserver.Headers;

public class WebServer extends BaseHttpServer {

	private static class Singleton {
		private static final WebServer instance = new WebServer();
	}

	public static WebServer getInstance() {
		return Singleton.instance;
	}

	private RouterLoggerEngine engine;

	public void init(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	@Override
	protected Set<BaseHttpHandler> createHandlers() {
		final Set<BaseHttpHandler> handlers = new HashSet<BaseHttpHandler>();
		handlers.add(new RootHandler(engine));
		handlers.add(new StatusHandler(engine));
		handlers.add(new RestartHandler(engine));
		handlers.add(new DisconnectHandler(engine));
		handlers.add(new ConnectHandler(engine));

		// Static resources
		final Headers faviconHeaders = new Headers();
		faviconHeaders.add("Content-Type", "image/x-icon");
		faviconHeaders.add("Cache-Control", "no-transform,public,max-age=86400,s-maxage=259200");
		handlers.add(new StaticResourceHandler("/favicon.ico", "favicon.ico", faviconHeaders));

		return handlers;
	}

}
