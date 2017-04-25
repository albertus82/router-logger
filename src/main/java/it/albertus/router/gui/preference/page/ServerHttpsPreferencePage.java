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

import it.albertus.httpserver.DefaultHttpServerConfiguration;
import it.albertus.jface.preference.StaticLabelsAndValues;
import it.albertus.jface.preference.page.RestartHeaderPreferencePage;

public class ServerHttpsPreferencePage extends RestartHeaderPreferencePage {

	private static final String[] KEY_STORE_FILE_EXTENSIONS = { "*.JKS;*.jks", "*.P12;*.p12;*.PFX;*.pfx", "*.*" };

	private static final Set<String> keyManagerFactoryAlgorithms = new TreeSet<String>();
	private static final Set<String> trustManagerFactoryAlgorithms = new TreeSet<String>();
	private static final Set<String> keyStoreAlgorithms = new TreeSet<String>();
	private static final Set<String> sslContextAlgorithms = new TreeSet<String>();

	static {
		keyStoreAlgorithms.add(DefaultHttpServerConfiguration.Defaults.SSL_KEYSTORE_TYPE);
		sslContextAlgorithms.add(DefaultHttpServerConfiguration.Defaults.SSL_PROTOCOL);

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
				if (sslContextClassName.equals(service.getType()) && !"Default".equals(service.getAlgorithm())) {
					sslContextAlgorithms.add(service.getAlgorithm());
				}
			}
		}
	}

	public static StaticLabelsAndValues getKeyManagerFactoryComboOptions() {
		return buildComboOptionsArray(keyManagerFactoryAlgorithms);
	}

	public static StaticLabelsAndValues getTrustManagerFactoryComboOptions() {
		return buildComboOptionsArray(trustManagerFactoryAlgorithms);
	}

	public static StaticLabelsAndValues getKeyStoreAlgorithmsComboOptions() {
		return buildComboOptionsArray(keyStoreAlgorithms);
	}

	public static StaticLabelsAndValues getSslContextAlgorithmsComboOptions() {
		return buildComboOptionsArray(sslContextAlgorithms);
	}

	public static String[] getKeyStoreFileExtensions() {
		return KEY_STORE_FILE_EXTENSIONS;
	}

	public static StaticLabelsAndValues buildComboOptionsArray(final Set<String> options) {
		final StaticLabelsAndValues entries = new StaticLabelsAndValues(options.size());
		for (final String value : options) {
			entries.put(value, value);
		}
		return entries;
	}

}
