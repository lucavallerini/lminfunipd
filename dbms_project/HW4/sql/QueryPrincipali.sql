-- QUERY NUMERO 1 [FIGURA 1]
-- Recupera i prodotti depositati nella casella del cliente tesserato Luca Vallerini: di questi, 
-- viene mostrato il titolo della serie, il numero del volume, la quantita' di volumi 
-- presenti in casella e il prezzo per singolo volume.

SELECT S.titolo AS prodotto, numero, prezzo_cadauno, quantita, prezzo_cadauno*quantita AS prezzo_totale FROM 
	serie AS S INNER JOIN (edizione AS E INNER JOIN 
		(SELECT V.numvol AS numero, V.prezzofin AS prezzo_cadauno, P.quantita AS quantita, V.edizione FROM
			(volume AS V INNER JOIN (incasellamento AS I INNER JOIN
				((SELECT codicefiscale FROM tesserato WHERE nome = 'Luca' AND cognome = 'Vallerini') AS TS INNER JOIN 
					tesseramento AS ON TS.codicefiscale = T.tesserato) AS A ON A.acctesserato = I.acctesserato) AS P ON P.prodotto = V.prodotto)) AS VP
					ON VP.edizione = E.prodotto) AS VPE ON VPE.serie = S.codserie
					GROUP BY S.titolo, numero, prezzo_cadauno, quantita, prezzo_totale ORDER BY numero ASC;
					
-- 	QUERY NUMERO 2 [FIGURA 2]
-- Oggi e' arrivato in fumetteria il volume 1 di 'Gen di Hiroshima':
-- tale volume viene inserito a catalogo con i sequenti parametri: titolo=Gen di Hiroshima, 
-- volume=1, prezzo=E 42.00, ISBN=1522583690251, pagine a colori=nessuna, numero di pagine=1000,
-- sovraccoperta=nessuna, autore=Keiji Nakazawa, anno=1982, editore=Hikari, 
-- stato edizione=In corso, periodicita'=Irregolare, volumi arrivati=3.

-- Query per l'inserimento
INSERT INTO serie (codserie, titolo, autore, anno, media, statoserie)
	SELECT md5('Gen di Hiroshima'||'Keiji Nakazawa'||1982||'Manga'), 'Gen di Hiroshima', 'Keiji Nakazawa', 1982, 'Manga', 'Completa'
		WHERE NOT EXISTS (SELECT codserie FROM serie WHERE codserie = md5('Gen di Hiroshima'||'Keiji Nakazawa'||1982||'Manga'));
		
INSERT INTO prodotto (codprod, datains) VALUES (md5('Gen di Hiroshima'||'Keiji Nakazawa'||1982||'Manga'||2015-06-15), DEFAULT);

INSERT INTO edizione (prodotto, period, col, inizpubbl, statoed, sovracop, numpag, editore, serie)
	VALUES (md5('Gen di Hiroshima'||'Keiji Nakazawa'||1982||'Manga'||2015-06-15), 'Irregolare', FALSE, '2015-06-15', 'In corso', FALSE, 1000,
		(SELECT piva FROM editore WHERE nome LIKE '%Hikari%') , md5('Gen di Hiroshima'||'Keiji Nakazawa'||1982||'Manga'));
		
INSERT INTO prodotto (codprod, datains) VALUES (md5('1522583690251'||1), DEFAULT);
INSERT INTO volume (prodotto, datapubbl, prezzofin, numvol, disp, isbn, edizione) VALUES 
	(md5('1522583690251'||1), '2015-06-15', '42', 1, 3, '1522583690251', md5('Gen di Hiroshima'||'Keiji Nakazawa'||1982||'Manga'||2015-06-15));

-- Query per la verifica dell'inserimento [FIGURA 2]
SELECT S.titolo, V.numvol AS volume, V.prezzofin AS prezzo, V.disp AS quantita, E.period AS periodicita,
	E.col AS pagine_colori, E.numpag AS numero_pag, V.isbn, editore.nome AS editore FROM 
		volume AS V INNER JOIN edizione AS E ON V.edizione = E.prodotto INNER JOIN serie AS S ON E.serie = S.codserie
			INNER JOIN editore ON E.editore = piva WHERE isbn = '1234567890123';

-- QUERY NUMERO 3 [FIGURA 3]
-- Ordina in ordine decrescente gli editori in base al prezzo medio dei loro prodotti a catalogo, 
-- indicando prezzo medio e numero di volumi pubblicati a catalogo.

SELECT editore.nome AS editore, COUNT(volume.numvol) AS numero_vol_pubblicati, AVG(prezzofin) AS prezzo_medio FROM
	editore INNER JOIN edizione ON editore.piva = edizione.editore INNER JOIN volume ON edizione.prodotto = volume.edizione
		GROUP BY editore.nome ORDER BY prezzo_medio DESC;


-- QUERY NUMERO 4 [FIGURA 4]
-- Lista della spesa: l'ultimo numero di Monster, primo numero di Dr. Slump e primo numero di Maison Ikkoku.
-- L'ordine di acquisto viene effettuato dal 'Titolare' a beneficio del cliente con tessera numero 4.

-- Query per l'inserimento dell'ordine
SELECT SUM(V.prezzofin)-SUM(V.prezzofin)*((SELECT sconto FROM tesseramento INNER JOIN tessera ON tesseramento.tessera = tessera.codtess WHERE codtess = 4))/100 
	INTO prezzo_totale FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto 
		WHERE (S.titolo = 'Dr. Slump' AND V.numvol = 1) OR (S.titolo = 'Maison Ikkoku' AND V.numvol = 1) OR (S.titolo = 'Monster' AND V.numvol = 9);

SELECT V.prezzofin INTO prezzo_elem_1 FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto 
	WHERE (S.titolo = 'Dr. Slump' AND V.numvol = 1);
SELECT V.prodotto INTO prodotto_1 FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto 
	WHERE (S.titolo = 'Dr. Slump' AND V.numvol = 1);

SELECT V.prezzofin INTO prezzo_elem_2 FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto 
	WHERE (S.titolo = 'Maison Ikkoku' AND V.numvol = 1);
SELECT V.prodotto INTO prodotto_2 FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto 
	WHERE (S.titolo = 'Maison Ikkoku' AND V.numvol = 1);

SELECT V.prezzofin INTO prezzo_elem_3 FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto 
	WHERE (S.titolo = 'Monster' AND V.numvol = 9);
SELECT V.prodotto INTO prodotto_3 FROM serie AS S INNER JOIN edizione AS E ON S.codserie = E.serie INNER JOIN volume as V ON V.edizione = E.prodotto 
	WHERE (S.titolo = 'Monster' AND V.numvol = 9);

SELECT sconto INTO sconto_tessera FROM tesseramento INNER JOIN tessera ON tesseramento.tessera = tessera.codtess WHERE codtess = 4;

INSERT INTO ordine (statoord, datains, prezzotot, tipoord, tesserato) VALUES ('Evaso', '2015-06-18', (SELECT * FROM prezzo_totale), 'Acquisto', (SELECT tesserato FROM tesseramento WHERE tessera = 4));

SELECT MAX(codord) INTO codice_ordine_ult FROM ordine;

INSERT INTO merce (ordine, prodotto, quantita, prezzoelem) VALUES ((SELECT * FROM codice_ordine_ult), (SELECT * FROM prodotto_1), 1, (SELECT * FROM prezzo_elem_1));
INSERT INTO merce (ordine, prodotto, quantita, prezzoelem) VALUES ((SELECT * FROM codice_ordine_ult), (SELECT * FROM prodotto_2), 1, (SELECT * FROM prezzo_elem_2));
INSERT INTO merce (ordine, prodotto, quantita, prezzoelem) VALUES ((SELECT * FROM codice_ordine_ult), (SELECT * FROM prodotto_3), 1, (SELECT * FROM prezzo_elem_3));

INSERT INTO lavorazione (ordine, acc, datalav) VALUES ((SELECT * FROM codice_ordine_ult), (SELECT codacc FROM account WHERE username = 'Titolare'), '2015-06-18');

-- Query per la verifica dell'inserimento [FIGURA 4]
SELECT serie.titolo, volume.numvol, merce.quantita, volume.prezzofin, tesserato.nome || ' ' || tesserato.cognome AS cliente FROM 
	ordine INNER JOIN merce ON ordine.codord = merce.ordine INNER JOIN lavorazione ON lavorazione.ordine = ordine.codord
		INNER JOIN volume ON volume.prodotto = merce.prodotto
			INNER JOIN tesserato ON ordine.tesserato = tesserato.codicefiscale
				INNER JOIN edizione ON volume.edizione = edizione.prodotto INNER JOIN serie ON 
					edizione.serie = serie.codserie WHERE codord = (SELECT MAX(codord) FROM ordine);