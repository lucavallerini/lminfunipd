import java.util.*;

/**
 * Classe che modella uno sportello unico realizzato
 * con il monitor di Java.
 * 
 * Specifiche del sistema: tema 7 dell'homework 2.
 * 
 * @author Luca Vallerini - matr. 1110975
 * @version 0.4.20150418
 */

public class SportelloUnicoMonitorJava implements SportelloUnico {

	private boolean busy;	// flag per l'occupazione dello sportello
	private boolean outoforder; 	// flag per lo sportello in 'fuori servizio'
	private long last; // ultimo istante in cui lo sportello è stato in 'fuori servizio'
	private Queue usersqueue; 	// coda degli utenti da servire
	private Clock clock;	// orologio che periodicamente controlla il tempo di attesa degli utenti
	private OutOfOrder outoforderservice; // servizio ausiliario che periodicamente manda in 'fuori servizio ' lo sportello

	/*
	 * Avvia lo sportello e avvia i thread di supporto
	 * 'Orologio' e 'Fuori servizio'.
	 */
	public SportelloUnicoMonitorJava () {
		busy = false;
		outoforder = false;
		last = 0;
		usersqueue = new Queue();
		
		clock = new Clock();
		clock.start();
		
		outoforderservice = new OutOfOrder();
		outoforderservice.start();
	}
	
	/*
	 * Si occupa dell'assegnazione del biglietto e del corretto
	 * accodamento degli utenti in base al servizio richiesto.
	 * 
	 * Inoltre è responsabilità di tale classe ogni gestione che
	 * coinvolge la coda o un utente in essa contenuta.
	 */
	class Queue {
		
		boolean ticketmachine;
		int ticketnumber;
		int curg;
		private Vector<Utente> queue;

		public Queue() {
			ticketmachine=true;
			ticketnumber=0;
			curg=0;
			queue=new Vector<Utente>();
		}
		
		synchronized int getTicket(Utente user) {
			while (!ticketmachine)
				try {
					wait();
				} catch (InterruptedException e) {
					// nothing to do here
				}
				
			ticketmachine = false;
			
			// Ritiro il biglietto
			ticketnumber++;
			
			// Effettuo l'accodamento
			if (user.isUrgent()) {
				queue.add(curg, user.joinQueue(ticketnumber, System.currentTimeMillis()));
				curg++;
			} else 
			    queue.add(user.joinQueue(ticketnumber, System.currentTimeMillis()));
				
			System.out.println(user.getName() + " ha ottenuto il biglietto " + ticketnumber + (user.isUrgent() ? "U" : "N") +
					" (attesa max: " + ((user.getMaxWait() == 0) ? "nessuna)." : (user.getMaxWait() +  " ms).")));
			
			ticketmachine = true;
			notifyAll();
			
			return ticketnumber;
		}

		synchronized Utente removeUser(Utente user) {
			while (!ticketmachine) {
				try {
					wait();
				} catch (InterruptedException e) {
					// nothing to do here
				}
			}
			
			ticketmachine = false;

			Utente tmp = queue.remove(queue.indexOf(user));
			if (tmp.isUrgent())
				curg--;
			
			ticketmachine = true;
			notifyAll();
			
			return tmp;
		}
		
		Utente removeFirst() throws NoSuchElementException {
			return  removeUser(queue.firstElement());
		}
		
		boolean isFirst(int nt) {
			return (!queue.isEmpty()) ? (queue.firstElement().getTicketNumber()==nt) : false;
		}

		Utente getFirst() throws NoSuchElementException {
			return queue.firstElement();
		}

		public synchronized void leaveQueue() {
			while (!ticketmachine) {
				try {
					wait();
				} catch (InterruptedException e) {
					// nothing to do here
				}
			}
			
			ticketmachine = false;
			
			for (int i = 0; i < queue.size(); i++) {
				Utente tmp = queue.get(i);
				if ((tmp.getMaxWait() != 0) && (System.currentTimeMillis() - tmp.getInit() - tmp.getMaxWait() > 0))
					tmp.setExpired();
			}
			
			ticketmachine = true;
			notifyAll();
			
		}
	}
	
	/*
	 * Thread 'Fuori servizio' che periodicamente
	 * manda in fuori servizio lo sportello.
	 */
	class OutOfOrder extends Thread {
		public OutOfOrder() {
			super("Fuori servizio");
		}

		public void run() {
			while (true) {
				outOfOrderService();
			}
		}
	}
	
	/*
	 * Thread 'Orologio' per il controllo
	 * del tempo di attesa in coda di ogni
	 * singolo utente. Se il tempo di attesa
	 * supera l'eventuale attesa massima
	 * specificata dall'utente, quest'ultimo 
	 * lascia la coda.
	 */
	class Clock extends Thread {
		
		Clock() {
			super("Orologio");
		}

		public void run() {
			while (true) {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					// nothing to do here
				}
				
				usersqueue.leaveQueue();
			}
		}
	}

	/*
	 * Recupera il biglietto e accoda opportunamente in base al servizio.
	 */
	public void retriveTicketNumber(Utente user) {
		usersqueue.getTicket(user);
	}

	/*
	 * Richiedi il servizio allo sportello (modellato come attesa casuale
	 * tra i 5 e i 10 s). Lo stesso si occupa anche dell'abbandono degli
	 * utenti che sono rimasti per troppo tempo in attesa in coda.
	 */
	public synchronized void retriveService(Utente user) {
		while (busy || outoforder || (!usersqueue.isFirst(user.getTicketNumber()) && !user.isExpired())) {
			try {
				wait();
			} catch (InterruptedException e) {
				// nothing to do here
			}
		}

		if (user.isExpired()) {
			System.out.println("\n! " + usersqueue.removeUser(user).getName() +
					" ha abbandonato la coda per la troppa attesa !\n");
		} else {
			busy = true;
		
			System.out.println("\n>>> È il turno dell'utente con il biglietto " + usersqueue.getFirst().getTicketNumber() +
					(usersqueue.getFirst().isUrgent() ? "U" : "N") + " <<<\n");
			Utente tmp = usersqueue.removeFirst();
			System.out.println("\n" + tmp.getName() + " è allo sportello.\n");
		
			try {
				Thread.sleep(((long)(5000 + Math.random()*5000)));
			} catch (InterruptedException e) {
				// nothing to do here
			}
			
			System.out.println("\n" + tmp.getName() + " ha terminato il suo turno e lascia lo sportello.\n");
		
			busy = false;
		}
		
		notifyAll();
	}
	
	/*
	 * Manda periodicamente lo sportello fuori servizio
	 * per un breve intervallo di tempo ([1,2) s).
	 */
	private void outOfOrderService() {
		synchronized (this) {
			if (!(!busy && (System.currentTimeMillis() - last - (5000 + Math.random()*5000) > 0))) {
				// nothing to do here
			} else {
		
			outoforder = true;
			System.out.println("\n!! Lo sportello è momentaneamente fuori servizio !!\n");
		
			try {
				Thread.sleep((long)(1000+Math.random()*2000));
			} catch (InterruptedException e) {
				// nothing to do here
			}
		
			last = System.currentTimeMillis();
			System.out.println("\n!! Lo sportello riprende il servizio !!\n");
			
			outoforder = false;
			notifyAll();
			}
		}
	}
}
