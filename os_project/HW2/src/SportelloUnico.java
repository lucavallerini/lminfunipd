/**
 * Interfaccia di modellamento di uno
 * sportello unico.
 * 
 * @author Luca Vallerini - matr. 1110975
 * @version 0.1.20150417
 */

public interface SportelloUnico {

	/*
	 * Consente di prendere il biglietto numerato
	 * e di accodarsi di conseguenza in attesa 
	 * del proprio turno.
	 */
	void retriveTicketNumber(Utente user);

	/*
	 * Se è il proprio turno consente di poter
	 * usufruire del servizio dello sportello.
	 * 
	 * Regola anche l'abbandono anticipato della 
	 * coda da parte di utenti che ritengono di
	 * avere atteso troppo.
	 */
	void retriveService(Utente user);
}
