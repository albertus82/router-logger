package it.albertus.router.server;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Messages;
import it.albertus.util.Configuration;
import it.albertus.util.Configured;
import it.albertus.util.DaemonThreadFactory;
import it.albertus.util.IOUtils;
import it.albertus.util.logging.LoggerFactory;

public abstract class BaseHttpServer {

	private static final Logger logger = LoggerFactory.getLogger(BaseHttpServer.class);

	public static final String PASSWORD_HASH_ALGORITHM = "SHA-256";

	private static final String CFG_KEY_SERVER_USERNAME = "server.username";
	private static final String CFG_KEY_SERVER_PASSWORD = "server.password";

	protected static final Configuration configuration = RouterLoggerConfiguration.getInstance();

	public static class Defaults {
		public static final int PORT = 8080;
		public static final boolean ENABLED = false;
		public static final boolean AUTHENTICATION = true;
		public static final byte THREADS = 2;
		public static final boolean SSL_ENABLED = false;
		public static final String SSL_KEYSTORE_TYPE = "JKS";
		public static final String SSL_PROTOCOL = "TLS";
		public static final String SSL_KMF_ALGORITHM = KeyManagerFactory.getDefaultAlgorithm();
		public static final String SSL_TMF_ALGORITHM = TrustManagerFactory.getDefaultAlgorithm();
		public static final short MAX_REQ_TIME = 10; // seconds
		public static final short MAX_RSP_TIME = 600; // seconds

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	protected static final int STOP_DELAY = 0;

	protected volatile HttpServer httpServer;
	protected volatile boolean running = false;
	protected volatile ExecutorService threadPool;

	private final Object lock = new Object();

	public void start() {
		if (!running && configuration.getBoolean("server.enabled", Defaults.ENABLED)) {
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
						catch (final Exception e) {
							logger.log(Level.SEVERE, e.toString(), e);
						}
					}
				}
				catch (final Exception e) {
					logger.log(Level.SEVERE, e.toString(), e);
				}
				running = false;
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	protected void createContexts() {
		final Authenticator authenticator;
		if (configuration.getBoolean("server.authentication", Defaults.AUTHENTICATION)) {
			try {
				final Configured<String> username = new Configured<String>() {
					@Override
					public String getValue() {
						return configuration.getString(CFG_KEY_SERVER_USERNAME);
					}
				};
				final Configured<char[]> password = new Configured<char[]>() {
					@Override
					public char[] getValue() {
						return configuration.getCharArray(CFG_KEY_SERVER_PASSWORD);
					}
				};
				authenticator = new WebAuthenticator(username, password, PASSWORD_HASH_ALGORITHM);
			}
			catch (final NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			authenticator = null;
		}

		for (final BaseHttpHandler handler : createHandlers()) {
			final HttpContext httpContext = httpServer.createContext(BaseHttpHandler.getPath(handler.getClass()), handler);
			if (authenticator != null) {
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
					// Avoid server starvation
					System.setProperty("sun.net.httpserver.maxReqTime", Short.toString(configuration.getShort("server.maxreqtime", Defaults.MAX_REQ_TIME)));
					System.setProperty("sun.net.httpserver.maxRspTime", Short.toString(configuration.getShort("server.maxrsptime", Defaults.MAX_RSP_TIME)));

					if (configuration.getBoolean("server.ssl.enabled", Defaults.SSL_ENABLED)) {
						final char[] storepass = configuration.getCharArray("server.ssl.storepass");
						final KeyStore keyStore = KeyStore.getInstance(configuration.getString("server.ssl.keystore.type", Defaults.SSL_KEYSTORE_TYPE));
						// keytool -genkey -alias "myalias" -keyalg "RSA" -keypass "mykeypass" -keystore "mykeystore.jks" -storepass "mystorepass" -validity 360
						FileInputStream fis = null;
						BufferedInputStream bis = null;
						try {
							fis = new FileInputStream(configuration.getString("server.ssl.keystore.file", true));
							bis = new BufferedInputStream(fis);
							keyStore.load(bis, storepass);
						}
						finally {
							IOUtils.closeQuietly(bis, fis);
						}

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
									logger.log(Level.SEVERE, e.toString(), e);
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
					running = true;
				}
			}
			catch (final BindException e) {
				logger.log(Level.SEVERE, Messages.get("err.server.start.port", port), e);
			}
			catch (final FileNotFoundException e) {
				logger.log(Level.SEVERE, Messages.get("err.server.start.keystore.file"), e);
			}
			catch (final Exception e) {
				logger.log(Level.SEVERE, Messages.get("err.server.start"), e);
			}
		}
	}

}
