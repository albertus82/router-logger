package it.albertus.router.web;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.util.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpServer;

public class Server {

	public interface Defaults {
		int PORT = 8081;
	}

	private static class Singleton {
		private static final Server instance = new Server();
	}

	public static Server getInstance() {
		return Singleton.instance;
	}

	private final Configuration configuration = RouterLoggerConfiguration.getInstance();
	private final Thread httpServerThread = new HttpServerThread();
	private final Authenticator authenticator = new ServerAuthenticator();
	private HttpServer httpServer;
	private RouterLoggerEngine engine;

	public void init(final RouterLoggerEngine engine) {
		this.engine = engine;

		final int port = configuration.getInt("server.port", Defaults.PORT);
		final InetSocketAddress address = new InetSocketAddress(port);
		try {
			httpServer = HttpServer.create(address, 0);
		}
		catch (final IOException ioe) {
			throw new RuntimeException("Impossibile avviare il server HTTP. Verificare che la porta "+ port + " non sia occupata.", ioe);
		}

		createContexts();

		httpServerThread.start();
	}

	public void destroy() {
		httpServer.stop(0);
	}

	private void createContexts() {
		final StatusHandler statusHandler = new StatusHandler(engine);
		httpServer.createContext(statusHandler.getPath(), statusHandler).setAuthenticator(authenticator);
	}

	private class HttpServerThread extends Thread {
		public HttpServerThread() {
			this.setName("httpServerThread");
			this.setDaemon(true);
		}

		@Override
		public void run() {
			httpServer.start();
		}
	}

}
