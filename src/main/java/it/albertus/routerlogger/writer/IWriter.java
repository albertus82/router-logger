package it.albertus.routerlogger.writer;

import it.albertus.routerlogger.engine.RouterData;

public interface IWriter {

	/**
	 * Salva le informazioni di interesse, precedentemente estratte tramite
	 * Telnet, con le modalit&agrave; desiderate, ad esempio su file o in un
	 * database.
	 * 
	 * @param info le informazioni da salvare.
	 */
	void saveInfo(RouterData info);

	/**
	 * Libera le risorse eventualmente allocate (file, connessioni a database,
	 * ecc.).
	 */
	void release();

}
