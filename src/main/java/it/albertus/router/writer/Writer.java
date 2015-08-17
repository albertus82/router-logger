package it.albertus.router.writer;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.util.Logger;
import it.albertus.util.Console;

public abstract class Writer {

	protected final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();
	protected final Logger logger = Logger.getInstance();

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
	public abstract void saveInfo(RouterData info);

	/**
	 * Libera le risorse eventualmente allocate (file, connessioni a database,
	 * ecc.).
	 */
	public abstract void release();

}
