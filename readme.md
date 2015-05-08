**RouterLogger** &egrave; una semplice applicazione Java console per la registrazione dello 
stato della connessione ADSL, che include un'implementazione specifica per il router **TP-Link TD-W8970**. 
Il funzionamento &egrave; basato sull'interfaccia telnet esposta dalla maggior parte dei modem/router ADSL odierni.

&Egrave; possibile estendere l'applicazione in modo da farla lavorare con qualsiasi router disponga 
di un'interfaccia **telnet** che permetta di recuperare informazioni sullo stato della connessione.
Per farlo, &egrave; sufficiente implementare una classe personalizzata che estenda la classe astratta <code>RouterLogger</code>.

La classe <code>RouterLogger</code> dispone di diversi metodi di utilit&agrave; 
che permettono di interagire agevolmente con il server telnet e che possono comunque
essere sovrascritti in caso di necessit&agrave;.
