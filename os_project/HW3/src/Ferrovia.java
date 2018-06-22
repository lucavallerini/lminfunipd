import os.Util;
import os.ada.ADAThread;
import os.ada.Entry;
import os.ada.Guard;

/**
 * Realizzazione in ADA-Java dell'esercizio B
 * del compitino del 5 dicembre 2003.
 * 
 * @author Luca Vallerini - matr. 1110975
 */

public class Ferrovia {
	
	// nome server
	private final String NMSERVER = "Controllore";
	
	// entry/accept
	private final String ENTRAINA = "EntraInA";
	private final String ENTRAINB = "EntraInB";
	private final String ENTRAINC = "EntraInC";
	private final String ENTRAIND = "EntraInD";
	private final String ENTRAINE = "EntraInE";
	private final String ENTRAINF = "EntraInF";
	
	// semafori della ferrovia
	// true = verde, false = rosso
	private boolean semafori[] = new boolean[6];
	
	// tratte della ferrovia
	// true = occupata, false = libera
	private boolean tratte[] = new boolean[6];
	
	private Azione controllore;
	
	// thread che emula il comportamento dei treni
	class Treno extends ADAThread {
		int trattaCorrente;

		public Treno(String name, int trattaIniziale) {
			super(name);
			trattaCorrente = trattaIniziale;
			impostaPosizione(trattaCorrente);
			
		}
		
		private void impostaPosizione(int trattaCorrente) {
			tratte[trattaCorrente] = true;
			semafori[trattaCorrente] = false;
			
			if (trattaCorrente == 1 || trattaCorrente == 4) {
				semafori[1] = false;
				semafori[4] = false;
			}
		}
		
		public void run() {
			System.out.println(getName() + " è in " + trattaCorrente);
			while(true) {
				switch(trattaCorrente) {
				case 0: 
					trattaCorrente = (Integer) controllore.entryCall(NMSERVER, ENTRAINB).getParams();
					break;
				case 1:
					trattaCorrente = (Integer) controllore.entryCall(NMSERVER, ENTRAINC).getParams();
					break;
				case 2:
					trattaCorrente = (Integer) controllore.entryCall(NMSERVER, ENTRAIND).getParams();
					break;
				case 3:
					trattaCorrente = (Integer) controllore.entryCall(NMSERVER, ENTRAINE).getParams();
					break;
				case 4:
					trattaCorrente = (Integer) controllore.entryCall(NMSERVER, ENTRAINF).getParams();
					break;
				case 5:
					trattaCorrente = (Integer) controllore.entryCall(NMSERVER, ENTRAINA).getParams();
					break;
				default:
					// nada
				}
				System.out.println(getName() + " è in " + trattaCorrente);
				statoFerrovia();
				Util.rsleep(2000,3000);
			}
		}
		
	}
	
	/*
	 *  thread che si occupa della gestione dei semafori,
	 *  abilitandoli e disabilitandoli opportunamente
	 */
	class Azione extends ADAThread {
		
		public Azione() {
			super("Controllore");
		}
		
		public void run() {
			Select request = new Select();
			
			/** F => A
			 * request.add(when(semafori[0]) => 
			 * 		"ENTRAINA" [out: int trattaCorrente] {
			 * 			semafori[0] = false;
			 * 			tratte[0] = true;
			 * 			semafori[5] = true;
			 * 			tratte[5] = false;
			 * 			return new Integer(0);
			 * 		}
			 * )
			 */
			request.add(new Guard() {
				public boolean when() {
					return semafori[0];
				}
			}, ENTRAINA, new Entry() {
				public Object exec(Object inp) {
					semafori[0] = false;
					tratte[0] = true;
					semafori[5] = true;
					tratte[5] = false;
					return new Integer(0);
				}
			});
			
			/** A => B
			 * request.add(when(semafori[1]) =>
			 * 		"ENTRAINB" [out: int trattaCorrente] {
			 * 			semafori[1] = semafori[4] = false;
			 * 			tratte[1] = true;
			 * 			semafori[0] = true;
			 * 			tratte[0] = false;
			 * 			return new Integer(1);
			 * 		}
			 * )
			 */
			request.add(new Guard() {
				public boolean when() {
					return semafori[1];
				}
			}, ENTRAINB, new Entry() {
				public Object exec(Object inp) {
					semafori[1] = semafori[4] = false;
					tratte[1] = true;
					semafori[0] = true;
					tratte[0] = false;
					return new Integer(1);
				}
			});
			
			/** B => C
			 * request.add(when(semafori[2]) =>
			 * 		"ENTRAINC" [out: int trattaCorrente] {
			 * 			semafori[2] = false;
			 * 			tratte[2] = true;
			 * 			semafori[1] = semafori[4] = true;
			 * 			tratte[1] = false;
			 * 			return new Integer(2);
			 * 		}
			 * )
			 */
			request.add(new Guard() {
				public boolean when() {
					return semafori[2];
				}
			}, ENTRAINC, new Entry() {
				public Object exec(Object inp) {
					semafori[2] = false;
					tratte[2] = true;
					tratte[1] = false;
					semafori[1] = semafori[4] = true;
					return new Integer(2);
				}
			});
			
			/** C => D
			 * request.add(when(semafori[3]) =>
			 * 		"ENTRAIND" [out: int trattaCorrente] {
			 * 			semafori[3] = false;
			 * 			tratte[3] = true;
			 * 			semafori[2] = true;
			 * 			tratte[2] = false;
			 * 			return new Integer(3);
			 * 		}
			 * )
			 */
			request.add(new Guard() {
				public boolean when() {
					return semafori[3];
				}
			}, ENTRAIND, new Entry() {
				public Object exec(Object inp) {
					semafori[3] = false;
					tratte[3] = true;
					semafori[2] = true;
					tratte[2] = false;
					return new Integer(3);
				}
			});
			
			/** D => E
			 * request.add(when(semafori[4]) =>
			 * 		"ENTRAINE" [out: int trattaCorrente] {
			 * 			semafori[4] = semafori[1] = false;
			 * 			tratte[4] = true;
			 *			tratte[3] = false;
			 * 			semafori[3] = true;
			 * 			return new Integer(4);
			 * 		}
			 * )
			 */
			request.add(new Guard() {
				public boolean when() {
					return semafori[4];
				}
			}, ENTRAINE, new Entry() {
				public Object exec(Object inp) {
					semafori[4] = false;
					semafori[1] = false;
					tratte[4] = true;
					tratte[3] = false;
					semafori[3] = true;
					return new Integer(4);
				}
			});	
			
			/** E => F
			 * request.add(when(semafori[5]) =>
			 * 		"ENTRAINF" [out: int trattaCorrente] {
			 * 			semafori[5] = false;
			 * 			tratte[5] = true;
			 * 			tratte[4] = false;
			 * 			semafori[4] = semafori[1] = true;
			 * 			return new Integer(5);
			 * 		}
			 * )
			 */
			request.add(new Guard() {
				public boolean when() {
					return semafori[5];
				}
			}, ENTRAINF, new Entry() {
				public Object exec(Object inp) {
					semafori[5] = false;
					tratte[5] = true;
					tratte[4] = false;
					semafori[4] = semafori[1] = true;
					return new Integer(5);
				}
			});
			
			while(true) {
				request.accept();
			}
		}
	}
	
	private void statoFerrovia() {
		System.out.println("\nSemaforo A: " + semafori[0] + ", semaforo B: " + semafori[1] + ", semaforo C: " + semafori[2] +
				",\nsemaforo D: " + semafori[3] + ", semaforo E: " + semafori[4] + ", semaforo F: " + semafori[5] + ".");
		System.out.println("\nTratta A: " + tratte[0] + ", tratta B: " + tratte[1] + ", tratta C: " + tratte[2] +
				",\ntratta D: " + tratte[3] + ", tratta E: " + tratte[4] + ", tratta F: " + tratte[5] + ".\n");
	}

	public static void main(String[] args) {
		System.out.println("Avvio della ferrovia in corso...");
		Ferrovia ferrovia = new Ferrovia();
		
		System.out.println("Impostazione delle tratte e dei semafori...");
		for (int i = 0; i < ferrovia.semafori.length; i++) {
			ferrovia.semafori[i] = true;
		}
		ferrovia.statoFerrovia();
		
		System.out.println("Creazioni dei due treni in A e in C...");
		Treno trenoUno = ferrovia.new Treno("Treno 1", 0);
		Treno trenoDue = ferrovia.new Treno("Treno 2", 2);
		
		System.out.println("Creazione e avvio del controllore...");
		ferrovia.controllore = ferrovia.new Azione();
		ferrovia.controllore.start();
		ferrovia.statoFerrovia();
		Util.rsleep(2000);
		
		System.out.println("Avvio dei treni...");
		trenoUno.start();
		trenoDue.start();
	}

}
