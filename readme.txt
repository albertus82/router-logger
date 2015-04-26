RouterLogger e' una semplice applicazione console per la registrazione dello stato della connessione ADSL, che include un'implementazione specifica per il router TP-Link TD-W8970. Il funzionamento e' basato sull'interfaccia telnet esposta dalla maggior parte dei modem/router ADSL odierni.

E' possibile estendere l'applicazione in modo da farla lavorare con qualsiasi router disponga di un'interfaccia telnet che permetta di recuperare informazioni sullo stato della connessione.
Per farlo, e' sufficiente implementare una classe personalizzata che estenda la classe astratta RouterLogger.

La classe RouterLogger dispone di diversi metodi di utilita' che permettono di interagire agevolmente con il server telnet e che possono comunque essere sovrascritti in caso di necessita'.