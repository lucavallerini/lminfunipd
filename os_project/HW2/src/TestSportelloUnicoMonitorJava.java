/**
 * Classe di test di uno sportello unico realizzato
 * con il monitor di Java.
 * 
 * Specifiche del sistema: tema 7 dell'homework 2.
 * 
 * @author Luca Vallerini - matr. 1110975
 * @version 0.3.20150418
 */

public class TestSportelloUnicoMonitorJava {

	private SportelloUnicoMonitorJava sportello;

	public TestSportelloUnicoMonitorJava() {
		sportello=new SportelloUnicoMonitorJava();
		System.out.println("\nLo sportello è ora APERTO!\n");
	}

	class User extends Thread implements Utente {

		private boolean urgent; // 'true' se servizio urgente, 'false' altrimenti
		private long maxWait; // tempo in ms di attesa massimo in coda, 0 se non specificato
		private long init; // istante in ms di ingresso in coda
		private int ticketnumber; // numero del biglietto
		private boolean expired; // 'true' se il tempo trascorso in coda ha superato 'maxWait', 'false' altrimenti
	
		public User(String nm, boolean urg, long wait) {
			super(nm);
			urgent = urg;
			maxWait = wait;
			expired = false;
		}
		
		public User(String nm, boolean urg) {
			new User(nm, urg, 0);
		}
		
		public void setTicketNumber(int ticketnumber) {
			this.ticketnumber = ticketnumber;
		}
		
		public int getTicketNumber() {
			return ticketnumber;
		}

		public boolean isUrgent() {
			return urgent;
		}

		public long getMaxWait() {
			return maxWait;
		}

		public long getInit() {
			return init;
		}
		
		public void setInit(long init) {
			this.init = init;
		}
		
		public Utente joinQueue(int ticketnumber, long init) {
			setTicketNumber(ticketnumber);
			setInit(init);
			return this;
		}
		
		public void setExpired() {
			expired = true;
		}
		
		public boolean isExpired() {
			return expired;
		}

		public void run() {
			sportello.retriveTicketNumber(this);
			sportello.retriveService(this);
		}
	}
	
	public static void main (String args[]) {
		// Inizializza il sistema
		System.out.println("AVVIO DEL SISTEMA IN CORSO...\n");
		System.out.println("La simulazione comporta l'arrivo di 15 utenti (Utente 1 ... Utente 15) \n" +
							"che richiedono un biglietto numerato progressivamente per il servizio \n" + 
							"normale (N) o urgente (U - probabilità del 25%).\n");
		System.out.println("Gli utenti specificano anche l'eventuale attesa massima che sono disposti \n" +
							"a sopportare in coda pena l'abbandono.\n");
		System.out.println("TERMINARE IL PROGRAMMA CON CTRL+C.\n");
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// nothing to do here
		}
		
		TestSportelloUnicoMonitorJava sport = new TestSportelloUnicoMonitorJava();
		
		/* Inizia la creazione degli utenti impostandone nome,
		 * tipo di servizio (urgente con probabilità del 25%) e
		 * attesa massima in coda (se prevista in [5000, 10000) ms, 
		 * altrimenti 0 con probabilità del 25%) e infine avvia i thread.
		 */
		for (int i=1; i<=15; i++) {
			try {
				Thread.sleep((long)(1000+Math.random()*2000));
			} catch (InterruptedException e) {
				// nothing to do here
			}
			sport.new User("Utente " + i, 
					((Math.random()-0.25<0) ? true : false), 
					((Math.random()-0.25<0) ? 0 : (5000+(long)(Math.random()*5000)))).start();
		}
		
	}
}