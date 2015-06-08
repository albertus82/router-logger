package it.albertus.router.writer;

import it.albertus.router.RouterLoggerConfiguration;
import it.albertus.util.Console;
import it.albertus.util.ExceptionUtils;

import java.util.Map;

public abstract class Writer {

	private interface Defaults {
		boolean DEBUG = false;
	}

	protected static final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	protected static final Console out = Console.getInstance();

	/**
	 * Salva le informazioni di interesse, precedentemente estratte tramite
	 * Telnet, con le modalit&agrave; desiderate, ad esempio su file o in un
	 * database.
	 * 
	 * @param info  le informazioni da salvare.
	 */
	public abstract void saveInfo(Map<String, String> info);

	/**
	 * Libera le risorse eventualmente allocate (file, connessioni a database,
	 * ecc.).
	 */
	public abstract void release();

	protected void printLog(Throwable throwable) {
		if (configuration.getBoolean("logger.debug", Defaults.DEBUG)) {
			out.print(ExceptionUtils.getStackTrace(throwable), true);
		}
		else {
			out.println(ExceptionUtils.getLogMessage(throwable), true);
		}
	}

}
