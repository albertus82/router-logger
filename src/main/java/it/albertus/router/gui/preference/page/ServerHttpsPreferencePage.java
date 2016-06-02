package it.albertus.router.gui.preference.page;

import it.albertus.router.server.BaseHttpServer;

import java.security.KeyStore;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class ServerHttpsPreferencePage extends ServerPreferencePage {

	private static final Set<String> keyManagerFactoryAlgorithms = new TreeSet<String>();
	private static final Set<String> trustManagerFactoryAlgorithms = new TreeSet<String>();
	private static final Set<String> keyStoreAlgorithms = new TreeSet<String>();
	private static final Set<String> sslContextAlgorithms = new TreeSet<String>();

	static {
		keyStoreAlgorithms.add(BaseHttpServer.Defaults.SSL_KEYSTORE_TYPE);
		sslContextAlgorithms.add(BaseHttpServer.Defaults.SSL_PROTOCOL);

		final String keyManagerFactoryClassName = KeyManagerFactory.class.getSimpleName();
		final String trustManagerFactoryClassName = TrustManagerFactory.class.getSimpleName();
		final String keyStoreClassName = KeyStore.class.getSimpleName();
		final String sslContextClassName = SSLContext.class.getSimpleName();

		for (final Provider provider : Security.getProviders()) {
			for (final Service service : provider.getServices()) {
				if (keyManagerFactoryClassName.equals(service.getType())) {
					keyManagerFactoryAlgorithms.add(service.getAlgorithm());
				}
				if (trustManagerFactoryClassName.equals(service.getType())) {
					trustManagerFactoryAlgorithms.add(service.getAlgorithm());
				}
				if (keyStoreClassName.equals(service.getType())) {
					keyStoreAlgorithms.add(service.getAlgorithm());
				}
				if (sslContextClassName.equals(service.getType())) {
					sslContextAlgorithms.add(service.getAlgorithm());
				}
			}
		}
	}

	public static String[][] getKeyManagerFactoryComboOptions() {
		final String[][] options = new String[keyManagerFactoryAlgorithms.size()][];
		int index = 0;
		for (final String algorithm : keyManagerFactoryAlgorithms) {
			options[index++] = new String[] { algorithm, algorithm };
		}
		return options;
	}

	public static String[][] getTrustManagerFactoryComboOptions() {
		final String[][] options = new String[trustManagerFactoryAlgorithms.size()][];
		int index = 0;
		for (final String algorithm : trustManagerFactoryAlgorithms) {
			options[index++] = new String[] { algorithm, algorithm };
		}
		return options;
	}

	public static String[][] getKeyStoreAlgorithmsComboOptions() {
		final String[][] options = new String[keyStoreAlgorithms.size()][];
		int index = 0;
		for (final String algorithm : keyStoreAlgorithms) {
			options[index++] = new String[] { algorithm, algorithm };
		}
		return options;
	}

	public static String[][] getSslContextAlgorithmsComboOptions() {
		final String[][] options = new String[sslContextAlgorithms.size()][];
		int index = 0;
		for (final String algorithm : sslContextAlgorithms) {
			options[index++] = new String[] { algorithm, algorithm };
		}
		return options;
	}

}
