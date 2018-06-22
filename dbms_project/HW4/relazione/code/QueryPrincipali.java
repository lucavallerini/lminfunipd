import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Universita' degli Studi di Padova
 * Dipartimento di Ingegneria dell'Informazione
 * Corso di laurea magistrale in Ingegneria Informatica
 * 
 * Basi di dati - Anno Accademico 2014/2015
 * 
 * Homework 4 - Interrogazioni principali per la base di dati 'fumetteria'.
 * 
 * @author Luca Vallerini - matr. 1110975
 * @version 0.1.20150614
 */
public class QueryPrincipali {

	// Informazioni essenziali per la connessione al database
	private static final String DRIVER = "org.postgresql.Driver";
	private static final String DATABASE = "jdbc:postgresql://localhost/fumetteria";
	private static final String USER = "luca";
	private static final String PASSWORD = "firefox";

	// Recupero dei prodotto depositati in una casella
	private static final String QUERY_1 = "Recupera i prodotti depositati nella casella del cliente tesserato Luca Vallerini: \n" + 
			"di questi, viene mostrato il titolo della serie, il numero del volume, la quantita' di volumi \n" + 
			"presenti in casella e il prezzo per singolo volume;";

	// Inserimento di un nuovo volume a catalogo
	private static final String QUERY_2 = "Oggi e' arrivato in fumetteria il volume 1 di \"Gen di Hiroshima\": \n" +
			"tale volume viene inserito a catalogo con i sequenti parametri: titolo=Gen di Hiroshima, \n" + 
			"volume=1, prezzo=E 42.00, ISBN=1522583690251, pagine a colori=nessuna, numero di pagine=1000, \n" +
			"sovraccoperta=nessuna, autore=Keiji Nakazawa, anno=1982, editore=Hikari, \n " + 
			"stato edizione=In corso, periodicita'=Irregolare, volumi arrivati=3;";

	private static final String QUERY_3 = "Ordina in ordine decrescente gli editori in base al prezzo medio dei loro prodotti a catalogo, \n" + 
			"indicando prezzo medio e numero di volumi pubblicati a catalogo;";

	private static final String QUERY_4 = "Lista della spesa: l'ultimo numero di Monster, primo numero di Dr. Slump e primo numero di Maison Ikkoku. \n" +
			"L'ordine di acquisto viene effettuato dal 'Titolare' a beneficio del cliente con tessera numero 4;";

	// insertSQLErrorException handling
	private static void insertSQLExceptionHandler(SQLException e) {
		System.out.printf("Errore di accesso al database:%n");
		System.out.printf("- Messaggio: %s%n", e.getMessage());
		System.out.printf("- Codice di stato SQL: %s%n", e.getSQLState());
		System.out.printf("- Codice di errore SQL: %s%n", e.getErrorCode());
		System.out.printf("%n");

	}

	private static void connectSQLExceptionHandler(SQLException e) {
		System.out.printf("Errore di connessione:%n");

		while (e != null) {
			System.out.printf("- Messaggio: %s%n", e.getMessage());
			System.out.printf("- Codice di stato SQL: %s%n", e.getSQLState());
			System.out.printf("- Codice di errore SQL: %s%n", e.getErrorCode());
			System.out.printf("%n");
			e = e.getNextException();
		}
	}

	public static void main(String[] args) {
		System.out.println("Scegliere un opzione per vedere eseguita la query corrispondente:");
		System.out.println("1) " + QUERY_1);
		System.out.println("2) " + QUERY_2);
		System.out.println("3) " + QUERY_3);
		System.out.println("4) " + QUERY_4);

		Scanner input = new Scanner(System.in);
		int choice = new Scanner(System.in).nextInt();

		// Connessione al database
		Connection connessione = null;

		// PreparedStatement e ResultSet
		PreparedStatement queryPS = null;
		PreparedStatement verifyQuery = null;
		ResultSet queryRS = null;

		// Caricamento del driver
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.printf("Driver %s non trovato: %s.%n", DRIVER, e.getMessage());
			System.exit(-1);
		}

		// Connessione al DB
		try {
			connessione = DriverManager.getConnection(DATABASE, USER, PASSWORD);
		} catch (SQLException e) {
			connectSQLExceptionHandler(e);
			System.exit(-1);
		}

		switch (choice) {
		case 1:
			String query1 = "SELECT S.titolo AS prodotto, numero, prezzo_cadauno, quantita, prezzo_cadauno*quantita AS prezzo_totale FROM " +
					"serie AS S INNER JOIN (edizione AS E INNER JOIN " + 
					"(SELECT V.numvol AS numero, V.prezzofin AS prezzo_cadauno, P.quantita AS quantita, V.edizione FROM " + 
					"(volume AS V INNER JOIN (incasellamento AS I INNER JOIN " + 
					"((SELECT codicefiscale FROM tesserato WHERE nome = 'Luca' AND cognome = 'Vallerini') AS TS INNER JOIN tesseramento AS T " + 
					"ON TS.codicefiscale = T.tesserato) AS A ON A.acctesserato = I.acctesserato) AS P ON P.prodotto = V.prodotto)) AS VP ON " + 
					"VP.edizione = E.prodotto) AS VPE ON VPE.serie = S.codserie " +
					"GROUP BY S.titolo, numero, prezzo_cadauno, quantita, prezzo_totale ORDER BY numero ASC";

			try {
				queryPS = connessione.prepareStatement(query1);
			} catch (SQLException e) {
				connectSQLExceptionHandler(e);
				System.exit(-1);
			}

			try {
				queryRS = queryPS.executeQuery();

				System.out.println("La casella del cliente Luca Vallerini contiene i seguenti elementi:");
				while (queryRS.next()) {
					System.out.println("- " + queryRS.getString(1) + " " + queryRS.getInt(2)+ ", E " + queryRS.getDouble(3) + " cad., " +
							queryRS.getInt(4) + " pezzi, tot: E " + queryRS.getDouble(5));
				}
			} catch (SQLException e) {
				insertSQLExceptionHandler(e);
			}
			break;

		case 2:
			String query2 = "INSERT INTO serie (codserie, titolo, autore, anno, media, statoserie) " +
					"SELECT md5('Gen di Hiroshima'||'Keiji Nakazawa'||1982||'Manga'), 'Gen di Hiroshima', 'Keiji Nakazawa', 1982, 'Manga', 'Completa' " + 
					"WHERE NOT EXISTS (SELECT codserie FROM serie WHERE codserie = md5('Gen di Hiroshima'||'Keiji Nakazawa'||1982||'Manga')); " + 
					"INSERT INTO prodotto (codprod, datains) VALUES (md5('Gen di Hiroshima'||'Keiji Nakazawa'||1982||'Manga'||2015-06-15), DEFAULT); " + 
					"INSERT INTO edizione (prodotto, period, col, inizpubbl, statoed, sovracop, numpag, editore, serie) " +
					"VALUES (md5('Gen di Hiroshima'||'Keiji Nakazawa'||1982||'Manga'||2015-06-15), 'Irregolare', FALSE, '2015-06-15', 'In corso', FALSE, 1000, " +
					"(SELECT piva FROM editore WHERE nome LIKE '%Hikari%') , md5('Gen di Hiroshima'||'Keiji Nakazawa'||1982||'Manga')); " +
					"INSERT INTO prodotto (codprod, datains) VALUES (md5('1522583690251'||1), DEFAULT); " +
					"INSERT INTO volume (prodotto, datapubbl, prezzofin, numvol, disp, isbn, edizione) " +
					"VALUES (md5('1522583690251'||1), '2015-06-15', '42', 1, 3, '1522583690251', md5('Gen di Hiroshima'||'Keiji Nakazawa'||1982||'Manga'||2015-06-15));";
			
			String verifyQuery2 = "SELECT S.titolo, V.numvol AS volume, V.prezzofin AS prezzo, V.disp AS quantita, E.period AS periodicita, " +
					"E.col AS pagine_colori, E.numpag AS numero_pag, V.isbn, editore.nome AS editore FROM " + 
					"volume AS V INNER JOIN edizione AS E ON V.edizione = E.prodotto INNER JOIN serie AS S ON E.serie = S.codserie " +
					"INNER JOIN editore ON E.editore = piva WHERE isbn = '1522583690251';";
			
			try {
				queryPS = connessione.prepareStatement(query2);
				verifyQuery = connessione.prepareStatement(verifyQuery2);
			} catch (SQLException e) {
				connectSQLExceptionHandler(e);
				System.exit(-1);
			}

			try {
				System.out.println("Inserimento a catalogo in corso...");
				queryPS.execute();

				ResultSet verifyQueryAfter = verifyQuery.executeQuery();
				System.out.println("Dopo l'inserimento:");
				while(verifyQueryAfter.next()) {
					System.out.println(verifyQueryAfter.getString(1) + " " + verifyQueryAfter.getInt(2) + ", E " + verifyQueryAfter.getDouble(3) + 
							", " + verifyQueryAfter.getInt(4) + " prodotti,\nperiodicita': " + verifyQueryAfter.getString(5) + ", pagine a colori: " + 
							verifyQueryAfter.getBoolean(6) + ", numero di pagine: " + verifyQueryAfter.getInt(7) + ",\nISBN " + verifyQueryAfter.getString(8) + 
							", edito da " + verifyQueryAfter.getString(9));
				}
			} catch (SQLException e) {
				insertSQLExceptionHandler(e);
			}
			break;

		case 3:
			String query3 = "SELECT editore.nome AS editore, COUNT(volume.numvol) AS numero_vol_pubblicati, AVG(prezzofin) AS prezzo_medio FROM " +
					"editore INNER JOIN edizione ON editore.piva = edizione.editore INNER JOIN volume ON edizione.prodotto = volume.edizione " +
					"GROUP BY editore.nome ORDER BY prezzo_medio DESC";
			try {
				queryPS = connessione.prepareStatement(query3);
			} catch (SQLException e) {
				connectSQLExceptionHandler(e);
				System.exit(-1);
			}

			try {
				queryRS = queryPS.executeQuery();

				System.out.println("Classifica degli editori per prezzo medio di vendita:");
				int i = 0;
				while (queryRS.next()) {
					System.out.println(++i + ") " + queryRS.getString(1) + ", " + queryRS.getInt(2)+ " pubblicazioni, prezzo medio di E " + 
							queryRS.getDouble(3));
				}
			} catch (SQLException e) {
				insertSQLExceptionHandler(e);
			}
			break;

		case 4:
			String query4 = "DROP TABLE IF EXISTS prezzo_totale, prezzo_elem_1, prezzo_elem_2, prezzo_elem_3, prodotto_1, prodotto_2, prodotto_3, sconto_tessera, codice_ordine_ult;" +
					"SELECT SUM(V.prezzofin)-SUM(V.prezzofin)*((SELECT sconto FROM tesseramento INNER JOIN tessera ON tesseramento.tessera = tessera.codtess WHERE codtess = 4))/100 " + 
					"INTO prezzo_totale FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto  " +
					"WHERE (S.titolo = 'Dr. Slump' AND V.numvol = 1) OR (S.titolo = 'Maison Ikkoku' AND V.numvol = 1) OR (S.titolo = 'Monster' AND V.numvol = 9); " +
					"SELECT V.prezzofin INTO prezzo_elem_1 FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto " + 
					"WHERE (S.titolo = 'Dr. Slump' AND V.numvol = 1); " +
					"SELECT V.prodotto INTO prodotto_1 FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto " + 
					"WHERE (S.titolo = 'Dr. Slump' AND V.numvol = 1); " +
					"SELECT V.prezzofin INTO prezzo_elem_2 FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto " + 
					"WHERE (S.titolo = 'Maison Ikkoku' AND V.numvol = 1); " +
					"SELECT V.prodotto INTO prodotto_2 FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto " + 
					"WHERE (S.titolo = 'Maison Ikkoku' AND V.numvol = 1); " +
					"SELECT V.prezzofin INTO prezzo_elem_3 FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto " + 
					"WHERE (S.titolo = 'Monster' AND V.numvol = 9); " +
					"SELECT V.prodotto INTO prodotto_3 FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto " + 
					"WHERE (S.titolo = 'Monster' AND V.numvol = 9);  " +
					"SELECT sconto INTO sconto_tessera FROM tesseramento INNER JOIN tessera ON tesseramento.tessera = tessera.codtess WHERE codtess = 4; " +
					"INSERT INTO ordine (statoord, datains, prezzotot, tipoord, tesserato) VALUES ('Evaso', '2015-12-18', (SELECT * FROM prezzo_totale), 'Acquisto', (SELECT tesserato FROM tesseramento WHERE tessera = 4)); " +
					"SELECT MAX(codord) INTO codice_ordine_ult FROM ordine; " +
					"INSERT INTO merce (ordine, prodotto, quantita, prezzoelem) VALUES ((SELECT * FROM codice_ordine_ult), (SELECT * FROM prodotto_1), 1, (SELECT * FROM prezzo_elem_1)); " +
					"INSERT INTO merce (ordine, prodotto, quantita, prezzoelem) VALUES ((SELECT * FROM codice_ordine_ult), (SELECT * FROM prodotto_2), 1, (SELECT * FROM prezzo_elem_2)); " +
					"INSERT INTO merce (ordine, prodotto, quantita, prezzoelem) VALUES ((SELECT * FROM codice_ordine_ult), (SELECT * FROM prodotto_3), 1, (SELECT * FROM prezzo_elem_3)); " +
					"INSERT INTO lavorazione (ordine, acc, datalav) VALUES ((SELECT * FROM codice_ordine_ult), (SELECT codacc FROM account WHERE username = 'Titolare'), '2015-12-18'); " + 
					"DROP TABLE IF EXISTS prezzo_totale, prezzo_elem_1, prezzo_elem_2, prezzo_elem_3, prodotto_1, prodotto_2, prodotto_3, sconto_tessera, codice_ordine_ult;";

			try {
				queryPS = connessione.prepareStatement(query4);
			} catch (SQLException e) {
				connectSQLExceptionHandler(e);
				System.exit(-1);
			}

			try {
				System.out.println("Inserimento dell'ordine in corso...");
				queryPS.execute();

				System.out.println("Riepilogo:");
				String verifyQuery4 = "SELECT serie.titolo, volume.numvol, merce.quantita, volume.prezzofin, tesserato.nome || ' ' || tesserato.cognome AS cliente " +
						"FROM ordine INNER JOIN merce ON ordine.codord = merce.ordine INNER JOIN lavorazione ON lavorazione.ordine = ordine.codord " +
						"INNER JOIN volume ON volume.prodotto = merce.prodotto " +
						"INNER JOIN tesserato ON ordine.tesserato = tesserato.codicefiscale " +
						"INNER JOIN edizione ON volume.edizione = edizione.prodotto INNER JOIN serie ON edizione.serie = serie.codserie WHERE codord = (SELECT MAX(codord) FROM ordine);";
				
				verifyQuery = connessione.prepareStatement(verifyQuery4);
				ResultSet verifyQueryRS = verifyQuery.executeQuery();
				int i = 0;
				while (verifyQueryRS.next()) {
					System.out.println(++i + ") " + verifyQueryRS.getString(1) + " " + verifyQueryRS.getInt(2)+ ", " + 
							verifyQueryRS.getInt(3) + " elemento, E " + verifyQueryRS.getDouble(4) + ", cliente: " + verifyQueryRS.getString(5));
				}
			} catch (SQLException e) {
				insertSQLExceptionHandler(e);
			}
			break;

		default:
			System.out.println("La scelta non e' valida. Chiusura inaspettata del programma.");
			System.exit(-1);
			break;
		}

	}

}
