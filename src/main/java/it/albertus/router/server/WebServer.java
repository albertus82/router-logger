package it.albertus.router.server;

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

import java.util.HashSet;
import java.util.Set;

public class WebServer extends BaseHttpServer {

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
