import os.Util;
import os.ada.ADAThread;
import os.ada.Entry;
import os.ada.Guard;

/**
 * Realizzazione in ADA-Java della prova tipo "Autolavaggio".
 * 
 * @author Luca Vallerini - matr. 1110975
 */

public class LavaggioAutoADAJava extends LavaggioAuto {
	
	static LavaggioAutoADAJava lavaggioAuto = new LavaggioAutoADAJava();
	
	class VeicoloParziale extends ADAThread {
		BoxPrenotazione autolavaggio;
		
		public VeicoloParziale(BoxPrenotazione autolavaggio, String codice) {
			super(codice);
			this.autolavaggio = autolavaggio;
		}
		
		public void run() {
			System.out.println(getName() + " chiede il lavaggio parziale " + stampaSituazioneLavaggio() + ".");
			autolavaggio.entryCall(NMSERVER, PRPARZ);
			
			System.out.println(getName() + " entra nella zona A e viene lavata esternamente " + stampaSituazioneLavaggio() + ".");
			Util.rsleep(3000, 8000);

			autolavaggio.entryCall(NMSERVER, PGPARZ);
			System.out.println(getName() + " ha terminato il lavaggio parziale ed esce dall'autolavaggio " + stampaSituazioneLavaggio() + "."); 

		}
	}
	
	class VeicoloTotale extends ADAThread {
		BoxPrenotazione autolavaggio;
		
		public VeicoloTotale(BoxPrenotazione autolavaggio, String codice) {
			super(codice);
			this.autolavaggio = autolavaggio;
		}
		
		public void run() {
			
			System.out.println(getName() + " chiede il lavaggio totale " + stampaSituazioneLavaggio() + ".");
			autolavaggio.entryCall(NMSERVER, PRTOT);
			
			System.out.println(getName() + " entra nella zona A e viene lavata esternamente " + stampaSituazioneLavaggio() + ".");
			Util.rsleep(3000, 8000);
			
			autolavaggio.entryCall(NMSERVER, LVINT);
			System.out.println(getName() + " entra nella zona B per il lavaggio interno " + stampaSituazioneLavaggio() + ".");
			Util.rsleep(5000, 10000);

			autolavaggio.entryCall(NMSERVER, PGTOT);
			System.out.println(getName() + " ha terminato il lavaggio totale ed esce dall'autolavaggio " + stampaSituazioneLavaggio() + "."); 

		}
	}
	
	class BoxPrenotazione extends ADAThread {
		
		public BoxPrenotazione() {
			super(NMSERVER);
		}
		
		public void run() {
			Select prenotazione = new Select();
			
			/**
			 * request.add(when(zonaA < MAX_ZONA_A AND (PRTOT'COUNT == 0 OR zonaB == MAX_ZONA_B)) =>
			 * 		"PRPARZ" {
			 * 			prenotazioniParziale++;
			 * 			zonaA++;
			 * 		}
			 * )
			 */
			prenotazione.add(new Guard() {
				public boolean when() {
					return zonaA < MAX_ZONA_A && (entryCount(PRTOT) == 0 || zonaB == MAX_ZONA_B);
				}
			}, PRPARZ, new Entry() {
				public Object exec(Object inp) {
					prenotazioniParziale++;
					zonaA++;
					return null;
				}
			});
			
			/**
			 * request.add("PGPARZ" {
			 * 		lavaggiParziale++;
			 * 		zonaA--;
			 * 		}
			 * )
			 */
			prenotazione.add(PGPARZ, new Entry() {
				public Object exec(Object inp) {
					lavaggiParziale++;
					zonaA--;
					return null;
				}
			});
			
			/**
			 * request.add(when(zonaA < MAX_ZONA_A OR zonaB < MAX_ZONA_B) =>
			 * 		"PRTOT" {
			 * 			prenotazioniTotale++;
			 * 			zonaA++;
			 * 			zonaB++;
			 * 		}
			 * )
			 */
			prenotazione.add(new Guard() {
				public boolean when() {
					return zonaA < MAX_ZONA_A && zonaB < MAX_ZONA_B;
				}
			}, PRTOT, new Entry() {
				public Object exec(Object inp) {
					prenotazioniTotale++;
					zonaA++;
					zonaB++;
					return null;
				}
			});
			
			/** 
			 * request.add("LVINT" {
			 * 		zonaA--;
			 * 		}
			 * )
			 */
			prenotazione.add(LVINT, new Entry() {
				public Object exec(Object inp) {
					zonaA--;
					return null;
				}
			});
			
			/** 
			 * request.add("PGTOT" {
			 * 		lavaggiTotale++;
			 * 		zonaB--;
			 * 		}
			 * )
			 */
			prenotazione.add(PGTOT, new Entry() {
				public Object exec(Object inp) {
					lavaggiTotale++;
					zonaB--;
					return null;
				}
			});
			
			while (true) {
				prenotazione.accept();
			}
		}
	}

	public static void main(String[] args) {
		System.out.println("Apertura del box prenotazioni...\n");
		BoxPrenotazione box = lavaggioAuto.new BoxPrenotazione();
		box.start();
		
		Util.rsleep(2000); // do il tempo al server di registrare gli accept
		
		for (int i = 0; i < 20; i++) {
			if (Math.random() < 0.5) {
				lavaggioAuto.new VeicoloParziale(box, "Auto " + (i + 1)).start();;
			} else {
				lavaggioAuto.new VeicoloTotale(box, "Auto " + (i + 1)).start();
			}
			
			Util.rsleep(1000, 2000);
		}
	}

}
