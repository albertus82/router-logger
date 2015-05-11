RouterLogger
============

**RouterLogger** &egrave; una semplice applicazione Java in riga di comando per la registrazione dello stato della connessione ADSL, che include un'implementazione specifica per il router **TP-Link TD-W8970**. Il funzionamento &egrave; basato sull'interfaccia telnet esposta dalla maggior parte dei modem/router ADSL odierni.

### Installazione e configurazione di base

1. scaricare una release <code>bin</code> in formato ZIP, possibilmente la pi&ugrave; recente;
2. scompattarla in una cartella a piacimento in cui l'utente abbia diritti di scrittura;
3. modificare il file <code>**routerlogger.cfg**</code> configurando le seguenti propriet&agrave;:
  * <code>**router.address**</code>= indirizzo IP del router (solitamente <code>192.168.0.1</code> o <code>192.168.1.1</code>);
  * <code>**router.port**</code>= porta telnet del router, normalmente <code>23</code>;
  * <code>**router.username**</code>= nome utente per accedere al router (normalmente &egrave; lo stesso usato per accedere all'interfaccia grafica tramite browser);
  * <code>**router.password**</code>= password per accedere al router (normalmente &egrave; la stessa usata per accedere all'interfaccia grafica tramite browser);

Per avviare l'applicazione &egrave; richiesta la presenza della variabile di ambiente <code>JAVA_HOME</code> e di Java Runtime Environment (JRE) versione 6 (1.6) o successiva.

In ambiente Windows &egrave; sufficiente richiamare il file batch <code>routerlogger.bat</code>, mentre in ambienti diversi (es. Linux) occorre richiamare Java specificando un *classpath* che includa <code>routerlogger.jar</code> e <code>/lib/*.jar</code> e la classe da eseguire: <code>it.albertus.router.tplink.TpLinkLogger</code>.

Il programma si connetter&agrave; al router e inizier&agrave; a salvare ciclicamente le informazioni in formato CSV all'interno della cartella del programma, generando un file per ogni giornata. Per specificare una cartella diversa, abilitare (rimuovendo <code>#</code>) e modificare la propriet&agrave; <code>**log.destination.dir**</code>.

### Configurazione avanzata

Il file <code>routerlogger.cfg</code> contiene varie impostazioni, molte delle quali  disabilitate per impostazione predefinita (chiave preceduta dal carattere <code>#</code>) ma che possono essere attivate in caso di necessit&agrave;. Per abilitare un'impostazione, &egrave; sufficiente rimuovere il carattere di commento <code>#</code> presente all'inizio della relativa chiave. Le impostazioni disponibili sono le seguenti:

* <code>socket.timeout.ms</code>= timeout del socket in millisecondi, ossia il tempo di inattivita massimo durante la comunicazione con il server, trascorso il quale si assume che la comunicazione si sia interrotta (default: <code>30000</code>).
* <code>connection.timeout.ms</code>= timeout della connessione in millisecondi, ossia il tempo di attesa massimo in fase di connessione, trascorso il quale si assume che il server non &egrave; raggiungibile (default: <code>20000</code> ms).
* <code>telnet.send.crlf</code>= specifica come inviare il comando di ritorno a capo al server: se impostato a <code>true</code>, sar&agrave; inviata la coppia di caratteri di controllo <code>CR</code> e <code>LF</code> (<code>\r\n</code>); se impostato a <code>false</code> sar&agrave; invece inviato il solo carattere <code>LF</code> (<code>\n</code>); (default: <code>true</code>).
* <code>logger.iterations</code>= numero di iterazioni da effettuare. Normalmente l'applicazione registra l'attivit&agrave; del modem per un tempo indefinito, ossia finch&eacute; non viene chiusa dall'utente, ma &egrave; possibile indicare un numero di iterazioni massimo dopo il quale l'applicazione si chiuder&agrave; automaticamente. Valori minori o uguali a zero equivalgono a infinito (default: <code>-1</code>).
* <code>logger.interval.normal.ms</code>= intervallo tra le richieste di informazioni al modem in condizioni normali (default: <code>5000</code> ms). Valori inferiori a <code>1000</code> potrebbero creare problemi di funzionamento del dispositivo o blocco dell'applicazione a causa dell'elevato numero di richieste.
* <code>logger.interval.fast.ms</code>= intervallo tra le richieste di informazioni al modem in caso di raggiungimento di una o pi&ugrave; soglie (cfr. par. *soglie*) (default: <code>1000</code> ms). Valori inferiori a <code>1000</code> potrebbero creare problemi di funzionamento del dispositivo o blocco dell'applicazione a causa dell'elevato numero di richieste.
* <code>logger.retry.count</code>= numero di tentativi di riavvio del ciclo da effettuare in caso di errore durante l'esecuzione (default: <code>3</code>). Utile, ad esempio, in caso di riavvio del modem.
* <code>logger.retry.interval.ms</code>= intervallo tra i tentativi di riavvio (default: <code>60000</code> ms).

### Estensione

&Egrave; possibile estendere l'applicazione in modo da farla lavorare con qualsiasi router disponga di un'interfaccia **telnet** che permetta di recuperare informazioni sullo stato della connessione. Per farlo, &egrave; sufficiente implementare una classe personalizzata che estenda la classe astratta <code>**RouterLogger**</code>, la quale dispone di diversi metodi di utilit&agrave; che permettono di interagire agevolmente con il server telnet e che possono comunque essere sovrascritti in caso di necessit&agrave;.

I metodi da implementare sono i seguenti:
* <code>**login**</code>: effettua l'autenticazione telnet.
* <code>**readInfo**</code>: interagisce con il server per ottenere le informazioni sulla connessione e poi restituirle sotto forma di mappa.
* <code>**saveInfo**</code>: effettua il salvataggio delle informazioni ottenute (ad esempio su database o su file).

All'occorrenza pu&ograve; essere opportuno sovrascrivere anche i seguenti metodi, che non sono dichiarati <code>abstract</code> in <code>RouterLogger</code>:
* <code>**release**</code>: libera risorse eventualmente allocate dal programma (file, connessioni a database, ecc.); l'implementazione predefinita non fa nulla.
* <code>**logout**</code>: invia il comando di logout al server; l'implementazione predefinita invia <code>logout</code>, ma alcuni router possono richiedere un comando diverso, ad esempio <code>exit</code>, pertanto in questi casi il metodo deve essere opportunamento sovrascritto.
* <code>**getDeviceModel**</code>: restituisce una stringa contenente marca e modello del router (utile solo in visualizzazione); l'implementazione predefinita restituisce <code>null</code>, determinando cos&igrave; l'assenza dell'informazione.
