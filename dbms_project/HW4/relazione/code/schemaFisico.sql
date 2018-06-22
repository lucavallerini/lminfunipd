-- Creazione del database e impostazione della codifica di default a UTF8
-- CREATE DATABASE fumetteria ENCODING 'UTF8';

-- Creazione del tipo di dati Media per il campo Media della tabella Serie
CREATE TYPE Media AS ENUM ('Anime', 'Drama', 'Light Novel', 'Live Action', 'Manga', 'Romanzo');

-- Creazione del tipo di dati StatoSerie per il campo StatoSerie della tabella Serie
CREATE TYPE StatoSerie AS ENUM ('Completa', 'In corso', 'Interrotta', 'Sospesa', 'Futura');

-- Creazione del tipo di dati Periodicita' per il campo Period della tabella Edizione
CREATE TYPE Periodicita AS ENUM ('Settimanale', 'Quindicinale', 'Mensile', 'Bimestrale', 'Trimestrale', 'Quadrimestrale', 'Semestrale', 'Annuale', 'Irregolare', 'Uscita singola');

-- Creazione del tipo di dati Supporto per il campo Supporto della tabella Edizione
CREATE TYPE Supporto AS ENUM ('DVD', 'BD', 'DVD + BD');

-- Creazione del tipo di dati StatoOrdine per il campo StatoOrd della tabella Ordine
CREATE TYPE StatoOrdine AS ENUM ('Nuovo', 'In lavorazione', 'Evaso', 'Annullato', 'Rifiutato');

-- Creazione del tipo di dati TipoOrdine per il campo TipoOrdine della tabella Ordine
CREATE TYPE TipoOrdine AS ENUM ('Acquisto', 'Abbonamento', 'Arretrato', 'Fornitura');

-- Creazione del tipo di dati StatoAccount per il campo StatoAcc della tabella Account
CREATE TYPE StatoAccount AS ENUM ('Attivo', 'Sospeso');

-- Creazione del tipo di dati TipoAccount per il campo TipoAcc della tabella Account
CREATE TYPE TipoAccount AS ENUM ('Tesserato', 'Venditore', 'Titolare');

CREATE TABLE Genere (
	NomeGen VARCHAR(50) PRIMARY KEY
);

CREATE TABLE Serie (
	CodSerie CHARACTER(32) PRIMARY KEY, -- hash md5 di titolo+autore+anno+media
	Titolo VARCHAR(100) NOT NULL,
	Autore VARCHAR(100) NOT NULL,
	Anno INT CHECK (Anno > 1800) NOT NULL,
	Media Media NOT NULL,
	StatoSerie StatoSerie NOT NULL,
	Trama TEXT,
	NumEp INT CHECK (NumEp > 0),
	Studio VARCHAR(100),
	NumVolOrig INT CHECK (NumVolOrig > 0),
	Regista VARCHAR(100),
	Rivista VARCHAR(100)
);

CREATE TABLE GenereSerie (
	Genere VARCHAR(50),
	Serie CHARACTER(32),
	PRIMARY KEY (Genere, Serie),
	FOREIGN KEY (Genere) REFERENCES Genere(NomeGen),
	FOREIGN KEY (Serie) REFERENCES Serie(CodSerie)
);

CREATE TABLE Tratto (
	SerieOrig CHARACTER(32),
	SerieDeriv CHARACTER(32),
	PRIMARY KEY (SerieOrig, SerieDeriv),
	FOREIGN KEY (SerieOrig) REFERENCES Serie(CodSerie),
	FOREIGN KEY (SerieDeriv) REFERENCES Serie(CodSerie)
);

CREATE TABLE Prodotto (
	CodProd CHARACTER(32) PRIMARY KEY, -- hash md5 
	DataIns TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(0)
);

CREATE TABLE Editore (
	PIVA CHARACTER(13) PRIMARY KEY,
	Nome VARCHAR(100) NOT NULL,
	Sito VARCHAR(100) NOT NULL
);

CREATE TABLE Edizione (
	Prodotto CHARACTER(32) PRIMARY KEY,
	Nome VARCHAR(50),
	Period Periodicita NOT NULL,
	Supporto Supporto,
	Col BOOLEAN,
	InizPubbl DATE,
	StatoEd StatoSerie,
	SovraCop BOOLEAN,
	Extra TEXT,
	NumVolTot INT CHECK (NumVolTot > 0),
	Audio TEXT,
	Video TEXT,
	Sub TEXT,
	NumPag INT CHECK (NumPag > 0),
	Editore CHARACTER(13),
	Serie CHARACTER(32),
	FOREIGN KEY (Prodotto) REFERENCES Prodotto(CodProd),
	FOREIGN KEY (Editore) REFERENCES Editore(PIVA),
	FOREIGN KEY (Serie) REFERENCES Serie(CodSerie)
);

CREATE TABLE Volume (
	Prodotto CHARACTER(32) PRIMARY KEY, --hash md5
	DataPubbl DATE NOT NULL,
	Ristampa BOOLEAN DEFAULT FALSE,
	NumRist INT DEFAULT 0 CHECK (NumRist >= 0),
	PrezzoFin NUMERIC(6,2) NOT NULL CHECK (PrezzoFin > 0),
	NumVol INT NOT NULL CHECK (NumRist >= 0),
	Cop BYTEA,
	Disp INT CHECK (Disp >= 0),
	ISBN CHARACTER(13) NOT NULL UNIQUE,
	Edizione CHARACTER(32) NOT NULL,
	FOREIGN KEY (Prodotto) REFERENCES Prodotto(CodProd),
	FOREIGN KEY (Edizione) REFERENCES Edizione(Prodotto)
);

CREATE TABLE Distributore (
	PIVA CHARACTER(13) PRIMARY KEY,
	Nome VARCHAR(100) NOT NULL,
	Sito VARCHAR(100) NOT NULL
);

CREATE TABLE Mail (
	Mail VARCHAR(100) PRIMARY KEY
);

CREATE TABLE TelFax (
	CodTelFax CHARACTER(32) PRIMARY KEY, --hash md5 num+fax
	Num VARCHAR(20) NOT NULL,
	Fax BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE TelFaxEd (
	TelFax CHARACTER(32),
	Editore CHARACTER(13),
	PRIMARY KEY (TelFax, Editore),
	FOREIGN KEY (TelFax) REFERENCES TelFax(CodTelFax),
	FOREIGN KEY (Editore) REFERENCES Editore(PIVA)
);

CREATE TABLE MailEd (
	Mail VARCHAR(100),
	Editore CHARACTER(13),
	PRIMARY KEY (Mail, Editore),
	FOREIGN KEY (Mail) REFERENCES   Mail(Mail),
	FOREIGN KEY (Editore) REFERENCES Editore(PIVA)
);

CREATE TABLE TelFaxDistr (
	TelFax CHARACTER(32),
	Distributore CHARACTER(13),
	PRIMARY KEY (TelFax, Distributore),
	FOREIGN KEY (TelFax) REFERENCES   TelFax(CodTelFax),
	FOREIGN KEY (Distributore) REFERENCES Distributore(PIVA)
);

CREATE TABLE MailDistr (
	Mail VARCHAR(100),
	Distributore CHARACTER(13),
	PRIMARY KEY (Mail, Distributore),
	FOREIGN KEY (Mail) REFERENCES   Mail(Mail),
	FOREIGN KEY (Distributore) REFERENCES Distributore(PIVA)
);

CREATE TABLE Distribuzione (
	Distributore CHARACTER(13),
	Editore CHARACTER(13),
	PRIMARY KEY (Distributore, Editore),
	FOREIGN KEY (Distributore) REFERENCES Distributore(PIVA),
	FOREIGN KEY (Editore) REFERENCES Editore(PIVA)
);

CREATE TABLE Indirizzo (
	CodInd CHARACTER(32) PRIMARY KEY, --md5 via+civico+citta+provincia 
	Via VARCHAR(100) NOT NULL,
	Civico VARCHAR(10) NOT NULL,
	CAP CHARACTER(5),
	Citta VARCHAR(50) NOT NULL,
	Provincia CHARACTER(2) NOT NULL
);

CREATE TABLE Tesserato (
	CodiceFiscale CHARACTER(16) PRIMARY KEY,
	Nome VARCHAR(50) NOT NULL,
	Cognome VARCHAR(50) NOT NULL,
	Nascita DATE NOT NULL,
	Mail VARCHAR(100) NOT NULL,
	FOREIGN KEY (Mail) REFERENCES Mail(Mail)
);

CREATE TABLE TelTesserato (
	Tesserato CHARACTER(16),
	TelFax CHARACTER(32),
	PRIMARY KEY (Tesserato, TelFax),
	FOREIGN KEY (Tesserato) REFERENCES Tesserato(CodiceFiscale),
	FOREIGN KEY (TelFax) REFERENCES TelFax(CodTelFax)
);

CREATE TABLE IndirizzoTesserato (
	Tesserato CHARACTER(16),
	Indirizzo CHARACTER(32),
	PRIMARY KEY (Tesserato, Indirizzo),
	FOREIGN KEY (Tesserato) REFERENCES Tesserato(CodiceFiscale),
	FOREIGN KEY (Indirizzo) REFERENCES Indirizzo(CodInd)
);

CREATE SEQUENCE Ordine_CodOrd_seq;
CREATE TABLE Ordine (
	CodOrd INT PRIMARY KEY DEFAULT nextval('Ordine_CodOrd_seq'),
	StatoOrd StatoOrdine,
	DataIns TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
	PrezzoTot NUMERIC(6,2) NOT NULL CHECK (PrezzoTot > 0),
	TipoOrd TipoOrdine NOT NULL,
	Tesserato CHARACTER(16),
	FOREIGN KEY (Tesserato) REFERENCES Tesserato(CodiceFiscale)
);

CREATE TABLE Rifornimento (
	Ordine INT,
	Distributore CHARACTER(13),
	PRIMARY KEY (Ordine, Distributore),
	FOREIGN KEY (Ordine) REFERENCES Ordine(CodOrd),
	FOREIGN KEY (Distributore) REFERENCES Distributore(PIVA)
);

CREATE TABLE Merce (
	Ordine INT,
	Prodotto CHARACTER(32),
	Quantita INT NOT NULL DEFAULT 1 CHECK(Quantita > 0),
	PrezzoElem NUMERIC(6,2) NOT NULL CHECK(PrezzoElem > 0),
	PRIMARY KEY (Ordine, Prodotto),
	FOREIGN KEY (Ordine) REFERENCES Ordine(CodOrd),
	FOREIGN KEY (Prodotto) REFERENCES Prodotto(CodProd)
);

CREATE SEQUENCE Account_CodAcc_seq;
CREATE TABLE Account (
	CodAcc INT PRIMARY KEY DEFAULT nextval('Account_CodAcc_seq'),
	Username VARCHAR(30) NOT NULL UNIQUE,
	PW CHARACTER(32) NOT NULL, --md5
	StatoAcc StatoAccount NOT NULL,
	TipoAcc TipoAccount NOT NULL
);

CREATE TABLE Lavorazione (
	Ordine INT,
	Acc INT,
	DataLav TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
	PRIMARY KEY (Ordine, Acc),
	FOREIGN KEY (Ordine) REFERENCES Ordine(CodOrd),
	FOREIGN KEY (Acc) REFERENCES Account(CodAcc)
);

CREATE SEQUENCE Notifica_CodMail_seq;
CREATE TABLE Notifica (
	CodMail INT PRIMARY KEY DEFAULT nextval('Notifica_CodMail_seq'),
	Mittente INT NOT NULL,
	Destinatario INT NOT NULL,
	DataInvio TIMESTAMP,
	DataGen TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
	Oggetto VARCHAR(100) NOT NULL,
	Testo TEXT NOT NULL,
	FOREIGN KEY (Mittente) REFERENCES Account(CodAcc),
	FOREIGN KEY (Destinatario) REFERENCES Account(CodAcc)
);

CREATE TABLE Incasellamento (
	Prodotto CHARACTER(32),
	AccStaff INT,
	AccTesserato INT,
	Quantita INT NOT NULL DEFAULT 1 CHECK (Quantita > 0),
	DataIns TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
	DataScad TIMESTAMP NOT NULL,
	DataRim TIMESTAMP,
	PRIMARY KEY (Prodotto, AccStaff, AccTesserato),
	FOREIGN KEY (Prodotto) REFERENCES Prodotto(CodProd),
	FOREIGN KEY (AccStaff) REFERENCES Account(CodAcc),
	FOREIGN KEY (AccTesserato) REFERENCES Account(CodAcc)
);

CREATE SEQUENCE Tessera_CodTess_seq;
CREATE TABLE Tessera (
	CodTess INT PRIMARY KEY DEFAULT nextval('Tessera_CodTess_seq'),
	InizioVal DATE NOT NULL,
	FineVal DATE NOT NULL,
	DurataCasella INT NOT NULL DEFAULT 1 CHECK (DurataCasella > 0),
	PrezzoTess NUMERIC(6,2) NOT NULL DEFAULT 5 CHECK (PrezzoTess > 0),
	Sconto INT NOT NULL DEFAULT 10 CHECK (Sconto >= 0 AND Sconto <= 100)
);

CREATE TABLE Tesseramento (
	AccTesserante INT,
	AccTesserato INT,
	Tesserato CHARACTER(16),
	Tessera INT,
	DataTess DATE NOT NULL,
	PRIMARY KEY (AccTesserante, AccTesserato, Tesserato, Tessera),
	FOREIGN KEY (AccTesserante) REFERENCES Account(CodAcc),
	FOREIGN KEY (AccTesserato) REFERENCES Account(CodAcc),
	FOREIGN KEY (Tesserato) REFERENCES Tesserato(CodiceFiscale),
	FOREIGN KEY (Tessera) REFERENCES Tessera(CodTess)
);