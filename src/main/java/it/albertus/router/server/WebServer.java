package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.util.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpServer;

public class WebServer {

	public interface Defaults {
		int PORT = 8080;
		boolean ENABLED = false;
	}

	private static class Singleton {
		private static final WebServer instance = new WebServer();
	}

	public static WebServer getInstance() {
		return Singleton.instance;
	}

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();
	private final Authenticator authenticator = new WebServerAuthenticator();
	private volatile HttpServer httpServer;
	private RouterLoggerEngine engine;
	private volatile boolean started = false;

	private final Object lock = new Object();

	public void init(final RouterLoggerEngine engine) {
		this.engine = engine;
	}

	public void start() {
		if (!started && configuration.getBoolean("server.enabled", Defaults.ENABLED)) {
			new HttpServerStartThread().start();
		}
	}

	public void stop() {
		if (httpServer != null) {
			synchronized (lock) {
				try {
					httpServer.stop(0);
				}
				catch (final Exception exception) {
					Logger.getInstance().log(exception);
				}
				started = false;
			}
		}
	}

	public boolean isStarted() {
		return started;
	}

	private void createContexts() {
		for (final BaseHttpHandler handler : createHandlers()) {
			httpServer.createContext(handler.getPath(), handler).setAuthenticator(authenticator);
		}
	}

	/**
	 * Creates {@code BaseHttpHandler} objects.
	 * 
	 * @return the {@code Set} containing the handlers.
	 */
	private Set<BaseHttpHandler> createHandlers() {
		final Set<BaseHttpHandler> handlers = new HashSet<BaseHttpHandler>();
		handlers.add(new RootHandler(engine));
		handlers.add(new StatusHandler(engine));
		handlers.add(new RestartHandler(engine));
		handlers.add(new DisconnectHandler(engine));
		handlers.add(new ConnectHandler(engine));
		return handlers;
	}

	private class HttpServerStartThread extends Thread {
		public HttpServerStartThread() {
			this.setName("httpServerStartThread");
			this.setDaemon(true);
		}

		@Override
		public void run() {
			final int port = configuration.getInt("server.port", Defaults.PORT);
			final InetSocketAddress address = new InetSocketAddress(port);
			try {
				synchronized (lock) {
					httpServer = HttpServer.create(address, 0);
					createContexts();
					httpServer.start();
					started = true;
				}
			}
			catch (final IOException ioe) {
				Logger.getInstance().log(new RuntimeException(Resources.get("err.server.start", port), ioe));
			}
		}
	}

}
