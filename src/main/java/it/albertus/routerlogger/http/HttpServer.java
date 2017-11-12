package it.albertus.routerlogger.http;

import it.albertus.net.httpserver.HttpPathHandler;
import it.albertus.net.httpserver.LightweightHttpServer;
import it.albertus.net.httpserver.config.HttpServerDefaultConfig;
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
			redirectionServer = new LightweightHttpServer(new RedirectionServerConfig());
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

	private class RedirectionServerConfig extends HttpServerDefaultConfig {

		@Override
		public HttpPathHandler[] getHandlers() {
			return new HttpPathHandler[] { new HttpsRedirectionHandler(httpServerConfig) };
		}

		@Override
		public long getMaxReqTime() {
			return httpServerConfig.getMaxReqTime();
		}

		@Override
		public long getMaxRspTime() {
			return httpServerConfig.getMaxRspTime();
		}

		@Override
		public int getMaxThreadCount() {
			return 2;
		}

		@Override
		public int getMinThreadCount() {
			return 1;
		}

		@Override
		public int getPort() {
			return httpServerConfig.getSslRedirectionListeningPort();
		}

		@Override
		public boolean isEnabled() {
			return httpServerConfig.isEnabled() && httpServerConfig.isSslEnabled() && httpServerConfig.isSslRedirectionEnabled();
		}
	}

}
