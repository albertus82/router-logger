package it.albertus.router.gui.preference.page;

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
		final int length = keyManagerFactoryAlgorithms.size();
		final String[][] options = new String[length][];
		int index = 0;
		for (final String algorithm : keyManagerFactoryAlgorithms) {
			options[index++] = new String[] { algorithm, algorithm };
		}
		return options;
	}

	public static String[][] getTrustManagerFactoryComboOptions() {
		final int length = trustManagerFactoryAlgorithms.size();
		final String[][] options = new String[length][];
		int index = 0;
		for (final String algorithm : trustManagerFactoryAlgorithms) {
			options[index++] = new String[] { algorithm, algorithm };
		}
		return options;
	}

	public static String[][] getKeyStoreAlgorithmsComboOptions() {
		final int length = keyStoreAlgorithms.size();
		final String[][] options = new String[length][];
		int index = 0;
		for (final String algorithm : keyStoreAlgorithms) {
			options[index++] = new String[] { algorithm, algorithm };
		}
		return options;
	}

	public static String[][] getSslContextAlgorithmsComboOptions() {
		final int length = sslContextAlgorithms.size();
		final String[][] options = new String[length][];
		int index = 0;
		for (final String algorithm : sslContextAlgorithms) {
			options[index++] = new String[] { algorithm, algorithm };
		}
		return options;
	}

}
