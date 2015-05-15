RouterLogger
============

**RouterLogger** &egrave; una semplice applicazione Java in riga di comando per la registrazione dello stato della connessione ADSL, che include un'implementazione specifica per il router **TP-Link TD-W8970 V1**. Il funzionamento &egrave; basato sull'interfaccia **Telnet** esposta dalla maggior parte dei modem/router ADSL odierni.


### Installazione e configurazione di base

1. [scaricare](http://github.com/Albertus82/RouterLogger/releases) una release `bin` in formato ZIP, possibilmente la pi&ugrave; recente;
2. scompattare il file ZIP in una cartella a piacimento in cui l'utente abbia diritti di scrittura;
3. modificare il file [**`routerlogger.cfg`**](src/routerlogger.cfg) configurando le seguenti propriet&agrave;:
  * **`router.address`**= indirizzo IP del router (solitamente `192.168.0.1` o `192.168.1.1`).
  * **`router.port`**= porta telnet del router, default: `23`.
  * **`router.username`**= nome utente per accedere al router (normalmente &egrave; lo stesso usato per accedere all'interfaccia grafica tramite browser).
  * **`router.password`**= password per accedere al router (normalmente &egrave; la stessa usata per accedere all'interfaccia grafica tramite browser).

Per avviare l'applicazione &egrave; richiesta la presenza della variabile di ambiente `JAVA_HOME` e di [Java Runtime Environment](http://www.java.com) (JRE) versione 6 (1.6) o successiva.

In ambiente Windows &egrave; sufficiente richiamare il file batch **`routerlogger.bat`**, passando come parametro il nome della classe che implementa il RouterLogger desiderato. Esempio:

`routerlogger `[`TPLinkTDW8970V1`](src/it/albertus/router/logger/TPLinkTDW8970V1.java)

In ambienti diversi (es. Linux) occorre richiamare Java specificando:
* un *classpath* che includa `routerlogger.jar` e `lib/*.jar`
* la classe da eseguire: [`it.albertus.router.logger.RouterLogger`](src/it/albertus/router/logger/RouterLogger.java)
* il nome della classe che implementa il RouterLogger desiderato, ad es.: [`TPLinkTDW8970V1`](src/it/albertus/router/logger/TPLinkTDW8970V1.java).

> Volendo eseguire implementazioni di [`RouterLogger`](src/it/albertus/router/logger/RouterLogger.java) personalizzate o comunque esterne al progetto, occorrer&agrave; specificare come parametro in riga di comando, il nome completo (inclusi tutti i package separati da `.`) della classe concreta che estende [`RouterLogger`](src/it/albertus/router/logger/RouterLogger.java). Sar&agrave; inoltre necessario copiare nella directory `lib` dell'applicazione il JAR aggiuntivo contenente la classe esterna. Per maggiori informazioni, vedere il paragrafo [Estensione](#estensione).

Il programma si connetter&agrave; al router e inizier&agrave; a interrogarlo ciclicamente, memorizzando di volta in volta le informazioni sullo stato della connessione in una mappa chiave-valore, dove le chiavi sono i nomi (o etichette) dei parametri di funzionamento del modem/router/linea ADSL. A ogni interrogazione, questa mappa viene rigenerata e il suo contenuto viene di norma aggiunto ad un file in formato CSV, ma &egrave; anche possibile configurare il salvataggio in una tabella di un database.

##### CSV
L'applicazione crea un file per ogni giornata, e a ogni iterazione corrisponde una riga nel file. Per attivare questo tipo di salvataggio non occorre configurare nulla: questa &egrave; la modalit&agrave; predefinita.

Di norma i file generati vengono salvati all'interno della cartella del programma; per specificare una cartella diversa, occorre abilitare la propriet&agrave; **`csv.destination.path`** nel file [`routerlogger.cfg`](src/routerlogger.cfg) (rimuovendo `#`) e assegnarle il valore desiderato (ad es.: `C:/Router/Logs`).

##### Database
L'applicazione crea una tabella per memorizzare i dati (se non presente), e a ogni iterazione corrisponde una riga nella tabella.
Per attivare il salvataggio su database, occorre innanzi tutto aggiungere la libreria JDBC del proprio database all'interno della directory `lib` dell'applicazione, quindi abilitare una serie di propriet&agrave; nel file [`routerlogger.cfg`](src/routerlogger.cfg) (rimuovendo `#`) e assegnare ad esse il valore desiderato:
* **`logger.writer.class.name`**=[**`DatabaseWriter`**](src/it/albertus/router/writer/DatabaseWriter.java)
* **`database.driver.class.name`**= nome completo della classe del driver JDBC (ad es.: `oracle.jdbc.driver.OracleDriver`).
* **`database.url`**= URL per il collegamento al database (ad es.: `jdbc:oracle:thin:@localhost:1521:XE`).
* **`database.username`**= nome utente per accedere al database.
* **`database.password`**= password per accedere al database.


### Configurazione avanzata

Il file [`routerlogger.cfg`](src/routerlogger.cfg) contiene gi&agrave; varie impostazioni, quasi tutte disabilitate per impostazione predefinita (chiave preceduta dal carattere `#`) ma che possono essere attivate in caso di necessit&agrave;. Per abilitare un'impostazione, &egrave; sufficiente rimuovere il carattere di commento `#` presente all'inizio della relativa chiave. Segue una disamina di tutte le impostazioni disponibili, in aggiunta a quelle gi&agrave; viste per la configurazione di base. Le seguenti impostazioni sono tutte facoltative, salvo diversa indicazione.

##### Impostazioni generali

* **`logger.iterations`**= numero di iterazioni da effettuare. Normalmente l'applicazione registra l'attivit&agrave; del modem per un tempo indefinito, ossia finch&eacute; non viene chiusa dall'utente, ma &egrave; possibile indicare un numero di iterazioni massimo dopo il quale l'applicazione si chiuder&agrave; automaticamente. Valori minori o uguali a zero equivalgono a infinito (default: `-1`).
* **`logger.interval.normal.ms`**= intervallo tra le richieste di informazioni al modem in condizioni normali, in millisecondi (default: `5000` ms). Valori inferiori a `1000` potrebbero creare problemi di funzionamento del dispositivo o blocco dell'applicazione a causa dell'elevato numero di richieste.
* **`logger.interval.fast.ms`**= intervallo tra le richieste di informazioni al modem in caso di raggiungimento di una o pi&ugrave; soglie (cfr. par. *soglie*), in millisecondi (default: `1000` ms). Valori inferiori a `1000` potrebbero creare problemi di funzionamento del dispositivo o blocco dell'applicazione a causa dell'elevato numero di richieste.
* **`logger.retry.count`**= numero di tentativi di riavvio del ciclo da effettuare in caso di errore durante l'esecuzione (default: `3`). Utile, ad esempio, in caso di riavvio del modem.
* **`logger.retry.interval.ms`**= intervallo tra i tentativi di riavvio, in millisecondi (default: `60000` ms). 

##### Rete

* **`socket.timeout.ms`**= timeout del socket in millisecondi, ossia il tempo di inattivita massimo durante la comunicazione con il server, trascorso il quale si assume che la comunicazione si sia interrotta (default: `30000` ms). Questo valore deve essere sempre maggiore dell'intervallo tra le richieste (`logger.interval.normal.ms`).
* **`connection.timeout.ms`**= timeout della connessione in millisecondi, ossia il tempo di attesa massimo in fase di connessione, trascorso il quale si assume che il server non &egrave; raggiungibile (default: `20000` ms).
* **`telnet.send.crlf`**= specifica come inviare il comando di ritorno a capo al server: se impostato a `true`, sar&agrave; inviata la coppia di caratteri di controllo `CR` (`0x0D`) e `LF` (`0x0A`) (`\r\n`, stile DOS/Windows); se impostato a `false` sar&agrave; invece inviato il solo carattere `LF` (`0x0A`) (`\n`, stile Unix/Posix); (default: `true`).

##### Console

* **`console.animation`**= specifica se si desidera visualizzare una piccola animazione in console che segnala il funzionamento dell'applicazione (default: `true`).
* **`console.show.keys`**= elenco, separato da delimitatore, dei nomi delle chiavi i cui valori devono essere visualizzati in console a ogni iterazione (default: vuoto). Un eccessivo numero di chiavi da visualizzare provocher&agrave; lo scorrimento verticale della console, un effetto collaterale probabilmente indesiderato.
* **`console.show.keys.separator`**= delimitatore (o espressione regolare) usato per separare i nomi delle chiavi specificate nella propriet&agrave; `console.show.keys` (default: `,`). Scegliere un delimitatore che non contenga sequenze di caratteri presenti anche nei nomi delle chiavi.

#### Destinazione

La selezione della modalit&agrave; di salvataggio delle informazioni si effettua configurando la seguente propriet&agrave;:
* **`logger.writer.class.name`**: identifica la classe che si occupa del salvataggio delle informazioni, e pu&ograve; assumere i valori seguenti:
  * [**`CsvWriter`**](src/it/albertus/router/writer/CsvWriter.java): scrittura su file CSV (default).
  * [**`DatabaseWriter`**](src/it/albertus/router/writer/DatabaseWriter.java): scrittura su database.
  * [**`DummyWriter`**](src/it/albertus/router/writer/DummyWriter.java): nessuna scrittura.
  * nome completo (inclusi tutti i package separati da `.`) di una classe concreta che estenda [**`Writer`**](src/it/albertus/router/writer/Writer.java).

##### CSV

* **`csv.destination.path`**= percorso in cui saranno salvati i file CSV generati (default: directory dell'applicazione).
* **`csv.record.separator.crlf`**= specifica come deve essere rappresentato il ritorno a capo nei file CSV generati; se impostato a `true`, sar&agrave; utilizzata la coppia di caratteri di controllo `CR` (`0x0D`) e `LF` (`0x0A`) (`\r\n`, stile DOS/Windows); se impostato a `false` sar&agrave; invece utilizzato il solo carattere `LF` (`0x0A`) (`\n`, stile Unix/Posix); (default: `true`).
* **`csv.field.separator`**= separatore dei campi utilizzato nei file CSV generati (default: `;`, compatibile con Microsoft Excel).
* **`csv.field.separator.replacement`**= poich&eacute; il testo da scrivere nei file CSV non deve mai contenere il separatore, tutte le eventuali occorrenze del separatore saranno sostituite da questa stringa (default: `,`).

##### Database

* **`database.table.name`**= nome della tabella in cui saranno inseriti i dati (default: `ROUTER_LOG`).
* **`database.connection.validation.timeout.ms`**= tempo di attesa massimo su richiesta di verifica della validit&agrave; della connessione al database, in millisecondi (default: `2000` ms).
* **`database.column.type`**= tipo di dato utilizzato per le colonne in fase di creazione della tabella (default: `VARCHAR`).
* **`database.column.length`**= lunghezza delle colonne utilizzata in fase di creazione della tabella (default: `250`).

#### Soglie

Le soglie permettono di specificare dei valori limite per uno o pi&ugrave; parametri di funzionamento del dispositivo; lo scopo è quello di poter incrementare la frequenza di interrogazione nelle situazioni critiche, in modo da aggiungere informazioni che potrebbero essere utili per la diagnosi di eventuali problemi della linea.

Nel caso delle linee ADSL, ad esempio, un parametro che determina la stabilit&agrave; della connessione e che pu&ograve; essere soggetto ad ampie e talvolta repentine variazioni, &egrave; il *rapporto segnale-rumore* (SNR). Utilizzando le soglie &egrave; possibile fare in modo che la frequenza di registrazione dei dati venga incrementata quando il valore del SNR scende al di sotto di una certa soglia.

Quando una soglia viene raggiunta, il periodo di registrazione passa da quello normale, definito dalla propriet&agrave; `logger.interval.normal.ms` (default 5 secondi), a quello definito dalla propriet&agrave; `logger.interval.fast.ms` (default un secondo).

##### Configurazione

Ogni soglia &egrave; costituita da una terna di propriet&agrave;: *chiave* (`key`), *tipologia* (`type`) e *valore di soglia* (`value`) nel file [`routerlogger.cfg`](src/routerlogger.cfg):

* <code>**threshold.*identificativo.univoco.soglia*.key**</code>= chiave del parametro di interesse; deve corrispondere ad una chiave presente nella mappa delle informazioni estratte.
* <code>**threshold.*identificativo.univoco.soglia*.type**</code>= condizione di raggiungimento:
  * **`lt`**: minore di...
  * **`le`**: minore o uguale a...
  * **`eq`**: uguale a...
  * **`ge`**: maggiore o uguale a...
  * **`gt`**: maggiore di...
  * **`ne`**: diverso da...
* <code>**threshold.*identificativo.univoco.soglia*.value**</code>= valore di soglia.

Il prefisso `threshold.` &egrave; obbligatorio perch&eacute; segnala all'applicazione che la propriet&agrave; riguarda una soglia.

L'*identificativo univoco soglia* pu&ograve; essere un testo qualsiasi (senza spazi n&eacute; carattere `=`) e ha l'unico scopo di raggruppare le tre propriet&agrave; `key`, `type` e `value`, che altrimenti, in presenza di pi&ugrave; soglie configurate, risulterebbero impossibili da correlare.

Gli unici suffissi ammessi per le propriet&agrave; relative alle soglie (`threshold.`) sono `.key`, `.type` e `.value`.

##### Esempio

Aggiungendo le seguenti tre righe al file [`routerlogger.cfg`](src/routerlogger.cfg), si imposter&agrave; una soglia di 10.0 dB per il SNR; qualora il valore del SNR dovesse scendere al di sotto di 10.0 dB, la frequenza (o, pi&ugrave; precisamente, il periodo) di logging passerebbe da 5000 a 1000 millisecondi.

```
threshold.snr.down.key=downstreamNoiseMargin
threshold.snr.down.type=lt
threshold.snr.down.value=100
```


### Estensione

&Egrave; possibile estendere l'applicazione in modo da farla lavorare con qualsiasi router disponga di un'interfaccia **Telnet** che permetta di recuperare informazioni sullo stato della connessione. Per farlo, &egrave; sufficiente implementare una classe personalizzata che estenda la classe astratta [**`RouterLogger`**](src/it/albertus/router/logger/RouterLogger.java), la quale dispone di diversi metodi di utilit&agrave; che permettono di interagire agevolmente con il server telnet e che possono comunque essere sovrascritti in caso di necessit&agrave;.

I metodi da implementare tassativamente sono i seguenti:
* **`login`**: effettua l'autenticazione al server telnet comunicando le credenziali di accesso.
* **`readInfo`**: interagisce con il server in modo da ottenere le informazioni sulla connessione ADSL e le restituisce sotto forma di mappa chiave-valore.

All'occorrenza pu&ograve; essere opportuno sovrascrivere anche i seguenti metodi, che non sono dichiarati `abstract` in [`RouterLogger`](src/it/albertus/router/logger/RouterLogger.java):
* **`logout`**: invia il comando di logout al server; l'implementazione predefinita invia `logout`, ma alcuni router possono richiedere un comando diverso, ad esempio `exit`, pertanto in questi casi il metodo deve essere opportunamento sovrascritto.
* **`getDeviceModel`**: restituisce una stringa contenente marca e modello del router (utile solo in visualizzazione); l'implementazione predefinita restituisce il nome della classe in esecuzione (senza package).

Nel caso in cui si volessero salvare le informazioni in formato diverso da CSV o database SQL, si pu&ograve; estendere la classe astratta [**`Writer`**](src/it/albertus/router/writer/Writer.java) e sar&agrave; ovviamente necessario implementare i due metodi seguenti:
* **`saveInfo`**: effettua il salvataggio delle informazioni ottenute con le modalit&agrave; desiderate.
* **`release`**: libera risorse eventualmente allocate dal programma.

Occorrer&agrave; quindi configurare l'applicazione in modo che faccia uso della classe realizzata modificando il file [`routerlogger.cfg`](src/routerlogger.cfg) e specificando come propriet&agrave; `logger.writer.class.name` il nome completo della classe (inclusi tutti i package separati da `.`). Sar&agrave; inoltre necessario copiare nella directory `lib` dell'applicazione il JAR aggiuntivo contenente la classe esterna.
