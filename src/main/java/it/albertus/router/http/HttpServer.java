package it.albertus.router.http;

import java.util.HashSet;
import java.util.Set;

import it.albertus.httpserver.AbstractHttpHandler;
import it.albertus.httpserver.AbstractHttpServer;
import it.albertus.httpserver.StaticResourceHandler;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.http.html.CloseHandler;
import it.albertus.router.http.html.ConfigurationHandler;
import it.albertus.router.http.html.ConnectHandler;
import it.albertus.router.http.html.DisconnectHandler;
import it.albertus.router.http.html.LogsHandler;
import it.albertus.router.http.html.RestartHandler;
import it.albertus.router.http.html.RootHtmlHandler;
import it.albertus.router.http.html.StatusHtmlHandler;
import it.albertus.router.http.json.DataJsonHandler;
import it.albertus.router.http.json.StatusJsonHandler;
import it.albertus.router.http.json.ThresholdsJsonHandler;

public class HttpServer extends AbstractHttpServer {

	private static final String BASE_PATH = '/' + HttpServer.class.getPackage().getName().toLowerCase().replace('.', '/');

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
	protected Set<AbstractHttpHandler> createHandlers() {
		final Set<AbstractHttpHandler> handlers = new HashSet<AbstractHttpHandler>();

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
		handlers.add(new StaticResourceHandler(BASE_PATH + "/html/favicon.ico", "/favicon.ico"));
		handlers.add(new StaticResourceHandler(BASE_PATH + "/html/js/jquery.min.js", "/js/jquery.min.js"));
		handlers.add(new StaticResourceHandler(BASE_PATH + "/html/js/bootstrap.min.js", "/js/bootstrap.min.js"));
		handlers.add(new StaticResourceHandler(BASE_PATH + "/html/css/bootstrap.min.css", "/css/bootstrap.min.css"));
		handlers.add(new StaticResourceHandler(BASE_PATH + "/html/css/bootstrap-theme.min.css", "/css/bootstrap-theme.min.css"));

		return handlers;
	}

}
