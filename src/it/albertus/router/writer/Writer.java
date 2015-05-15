package it.albertus.router.writer;

import java.util.Map;

public interface Writer {

	/**
	 * Salva le informazioni di interesse, precedentemente estratte tramite
	 * Telnet, con le modalit&agrave; desiderate, ad esempio su file o in un
	 * database.
	 * 
	 * @param info
	 *            le informazioni da salvare.
	 */
	void saveInfo(Map<String, String> info);

	/**
	 * Libera le risorse eventualmente allocate (file, connessioni a database,
	 * ecc.).
	 */
	void release();

}
