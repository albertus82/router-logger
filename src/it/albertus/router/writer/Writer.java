package it.albertus.router.writer;

import it.albertus.router.RouterLoggerConfiguration;

import java.io.PrintStream;
import java.util.Map;

public abstract class Writer {

	protected final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

//	protected final LoggerTerminal terminal = LoggerTerminal.getInstance();
	protected final PrintStream terminal = System.out;

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

}
