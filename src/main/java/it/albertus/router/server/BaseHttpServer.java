package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.Configuration;
import it.albertus.util.DaemonThreadFactory;
import it.albertus.util.ExceptionUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

public abstract class BaseHttpServer {

	public interface Defaults {
		int PORT = 8080;
		boolean ENABLED = false;
		boolean AUTHENTICATION = true;
		byte THREADS = 1;
		boolean SSL_ENABLED = false;
		String SSL_KEYSTORE_TYPE = "JKS";
		String SSL_PROTOCOL = "TLS";
		String SSL_KMF_ALGORITHM = KeyManagerFactory.getDefaultAlgorithm();
		String SSL_TMF_ALGORITHM = TrustManagerFactory.getDefaultAlgorithm();
	}

	protected static final int STOP_DELAY = 0;

	protected final Configuration configuration = RouterLoggerConfiguration.getInstance();
	protected final Authenticator authenticator = new WebServerAuthenticator();
	protected volatile HttpServer httpServer;
	protected volatile boolean started = false;
	protected volatile ExecutorService threadPool;

	private final Object lock = new Object();

	public void start() {
		if (!started && configuration.getBoolean("server.enabled", Defaults.ENABLED)) {
			new HttpServerStartThread().start();
		}
	}

	public void stop() {
		if (httpServer != null) {
			synchronized (lock) {
				try {
					httpServer.stop(STOP_DELAY);
					if (threadPool != null && !threadPool.isShutdown()) {
						try {
							threadPool.shutdown();
						}
						catch (final Exception exception) {
							Logger.getInstance().log(exception);
						}
					}
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

	protected void createContexts() {
		for (final BaseHttpHandler handler : createHandlers()) {
			final HttpContext httpContext = httpServer.createContext(handler.getPath(), handler);
			if (configuration.getBoolean("server.authentication", Defaults.AUTHENTICATION)) {
				httpContext.setAuthenticator(authenticator);
			}
		}
	}

	/**
	 * Creates {@code BaseHttpHandler} objects.
	 * 
	 * @return the {@code Set} containing the handlers.
	 */
	protected abstract Set<BaseHttpHandler> createHandlers();

	protected class HttpServerStartThread extends Thread {

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
					if (configuration.getBoolean("server.ssl.enabled", Defaults.SSL_ENABLED)) {
						final char[] storepass = configuration.getCharArray("server.ssl.storepass");
						final KeyStore keyStore = KeyStore.getInstance(configuration.getString("server.ssl.keystore.type", Defaults.SSL_KEYSTORE_TYPE));
						// keytool -genkey -alias "myalias" -keyalg "RSA" -keypass "mykeypass" -keystore "mykeystore.jks" -storepass "mystorepass" -validity 360
						final InputStream bis = new BufferedInputStream(new FileInputStream(configuration.getString("server.ssl.keystore.file", "")));
						keyStore.load(bis, storepass);
						bis.close();

						final char[] keypass = configuration.getCharArray("server.ssl.keypass");
						final KeyManagerFactory kmf = KeyManagerFactory.getInstance(configuration.getString("server.ssl.kmf.algorithm", Defaults.SSL_KMF_ALGORITHM));
						kmf.init(keyStore, keypass);

						final TrustManagerFactory tmf = TrustManagerFactory.getInstance(configuration.getString("server.ssl.tmf.algorithm", Defaults.SSL_TMF_ALGORITHM));
						tmf.init(keyStore);

						final SSLContext sslContext = SSLContext.getInstance(configuration.getString("server.ssl.protocol", Defaults.SSL_PROTOCOL));
						sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
						final HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext) {
							@Override
							public void configure(final HttpsParameters params) {
								try {
									final SSLEngine sslEngine = getSSLContext().createSSLEngine();
									params.setNeedClientAuth(false);
									params.setCipherSuites(sslEngine.getEnabledCipherSuites());
									params.setProtocols(sslEngine.getEnabledProtocols());

									final SSLParameters defaultSSLParameters = getSSLContext().getDefaultSSLParameters();
									params.setSSLParameters(defaultSSLParameters);
								}
								catch (final Exception e) {
									Logger.getInstance().log(e);
								}
							}
						};

						final HttpsServer httpsServer = HttpsServer.create(address, 0);
						httpsServer.setHttpsConfigurator(httpsConfigurator);
						httpServer = httpsServer;
					}
					else {
						httpServer = HttpServer.create(address, 0);
					}
					createContexts();

					final byte threads = configuration.getByte("server.threads", Defaults.THREADS);
					if (threads > 1) {
						threadPool = Executors.newFixedThreadPool(threads, new DaemonThreadFactory());
						httpServer.setExecutor(threadPool);
					}

					httpServer.start();
					started = true;
				}
			}
			catch (final BindException be) {
				Logger.getInstance().log(new BindException(Resources.get("err.server.start.port", port)), Destination.CONSOLE, Destination.FILE);
				Logger.getInstance().log(be, Destination.FILE, Destination.EMAIL);
			}
			catch (final FileNotFoundException fnfe) {
				Logger.getInstance().log(new FileNotFoundException(Resources.get("err.server.start.keystore.file")), Destination.CONSOLE, Destination.FILE);
				Logger.getInstance().log(fnfe, Destination.FILE, Destination.EMAIL);
			}
			catch (final Exception e) {
				Logger.getInstance().log(new Exception(Resources.get("err.server.start", ExceptionUtils.getUIMessage(e))), Destination.CONSOLE, Destination.FILE);
				Logger.getInstance().log(e, Destination.FILE, Destination.EMAIL);
			}
		}
	}

}
