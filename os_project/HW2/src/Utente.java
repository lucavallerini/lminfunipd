/**
 * Interfaccia di modellamento
 * di un utente da servizio ad
 * uno sportello.
 * 
 * @author Luca Vallerini - matr. 1110975
 * @version 0.1.20150417
 */

public interface Utente {

	// Recupera il nome dell'utente
	String getName();
	
	// 'true' se servizio urgente, 'false' altrimenti
	boolean isUrgent();
	
	// Recupera l'attesa massima in ms in coda se specificata, altrimenti 0
	long getMaxWait();
	
	// Recupera l'instante iniziale in ms dell'entrata in coda dell'utente
	long getInit();
	
	// Recupera il numero del biglietto ottenuto dall'utente
	int getTicketNumber();
	
	/* Accodamento dell'utente.
	 * 
	 * Memorizza in esso il numero del biglietto e l'istante in cui è entrato in coda.
	 */
	Utente joinQueue(int ticketnumber, long init);
	
	// Pone a 'true' il flag che permette di determinare se il tempo di attesa in coda è terminato
	void setExpired();
	
	// 'true' se l'attesa in coda ha superato il limite massimo (se specificato dall'utente), 'false' altrimenti
	boolean isExpired();
	
	// Imposta il numero del biglietto 
	void setTicketNumber(int ticketnumber);
	
	// Imposta l'istante (in ms) di ingresso in coda
	void setInit(long init);
	
}
