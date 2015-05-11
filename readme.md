RouterLogger
============

**RouterLogger** &egrave; una semplice applicazione Java in riga di comando per la registrazione dello stato della connessione ADSL, che include un'implementazione specifica per il router **TP-Link TD-W8970**. Il funzionamento &egrave; basato sull'interfaccia telnet esposta dalla maggior parte dei modem/router ADSL odierni.


### Installazione e configurazione di base

1. scaricare una release `bin` in formato ZIP, possibilmente la pi&ugrave; recente;
2. scompattare il file ZIP in una cartella a piacimento in cui l'utente abbia diritti di scrittura;
3. modificare il file **`routerlogger.cfg`** configurando le seguenti propriet&agrave;:
  * **`router.address`**= indirizzo IP del router (solitamente `192.168.0.1` o `192.168.1.1`).
  * **`router.port`**= porta telnet del router, default: `23`.
  * **`router.username`**= nome utente per accedere al router (normalmente &egrave; lo stesso usato per accedere all'interfaccia grafica tramite browser).
  * **`router.password`**= password per accedere al router (normalmente &egrave; la stessa usata per accedere all'interfaccia grafica tramite browser).

Per avviare l'applicazione &egrave; richiesta la presenza della variabile di ambiente `JAVA_HOME` e di Java Runtime Environment (JRE) versione 6 (1.6) o successiva.

In ambiente Windows &egrave; sufficiente richiamare il file batch **`routerlogger.bat`**, mentre in ambienti diversi (es. Linux) occorre richiamare Java specificando un *classpath* che includa `routerlogger.jar` e `/lib/*.jar` e la classe da eseguire: `it.albertus.router.tplink.TpLinkLogger`.

Il programma si connetter&agrave; al router e inizier&agrave; a interrogarlo ciclicamente, memorizzando di volta in volta le informazioni sullo stato della connessione in una mappa chiave-valore, dove le chiavi sono i nomi (o etichette) dei parametri di funzionamento del modem/router/linea ADSL. A ogni interrogazione, questa mappa viene rigenerata e il suo contenuto viene aggiunto ad un file in formato CSV. L'applicazione crea un file per ogni giornata, e a ogni iterazione corrisponde una riga nel file.
Di norma i file generati vengono salvati all'interno della cartella del programma. Per specificare una cartella diversa, occorre abilitare la propriet&agrave; **`log.destination.dir`** nel file `routerlogger.cfg` (rimuovendo `#`) e modificarne il valore.


### Configurazione avanzata

Il file `routerlogger.cfg` contiene varie impostazioni, molte delle quali disabilitate per impostazione predefinita (chiave preceduta dal carattere `#`) ma che possono essere attivate in caso di necessit&agrave;. Per abilitare un'impostazione, &egrave; sufficiente rimuovere il carattere di commento `#` presente all'inizio della relativa chiave. Le impostazioni disponibili, in aggiunta alle quattro gi&agrave; viste per la configurazione della connessione al dispositivo, sono le seguenti:

##### Impostazioni generali

* **`socket.timeout.ms`**= timeout del socket in millisecondi, ossia il tempo di inattivita massimo durante la comunicazione con il server, trascorso il quale si assume che la comunicazione si sia interrotta (default: `30000` ms).
* **`connection.timeout.ms`**= timeout della connessione in millisecondi, ossia il tempo di attesa massimo in fase di connessione, trascorso il quale si assume che il server non &egrave; raggiungibile (default: `20000` ms).
* **`telnet.send.crlf`**= specifica come inviare il comando di ritorno a capo al server: se impostato a `true`, sar&agrave; inviata la coppia di caratteri di controllo `CR` (`0x0D`) e `LF` (`0x0A`) (`\r\n`, stile DOS/Windows); se impostato a `false` sar&agrave; invece inviato il solo carattere `LF` (`0x0A`) (`\n`, stile Unix/Posix); (default: `true`).
* **`logger.iterations`**= numero di iterazioni da effettuare. Normalmente l'applicazione registra l'attivit&agrave; del modem per un tempo indefinito, ossia finch&eacute; non viene chiusa dall'utente, ma &egrave; possibile indicare un numero di iterazioni massimo dopo il quale l'applicazione si chiuder&agrave; automaticamente. Valori minori o uguali a zero equivalgono a infinito (default: `-1`).
* **`logger.interval.normal.ms`**= intervallo tra le richieste di informazioni al modem in condizioni normali (default: `5000` ms). Valori inferiori a `1000` potrebbero creare problemi di funzionamento del dispositivo o blocco dell'applicazione a causa dell'elevato numero di richieste.
* **`logger.interval.fast.ms`**= intervallo tra le richieste di informazioni al modem in caso di raggiungimento di una o pi&ugrave; soglie (cfr. par. *soglie*) (default: `1000` ms). Valori inferiori a `1000` potrebbero creare problemi di funzionamento del dispositivo o blocco dell'applicazione a causa dell'elevato numero di richieste.
* **`logger.retry.count`**= numero di tentativi di riavvio del ciclo da effettuare in caso di errore durante l'esecuzione (default: `3`). Utile, ad esempio, in caso di riavvio del modem.
* **`logger.retry.interval.ms`**= intervallo tra i tentativi di riavvio (default: `60000` ms). 
* **`console.animation`**= specifica se si desidera visualizzare una piccola animazione in console che segnala il funzionamento dell'applicazione (default: `true`).
* **`console.show.keys`**= elenco, separato da delimitatore, dei nomi delle chiavi i cui valori devono essere visualizzati in console a ogni iterazione (default: vuoto).
* **`console.show.keys.separator`**= delimitatore (o espressione regolare) usato per separare i nomi delle chiavi specificate nella propriet&agrave; `console.show.keys` (default: `,`). Scegliere un delimitatore che non contenga sequenze di caratteri presenti anche nei nomi delle chiavi.

##### Soglie

Le soglie permettono di specificare dei valori limite per uno o pi&ugrave; parametri di funzionamento del dispositivo; lo scopo è quello di poter incrementare la frequenza di interrogazione nelle situazioni critiche, in modo da aggiungere informazioni che potrebbero essere utili per la diagnosi di eventuali problemi della linea. Nel caso delle linee ADSL, ad esempio, un parametro che determina la stabilit&agrave; della connessione e che pu&ograve; essere soggetto ad ampie e talvolta repentine variazioni, &egrave; il *rapporto segnale-rumore* (SNR); utilizzando le soglie &egrave; possibile specificare un valore del SNR al raggiungimento del quale la frequenza di registrazione dei dati verr&agrave; incrementata al valore in millisecodi definito dalla propriet&agrave; `logger.interval.fast.ms`.

Ogni soglia &egrave; costituita da tre distinte propriet&agrave;: *chiave* (`key`), *tipologia* (`type`) e *valore di soglia* (`value`) nel file `routerlogger.cfg`:

* **`threshold.`**_identificativo.univoco.soglia_**`.key`**= chiave del parametro di interesse; deve corrispondere ad una chiave presente nella mappa delle informazioni estratte.
* **`threshold.`**_identificativo.univoco.soglia_**`.type`**= condizione di raggiungimento:
 * **`lt`**: minore di...
 * **`le`**: minore o uguale a...
 * **`eq`**: uguale a...
 * **`ge`**: maggiore o uguale a...
 * **`gt`**: maggiore di...
 * **`ne`**: diverso da...
* **`threshold.`**_identificativo.univoco.soglia_**`.value`**= valore di soglia.

Il prefisso `threshold.` &egrave; obbligatorio perch&eacute; segnala all'applicazione che la propriet&agrave; riguarda una soglia.

L'identificativo univoco di soglia pu&ograve; essere un testo qualsiasi (senza spazi n&eacute; carattere `=`) e ha l'unico scopo di raggruppare le tre propriet&agrave; `key`, `type` e `value`, che altrimenti, in presenza di pi&ugrave; soglie configurate, risulterebbero impossibili da correlare.

Gli unici suffissi ammessi per le propriet&agrave; relative alle soglie (`threshold.`) sono `.key`, `.type` e `.value`.

###### Esempio

```
threshold.snr.down.key=downstreamNoiseMargin
threshold.snr.down.type=lt
threshold.snr.down.value=100
```


### Estensione

&Egrave; possibile estendere l'applicazione in modo da farla lavorare con qualsiasi router disponga di un'interfaccia **telnet** che permetta di recuperare informazioni sullo stato della connessione. Per farlo, &egrave; sufficiente implementare una classe personalizzata che estenda la classe astratta **`RouterLogger`**, la quale dispone di diversi metodi di utilit&agrave; che permettono di interagire agevolmente con il server telnet e che possono comunque essere sovrascritti in caso di necessit&agrave;.

I metodi da implementare sono i seguenti:
* **`login`**: effettua l'autenticazione telnet.
* **`readInfo`**: interagisce con il server per ottenere le informazioni sulla connessione e poi restituirle sotto forma di mappa.
* **`saveInfo`**: effettua il salvataggio delle informazioni ottenute (ad esempio su database o su file).

All'occorrenza pu&ograve; essere opportuno sovrascrivere anche i seguenti metodi, che non sono dichiarati `abstract` in `RouterLogger`:
* **`release`**: libera risorse eventualmente allocate dal programma (file, connessioni a database, ecc.); l'implementazione predefinita non fa nulla.
* **`logout`**: invia il comando di logout al server; l'implementazione predefinita invia `logout`, ma alcuni router possono richiedere un comando diverso, ad esempio `exit`, pertanto in questi casi il metodo deve essere opportunamento sovrascritto.
* **`getDeviceModel`**: restituisce una stringa contenente marca e modello del router (utile solo in visualizzazione); l'implementazione predefinita restituisce `null`, determinando cos&igrave; l'assenza dell'informazione.
