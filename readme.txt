Semplice applicazione console per la registrazione dello stato della connessione ADSL, che include un'implementazione specifica per il router TP-Link TD-W8970.

E' possibile estendere l'applicazione in modo da farla lavorare con qualsiasi router disponga di un'interfaccia telnet che permette di recuperare informazioni sullo stato della connessione.
Per farlo, e' sufficiente implementare una classe personalizzata che estenda la classe astratta RouterLogger e implementi almeno i metodi login, readInfo e saveInfo.

La classe RouterLogger dispone di diversi metodi di utilita' che permettono di interagire agevolmente con il server telnet.