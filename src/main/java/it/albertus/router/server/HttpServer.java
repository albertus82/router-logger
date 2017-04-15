package it.albertus.router.server;

import java.util.HashSet;
import java.util.Set;

import com.sun.net.httpserver.HttpHandler;

import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.server.html.CloseHandler;
import it.albertus.router.server.html.ConfigurationHandler;
import it.albertus.router.server.html.ConnectHandler;
import it.albertus.router.server.html.DisconnectHandler;
import it.albertus.router.server.html.LogsHandler;
import it.albertus.router.server.html.RestartHandler;
import it.albertus.router.server.html.RootHtmlHandler;
import it.albertus.router.server.html.StatusHtmlHandler;
import it.albertus.router.server.json.DataJsonHandler;
import it.albertus.router.server.json.StatusJsonHandler;
import it.albertus.router.server.json.ThresholdsJsonHandler;

public class HttpServer extends AbstractHttpServer {

	private static class Singleton {
		private static final HttpServer instance = new HttpServer();

		private Singleton() {
			throw new IllegalAccessError();
		}
	}

	private RouterLoggerEngine engine;

	private HttpServer() {
		super(new HttpServerConfiguration());
	}

	public static HttpServer getInstance() {
		return Singleton.instance;
	}

	public void init(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	@Override
	protected Set<HttpHandler> createHandlers() {
		final Set<HttpHandler> handlers = new HashSet<HttpHandler>();

		// HTML
		handlers.add(new RootHtmlHandler());
		handlers.add(new StatusHtmlHandler(engine));
		handlers.add(new RestartHandler(engine));
		handlers.add(new DisconnectHandler(engine));
		handlers.add(new ConnectHandler(engine));
		handlers.add(new CloseHandler(engine));
		handlers.add(new LogsHandler());
		handlers.add(new ConfigurationHandler());

		// JSON
		handlers.add(new DataJsonHandler(engine));
		handlers.add(new StatusJsonHandler(engine));
		handlers.add(new ThresholdsJsonHandler(engine));

		// Static resources
		handlers.add(new FaviconHandler());

		return handlers;
	}

}
