package it.albertus.routerlogger.http;

import it.albertus.net.httpserver.LightweightHttpServer;
import it.albertus.routerlogger.engine.RouterLoggerEngine;

public class HttpServer {

	private final HttpServerConfig httpServerConfig;

	private LightweightHttpServer mainServer;
	private LightweightHttpServer redirectionServer;

	public HttpServer(final RouterLoggerEngine engine) {
		this.httpServerConfig = new HttpServerConfig(engine);
	}

	public synchronized void start() {
		startMainServer();
		startRedirectionServer();
	}

	public void stop() {
		stopRedirectionServer();
		stopMainServer();
	}

	private void startMainServer() {
		if (mainServer == null) {
			mainServer = new LightweightHttpServer(httpServerConfig);
		}
		mainServer.start();
	}

	private void startRedirectionServer() {
		if (redirectionServer == null) {
			redirectionServer = new LightweightHttpServer(new RedirectionServerConfig(httpServerConfig));
		}

		redirectionServer.start();
	}

	private void stopMainServer() {
		if (mainServer != null) {
			mainServer.stop();
		}
	}

	private void stopRedirectionServer() {
		if (redirectionServer != null) {
			redirectionServer.stop();
		}
	}

}
