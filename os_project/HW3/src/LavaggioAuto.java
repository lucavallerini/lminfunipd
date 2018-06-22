/**
 * Realizzazione in ADA-Java della prova tipo "Autolavaggio".
 * 
 * @author Luca Vallerini - matr. 1110975
 */

public abstract class LavaggioAuto {

	protected final String NMSERVER = "Box Prenotazione"; // nome del server
	
	protected final String PRPARZ = "prenotaParziale"; // richiesta per un lavaggio parziale
	protected final String PGPARZ = "pagaParziale"; // pagamento per il lavaggio parziale
	
	protected final String PRTOT = "prenotaTotale"; // richiesta per un lavaggio completo
	protected final String LVINT = "lavaInterno"; // proseguimento con la seconda parte del lavaggio completo
	protected final String PGTOT = "pagaTotale"; // pagamento per il lavaggio completo
	
	protected final int MAX_ZONA_A = 8; // numero massimo di posti disponibili nella zona A (lavaggio esterno)
	protected final int MAX_ZONA_B = 4; // numero massimo di posti disponibili nella zona B (lavaggio interno)
	
	protected int zonaA = 0; // posti occupati in A
	protected int zonaB = 0; // posti occupati in B
	
	protected int prenotazioniParziale; // totale prenotazioni per un lavaggio parziale
	protected int prenotazioniTotale; // totale prenotazioni per un lavaggio completo
	protected int lavaggiParziale; // totale lavaggi parziali terminati
	protected int lavaggiTotale; // totale lavaggi completi terminati
	
	/**
	 * Stampa la situazione dell'autolavaggio all'istante di chiamata.
	 * 
	 * @return Stringa che mostra la situazione dell'autolavaggio
	 */
	public String stampaSituazioneLavaggio() {
		return "(zona A: " + zonaA + "; zona B: " + zonaB + "; prParz: " + prenotazioniParziale + "; prTot: " + prenotazioniTotale +
				"; lvParz: " + lavaggiParziale + "; lvTot: " + lavaggiTotale + ")";
	}
}
