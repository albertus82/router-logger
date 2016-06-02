package it.albertus.router.server;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.util.Configuration;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
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

// Linux: keytool -genkey -alias alias -keypass PASSWORD -keystore lig.keystore -storepass PASSWORD
// Windows: keytool -genkey -keyalg RSA -alias selfsigned -keystore testkey.jks -storepass PASSWORD -validity 360 -keysize 2048
public abstract class BaseHttpServer {

	public interface Defaults {
		int PORT = 8080;
		boolean ENABLED = false;
		boolean HTTPS_ENABLED = false;
	}

	protected static final String SSL_PROTOCOL = "TLS";
	protected static final String KEYSTORE_TYPE = "JKS";
	protected static final String SECURITY_ALGORITHM = "SunX509";

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
						final HttpsServer httpsServer = HttpsServer.create(address, 0);
						final SSLContext sslContext = SSLContext.getInstance(SSL_PROTOCOL);

						// initialise the keystore
						final char[] storepass = configuration.getCharArray("server.https.storepass"); // Keystore password
						final KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
						final InputStream fis = new BufferedInputStream(new FileInputStream(configuration.getString("server.https.keystore.file")));
						ks.load(fis, storepass);
						fis.close();

						// setup the key manager factory
						final char[] keypass = configuration.getCharArray("server.https.keypass"); // Key password
						final KeyManagerFactory kmf = KeyManagerFactory.getInstance(SECURITY_ALGORITHM);
						kmf.init(ks, keypass);

						// setup the trust manager factory
						final TrustManagerFactory tmf = TrustManagerFactory.getInstance(SECURITY_ALGORITHM);
						tmf.init(ks);

						// setup the HTTPS context and parameters
						sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
						httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
							@Override
							public void configure(final HttpsParameters params) {
								try {
									// initialise the SSL context
									final SSLContext c = SSLContext.getDefault();
									final SSLEngine engine = c.createSSLEngine();
									params.setNeedClientAuth(false);
									params.setCipherSuites(engine.getEnabledCipherSuites());
									params.setProtocols(engine.getEnabledProtocols());

									// get the default parameters
									final SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
									params.setSSLParameters(defaultSSLParameters);
								}
								catch (final Exception e) {
									Logger.getInstance().log(Resources.get("err.server.start", e.getLocalizedMessage()));
								}
							}
						});
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
			catch (final Exception e) {
				Logger.getInstance().log(Resources.get("err.server.start", e.getLocalizedMessage()));
			}
		}
	}

}
