package it.albertus.router.reader;

import java.io.IOException;
import java.util.LinkedHashMap;

import it.albertus.util.ConfigurationException;

public interface IReader {

	/**
	 * Restituisce una stringa contenente marca e modello del router relativo
	 * all'implementazione realizzata.
	 */
	String getDeviceModel();

	/**
	 * Effettua la connessione al server Telnet, ma non l'autenticazione.
	 * <b>Normalmente non occorre sovrascrivere questo metodo</b>.
	 * 
	 * @return <tt>true</tt> se la connessione &egrave; riuscita, <tt>false</tt>
	 *         altrimenti.
	 * 
	 * @throws ConfigurationException se i parametri di connessione non sono
	 *         validi.
	 */
	boolean connect();

	/**
	 * Effettua l'autenticazione sul server Telnet, utilizzando i metodi
	 * {@link #readFromTelnet(String, boolean)} e {@link #writeToTelnet(String)}
	 * per interagire con il server e comunicare le credenziali di accesso.
	 * 
	 * @param username proveniente dalla propriet&agrave;
	 *        <tt>router.username</tt> del file di configurazione.
	 * @param password proveniente dalla propriet&agrave;
	 *        <tt>router.password</tt> del file di configurazione.
	 * 
	 * @return <tt>true</tt> se l'autenticazione &egrave; riuscita,
	 *         <tt>false</tt> altrimenti.
	 * 
	 * @throws IOException in caso di errore nella comunicazione con il server.
	 */
	boolean login(String string, char[] charArray) throws IOException;

	/**
	 * Estrae le informazioni di interesse dai dati ricevuti dal server Telnet,
	 * utilizzando i metodi {@link #writeToTelnet(String)} e
	 * {@link #readFromTelnet(String, boolean)}.
	 * 
	 * @return la mappa contenente le informazioni estratte.
	 * @throws IOException in caso di errore nella lettura dei dati.
	 */
	LinkedHashMap<String, String> readInfo() throws IOException;

	/**
	 * Effettua il logout dal server Telnet inviando il comando <tt>exit</tt>.
	 * &Egrave; possibile sovrascrivere questo metodo per aggiungere altri o
	 * diversi comandi che debbano essere eseguiti in fase di logout. <b>Questo
	 * metodo non effettua esplicitamente la disconnessione dal server</b>.
	 * 
	 * @throws IOException in caso di errore nella comunicazione con il server.
	 */
	void logout() throws IOException;

	/**
	 * Effettua la disconnessione dal server Telnet, ma non invia alcun comando
	 * di logout. &Egrave; buona norma richiamare prima il metodo
	 * {@link #logout()} per inviare al server Telnet gli opportuni comandi di
	 * chiusura della sessione (ad esempio <tt>logout</tt>). <b>Normalmente non
	 * occorre sovrascrivere questo metodo</b>.
	 */
	void disconnect();

	void release();

}
