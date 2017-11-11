package it.albertus.routerlogger.http;

import it.albertus.net.httpserver.HttpPathHandler;
import it.albertus.net.httpserver.LightweightHttpServer;
import it.albertus.net.httpserver.config.HttpServerDefaultConfig;
import it.albertus.routerlogger.engine.RouterLoggerEngine;

public class HttpServer {

	private final HttpServerConfig httpServerConfig;

	public HttpServer(final RouterLoggerEngine engine) {
		this.httpServerConfig = new HttpServerConfig(engine);
	}

	private LightweightHttpServer mainServer;
	private LightweightHttpServer redirectionServer;

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
			redirectionServer = new LightweightHttpServer(new HttpServerDefaultConfig() {
				@Override
				public boolean isEnabled() {
					return httpServerConfig.isEnabled() && httpServerConfig.isSslEnabled() && httpServerConfig.isSslRedirectionEnabled();
				}

				@Override
				public HttpPathHandler[] getHandlers() {
					return new HttpPathHandler[] { new HttpsRedirectionHandler(httpServerConfig) };
				}

				@Override
				public int getPort() {
					return httpServerConfig.getSslRedirectionListeningPort();
				}
			});
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
