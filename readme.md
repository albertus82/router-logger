**RouterLogger** &egrave; una semplice applicazione Java in riga di comando per la registrazione dello stato della connessione ADSL, che include un'implementazione specifica per il router **TP-Link TD-W8970**. Il funzionamento &egrave; basato sull'interfaccia telnet esposta dalla maggior parte dei modem/router ADSL odierni.

Per eseguire l'applicazione in ambiente Windows, &egrave; occorre:

1. scaricare una release, possibilmente la pi&ugrave; recente, in formato ZIP;
2. scompattarla in una cartella a piacimento in cui l'utente abbia diritti di scrittura;
3. modificare il file <code>routerlogger.cfg</code> configurando le seguenti propriet&agrave;:
  * <code>**router.address**</code>=indirizzo IP del router (solitamente <code>192.168.0.1</code> o <code>192.168.1.1</code>);
  * <code>**router.port**</code>=porta telnet del router, normalmente <code>23</code>;
  * <code>**router.username**</code>=nome utente per accedere al router (normalmente &egrave; lo stesso usato per accedere all'interfaccia grafica tramite browser);
  * <code>**router.password**</code>=password per accedere al router (normalmente &egrave; la stessa usata per accedere all'interfaccia grafica tramite browser);
4. avviare l'applicazione richiamando il file batch <code>routerlogger.bat</code> (deve essere presente la variabile di ambiente <code>JAVA_HOME</code>).

&Egrave; possibile estendere l'applicazione in modo da farla lavorare con qualsiasi router disponga di un'interfaccia **telnet** che permetta di recuperare informazioni sullo stato della connessione. Per farlo, &egrave; sufficiente implementare una classe personalizzata che estenda la classe astratta <code>**RouterLogger**</code>, la quale dispone di diversi metodi di utilit&agrave; che permettono di interagire agevolmente con il server telnet e che possono comunque essere sovrascritti in caso di necessit&agrave;.

I metodi da implementare sono i seguenti:
* <code>**login**</code>: effettua l'autenticazione telnet.
* <code>**readInfo**</code>: interagisce con il server per ottenere le informazioni sulla connessione e poi restituirle sotto forma di mappa.
* <code>**saveInfo**</code>: effettua il salvataggio delle informazioni ottenute (ad esempio su database o su file).

All'occorrenza pu&ograve; essere opportuno sovrascrivere anche i seguenti metodi, che non sono dichiarati <code>abstract</code> in <code>RouterLogger</code>:
* <code>**release**</code>: libera risorse eventualmente allocate dal programma (file, connessioni a database, ecc.); l'implementazione predefinita non fa nulla.
* <code>**logout**</code>: invia il comando di logout al server; l'implementazione predefinita invia <code>logout</code>, ma alcuni router possono richiedere un comando diverso, ad esempio <code>exit</code>, pertanto in questi casi il metodo deve essere opportunamento sovrascritto.
* <code>**getDeviceModel**</code>: restituisce una stringa contenente marca e modello del router (utile solo in visualizzazione); l'implementazione predefinita restituisce <code>null</code>, determinando cos&igrave; l'assenza dell'informazione.
