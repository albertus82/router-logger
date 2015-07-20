package it.albertus.router.writer;

import it.albertus.router.RouterLoggerConfiguration;
import it.albertus.router.util.Logger;
import it.albertus.util.Console;

import java.util.Map;

public abstract class Writer {

	protected static final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	protected static final Logger logger = Logger.getInstance();

	protected Console out;

	public void init(Console console) {
		this.out = console;
	}

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
