package it.albertus.router.gui.preference.page;

import it.albertus.jface.preference.LocalizedNamesAndValues;
import it.albertus.router.server.BaseHttpServer;
import it.albertus.util.Localized;

import java.security.KeyStore;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.Set;
import java.util.TreeSet;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class ServerHttpsPreferencePage extends RestartHeaderPreferencePage {

	private static final String[] KEY_STORE_FILE_EXTENSIONS = { "*.JKS;*.jks", "*.P12;*.p12;*.PFX;*.pfx", "*.*" };

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
				if (sslContextClassName.equals(service.getType()) && !"Default".equals(service.getAlgorithm())) {
					sslContextAlgorithms.add(service.getAlgorithm());
				}
			}
		}
	}

	public static LocalizedNamesAndValues getKeyManagerFactoryComboOptions() {
		return buildComboOptionsArray(keyManagerFactoryAlgorithms);
	}

	public static LocalizedNamesAndValues getTrustManagerFactoryComboOptions() {
		return buildComboOptionsArray(trustManagerFactoryAlgorithms);
	}

	public static LocalizedNamesAndValues getKeyStoreAlgorithmsComboOptions() {
		return buildComboOptionsArray(keyStoreAlgorithms);
	}

	public static LocalizedNamesAndValues getSslContextAlgorithmsComboOptions() {
		return buildComboOptionsArray(sslContextAlgorithms);
	}

	public static String[] getKeyStoreFileExtensions() {
		return KEY_STORE_FILE_EXTENSIONS;
	}

	public static LocalizedNamesAndValues buildComboOptionsArray(final Set<String> options) {
		final LocalizedNamesAndValues entries = new LocalizedNamesAndValues(options.size());
		for (final String value : options) {
			final Localized name = new Localized() {
				@Override
				public String getString() {
					return value;
				}
			};
			entries.put(name, value);
		}
		return entries;
	}

}
