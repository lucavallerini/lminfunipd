import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Scanner;

/**
 * Universita' degli Studi di Padova
 * Dipartimento di Ingegneria dell'Informazione
 * Corso di laurea magistrale in Ingegneria Informatica
 * 
 * Basi di dati - Anno Accademico 2014/2015
 * 
 * Homework 4 - Popolamento della base di dati 'fumetteria'.
 * 
 * @author Luca Vallerini - matr. 1110975
 * @version 0.3.20150615
 */

public class InsertDataFumetteria {

	// Informazioni essenziali per la connessione al database
	private static final String DRIVER = "org.postgresql.Driver";
	private static final String DATABASE = "jdbc:postgresql://localhost/database"; // DATABASE
	private static final String USER = "username"; // USERNAME
	private static final String PASSWORD = "password"; // PASSWORD

	// Input files
	private static final String INPUT_EDITORE = "data/editore.csv";
	private static final String INPUT_DISTRIBUTORE = "data/distributore.csv";
	private static final String INPUT_DISTRIBUZIONE = "data/distribuzione.csv";
	private static final String INPUT_SERIE = "data/serie.csv";
	private static final String INPUT_VOLUME = "data/volume.csv";
	private static final String INPUT_STAFF = "data/staff.csv";
	private static final String INPUT_TESSERATO = "data/tesserato.csv";
	private static final String INPUT_INCASELLAMENTO = "data/incasellamento.csv";
	private static final String INPUT_ORDINE = "data/ordine.csv";

	private static final String[] INPUT = {INPUT_EDITORE, INPUT_DISTRIBUTORE, INPUT_DISTRIBUZIONE, INPUT_SERIE, 
		INPUT_VOLUME, INPUT_STAFF, INPUT_TESSERATO, INPUT_INCASELLAMENTO, INPUT_ORDINE};

	// SQL statement
	private static final String INSERT_INTO_EDITORE = "INSERT INTO editore (piva, nome, sito) VALUES (?, ?, ?)";
	private static final String INSERT_INTO_DISTRIBUTORE = "INSERT INTO distributore (piva, nome, sito) VALUES (?, ?, ?)";
	private static final String INSERT_INTO_MAIL = "INSERT INTO mail (mail) VALUES (?)";
	private static final String INSERT_INTO_MAILDISTR = "INSERT INTO maildistr (mail, distributore) VALUES (?, ?)";
	private static final String INSERT_INTO_MAILED = "INSERT INTO mailed (mail, editore) VALUES (?, ?)";
	private static final String INSERT_INTO_TELFAX = "INSERT INTO telfax (codtelfax, num, fax) VALUES (md5(?), ?, ?) RETURNING codtelfax";
	private static final String INSERT_INTO_TELFAXDISTR = "INSERT INTO telfaxdistr (telfax, distributore) VALUES (?, ?)";
	private static final String INSERT_INTO_TELFAXED = "INSERT INTO telfaxed (telfax, editore) VALUES (?, ?)";
	private static final String INSERT_INTO_DISTRIBUZIONE = "INSERT INTO distribuzione (distributore, editore) VALUES (?, ?)";

	private static final String INSERT_INTO_SERIE = "INSERT INTO serie (codserie, titolo, autore, anno, media, statoserie, trama, " + 
			"numep, studio, numvolorig, regista, rivista) VALUES (md5(?), ?, ?, ?, ?::media , ?::statoserie , ?, ?, ?, ?, ?, ?) RETURNING codserie";
	private static final String INSERT_INTO_GENERE = "INSERT INTO genere (nomegen) SELECT ? WHERE NOT EXISTS (SELECT nomegen FROM genere WHERE nomegen = ?)";
	private static final String INSERT_INTO_GENERESERIE = "INSERT INTO genereserie (genere, serie) VALUES (?, ?)";
	private static final String INSERT_INTO_TRATTO = "INSERT INTO tratto (serieorig, seriederiv) VALUES (md5(?), ?)";

	private static final String INSERT_INTO_PRODOTTO = "INSERT INTO prodotto (codprod, datains) VALUES (md5(?), DEFAULT) RETURNING codprod";
	private static final String INSERT_INTO_EDIZIONE = "INSERT INTO edizione (prodotto, nome, period, supporto, " + 
			"col, inizpubbl, statoed, sovracop, extra, numvoltot, audio, video, sub, numpag, editore, serie) " +
			"VALUES (?, ?, ?::periodicita, ?::supporto, ?, ?::date, ?::statoserie, ?, ?, ?, ?, ?, ?, ?, ?, md5(?)) RETURNING prodotto";
	private static final String INSERT_INTO_VOLUME = "INSERT INTO volume (prodotto, datapubbl, ristampa, numrist, prezzofin, numvol, " + 
			"disp, isbn, edizione) VALUES (?, ?, ?, ?, ?, ?, ?, ?, md5(?))";

	private static final String INSERT_INTO_ACCOUNT = "INSERT INTO account (codacc, username, pw, statoacc, tipoacc) " + 
			"VALUES (DEFAULT, ?, md5(?), ?::statoaccount, ?::tipoaccount) RETURNING codacc";

	private static final String INSERT_INTO_TESSERAMENTO = "INSERT INTO tesseramento (acctesserante, acctesserato, tesserato, tessera, datatess) " +
			"VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT_INTO_TESSERA = "INSERT INTO tessera (codtess, inizioval, fineval, duratacasella, prezzotess, sconto) " +
			"VALUES (DEFAULT, ?, ?, ?, ?, ?) RETURNING codtess";
	private static final String INSERT_INTO_TESSERATO = "INSERT INTO tesserato (codicefiscale, nome, cognome, nascita, mail) " + 
			"VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT_INTO_TELTESSERATO = "INSERT INTO teltesserato (tesserato, telfax) VALUES (?, ?)";
	private static final String INSERT_INTO_INDIRIZZO = "INSERT INTO indirizzo (codind, via, civico, cap, citta, provincia) " + 
			"VALUES (md5(?), ?, ?, ?, ?, ?) RETURNING codind";
	private static final String INSERT_INTO_INDIRIZZOTESSERATO = "INSERT INTO indirizzotesserato (tesserato, indirizzo) " +
			"VALUES (?, ?)";

	private static final String INSERT_INTO_INCASELLAMENTO = "INSERT INTO incasellamento (accstaff, acctesserato, quantita, datains, datascad, " + 
			"datarim, prodotto) VALUES (?, ?, ?, ?, ?, ?, ?)";

	private static final String INSERT_INTO_ORDINE = "INSERT INTO ordine (codord, statoord, datains, prezzotot, tipoord, tesserato) " +
			"VALUES (DEFAULT, ?::statoordine, ?, ?, ?::tipoordine, ?) RETURNING codord";
	private static final String INSERT_INTO_MERCE = "INSERT INTO merce (ordine, quantita, prezzoelem, prodotto) VALUES (?, ?, ?, ?)";
	private static final String INSERT_INTO_LAVORAZIONE = "INSERT INTO lavorazione (ordine, acc, datalav) VALUES (?, ?, ?)";
	private static final String INSERT_INTO_RIFORNIMENTO = "INSERT INTO rifornimento (ordine, distributore) VALUES (?, ?)";

	private static final String INSERT_INTO_NOTIFICA = "INSERT INTO notifica (mittente, destinatario, datainvio, datagen, oggetto, testo) " +
			"VALUES (?, ?, ?, ?, ?, ?)";


	// insertSQLErrorException handling
	private static void insertSQLExceptionHandler(SQLException e, String relation, String id) {

		if (e.getSQLState().equals("23505")) {

			System.out.printf("%s %s già inserito, verrà ignorato. [%s]%n", relation, id, e.getMessage());

		} else {

			System.out.printf("Errore nell'inserire %s %s:%n", relation, id);
			System.out.printf("- Messaggio: %s%n", e.getMessage());
			System.out.printf("- Codice di stato SQL: %s%n", e.getSQLState());
			System.out.printf("- Codice di errore SQL: %s%n", e.getErrorCode());
			System.out.printf("%n");

		}
	}

	public static void main(String[] args) {

		// Connessione al database
		Connection connessione = null;

		// Prepared statement
		PreparedStatement insertIntoEditore = null;
		PreparedStatement insertIntoDistributore = null;
		PreparedStatement insertIntoDistribuzione = null;
		PreparedStatement insertIntoMail = null;
		PreparedStatement insertIntoMailDistr = null;
		PreparedStatement insertIntoMailEd = null;
		PreparedStatement insertIntoTelfax = null;
		PreparedStatement insertIntoTelfaxDistr = null;
		PreparedStatement insertIntoTelfaxEd = null;

		PreparedStatement insertIntoSerie = null;
		PreparedStatement insertIntoGenere = null;
		PreparedStatement insertIntoGenereSerie = null;
		PreparedStatement insertIntoTratto = null;

		PreparedStatement insertIntoProdotto = null;
		PreparedStatement insertIntoEdizione = null;
		PreparedStatement insertIntoVolume = null;

		PreparedStatement insertIntoAccount = null;

		PreparedStatement insertIntoTesseramento = null;
		PreparedStatement insertIntoTessera = null;
		PreparedStatement insertIntoTesserato = null;
		PreparedStatement insertIntoTelTesserato = null;
		PreparedStatement insertIntoIndirizzo = null;
		PreparedStatement insertIntoIndirizzoTesserato = null;

		PreparedStatement insertIntoIncasellamento = null;

		PreparedStatement insertIntoOrdine = null;
		PreparedStatement insertIntoMerce = null;
		PreparedStatement insertIntoLavorazione = null;
		PreparedStatement insertIntoRifornimento = null;

		PreparedStatement insertIntoNotifica = null;


		// Input file
		Reader inputFile = null;

		// Scanner
		Scanner scanner = null;

		// Caricamento del driver
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.printf(
					"Driver %s non trovato: %s.%n", DRIVER, e.getMessage());

			System.exit(-1);
		}

		// Connessione al DB e prepareStatement
		try {
			connessione = DriverManager.getConnection(DATABASE, USER, PASSWORD);

			insertIntoEditore = connessione.prepareStatement(INSERT_INTO_EDITORE);
			insertIntoDistributore = connessione.prepareStatement(INSERT_INTO_DISTRIBUTORE);
			insertIntoMail = connessione.prepareStatement(INSERT_INTO_MAIL);
			insertIntoMailDistr = connessione.prepareStatement(INSERT_INTO_MAILDISTR);
			insertIntoMailEd = connessione.prepareStatement(INSERT_INTO_MAILED);
			insertIntoTelfax = connessione.prepareStatement(INSERT_INTO_TELFAX);
			insertIntoTelfaxDistr = connessione.prepareStatement(INSERT_INTO_TELFAXDISTR);
			insertIntoTelfaxEd = connessione.prepareStatement(INSERT_INTO_TELFAXED);
			insertIntoDistribuzione = connessione.prepareStatement(INSERT_INTO_DISTRIBUZIONE);

			insertIntoSerie = connessione.prepareStatement(INSERT_INTO_SERIE);
			insertIntoGenere = connessione.prepareStatement(INSERT_INTO_GENERE);
			insertIntoGenereSerie = connessione.prepareStatement(INSERT_INTO_GENERESERIE);
			insertIntoTratto = connessione.prepareStatement(INSERT_INTO_TRATTO);

			insertIntoProdotto = connessione.prepareStatement(INSERT_INTO_PRODOTTO);
			insertIntoEdizione = connessione.prepareStatement(INSERT_INTO_EDIZIONE); 
			insertIntoVolume = connessione.prepareStatement(INSERT_INTO_VOLUME);

			insertIntoAccount = connessione.prepareStatement(INSERT_INTO_ACCOUNT);

			insertIntoTesseramento = connessione.prepareStatement(INSERT_INTO_TESSERAMENTO);
			insertIntoTessera = connessione.prepareStatement(INSERT_INTO_TESSERA);
			insertIntoTesserato = connessione.prepareStatement(INSERT_INTO_TESSERATO);
			insertIntoTelTesserato = connessione.prepareStatement(INSERT_INTO_TELTESSERATO);
			insertIntoIndirizzo = connessione.prepareStatement(INSERT_INTO_INDIRIZZO);
			insertIntoIndirizzoTesserato = connessione.prepareStatement(INSERT_INTO_INDIRIZZOTESSERATO);

			insertIntoIncasellamento = connessione.prepareStatement(INSERT_INTO_INCASELLAMENTO);

			insertIntoOrdine = connessione.prepareStatement(INSERT_INTO_ORDINE);
			insertIntoMerce = connessione.prepareStatement(INSERT_INTO_MERCE);
			insertIntoLavorazione = connessione.prepareStatement(INSERT_INTO_LAVORAZIONE);
			insertIntoRifornimento = connessione.prepareStatement(INSERT_INTO_RIFORNIMENTO);

			insertIntoNotifica = connessione.prepareStatement(INSERT_INTO_NOTIFICA);


		} catch (SQLException e) {
			System.out.printf("Errore di connessione:%n");

			// Recupero dell'errore
			while (e != null) {
				System.out.printf("- Messaggio: %s%n", e.getMessage());
				System.out.printf("- Codice di stato SQL: %s%n", e.getSQLState());
				System.out.printf("- Codice di errore SQL: %s%n", e.getErrorCode());
				System.out.printf("%n");
				e = e.getNextException();
			}

			System.exit(-1);
		}

		// Variabili di supporto generali
		int num; String input_file = null;

		// Variabili di supporto per INSERT_INTO_EDITORE e INSERT_INTO_DISTRIBUTORE
		String piva, nomeEdDistr, sito;
		String[] mail, tel, fax;

		// Variabili di supporto per INSERT_INTO_DISTRIBUZIONE
		String editore, distributore;

		// Variabili di supporto per INSERT_INTO_SERIE
		String titolo, autore, media, statoSerie, trama, studio, regista, rivista;
		int anno, numEp, numVolOrig;
		String[] genere, serie;

		// Variabili di supporto per INSERT_INTO_EDIZIONE, INSERT_INTO_VOLUME
		String nome, period, supporto, dataPubblEd, statoEd, extra, audio, video, sub, editoreEd;
		String dataPubblVol, isbn;
		boolean col, sovraCop, rist;
		int numVolTot, numPag, numVol, numRist, disp;
		double prezzoVol;

		// Variabili di supporto per INSERT_INTO_ACCOUNT
		String user, pw, statoAcc, tipoAcc;

		// Variabili di supporto per INSERT_INTO_TESSERATO
		String codicefiscale, nomeTess, cognomeTess, mailTess, via, citta, civico, cap;
		String provincia, accTess;
		double prezzoTess;
		int sconto, duratacasella;
		String[] numTel;

		// Variabili di supporto per INSERT_INTO_INCASELLAMENTO
		String accStaff, ogg, prodotto;
		Date datains, datarim;
		int quantita;

		// Variabili di supporto per INSERT_INTO_ORDINE
		String statoOrd, tipoOrd, codiceordine;
		Date datalav;
		int codord;
		double prezzoTot;

		// Lettura dei file e inserimento dei dati nel database
		try {
			System.out.println("Popolamento della base di dati in corso...\n");

			for (int n = 0; n < INPUT.length; n++) {
				input_file = INPUT[n];

				// Apertura del file di input da riga di comando
				try {
					inputFile = new BufferedReader(new FileReader(input_file));
					System.out.printf("\nFile %s aperto con successo.%n", input_file);
				} catch (IOException e) {
					System.out.printf("Impossibile leggere il file %s: %s%n", input_file, e.getMessage());
					System.exit(-1);
				}
				
				scanner = new Scanner(inputFile);
				scanner.useDelimiter(";");

				// Numero di riga
				int riga = 0;
				
				while (scanner.hasNext()) {
					riga++;

					switch (input_file) {

					// INSERT_INTO_EDITORE
					case INPUT_EDITORE:
						piva = scanner.next();
						nomeEdDistr = scanner.next();
						sito = scanner.next();

						num = scanner.nextInt();
						mail = new String[num];

						for (int i = 0; i < num; i++) {
							mail[i] = scanner.next();
						}

						num = scanner.nextInt();
						tel = new String[num];

						for (int i = 0; i < num; i++) {
							tel[i] = scanner.next();
						}

						num = scanner.nextInt();
						fax = new String[num];

						for (int i = 0; i < num; i++) {
							fax[i] = scanner.next();
						}

						try {			
							insertIntoEditore.setString(1, piva);
							insertIntoEditore.setString(2, nomeEdDistr);
							insertIntoEditore.setString(3, sito);

							insertIntoEditore.execute();

							if (mail.length != 0) {
								for (int i = 0; i < mail.length; i++) {
									insertIntoMail.setString(1, mail[i]);
									insertIntoMailEd.setString(1, mail[i]);
									insertIntoMailEd.setString(2, piva);
									insertIntoMail.execute();
									insertIntoMailEd.execute();
								}
							}

							if (tel.length != 0) {
								for (int i = 0; i < tel.length; i++) {
									insertIntoTelfax.setString(1, tel[i] + "false");
									insertIntoTelfax.setString(2, tel[i]);
									insertIntoTelfax.setBoolean(3, false);
									ResultSet tmpRS = insertIntoTelfax.executeQuery();
									tmpRS.next();
									String tmpCod = tmpRS.getString(1);
									insertIntoTelfaxEd.setString(1, tmpCod);
									insertIntoTelfaxEd.setString(2, piva);
									insertIntoTelfaxEd.execute();
								}
							}

							if (fax.length != 0) {
								for (int i = 0; i < fax.length; i++) {
									insertIntoTelfax.setString(1, tel[i] + "true");
									insertIntoTelfax.setString(2, tel[i]);
									insertIntoTelfax.setBoolean(3, true);
									ResultSet tmpRS = insertIntoTelfax.executeQuery();
									tmpRS.next();
									String tmpCod = tmpRS.getString(1);
									insertIntoTelfaxEd.setString(1, tmpCod);
									insertIntoTelfaxEd.setString(2, piva);
									insertIntoTelfaxEd.execute();
								}
							}

							System.out.println("Riga " + riga + " inserita con successo!");	
						} catch (SQLException e) {
							insertSQLExceptionHandler(e, "Editore", nomeEdDistr + " (" + piva + ")");
						}	
						break;

						// INSERT_INTO_DISTRIBUTORE
					case INPUT_DISTRIBUTORE:
						piva = scanner.next();
						nomeEdDistr = scanner.next();
						sito = scanner.next();

						num = scanner.nextInt();
						mail = new String[num];

						for (int i = 0; i < num; i++) {
							mail[i] = scanner.next();
						}

						num = scanner.nextInt();
						tel = new String[num];

						for (int i = 0; i < num; i++) {
							tel[i] = scanner.next();
						}

						num = scanner.nextInt();
						fax = new String[num];

						for (int i = 0; i < num; i++) {
							fax[i] = scanner.next();
						}

						try {		
							insertIntoDistributore.setString(1, piva);
							insertIntoDistributore.setString(2, nomeEdDistr);
							insertIntoDistributore.setString(3, sito);

							insertIntoDistributore.execute();

							if (mail.length != 0) {
								for (int i = 0; i < mail.length; i++) {
									insertIntoMail.setString(1, mail[i]);
									insertIntoMailDistr.setString(1, mail[i]);
									insertIntoMailDistr.setString(2, piva);
									insertIntoMail.execute();
									insertIntoMailDistr.execute();
								}
							}

							if (tel.length != 0) {
								for (int i = 0; i < tel.length; i++) {
									insertIntoTelfax.setString(1, tel[i] + "false");
									insertIntoTelfax.setString(2, tel[i]);
									insertIntoTelfax.setBoolean(3, false);
									ResultSet tmpRS = insertIntoTelfax.executeQuery();
									tmpRS.next();
									insertIntoTelfaxDistr.setString(1, tmpRS.getString(1));
									insertIntoTelfaxDistr.setString(2, piva);
									insertIntoTelfaxDistr.execute();
								}
							}

							if (fax.length != 0) {
								for (int i = 0; i < fax.length; i++) {
									insertIntoTelfax.setString(1, tel[i] + "true");
									insertIntoTelfax.setString(2, fax[i]);
									insertIntoTelfax.setBoolean(3, true);
									ResultSet tmpRS = insertIntoTelfax.executeQuery();
									tmpRS.next();
									insertIntoTelfaxDistr.setString(1, tmpRS.getString(1));
									insertIntoTelfaxDistr.setString(2, piva);
									insertIntoTelfaxDistr.execute();
								}
							}

							System.out.println("Riga " + riga + " inserita con successo!");
						} catch (SQLException e) {
							insertSQLExceptionHandler(e, "Distributore", nomeEdDistr + " (" + piva + ")");
						}
						break;

						// INSERT_INTO_DISTRIBUZIONE
					case INPUT_DISTRIBUZIONE:
						distributore = scanner.next();
						editore = scanner.next();

						try {
							insertIntoDistribuzione.setString(1, distributore);
							insertIntoDistribuzione.setString(2, editore);
							insertIntoDistribuzione.execute();

							System.out.println("Riga " + riga + " inserita con successo!");	
						} catch (SQLException e) {
							insertSQLExceptionHandler(e, "Distribuzione", distributore + " <-> " + editore);
						}
						break;

						// INSERT_INTO_SERIE
					case INPUT_SERIE:
						//						codSerie = scanner.next();
						titolo = scanner.next();
						autore = scanner.next();
						anno = scanner.nextInt();
						media = scanner.next();
						statoSerie = scanner.next();
						trama = scanner.next();

						if (media.equals("Anime")) 
							numEp = scanner.nextInt();
						else
							numEp = scanner.next().length(); // dummy

						studio = scanner.next();

						if (media.equals("Manga"))
							numVolOrig = scanner.nextInt();
						else
							numVolOrig = scanner.next().length(); // dummy

						regista = scanner.next();
						rivista = scanner.next();

						num = scanner.nextInt();
						genere = new String[num];

						for (int i = 0; i < genere.length; i++) {
							genere[i] = scanner.next();
						}

						num = scanner.nextInt();
						serie = new String[num*4];

						for (int i = 0; i < serie.length; i++) {
							serie[i] = scanner.next();
						}


						try {
							//							insertIntoSerie.setString(1, codSerie);
							insertIntoSerie.setString(1, (titolo+autore+anno+media));
							insertIntoSerie.setString(2, titolo);
							insertIntoSerie.setString(3, autore);
							insertIntoSerie.setInt(4, anno);
							insertIntoSerie.setString(5, media);
							insertIntoSerie.setString(6, statoSerie);
							insertIntoSerie.setString(7, trama);

							if (media.equals("Anime"))
								insertIntoSerie.setInt(8, numEp);
							else
								insertIntoSerie.setNull(8, 0);

							if (media.equals("Anime") && !studio.equals("NULL"))
								insertIntoSerie.setString(9, studio);
							else
								insertIntoSerie.setNull(9, 0);

							if (media.equals("Manga"))
								insertIntoSerie.setInt(10, numVolOrig);
							else
								insertIntoSerie.setNull(10, 0);

							if (media.equals("Anime") && !regista.equals("NULL"))
								insertIntoSerie.setString(11, regista);
							else
								insertIntoSerie.setNull(11, 0);

							if (media.equals("Manga") && !rivista.equals("NULL"))
								insertIntoSerie.setString(12, rivista);
							else
								insertIntoSerie.setNull(12, 0);

							ResultSet getCodSerie = insertIntoSerie.executeQuery();
							getCodSerie.next();

							if (genere.length != 0) {
								for (int i = 0; i < genere.length; i++) {
									insertIntoGenere.setString(1, genere[i]);
									insertIntoGenere.setString(2, genere[i]);
									insertIntoGenere.execute();
									insertIntoGenereSerie.setString(1, genere[i]);
									//									insertIntoGenereSerie.setString(2, codserie);
									insertIntoGenereSerie.setString(2, getCodSerie.getString(1));
									insertIntoGenereSerie.execute();
								}
							}

							if (serie.length != 0) {
								for (int i = 0; i < serie.length/4; i++) {
									insertIntoTratto.setString(1, serie[i]+serie[i+1]+serie[i+2]+serie[i+3]);
									//									insertIntoTratto.setString(2, codSerie);
									insertIntoTratto.setString(2, getCodSerie.getString(1));
									insertIntoTratto.execute();
								}
							}

							System.out.println("Riga " + riga + " inserita con successo!");
						} catch (SQLException e) {
							insertSQLExceptionHandler(e, "Serie", titolo);
						}	
						break;

						// INPUT_VOLUME
					case INPUT_VOLUME:
						String tmpDatePubblVol = scanner.next();
						dataPubblVol = tmpDatePubblVol.substring(6, 10) + tmpDatePubblVol.substring(2, 6) + tmpDatePubblVol.substring(0, 2);
						Date dateVol = Date.valueOf(dataPubblVol);

						rist = scanner.nextBoolean();
						numRist = scanner.nextInt();
						prezzoVol = Double.parseDouble(scanner.next());
						numVol = scanner.nextInt();

						scanner.next(); // ignoro la copertina

						disp = scanner.nextInt();
						isbn = scanner.next();

						String tmpnome = scanner.next();
						if (tmpnome.equals("NULL"))
							nome = null;
						else
							nome = tmpnome;

						period = scanner.next();

						String tmpsupporto = scanner.next();
						if (tmpsupporto.equals("NULL"))
							supporto = null;
						else
							supporto = tmpsupporto;

						String tmpcol = scanner.next();
						if (tmpcol.equals("NULL"))
							col = false;
						else if	(tmpcol.equals("true"))
							col = true;
						else
							col = false;

						dataPubblEd = scanner.next();

						Date date = Date.valueOf(dataPubblEd);

						statoEd = scanner.next();

						String tmpsovracop = scanner.next();
						if (tmpsovracop.equals("NULL"))
							sovraCop = false;
						else if (tmpsovracop.equals("true"))
							sovraCop = true;
						else
							sovraCop = false;

						String tmpextra = scanner.next();
						if (tmpsovracop.equals("NULL"))
							extra = null;
						else
							extra = tmpextra;

						numVolTot = scanner.nextInt();

						if (supporto != null) {
							audio = scanner.next();
							video = scanner.next();
							sub = scanner.next();
						} else {
							audio = scanner.next();
							video = scanner.next();
							sub = scanner.next();
							audio = video = sub = null;
						}

						String tmpnumpag = scanner.next();
						if (tmpnumpag.equals("NULL"))
							numPag = 0; // dummy
						else
							numPag = Integer.parseInt(tmpnumpag);

						editoreEd = scanner.next();

						String[] serieEdizione = new String[4];
						for (int m = 0; m < serieEdizione.length; m++) {
							serieEdizione[m] = scanner.next();
						}

						try {
							String queryCheckProdotto = "SELECT codprod FROM prodotto WHERE codprod = md5(?)";
							PreparedStatement checkProdotto = connessione.prepareStatement(queryCheckProdotto);
							checkProdotto.setString(1, serieEdizione[0]+serieEdizione[1]+serieEdizione[2]+serieEdizione[3]);
							ResultSet prodottoEsiste = checkProdotto.executeQuery();

							if (prodottoEsiste.next()) {
								// violazione prodotto_codprod_pkey e edizione_prodotto_pkey
								// l'edizione esiste già, ignoro e passo all'inserimento del volume
							} else {
								insertIntoProdotto.setString(1, serieEdizione[0]+serieEdizione[1]+serieEdizione[2]+serieEdizione[3]);
								ResultSet prodottoedizione = insertIntoProdotto.executeQuery();
								prodottoedizione.next();
								String prodottoEdizione = prodottoedizione.getString(1);

								insertIntoEdizione.setString(1, prodottoEdizione);

								if (nome != null)
									insertIntoEdizione.setString(2, nome);
								else
									insertIntoEdizione.setNull(2, Types.VARCHAR);

								insertIntoEdizione.setString(3, period);

								if (supporto != null) {
									insertIntoEdizione.setString(4, supporto);
									insertIntoEdizione.setString(11, audio);
									insertIntoEdizione.setString(12, video);
									insertIntoEdizione.setString(13, sub);
								} else {
									insertIntoEdizione.setNull(4, Types.VARCHAR);
									insertIntoEdizione.setNull(11, Types.VARCHAR);
									insertIntoEdizione.setNull(12, Types.VARCHAR);
									insertIntoEdizione.setNull(13, Types.VARCHAR);
								}

								if (!col && !tmpcol.equals("NULL"))
									insertIntoEdizione.setBoolean(5, false);
								else
									insertIntoEdizione.setBoolean(5, true);

								insertIntoEdizione.setDate(6, date);

								insertIntoEdizione.setString(7, statoEd);

								if (!sovraCop && !tmpsovracop.equals("NULL"))
									insertIntoEdizione.setBoolean(8, false);
								else
									insertIntoEdizione.setBoolean(8, true);

								if (extra != null)
									insertIntoEdizione.setString(9, extra);
								else
									insertIntoEdizione.setNull(9, Types.NULL);

								insertIntoEdizione.setInt(10, numVolTot);

								if (numPag != 0)
									insertIntoEdizione.setInt(14, numPag);
								else
									insertIntoEdizione.setNull(14, Types.INTEGER);

								insertIntoEdizione.setString(15, editoreEd);
								insertIntoEdizione.setString(16, serieEdizione[0]+serieEdizione[1]+serieEdizione[2]+serieEdizione[3]);
								insertIntoEdizione.execute();
							}

							insertIntoProdotto.setString(1, isbn + numVol);
							ResultSet prodottovolume = insertIntoProdotto.executeQuery();
							prodottovolume.next();

							insertIntoVolume.setString(1, prodottovolume.getString(1));
							insertIntoVolume.setDate(2, dateVol);
							insertIntoVolume.setBoolean(3, rist);
							insertIntoVolume.setInt(4, numRist);
							insertIntoVolume.setDouble(5, prezzoVol);
							insertIntoVolume.setInt(6, numVol);
							insertIntoVolume.setInt(7, disp);
							insertIntoVolume.setString(8, isbn);
							insertIntoVolume.setString(9, serieEdizione[0]+serieEdizione[1]+serieEdizione[2]+serieEdizione[3]);

							insertIntoVolume.execute();

							System.out.println("Riga " + riga + " inserita con successo!");
						} catch (SQLException e) {
							insertSQLExceptionHandler(e, "Volume", serieEdizione[0] + " (" + serieEdizione[3] + ") " + numVol);
						}
						break;

					case INPUT_STAFF:
						user = scanner.next();
						pw = scanner.next();
						statoAcc = scanner.next();
						tipoAcc = scanner.next();

						try {
							insertIntoAccount.setString(1, user);
							insertIntoAccount.setString(2, pw);
							insertIntoAccount.setString(3, statoAcc);
							insertIntoAccount.setString(4, tipoAcc);
							insertIntoAccount.execute();

							System.out.println("Riga " + riga + " inserita con successo!");
						} catch (SQLException e) {
							insertSQLExceptionHandler(e, "Account staff", user + " <-> " + tipoAcc);
						}
						break;

					case INPUT_TESSERATO:
						codicefiscale = scanner.next();
						nomeTess = scanner.next();
						cognomeTess = scanner.next();
						Date datanascita = Date.valueOf(scanner.next());
						mailTess = scanner.next();
						via = scanner.next();
						civico = scanner.next();
						cap = scanner.next();
						citta = scanner.next();
						provincia = scanner.next();

						int i = scanner.nextInt();
						numTel = new String[i];
						for (int k = 0; k < i; k++)
							numTel[k] = scanner.next();

						user = scanner.next();
						pw = scanner.next();
						statoAcc = scanner.next();
						tipoAcc = scanner.next();
						Date inizioVal = Date.valueOf(scanner.next());
						Date fineVal = Date.valueOf(scanner.next());
						duratacasella = scanner.nextInt();
						prezzoTess = Double.parseDouble(scanner.next());
						sconto = scanner.nextInt();
						accTess = scanner.next(); // account tesserante
						Date dataTess = Date.valueOf(scanner.next());

						try {
							insertIntoMail.setString(1, mailTess);
							insertIntoMail.execute();

							insertIntoTesserato.setString(1, codicefiscale);
							insertIntoTesserato.setString(2, nomeTess);
							insertIntoTesserato.setString(3, cognomeTess);
							insertIntoTesserato.setDate(4, datanascita);
							insertIntoTesserato.setString(5, mailTess);
							insertIntoTesserato.execute();

							insertIntoIndirizzo.setString(1, via+civico+citta+provincia);
							insertIntoIndirizzo.setString(2, via);
							insertIntoIndirizzo.setString(3, civico);
							insertIntoIndirizzo.setString(4, cap);
							insertIntoIndirizzo.setString(5, citta);
							insertIntoIndirizzo.setString(6, provincia);

							ResultSet indirizzo = insertIntoIndirizzo.executeQuery();
							indirizzo.next();

							insertIntoIndirizzoTesserato.setString(1, codicefiscale);
							insertIntoIndirizzoTesserato.setString(2, indirizzo.getString(1));
							insertIntoIndirizzoTesserato.execute();

							for (int j = 0; j < numTel.length; j++) {
								insertIntoTelfax.setString(1, numTel[j] + "false");
								insertIntoTelfax.setString(2, numTel[j]);
								insertIntoTelfax.setBoolean(3, false);

								ResultSet telefono = insertIntoTelfax.executeQuery();
								telefono.next();

								insertIntoTelTesserato.setString(1, codicefiscale);
								insertIntoTelTesserato.setString(2, telefono.getString(1));
								insertIntoTelTesserato.execute();
							}

							insertIntoAccount.setString(1, user);
							insertIntoAccount.setString(2, pw);
							insertIntoAccount.setString(3, statoAcc);
							insertIntoAccount.setString(4, tipoAcc);

							ResultSet account = insertIntoAccount.executeQuery();
							account.next();

							insertIntoTessera.setDate(1, inizioVal);
							insertIntoTessera.setDate(2, fineVal);
							insertIntoTessera.setInt(3, duratacasella);
							insertIntoTessera.setDouble(4, prezzoTess);
							insertIntoTessera.setInt(5, sconto);

							ResultSet tessera = insertIntoTessera.executeQuery();
							tessera.next();

							PreparedStatement getAccountTesserante = connessione.prepareStatement("SELECT codacc FROM account WHERE username = ?");
							getAccountTesserante.setString(1, accTess);
							ResultSet tesserante = getAccountTesserante.executeQuery();
							tesserante.next();

							insertIntoTesseramento.setInt(1, tesserante.getInt(1));
							insertIntoTesseramento.setInt(2, account.getInt(1));
							insertIntoTesseramento.setString(3, codicefiscale);
							insertIntoTesseramento.setInt(4, tessera.getInt(1));
							insertIntoTesseramento.setDate(5, dataTess);
							insertIntoTesseramento.execute();

							System.out.println("Riga " + riga + " inserita con successo!");
						} catch (SQLException e) {
							insertSQLExceptionHandler(e, "Tesseramento", user);
						}
						break;

					case INPUT_INCASELLAMENTO:
						accStaff = scanner.next();
						accTess = scanner.next();
						quantita = scanner.nextInt();
						datains = Date.valueOf(scanner.next());

						String datetmp = scanner.next();
						if (!datetmp.equals("NULL"))
							datarim = Date.valueOf(datetmp);
						else
							datarim = null;

						prodotto = scanner.next();
						ogg = scanner.next();

						String titoloProdotto = ""; // dummy
						int numVolProdotto = 0; // dummy
						try {
							String queryAccount = "SELECT codacc FROM account WHERE username = ?";
							PreparedStatement getAccount = connessione.prepareStatement(queryAccount);

							getAccount.setString(1, accStaff);
							ResultSet codAccountStaff = getAccount.executeQuery();
							codAccountStaff.next();
							int codStaff = codAccountStaff.getInt(1);

							getAccount.setString(1, accTess);
							ResultSet codAccountTess = getAccount.executeQuery();
							codAccountTess.next();
							int codTess = codAccountTess.getInt(1);

							String queryScadenza = "SELECT T.duratacasella FROM tessera AS T INNER JOIN tesseramento AS TS " + 
									"ON T.codtess = TS.tessera WHERE acctesserato = ?";
							PreparedStatement getScadenza = connessione.prepareStatement(queryScadenza);
							getScadenza.setInt(1, codTess);
							ResultSet scadenza = getScadenza.executeQuery();
							scadenza.next();
							int mesiGiacenza = scadenza.getInt(1);
							String dataIns = datains.toString();
							Calendar dataScadenza = Calendar.getInstance(); // dummy
							dataScadenza.set(Integer.parseInt(dataIns.substring(0, 4)), (Integer.parseInt(dataIns.substring(5, 7)) - 1 + mesiGiacenza),
									Integer.parseInt(dataIns.substring(8, 10)));
							Date dataScad = new Date(dataScadenza.getTimeInMillis());

							String queryProdotto = "SELECT V.prodotto, S.titolo, V.numvol FROM volume AS V INNER JOIN edizione AS E " + 
									"ON V.edizione = E.prodotto INNER JOIN serie AS S ON E.serie = S.codserie WHERE isbn = ?";
							PreparedStatement getDatiProdotto = connessione.prepareStatement(queryProdotto);
							getDatiProdotto.setString(1, prodotto);
							ResultSet datiProdotto = getDatiProdotto.executeQuery();
							datiProdotto.next();
							String codProdotto = datiProdotto.getString(1);
							titoloProdotto = datiProdotto.getString(2);
							numVolProdotto = datiProdotto.getInt(3);
							String text = "Il prodotto " + titoloProdotto + " " + numVolProdotto + " è stato depositato nella tua casella!";

							insertIntoIncasellamento.setInt(1, codStaff);
							insertIntoIncasellamento.setInt(2, codTess);
							insertIntoIncasellamento.setInt(3, quantita);
							insertIntoIncasellamento.setDate(4, datains);
							insertIntoIncasellamento.setDate(5, dataScad);

							if (datarim != null)
								insertIntoIncasellamento.setDate(6, datarim);
							else
								insertIntoIncasellamento.setNull(6, Types.DATE);

							insertIntoIncasellamento.setString(7, codProdotto);
							insertIntoIncasellamento.execute();

							insertIntoNotifica.setInt(1, codStaff);
							insertIntoNotifica.setInt(2, codTess);
							insertIntoNotifica.setDate(3, datains);
							insertIntoNotifica.setDate(4, datains);
							insertIntoNotifica.setString(5, ogg);
							insertIntoNotifica.setString(6, text);
							insertIntoNotifica.execute();

							System.out.println("Riga " + riga + " inserita con successo!");
						} catch (SQLException e) {
							insertSQLExceptionHandler(e, "Incasellamento", accTess + ": " + titoloProdotto + " " + numVolProdotto);
						}
						break;

					case INPUT_ORDINE:
						statoOrd = scanner.next();
						datains = Date.valueOf(scanner.next());
						tipoOrd = scanner.next();

						String tmp = scanner.next();
						if (tmp.equals("NULL"))
							accTess = null;
						else
							accTess = tmp;

						quantita = scanner.nextInt();
						prodotto = scanner.next();
						datalav = Date.valueOf(scanner.next());
						accStaff = scanner.next();
						codiceordine = "Ordine problematico!"; // dummy

						try {
							PreparedStatement getProdotto = connessione.prepareStatement("SELECT prodotto, prezzofin FROM volume WHERE isbn = ?");
							getProdotto.setString(1, prodotto);
							ResultSet codiceProdotto = getProdotto.executeQuery();
							codiceProdotto.next();

							String codprod = codiceProdotto.getString(1);
							prezzoVol = codiceProdotto.getDouble(2);

							PreparedStatement getAccount = connessione.prepareStatement("SELECT codacc FROM account WHERE username = ?");
							getAccount.setString(1, accStaff);
							ResultSet accountStaff = getAccount.executeQuery();
							accountStaff.next();
							int staff = accountStaff.getInt(1);

							String tess = ""; // dummy
							sconto = 0;
							if (accTess != null) {
								String queryTess = "SELECT T.tesserato, TS.sconto FROM account AS A INNER JOIN tesseramento AS T ON A.codacc = T.acctesserato " + 
										"INNER JOIN tessera AS TS ON TS.codtess = T.tessera WHERE A.username = ?";
								PreparedStatement getTesserato = connessione.prepareStatement(queryTess);
								getTesserato.setString(1, accTess);
								ResultSet tesserato = getTesserato.executeQuery();
								tesserato.next();
								tess = tesserato.getString(1);
								sconto = tesserato.getInt(2);
							}

							prezzoTot = prezzoVol * quantita;
							double prezzoScontato = prezzoTot - prezzoTot * sconto / 100;

							insertIntoOrdine.setString(1, statoOrd);
							insertIntoOrdine.setDate(2, datains);
							insertIntoOrdine.setDouble(3, prezzoScontato);
							insertIntoOrdine.setString(4, tipoOrd);

							if (accTess != null)
								insertIntoOrdine.setString(5, tess);
							else
								insertIntoOrdine.setNull(5, Types.VARCHAR);

							ResultSet ordine = insertIntoOrdine.executeQuery();
							ordine.next();
							codord = ordine.getInt(1); 
							codiceordine = String.valueOf(codord);

							insertIntoMerce.setInt(1, codord);
							insertIntoMerce.setInt(2, quantita);
							insertIntoMerce.setDouble(3, prezzoVol);
							insertIntoMerce.setString(4, codprod);
							insertIntoMerce.execute();

							insertIntoLavorazione.setInt(1, codord);
							insertIntoLavorazione.setInt(2, staff);
							insertIntoLavorazione.setDate(3, datalav);
							insertIntoLavorazione.execute();

							if (tipoOrd.equals("Fornitura")) {
								String queryEd = "SELECT D.distributore FROM (volume AS V INNER JOIN edizione AS E ON V.edizione = E.prodotto) AS VE " + 
										" INNER JOIN distribuzione AS D ON D.editore = VE.editore WHERE VE.isbn = ?";
								PreparedStatement getEditore = connessione.prepareStatement(queryEd);
								getEditore.setString(1, prodotto);
								ResultSet editoreProdotto = getEditore.executeQuery();
								editoreProdotto.next();

								insertIntoRifornimento.setInt(1, codord);
								insertIntoRifornimento.setString(2, editoreProdotto.getString(1));
								insertIntoRifornimento.execute();
							}

							System.out.println("Riga " + riga + " inserita con successo!");
						} catch (SQLException e) {
							insertSQLExceptionHandler(e, "Ordine", codiceordine);
						}
						break;

						// Gestione di input non validi
					default:
						System.out.println("Errore: il nome e/o il percorso del file non sono validi.");
						break;
					}

					scanner.nextLine();
				}
			}
			System.out.println("\nPopolamento della base di dati terminato con successo.\n");
		} finally {
			scanner.close();
			System.out.println("File chiuso con successo.");

			try {
				insertIntoEditore.close();
				insertIntoDistributore.close();
				insertIntoMail.close();
				insertIntoMailDistr.close();
				insertIntoMailEd.close();
				insertIntoTelfax.close();
				insertIntoTelfaxDistr.close();
				insertIntoTelfaxEd.close();
				insertIntoDistribuzione.close();
				insertIntoSerie.close();
				insertIntoGenere.close();
				insertIntoGenereSerie.close();
				insertIntoTratto.close();
				insertIntoProdotto.close();
				insertIntoEdizione.close();
				insertIntoVolume.close();
				insertIntoAccount.close();
				insertIntoTesseramento.close();
				insertIntoTessera.close();
				insertIntoTesserato.close();
				insertIntoTelTesserato.close();
				insertIntoIndirizzo.close();
				insertIntoIndirizzoTesserato.close();
				insertIntoIncasellamento.close();
				insertIntoOrdine.close();
				insertIntoMerce.close();
				insertIntoLavorazione.close();
				insertIntoRifornimento.close();
				insertIntoNotifica.close();

				System.out.println("Statement chiusi con successo.");

				connessione.close();
				System.out.println("Connessione chiusa con successo.");
			} catch (SQLException e) {
				System.out.printf("Errore nel rilasciare le risorse:%n");

				// Recupero l'errore
				while (e != null) {
					System.out.printf("- Messaggio: %s%n", e.getMessage());
					System.out.printf("- Codice di stato SQLe: %s%n", e.getSQLState());
					System.out.printf("- Codice di errore SQL: %s%n", e.getErrorCode());
					System.out.printf("%n");
					e = e.getNextException();
				}
			}
		}
	}
}
