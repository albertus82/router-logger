package it.albertus.router.server;

import java.util.HashSet;
import java.util.Set;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.server.html.CloseHandler;
import it.albertus.router.server.html.ConnectHandler;
import it.albertus.router.server.html.DisconnectHandler;
import it.albertus.router.server.html.RestartHandler;
import it.albertus.router.server.html.RootHtmlHandler;
import it.albertus.router.server.html.StatusHtmlHandler;
import it.albertus.router.server.json.DataJsonHandler;
import it.albertus.router.server.json.StatusJsonHandler;
import it.albertus.router.server.json.ThresholdsJsonHandler;

public class WebServer extends BaseHttpServer {

	static {
//		System.setProperty("sun.net.httpserver.clockTick", "1");
//		System.setProperty("sun.net.httpserver.timerMillis", "1");
		System.setProperty("sun.net.httpserver.maxReqTime", "1");
//		System.setProperty("sun.net.httpserver.maxRspTime", "1");
//		System.getProperty("sun.net.httpserver.debug", Boolean.TRUE.toString());
	}

	private static class Singleton {
		private static final WebServer instance = new WebServer();
	}

	public static WebServer getInstance() {
		return Singleton.instance;
	}

	private WebServer() {}

	private RouterLoggerEngine engine;

	public void init(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	@Override
	protected Set<BaseHttpHandler> createHandlers() {
		final Set<BaseHttpHandler> handlers = new HashSet<BaseHttpHandler>();

		// HTML
		handlers.add(new RootHtmlHandler(engine));
		handlers.add(new StatusHtmlHandler(engine));
		handlers.add(new RestartHandler(engine));
		handlers.add(new DisconnectHandler(engine));
		handlers.add(new ConnectHandler(engine));
		handlers.add(new CloseHandler(engine));

		// JSON
		handlers.add(new DataJsonHandler(engine));
		handlers.add(new StatusJsonHandler(engine));
		handlers.add(new ThresholdsJsonHandler(engine));

		// Static resources
		handlers.add(new FaviconHandler());

		return handlers;
	}

}
