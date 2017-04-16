RouterLogger
============

[![Build Status](https://travis-ci.org/Albertus82/RouterLogger.svg?branch=master)](https://travis-ci.org/Albertus82/RouterLogger)
[![Build status](https://ci.appveyor.com/api/projects/status/github/Albertus82/RouterLogger?branch=master&svg=true)](https://ci.appveyor.com/project/Albertus82/RouterLogger)

**RouterLogger** &egrave; una semplice applicazione per la registrazione dello stato della connessione ADSL, che include implementazioni specifiche per i seguenti router:
* **TP-Link TD-W8970 V1**
* **ASUS DSL-N12E**
* **ASUS DSL-N14U**
* **D-Link DSL-2750B**

![Screenshot](https://cloud.githubusercontent.com/assets/8672431/23581689/067ff98c-0119-11e7-80ac-dfb4ddd0ad9c.png)

Il funzionamento &egrave; basato sull'interfaccia **Telnet** esposta dalla maggior parte dei modem router ADSL odierni, pertanto &egrave; possibile estendere l'applicazione in modo da farla lavorare con qualsiasi modem router disponga di una tale interfaccia che permetta di recuperare informazioni sullo stato della connessione.
>Molti dispositivi hanno l'interfaccia Telnet disabilitata per impostazione predefinita, occorre pertanto abilitarla tramite configurazione web prima di poter utilizzare l'applicazione.


## Installazione e avvio

1. [scaricare](http://github.com/Albertus82/RouterLogger/releases) la release adatta alla propria piattaforma, possibilmente la pi&ugrave; recente;
	* per **[Raspberry Pi](http://www.raspberrypi.org)** scaricare la release **`gtk-linux-armhf`**;
2. installare (o scompattare, a seconda dei casi) il programma in una cartella a piacimento.

Per avviare l'applicazione &egrave; richiesto [Java Runtime Environment](http://www.java.com) (JRE) versione 6 (1.6) o successiva.

Al primo avvio sar&agrave; necessario accedere alla configurazione, scheda **Router**, per specificare il modello di router (**Dispositivo/Classe Reader**) e pochi altri parametri di connessione:

* **Nome utente**: username per accedere al router (normalmente &egrave; lo stesso usato per accedere all'interfaccia grafica tramite browser);
* **Password**: password per accedere al router (normalmente &egrave; la stessa usata per accedere all'interfaccia grafica tramite browser);
* **Indirizzo**: indirizzo IP del router (solitamente `192.168.0.1` oppure `192.168.1.1` che &egrave; il valore predefinito);
* **Porta Telnet**: porta Telnet del router (predefinita: `23`).

Una volta configurato, il programma si connetter&agrave; al router e inizier&agrave; a interrogarlo ciclicamente, memorizzando di volta in volta le informazioni sullo stato della connessione in una mappa chiave-valore, dove le chiavi sono i nomi (o etichette) dei parametri di funzionamento del modem router/linea ADSL. A ogni interrogazione, questa mappa viene rigenerata e il suo contenuto viene di norma aggiunto ad un file in formato CSV, ma &egrave; anche possibile configurare il salvataggio in una tabella di un database.  

#### Salvataggio su file CSV

L'applicazione crea un file per ogni giornata, e a ogni iterazione corrisponde una riga nel file. Per attivare questo tipo di salvataggio non occorre configurare nulla: questa &egrave; la modalit&agrave; predefinita.

Di norma i file generati vengono salvati in una sottocartella `RouterLogger` all'interno della cartella `Documenti`; per specificare una cartella diversa occorre accedere alla configurazione (men&ugrave; **Strumenti**) e modificare la relativa opzione presente nella scheda **Salvataggio > CSV**.

#### Salvataggio su database relazionale

L'applicazione crea una tabella per memorizzare i dati (se non presente), e a ogni iterazione corrisponde una riga nella tabella.
Per attivare il salvataggio su database, occorre innanzi tutto aggiungere la libreria JDBC del proprio database (ad es. `ojdbc6.jar` nel caso di Oracle) all'interno della directory `lib` dell'applicazione, quindi accedere alla configurazione  (men&ugrave; **Strumenti**) e compilare le seguenti opzioni nella scheda **Salvataggio > Database**:

* **Nome classe driver**: nome completo della classe del driver JDBC (ad es.: `oracle.jdbc.OracleDriver`).
* **URL di connessione JDBC**: URL per il collegamento al database (ad es.: `jdbc:oracle:thin:@localhost:1521:XE`).
* **Nome utente**: nome utente per accedere al database.
* **Password**: password per accedere al database.

Infine, impostare la seguente opzione nella scheda **Salvataggio**:

* **Destinazione/Classe Writer**: [**Database**](src/main/java/it/albertus/router/writer/DatabaseWriter.java)


## Interfaccia a riga di comando

&Egrave; disponibile un'opzione che consente di avviare l'applicazione in modalit&agrave; riga di comando (senza interfaccia grafica):

* Windows: **`routerlogger.bat -c`**
* Linux: **`routerlogger.sh -c`**
* OS X: **`routerlogger.command -c`**

In questo caso, prima del primo avvio, occorre modificare manualmente il file di configurazione [**`routerlogger.cfg`**](src/main/config/routerlogger.cfg) con un editor di testo per attivare (rimuovendo il `#` a inizio riga) e impostare le seguenti propriet&agrave;:
* **`reader.class.name`**= Uno tra [`TpLink8970Reader`](src/main/java/it/albertus/router/reader/TpLink8970Reader.java), [`AsusDslN12EReader`](src/main/java/it/albertus/router/reader/AsusDslN12EReader.java), [`AsusDslN14UReader`](src/main/java/it/albertus/router/reader/AsusDslN14UReader.java) e [`DLinkDsl2750Reader`](src/main/java/it/albertus/router/reader/DLinkDsl2750Reader.java), a seconda del modello di dispositivo da monitorare;
* **`router.address`**= indirizzo IP del router (solitamente `192.168.0.1` oppure `192.168.1.1` che &egrave; il valore predefinito);
* **`router.port`**= porta Telnet del router (predefinita: `23`);
* **`router.username`**= nome utente per accedere al router (normalmente &egrave; lo stesso usato per accedere all'interfaccia grafica tramite browser);
* **`router.password`**= password per accedere al router (normalmente &egrave; la stessa usata per accedere all'interfaccia grafica tramite browser).


## Configurazione avanzata

Se si utilizza l'interfaccia grafica, &egrave; possibile accedere alla configurazione dal men&ugrave; **Strumenti > Configurazione**. Utilizzando l'interfaccia a riga di comando, invece, occorre modificare manualmente il file [`routerlogger.cfg`](src/main/config/routerlogger.cfg) con un editor di testo come Blocco note.

Il file [`routerlogger.cfg`](src/main/config/routerlogger.cfg) fornito contiene gi&agrave; tutte le impostazioni dell'applicazione, e quasi tutte sono disabilitate per impostazione predefinita (chiave preceduta dal carattere `#`). Il principio &egrave; il cosiddetto *Convention Over Configuration* (noto anche come *Configuration by Exception*), secondo il quale non occorre preoccuparsi di configurare nulla esplicitamente, fatta eccezione per pochi parametri per i quali non pu&ograve; esistere un valore predefinito; nel nostro caso si tratta esclusivamente delle credenziali di accesso del router e degli eventuali parametri di connessione al database.

Tutte le propriet&agrave; che non vengono configurate esplicitamente assumono un certo valore predefinito (default) che sar&agrave; specificato nei paragrafi seguenti. In caso di necessit&agrave;, &egrave; possibile abilitare una o pi&ugrave; impostazioni rimuovendo il carattere di commento `#` presente all'inizio della relativa chiave e sostituendo il valore predefinito con quello desiderato. &Egrave; altres&igrave; possibile ripulire il file [`routerlogger.cfg`](src/main/config/routerlogger.cfg) rimuovendo completamente le righe ritenute inutili.

Segue una disamina di tutte le impostazioni disponibili, in aggiunta a quelle gi&agrave; viste per la configurazione di base. Le seguenti impostazioni sono tutte facoltative, salvo diversa indicazione.

#### Impostazioni generali

* **`logger.iterations`**= numero di iterazioni da effettuare. Normalmente l'applicazione registra l'attivit&agrave; del modem per un tempo indefinito, ossia finch&eacute; non viene chiusa dall'utente, ma &egrave; possibile indicare un numero di iterazioni massimo dopo il quale l'applicazione si chiuder&agrave; automaticamente. Valori minori o uguali a zero equivalgono a infinito (default: `0`).
	* **`logger.close.when.finished`**= specifica se l'applicazione debba chiudersi automaticamente dopo l'ultima iterazione. Ininfluente se il numero di iterazioni &egrave; infinito (default: `false`).
* **`logger.interval.normal.ms`**= intervallo tra le richieste di informazioni al modem in condizioni normali, in millisecondi (default: `5000` ms). Valori inferiori a `1000` potrebbero creare problemi di funzionamento del dispositivo o blocco dell'applicazione a causa dell'elevato numero di richieste.
* **`logger.interval.fast.ms`**= intervallo tra le richieste di informazioni al modem in caso di raggiungimento di una o pi&ugrave; soglie (cfr. par. *soglie*), in millisecondi (default: `1000` ms). Valori inferiori a `1000` potrebbero creare problemi di funzionamento del dispositivo o blocco dell'applicazione a causa dell'elevato numero di richieste.
* **`logger.hysteresis.ms`**= intervallo di tempo durante il quale l'applicazione continua a registrare a frequenza accelerata (`logger.interval.fast.ms`) anche dopo che un valore, che precedentemente aveva raggiunto una soglia, &egrave; rientrato nella norma, in millisecondi (default: `10000` ms).
* **`logger.retry.count`**= numero di tentativi di riavvio del ciclo da effettuare in caso di errore durante l'esecuzione (default: `3`). Il contatore si azzera se il ciclo riparte con successo. Utile, ad esempio, in caso di riavvio del modem.
* **`logger.retry.interval.ms`**= intervallo tra i tentativi di riavvio, in millisecondi (default: `30000` ms).
* **`logger.error.log.destination.path`**= percorso in cui saranno salvati eventuali file registro (`.log`) contenenti i dettagli degli errori occorsi durante l'esecuzione del programma (default: stessa cartella del programma).
* **`language`**= codice nazione (`en`, `it`, ...) che determina la lingua dell'interfaccia (default: lingua di sistema; se non disponibile: `en`).

#### Rete

* **`socket.timeout.ms`**= timeout del socket in millisecondi, ossia il tempo di inattivit&agrave; massimo *durante la comunicazione* con il server, trascorso il quale si assume che la comunicazione si sia interrotta (default: `30000` ms). Questo valore deve essere sempre maggiore dell'intervallo tra le richieste (`logger.interval.normal.ms`).
* **`connection.timeout.ms`**= timeout della connessione in millisecondi, ossia il tempo di attesa massimo *in fase di connessione*, trascorso il quale si assume che il server non sia raggiungibile (default: `20000` ms).
* **`telnet.newline.characters`**= specifica come inviare il comando di ritorno a capo al server; pu&ograve; assumere uno tra i seguenti valori:
	* **`CRLF`** (default): invia la coppia di caratteri di controllo `CR` (`0x0D`) e `LF` (`0x0A`) (`\r\n`, stile DOS/Windows).
	* **`LF`**: invia il solo carattere `LF` (`0x0A`) (`\n`, stile Linux/OS X).
	* **`CR`**: invia il solo carattere `CR` (`0x0D`) (`\r`).
* **`reader.log.connected`**= specifica se registrare l'avvenuta connessione al router su file e via email (default: `false`).
* **`reader.wait.disconnected`**= specifica se disconnettersi dal router dopo ogni interrogazione. Normalmente l'applicazione resta sempre connessa al dispositivo, ma in caso di intervallo tra le richieste maggiore di alcuni secondi pu&ograve; essere opportuno abilitare questa opzione che forza una disconnessione e riconnessione ad ogni iterazione (default: `false`).
	* **`reader.wait.disconnected.interval.threshold`**= disconnette solo se l'intervallo &egrave; superiore ad una certa soglia configurabile con la propriet&agrave; `reader.wait.disconnected.interval.threshold.ms` (default: `true`).
	* **`reader.wait.disconnected.interval.threshold.ms`**= l'intervallo minimo al di sotto del quale non viene effettuata la disconnessione, in millisecondi (default: `1000` ms).

#### Interfaccia grafica (GUI)

* **`gui.table.items.max`**= numero massimo di righe contenute nella tabella a video; al raggiungimento del limite, le righe pi&ugrave; vecchie vengono cancellate. Questa impostazione non influisce in alcun modo sul salvataggio delle informazioni ma ha effetto unicamente sull'interfaccia grafica dell'applicazione (default: `2000`, valori maggiori comportano una maggiore occupazione di memoria).
* **`gui.table.columns.pack`**= riduce al minimo la larghezza delle colonne della tabella a video adattandola ai valori e ignorando la larghezza dei nomi delle chiavi in intestazione (default: `false`).
* **`gui.table.columns.padding.right`**= aggiunge un margine destro alle colonne della tabella a video; utile con alcuni ambienti grafici Linux. Il valore determina le dimensioni del margine (default: `0`, ossia nessun margine).
* **`gui.minimize.tray`**= specifica se l'applicazione deve essere ridotta a icona nell'area di notifica invece che nella barra delle applicazioni (default: `true`).
* **`gui.start.minimized`**= specifica se l'applicazione deve essere avviata gi&agrave; ridotta a icona (default: `false`).
* **`gui.tray.tooltip`**= specifica se deve essere mostrato un avviso nell'area di notifica in caso di raggiungimento di una soglia (default: `true`).
* **`gui.confirm.close`**= specifica se deve essere mostrato un messaggio di conferma quando si tenta di chiudere l'applicazione (default: `false`).
* **`gui.console.max.chars`**= dimensione massima della console, in caratteri; la console viene automaticamente ripulita al raggiungimento della soglia per limitare l'utilizzo di memoria (default: `50000` caratteri).
* **`gui.important.keys`**= elenco, separato da delimitatore, dei nomi delle chiavi i cui valori saranno evidenziati nella tabella (default: vuoto). Gli stessi valori saranno mostrati anche nel suggerimento che compare soffermandosi con il mouse sull'eventuale icona di RouterLogger nell'area di notifica.
* **`gui.important.keys.separator`**= delimitatore (o espressione regolare) usato per separare i nomi delle chiavi specificate nella propriet&agrave; `gui.important.keys` (default: `,`). Scegliere un delimitatore che non contenga sequenze di caratteri presenti anche nei nomi delle chiavi.
* **`gui.clipboard.max.chars`**= dimensione massima degli appunti, in caratteri. (default: `100000` caratteri). **In ambiente Linux/GTK, non impostare mai valori superiori a `131072` (128 KiB), in caso contrario potrebbero verificarsi problemi dovuti all'esaurimento della memoria.**
* **`gui.important.keys.color`**= codice RGB del colore di evidenziazione delle colonne della tabella (default: `255,255,0`, ossia giallo).
* **`gui.thresholds.reached.color`**= codice RGB del colore dei valori oltre soglia in tabella (default: `255,0,0` ossia rosso).

#### Console

* **`console.animation`**= specifica se si desidera visualizzare una piccola animazione in console che segnala il funzionamento dell'applicazione (default: `true`).
* **`console.show.configuration`**= specifica se si desidera visualizzare l'elenco delle propriet&agrave; attive del [`routerlogger.cfg`](src/main/config/routerlogger.cfg) all'avvio dell'applicazione (default: `false`).
* **`console.show.keys`**= elenco, separato da delimitatore, dei nomi delle chiavi i cui valori devono essere visualizzati in console a ogni iterazione (default: vuoto). Un eccessivo numero di chiavi da visualizzare provocher&agrave; lo scorrimento verticale della console, un effetto collaterale probabilmente indesiderato.
* **`console.show.keys.separator`**= delimitatore (o espressione regolare) usato per separare i nomi delle chiavi specificate nella propriet&agrave; `console.show.keys` (default: `,`). Scegliere un delimitatore che non contenga sequenze di caratteri presenti anche nei nomi delle chiavi.
* **`console.debug`**= in caso di errore, stampa messaggi dettagliati (default: `false`).


### Sorgente (modello di router)

La selezione del modello di modem router da interrogare si effettua configurando nel [`routerlogger.cfg`](src/main/config/routerlogger.cfg) la seguente propriet&agrave;:

* **`reader.class.name`**= identifica la classe che si occupa di ricavare dallo specifico modello di modem router le informazioni sullo stato della connessione tramite Telnet, e pu&ograve; assumere i valori seguenti:
	* [**`TpLink8970Reader`**](src/main/java/it/albertus/router/reader/TpLink8970Reader.java): lettura informazioni dal router **TP-Link TD-W8970 V1**.
	* [**`AsusDslN12EReader`**](src/main/java/it/albertus/router/reader/AsusDslN12EReader.java): lettura informazioni dal router **ASUS DSL-N12E**.
	* [**`AsusDslN14UReader`**](src/main/java/it/albertus/router/reader/AsusDslN14UReader.java): lettura informazioni dal router **ASUS DSL-N14U**.
	* [**`DLinkDsl2750Reader`**](src/main/java/it/albertus/router/reader/DLinkDsl2750Reader.java): lettura informazioni dal router **D-Link DSL-2750B**.
	* [**`DummyReader`**](src/main/java/it/albertus/router/reader/DummyReader.java): generazione di dati casuali (nessuna connessione n&eacute; lettura da alcun dispositivo), da usarsi solo a scopo di test.
	* nome completo (inclusi tutti i package separati da `.`) di una classe concreta che estenda [**`Reader`**](src/main/java/it/albertus/router/reader/Reader.java). Per maggiori informazioni, vedere il paragrafo [**Supporto di altri modelli di router**](#supporto-di-altri-modelli-di-router).

##### TP-Link TD-W8970 V1

* **`tplink.8970.command.info.adsl`**: comando da inviare al router per ottenere informazioni sullo stato della portante ADSL (default: `adsl show info`).
* **`tplink.8970.command.info.wan`**: comando da inviare al router per ottenere informazioni sullo stato della connessione ad Internet (default: non valorizzato, di conseguenza non vengono estratte queste informazioni).

##### ASUS DSL-N12E

* **`asus.dsln12e.command.info.adsl`**: comando da inviare al router per ottenere informazioni sullo stato della portante ADSL (default: `show wan adsl`).
* **`asus.dsln12e.command.info.wan`**: comando da inviare al router per ottenere informazioni sullo stato della connessione ad Internet (default: `show wan interface`).

##### ASUS DSL-N14U

* **`asus.dsln14u.command.info.adsl`**: comando da inviare al router per ottenere informazioni sullo stato della portante ADSL (default: `tcapi show Info_Adsl`).
* **`asus.dsln14u.command.info.wan`**: comando da inviare al router per ottenere informazioni sullo stato della connessione ad Internet (default: non valorizzato, di conseguenza non vengono estratte queste informazioni).

##### D-Link DSL-2750B

* **`dlink.2750.command.info.adsl.status`**: comando da inviare al router per ottenere informazioni sullo stato della portante ADSL (Up/Down) (default: `adsl status`).
* **`dlink.2750.command.info.adsl.snr`**: comando da inviare al router per ottenere informazioni sul rapporto segnale/rumore della linea ADSL (default: `adsl snr`).


### Destinazione (file, database, ...)

La selezione della modalit&agrave; di salvataggio delle informazioni si effettua configurando la seguente propriet&agrave;:
* **`writer.class.name`**: identifica la classe che si occupa del salvataggio delle informazioni, e pu&ograve; assumere i valori seguenti:
	* [**`CsvWriter`**](src/main/java/it/albertus/router/writer/CsvWriter.java): scrittura su file **CSV** (default).
	* [**`DatabaseWriter`**](src/main/java/it/albertus/router/writer/DatabaseWriter.java): scrittura su **database**.
	* [**`DummyWriter`**](src/main/java/it/albertus/router/writer/DummyWriter.java): nessuna scrittura (utile a scopo di test).
	* nome completo (inclusi tutti i package separati da `.`) di una classe concreta che estenda [**`Writer`**](src/main/java/it/albertus/router/writer/Writer.java). Per maggiori informazioni, vedere il paragrafo [**Modalit&agrave; di salvataggio alternative**](#modalit%C3%A0-di-salvataggio-alternative).

##### CSV

* **`csv.destination.path`**= percorso in cui saranno salvati i file CSV generati (default: directory dell'applicazione).
* **`csv.newline.characters`**= specifica come deve essere rappresentato il ritorno a capo nei file CSV generati. Se questa propriet&agrave; non &egrave; presente (o &egrave; commentata), viene utilizzata la rappresentazione specifica della piattaforma su cui si esegue l'applicazione. La propriet&agrave; pu&ograve; assumere uno tra i seguenti valori:
	* **`CRLF`**: scrive la coppia di caratteri di controllo `CR` (`0x0D`) e `LF` (`0x0A`) (`\r\n`, stile DOS/Windows).
	* **`LF`**: scrive il solo carattere `LF` (`0x0A`) (`\n`, stile Linux/OS X).
	* **`CR`**: scrive il solo carattere `CR` (`0x0D`) (`\r`).
* **`csv.field.separator`**= separatore dei campi utilizzato nei file CSV generati (default: `;`, compatibile con Microsoft Excel).
* **`csv.field.separator.replacement`**= poich&eacute; il testo da scrivere nei file CSV non deve mai contenere il separatore, tutte le eventuali occorrenze del separatore saranno sostituite da questa stringa (default: `,`).

##### Database

* **`database.driver.class.name`**= nome completo della classe del driver JDBC (ad es.: `oracle.jdbc.OracleDriver`).
* **`database.url`**= URL per il collegamento al database (ad es.: `jdbc:oracle:thin:@localhost:1521:XE`).
* **`database.username`**= nome utente per accedere al database.
* **`database.password`**= password per accedere al database.
* **`database.table.name`**= nome della tabella in cui saranno inseriti i dati (default: `router_log`).
* **`database.connection.validation.timeout.ms`**= tempo di attesa massimo su richiesta di verifica della validit&agrave; della connessione al database, in millisecondi (default: `2000` ms).
* **`database.timestamp.column.type`**= tipo di dato utilizzato per la colonna del *timestamp* in fase di creazione della tabella (default: `TIMESTAMP`).
* **`database.response.column.type`**= tipo di dato utilizzato per la colonna del *response_time_ms* in fase di creazione della tabella (default: `INTEGER`).
* **`database.info.column.type`**= tipo di dato utilizzato per tutte le altre colonne in fase di creazione della tabella (default: `VARCHAR(250)`).
* **`database.column.name.prefix`**= prefisso per i nomi delle colonne della tabella (default: `rl_`).
* **`database.column.name.max.length`**= lunghezza massima dei nomi delle colonne, superata la quale il nome viene troncato (default: `30`).


### Soglie

Le soglie permettono di specificare dei valori limite per uno o pi&ugrave; parametri di funzionamento del dispositivo; lo scopo &egrave; di poter incrementare la frequenza di interrogazione nelle situazioni critiche, in modo da aggiungere informazioni che potrebbero essere utili per la diagnosi di eventuali problemi della linea.

Nel caso delle linee ADSL, ad esempio, un parametro che determina la stabilit&agrave; della connessione e che pu&ograve; essere soggetto ad ampie e talvolta repentine variazioni, &egrave; il *rapporto segnale-rumore* (SNR). Utilizzando le soglie &egrave; possibile fare in modo che la frequenza di registrazione dei dati venga incrementata quando il valore del SNR scende al di sotto di una certa soglia.

Quando una soglia viene raggiunta, il periodo di registrazione passa da quello normale, definito dalla propriet&agrave; `logger.interval.normal.ms` (default 5 secondi), a quello definito dalla propriet&agrave; `logger.interval.fast.ms` (default un secondo).

#### Configurazione

Ogni soglia &egrave; costituita da una propriet&agrave; nel file [`routerlogger.cfg`](src/main/config/routerlogger.cfg) definita come segue:

**<code>threshold.*identificativo.univoco.soglia*</code>**= ***chiave*** ***operatore*** ***valore***

dove:
* **chiave**: chiave del parametro di interesse; deve corrispondere ad una chiave presente nella mappa delle informazioni estratte.
* **operatore**: operatore relazionale (di confronto) che determina la condizione di raggiungimento:
	* **`lt`** (oppure `<`): minore di...
	* **`le`** (oppure `<=`): minore o uguale a...
	* **`eq`** (oppure `=`, `==`): uguale a...
	* **`ge`** (oppure `>=`): maggiore o uguale a...
	* **`gt`** (oppure `>`): maggiore di...
	* **`ne`** (oppure `<>`, `!=`, `^=`): diverso da...
* **valore**: valore di soglia.

Il prefisso `threshold.` &egrave; obbligatorio perch&eacute; segnala all'applicazione che la propriet&agrave; riguarda una soglia.

L'*identificativo univoco soglia* pu&ograve; essere un qualsiasi testo senza spazi n&eacute; carattere `=`.

#### Esempio

Aggiungendo la riga seguente al file [`routerlogger.cfg`](src/main/config/routerlogger.cfg), si imposter&agrave; una soglia di 10.0 dB per il SNR; qualora il valore del SNR dovesse scendere al di sotto di 10.0 dB, la frequenza (o, pi&ugrave; precisamente, il periodo) di logging passerebbe da 5000 a 1000 millisecondi.

**`threshold.snr.down=downstreamNoiseMargin lt 100`**

#### Esclusioni

Pu&ograve; capitare che, al raggiungimento di una o pi&ugrave; soglie specifiche, non si desideri incrementare la frequenza di registrazione n&eacute; ricevere eventuali segnalazioni via email; il caso tipico &egrave; quello della velocit&agrave; di downstream agganciata, che in alcuni casi potrebbe essere inferiore al normale. In questi casi pu&ograve; comunque risultare utile un avviso nell'area di notifica e una particolare evidenziazione nella tabella a video (solo versione con interfaccia grafica), come normalmente avviene quando una soglia viene raggiunta. Per ottenere questo comportamento, valorizzare opportunamente le seguenti propriet&agrave; nel file [`routerlogger.cfg`](src/main/config/routerlogger.cfg):
* **`thresholds.excluded`**= elenco, separato da delimitatore, degli identificativi univoci delle soglie per le quali, al raggiungimento, non si desidera n&eacute; l'incremento della frequenza di registrazione, n&eacute; l'invio di segnalazioni via email.
* **`thresholds.excluded.separator`**= delimitatore (o espressione regolare) usato per separare gli identificativi univoci delle soglie specificati nella propriet&agrave; `thresholds.excluded` (default: `,`). Scegliere un delimitatore che non contenga sequenze di caratteri presenti anche negli identificativi delle soglie.


### Email

&Egrave; possibile configurare RouterLogger in modo che invii comunicazioni via email. Questa funzionalit&agrave; &egrave; particolarmente utile se si esegue il programma in un dispositivo dedicato o comunque non presidiato, come un [Raspberry Pi](https://www.raspberrypi.org).

In particolare si possono configurare i seguenti invii:

* **`log.email`**= invia una segnalazione per ogni errore che si verifica durante l'esecuzione del programma. Se la connessione non fosse disponibile al momento, ritenta periodicamente l'invio (default: `false`).
	* **`log.email.ignore.duplicates`**= evita l'invio di messaggi duplicati causati da eccezione persistente, ad esempio quando il programma non riesce a connettersi al router perch&eacute; spento (default: `true`).
* **`csv.email`**= invia i file CSV delle giornate precedenti, compressi in formato ZIP, uno per messaggio. Funziona solo se si imposta il salvataggio in formato CSV (default: `false`). A regime viene inviata un'email al giorno, ma *alla prima attivazione vengono inviati tutti i file CSV presenti nella cartella di destinazione dei CSV*, pu&ograve; quindi essere il caso di spostarli altrove o comprimerli prima di attivare questa opzione. I file inviati con successo vengono mantenuti in formato ZIP nella cartella di destinazione dei CSV (i relativi CSV non compressi vengono invece cancellati dopo l'invio per risparmiare spazio di archiviazione).
* **`thresholds.email`**= invia una segnalazione quando vengono raggiunte una o pi&ugrave; soglie (default: `false`). Per evitare l'invio di un numero eccessivo di messaggi, &egrave; disponibile la seguente propriet&agrave;:
	* **`thresholds.email.send.interval.secs`**= intervallo, in secondi, tra gli invii delle email relative al raggiungimento delle soglie. Le email conterranno tutti i dettagli sulle soglie raggiunte nell'intervallo (default: `3600` secondi).
	* **`thresholds.email.max.items`**= numero massimo di segnalazioni che possono essere incluse in un singolo messaggio (default: `50`). Valori eccessivi possono causare l'invio di email voluminose e un'elevata occupazione di memoria.

Per consentire l'invio delle email occorre avere un account di posta elettronica e configurare i parametri presenti nella sezione **Email** della configurazione, identificati dalle seguenti chiavi di configurazione:

* **`email.host`**= indirizzo del server SMTP da utilizzare per l'invio delle email.
* **`email.username`**= nome utente per l'autenticazione al server SMTP.
* **`email.password`**= password per l'autenticazione al server SMTP.
* **`email.from.name`**= nome da utilizzare come mittente.
* **`email.from.address`**= indirizzo email da utilizzare come mittente.
* **`email.to.addresses`**= indirizzi email dei destinatari "A" (separati da virgola).
* **`email.cc.addresses`**= indirizzi email dei destinatari "Cc" (separati da virgola).
* **`email.bcc.addresses`**= indirizzi email dei destinatari "Ccn" (separati da virgola).
* **`email.ssl.connect`**= specifica se utilizzare la connessione sicura SSL (default: `false`).
* **`email.port`**= porta SMTP del server (default: `25`).
* **`email.ssl.port`**= porta SMTP SSL del server (default: `465`).
* **`email.ssl.identity`**= specifica se effettuare il controllo di identit&agrave; del server secondo l'RFC 2595 (default: `false`).
* **`email.starttls.enabled`**= abilita l'uso del comando STARTTLS (default: `false`).
* **`email.starttls.required`**= richiede l'uso del comando STARTTLS (default: `false`).
* **`email.send.interval.secs`**= intervallo, in secondi, tra i tentativi di invio dei messaggi in caso di problemi (default: `60` secondi).
* **`email.connection.timeout`**= timeout in fase di connessione al server SMTP (default: `60000` ms).
* **`email.socket.timeout`**= timeout della connessione al server SMTP, una volta stabilita (default: `60000` ms).
* **`email.max.sendings.per.cycle`**= numero massimo di email che possono essere inviate contemporaneamente (default: `3`). Un valore eccessivo potrebbe far scattare il blocco dell'account utilizzato per l'invio per sospetto spamming. I messaggi non inviati saranno comunque man mano inviati ai successivi tentativi, intervallati come da propriet&agrave; `email.send.interval.secs`.
* **`email.max.queue.size`**= dimensione massima della coda delle email (default: `10` messaggi). Quando un messaggio non pu&ograve; essere inviato per assenza di rete o per superamento del numero massimo di email che possono essere inviate contemporaneamente, esso viene messo in una coda. Valori eccessivi possono causare un'elevata occupazione di memoria.

L'invio funziona senza problemi con [Gmail](https://mail.google.com), a patto di [consentire l'accesso alle applicazioni "meno sicure"](http://www.google.com/settings/security/lesssecureapps); per questo motivo &egrave; consigliabile creare un account dedicato a RouterLogger.


### Server web

RouterLogger pu&ograve; esporre una semplice interfaccia web che consente di:

* visualizzare lo **stato** della connessione ad Internet;
* **riavviare** l'applicazione;
* **connettere** l'applicazione al server Telnet del router;
* **disconnettere** l'applicazione dal server Telnet del router;
* **chiudere** l'applicazione.

Per attivare e configurare il server web sono disponibili le seguenti opzioni:

* **`server.enabled`**= abilita il server web (default: `false`).
* **`server.authentication`**= richiede autenticazione con nome utente e password per l'accesso all'interfaccia web. Per motivi di sicurezza, questa opzione dovrebbe essere mantenuta sempre attiva (default: `true`).
	* **`server.username`**= nome utente per l'accesso all'interfaccia web.
	* **`server.password`**= password per l'accesso all'interfaccia web.
* **`server.port`**= porta del server web (default: `8080`).
* **`server.compress.response`**= abilita la compressione dati, quando possibile (default: `false`).
* **`server.handler.root.enabled`**= abilita la pagina **Home** (default: `true`).
* **`server.handler.close.enabled`**= abilita la funzione **Chiudi** (default: `false`).
* **`server.handler.connect.enabled`**= abilita la funzione **Connetti** (default: `false`).
* **`server.handler.disconnect.enabled`**= abilita la funzione **Disconnetti** (default: `false`).
* **`server.handler.restart.enabled`**= abilita la funzione **Riavvia** (default: `false`).
* **`server.handler.status.enabled`**= abilita la funzione **Stato** (default: `true`).
* **`server.handler.status.refresh`**= aggiorna automaticamente la pagina **Stato** inserendo l'header HTTP *Refresh* (default: `false`).
* **`server.handler.status.refresh.secs`**= intervallo di aggiornamento della pagina **Stato**, in secondi (default: `5`).
* **`server.handler.json.enabled`**= abilita le risorse JSON (default: `true`).
* **`server.handler.json.refresh`**= aggiorna automaticamente le risorse JSON inserendo l'header HTTP *Refresh* (default: `true`).
* **`server.handler.json.refresh.secs`**= ntervallo di aggiornamento delle risorse JSON, in secondi (default: `0`, ossia *autodetermina*).
* **`server.log.request`**= regola il livello di registrazione delle richieste HTTP ricevute dall'applicazione. La propriet&agrave; pu&ograve; assumere uno tra i seguenti valori:
	* **`0`**: nessuna registrazione.
	* **`1`**: registrazione nel registro a video (default).
	* **`2`**: registrazione su file.
	* **`3`**: registrazione nel registro a video e su file.
* **`server.threads`**= numero di thread dedicati alla gestione delle richieste HTTP. Bench&eacute; valori inferiori o uguali al numero di thread del processore possano garantire una risposta pi&ugrave; pronta da parte del server, valori eccessivi possono provocare grave degrado prestazionale (default: `1`).

#### HTTPS

Per aumentare il livello di sicurezza della comunicazione con RouterLogger, &egrave; possibile abilitare il protocollo di comunicazione sicura HTTPS. Questa funzione richiede la presenza di un certificato SSL sul sistema che esegue l'applicazione. Per creare un certificato SSL in modo semplice e veloce, &egrave; possibile utilizzare il comando **keytool**, che prevede una sintassi del seguente tipo:

`keytool -genkey -alias "myalias" -keyalg "RSA" -keypass "mykeypass" -keystore "mykeystore.jks" -storepass "mystorepass" -validity 365`

Volendo invece utilizzare **OpenSSL**, una possibile sequenza di comandi &egrave; la seguente:
```
openssl genrsa -des3 -out server.key 1024
openssl req -new -key server.key -out server.csr
openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
openssl pkcs12 -export -out mykeystore.pfx -inkey server.key -in server.crt
```
**&Egrave; caldamente consigliato mantenere sempre aggiornata l'installazione di Java** per garantire i pi&ugrave; elevati livelli di sicurezza disponibili. Molti browser bloccano le connessioni con i server che non rispettano dei criteri minimi di sicurezza, e questi criteri vengono periodicamente rivisti.

La configurazione del protocollo HTTPS si effettua mediante le seguenti propriet&agrave;:
* **`server.ssl.enabled`**= Abilita il protocollo di comunicazione sicura HTTPS (default: `false`).  
* **`server.ssl.keystore.file`**= Puntamento al file *keystore* (prodotto da **keytool** o **OpenSSL**). 
* **`server.ssl.storepass`**= Password *keystore* (`storepass`).
* **`server.ssl.keypass`**= Password *chiave privata* (`keypass`).

Le seguenti propriet&agrave; consentono di personalizzare ulteriormente la configurazione della connessione sicura:
* **`server.ssl.keystore.type`**= Tipo di `KeyStore` (default: `JKS`).
* **`server.ssl.kmf.algorithm`**= Algoritmo utilizzato da `KeyManagerFactory` (default: dipendente dalla piattaforma).
* **`server.ssl.tmf.algorithm`**= Algoritmo utilizzato da `TrustManagerFactory` (default: dipendente dalla piattaforma).
* **`server.ssl.protocol`**= Protocollo SSL utilizzato (default: `TLS`).


### Client MQTT (Message Queuing Telemetry Transport)

RouterLogger pu&ograve; collegarsi ad un server (broker) MQTT per inviare messaggi [JSON](http://www.json.org) sullo stato della connessione e del programma stesso. Per attivare e configurare il client MQTT sono disponibili le seguenti opzioni:

* **`mqtt.enabled`**= abilita il client MQTT (default: `false`).
* **`mqtt.server.uri`**= elenco, separato da `|`, delle URI di collegamento al server MQTT. Tipicamente &egrave; sufficiente inserire un unico valore (es. `tcp://192.168.1.100:1883`).
* **`mqtt.username`**= nome utente per la connessione al server MQTT (se richiesto dal server).
* **`mqtt.password`**= password per la connessione al server MQTT (se richiesta dal server).
* **`mqtt.clean.session`**= se attiva, questa opzione disabilita la persistenza della sessione del client. Questa opzione dovrebbe essere mantenuta sempre attiva, in quanto RouterLogger si limita a pubblicare messaggi (default: `true`).
* **`mqtt.automatic.reconnect`**= specifica se tentare automaticamente la riconnessione in caso di disconnessione dovuta a problemi di rete (default: `true`).
* **`mqtt.connect.retry`**= specifica se ritentare automaticamente la connessione in caso di problemi di comunicazione (default: `true`).
* **`mqtt.client.id`**= identificativo univoco del client (default: `RouterLogger`).
* **`mqtt.connection.timeout`**= timeout della connessione in secondi, ossia il tempo di attesa massimo *in fase di connessione*, trascorso il quale si assume che il server non sia raggiungibile (default: `30` secondi).
* **`mqtt.keep.alive.interval`**= tempo di inattivit&agrave; massimo *durante la comunicazione* con il server, in secondi, trascorso il quale si assume che la comunicazione si sia interrotta (default: `60` secondi).
* **`mqtt.max.inflight`**= numero massimo di messaggi in transito (default: `10`).
* **`mqtt.version`**= versione del protocollo MQTT. La propriet&agrave; pu&ograve; assumere uno tra i seguenti valori:
	* **`0`**: se possibile *3.1.1*, in caso contrario *3.1* (default).
	* **`3`**: *3.1*.
	* **`4`**: *3.1.1*.
* **`mqtt.persistence.file.enabled`**= abilita la persistenza su file dei messaggi in transito con QoS 1 o 2. Se questa opzione &egrave; disattivata, i messaggi vengono mantenuti in memoria e quindi vengono persi in caso di arresto dell'applicazione (default: `false`).
* **`mqtt.persistence.file.custom`**= abilita la selezione di un percorso specifico per la persistenza dei messaggi in transito (default: `false`).
* **`mqtt.persistence.file.path`**= directory di base per la persistenza dei messaggi in transito (default: directory da cui &egrave; stato avviato il programma).
* **`mqtt.data.enabled`**= abilita la pubblicazione dei messaggi contenenti le informazioni sulla connessione (default: `true`).
* **`mqtt.data.topic`**= topic (argomento) dei messaggi contenenti le informazioni sulla connessione (default: `router/logger/data`).
* **`mqtt.data.qos`**= Livello QoS dei messaggi contenenti le informazioni sulla connessione (default: `0`).
* **`mqtt.data.retained`**= specifica se l'ultimo messaggio contenente le informazioni sulla connessione deve essere mantenuto sul server (default: `true`).
* **`mqtt.data.throttling.ms`**= intervallo minimo tra l'invio dei messaggi contenenti le informazioni sulla connessione. Di norma viene inviato un messaggio per ogni iterazione, ma pu&ograve; essere utile impostare questo valore per evitare un sovraccarico dei client riceventi o del broker (default `0`, ossia nessun intervallo minimo).
* **`mqtt.status.enabled`**= abilita la pubblicazione dei messaggi contenenti lo stato dell'applicazione (default: `true`).
* **`mqtt.status.topic`**= topic (argomento) dei messaggi contenenti lo stato dell'applicazione (default: `router/logger/status`).
* **`mqtt.status.qos`**= Livello QoS dei messaggi contenenti lo stato dell'applicazione (default: `2`).
* **`mqtt.status.retained`**= specifica se l'ultimo messaggio contenente lo stato dell'applicazione deve essere mantenuto sul server (default: `true`).
* **`mqtt.thresholds.enabled`**= abilita la pubblicazione dei messaggi contenenti le soglie raggiunte (default: `true`).
* **`mqtt.thresholds.topic`**= topic (argomento) dei messaggi contenenti le soglie raggiunte (default: `router/logger/data`).
* **`mqtt.thresholds.qos`**= Livello QoS dei messaggi contenenti le soglie raggiunte (default: `0`).
* **`mqtt.thresholds.retained`**= specifica se l'ultimo messaggio contenente le soglie raggiunte deve essere mantenuto sul server (default: `true`).
* **`mqtt.thresholds.throttling.ms`**= intervallo minimo tra l'invio dei messaggi contenenti le raggiunte. Di norma viene inviato un messaggio per ogni iterazione in cui risulta raggiunta almeno una soglia non esclusa, ma pu&ograve; essere utile impostare questo valore per evitare un sovraccarico dei client riceventi o del broker, anche in ragione del fatto che quando viene raggiunta una soglia, tipicamente la frequenza di interrogazione aumenta (default `0`, ossia nessun intervallo minimo).


## Estensione

#### Supporto di altri modelli di router

&Egrave; possibile estendere l'applicazione in modo da farla lavorare con qualsiasi modem router disponga di un'interfaccia **Telnet** che permetta di recuperare informazioni sullo stato della connessione. Per farlo, &egrave; sufficiente implementare una classe personalizzata che estenda la classe astratta [**`Reader`**](src/main/java/it/albertus/router/reader/Reader.java).

I metodi da implementare tassativamente sono i seguenti:
* **`login`**: effettua l'autenticazione al server Telnet comunicando le credenziali di accesso che, per semplicit&agrave;, vengono preventivamente lette dal file [`routerlogger.cfg`](src/main/config/routerlogger.cfg) e rese disponibili direttamente nel metodo sotto forma di parametri `username` e `password`.
* **`readInfo`**: interagisce con il server in modo da ottenere le informazioni sulla connessione ADSL e le restituisce sotto forma di mappa chiave-valore.

All'occorrenza pu&ograve; essere opportuno sovrascrivere anche i seguenti metodi, che non sono dichiarati `abstract` in [`Reader`](src/main/java/it/albertus/router/reader/Reader.java):
* **`logout`**: invia il comando di logout al server; l'implementazione predefinita invia `exit`, ma alcuni router possono richiedere un comando diverso, ad esempio `logout`, pertanto in questi casi il metodo deve essere opportunamente sovrascritto.
* **`getDeviceModel`**: restituisce una stringa contenente marca e modello del router (utile solo in visualizzazione); l'implementazione predefinita restituisce il nome della classe in esecuzione (senza package).
* **`release`**: libera risorse eventualmente allocate dal [`Reader`](src/main/java/it/albertus/router/reader/Reader.java), ad esempio file o connessioni a database. Normalmente non necessario.

>La classe astratta [**`Reader`**](src/main/java/it/albertus/router/reader/Reader.java) dispone di alcuni metodi di utilit&agrave; che permettono di interagire agevolmente con il server Telnet e che possono essere quindi utilizzati, oltre che sovrascritti, in caso di necessit&agrave;; in particolare:
* **`readFromTelnet(...)`**: legge l'output del server Telnet e lo restituisce come stringa.
* **`writeToTelnet(...)`**: invia comandi al server Telnet.

>&Egrave; inoltre possibile accedere alle propriet&agrave; di configurazione ([`routerlogger.cfg`](src/main/config/routerlogger.cfg)) tramite la variabile **`configuration`** dichiarata `protected` nella classe [`Reader`](src/main/java/it/albertus/router/reader/Reader.java).

>Per maggiori informazioni &egrave; possibile consultare la documentazione Javadoc inclusa nel codice sorgente.

Occorrer&agrave; quindi configurare l'applicazione in modo che faccia uso della classe realizzata modificando il file [`routerlogger.cfg`](src/main/config/routerlogger.cfg) e specificando come propriet&agrave; `reader.class.name` il nome completo della classe (inclusi tutti i package separati da `.`). Sar&agrave; inoltre necessario copiare nella directory `lib` dell'applicazione il JAR aggiuntivo contenente la classe esterna, in modo che sia aggiunta automaticamente al *classpath*.

#### Modalit&agrave; di salvataggio alternative

Nel caso in cui si volessero salvare le informazioni in formato diverso da CSV o database SQL, si pu&ograve; estendere la classe astratta [**`Writer`**](src/main/java/it/albertus/router/writer/Writer.java) e sar&agrave; ovviamente necessario implementare i due metodi seguenti:
* **`saveInfo`**: effettua il salvataggio delle informazioni ottenute, con le modalit&agrave; desiderate.
* **`release`**: libera risorse eventualmente allocate dal [`Writer`](src/main/java/it/albertus/router/writer/Writer.java), ad esempio file o connessioni a database.

>&Egrave; possibile accedere alle propriet&agrave; di configurazione ([`routerlogger.cfg`](src/main/config/routerlogger.cfg)) tramite la variabile **`configuration`** dichiarata `protected` nella classe [`Writer`](src/main/java/it/albertus/router/writer/Writer.java).

Occorrer&agrave; quindi configurare l'applicazione in modo che faccia uso della classe realizzata modificando il file [`routerlogger.cfg`](src/main/config/routerlogger.cfg) e specificando come propriet&agrave; `writer.class.name` il nome completo della classe (inclusi tutti i package separati da `.`). Sar&agrave; inoltre necessario copiare nella directory `lib` dell'applicazione il JAR aggiuntivo contenente la classe esterna, in modo che quest'ultima sia aggiunta automaticamente al *classpath*.


## Riconoscimenti

Quest'applicazione include componenti realizzati da [Apache Software Foundation](lib/license/apache-2.0.txt) e da [Eclipse Foundation](lib/license/eclipse-1.0.txt).

L'icona dell'applicazione &egrave; stata realizzata da [Pedram Pourang](http://tsujan.deviantart.com) (licenza [GPL](http://www.gnu.org/licenses/gpl.html)) e prelevata da [DeviantArt](http://www.deviantart.com).
