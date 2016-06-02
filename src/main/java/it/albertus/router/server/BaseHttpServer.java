package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.util.Configuration;
import it.albertus.util.ExceptionUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.Set;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

public abstract class BaseHttpServer {

	public interface Defaults {
		int PORT = 8080;
		boolean ENABLED = false;
		boolean HTTPS_ENABLED = false;
	}

	protected static final String SSL_PROTOCOL = "TLS";
	protected static final String KEYSTORE_TYPE = "JKS";
	protected static final String SECURITY_ALGORITHM = "SunX509";
	protected static final int STOP_DELAY = 0;

	protected final Configuration configuration = RouterLoggerConfiguration.getInstance();
	protected final Authenticator authenticator = new WebServerAuthenticator();
	protected volatile HttpServer httpServer;
	protected volatile boolean started = false;

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
			httpServer.createContext(handler.getPath(), handler).setAuthenticator(authenticator);
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
					if (configuration.getBoolean("server.https.enabled", Defaults.HTTPS_ENABLED)) {
						final SSLContext sslContext = SSLContext.getInstance(SSL_PROTOCOL);

						final char[] storepass = configuration.getCharArray("server.https.storepass"); // Keystore password
						final KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
						// keytool -genkey -alias "myalias" -keyalg "RSA" -keypass "mykeypass" -keystore "mykeystore.ks" -storepass "mystorepass" -validity 360
						final InputStream bis = new BufferedInputStream(new FileInputStream(configuration.getString("server.https.keystore.file")));
						keyStore.load(bis, storepass);
						bis.close();

						final char[] keypass = configuration.getCharArray("server.https.keypass"); // Key password
						final KeyManagerFactory kmf = KeyManagerFactory.getInstance(SECURITY_ALGORITHM);
						kmf.init(keyStore, keypass);

						final TrustManagerFactory tmf = TrustManagerFactory.getInstance(SECURITY_ALGORITHM);
						tmf.init(keyStore);

						sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
						final HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext) {
							@Override
							public void configure(final HttpsParameters params) {
								try {
									final SSLContext sslContext = SSLContext.getDefault();
									final SSLEngine sslEngine = sslContext.createSSLEngine();
									params.setNeedClientAuth(false);
									params.setCipherSuites(sslEngine.getEnabledCipherSuites());
									params.setProtocols(sslEngine.getEnabledProtocols());

									final SSLParameters defaultSSLParameters = sslContext.getDefaultSSLParameters();
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
