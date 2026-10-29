import { describe, expect, it } from 'vitest';
import { italianTitleCase, personName, sentenceCase } from './names.ts';

describe('italianTitleCase', () => {
	it('capitalizes words and keeps minor words lowercase', () => {
		expect(italianTitleCase('ALGORITMI E STRUTTURE DATI')).toBe('Algoritmi e Strutture Dati');
		expect(italianTitleCase('BASI DI DATI')).toBe('Basi di Dati');
		expect(italianTitleCase('ANALISI DELLE RETI SOCIALI')).toBe('Analisi delle Reti Sociali');
	});

	it('capitalizes a minor word that opens the title', () => {
		expect(italianTitleCase('IL LABORATORIO')).toBe('Il Laboratorio');
		expect(italianTitleCase('E-LEARNING E DIDATTICA')).toBe('E-learning e Didattica');
	});

	it('restores final accents written as apostrophes', () => {
		expect(italianTitleCase("AFFIDABILITA' DEI SISTEMI")).toBe('Affidabilità dei Sistemi');
		expect(italianTitleCase('PROBABILITÀ E STATISTICA')).toBe('Probabilità e Statistica');
	});

	it('handles elided articles and prepositions', () => {
		expect(italianTitleCase("FONDAMENTI DELL'INFORMATICA")).toBe("Fondamenti dell'Informatica");
		expect(italianTitleCase("L'ANALISI MATEMATICA")).toBe("L'Analisi Matematica");
		expect(italianTitleCase('STORIA DELL’ARTE')).toBe("Storia dell'Arte");
	});

	it('keeps roman numerals in capitals', () => {
		expect(italianTitleCase('ANALISI MATEMATICA II')).toBe('Analisi Matematica II');
		expect(italianTitleCase('FISICA I')).toBe('Fisica I');
		expect(italianTitleCase('I SISTEMI E I LINGUAGGI')).toBe('I Sistemi e i Linguaggi');
	});

	it('collapses whitespace and capitalizes after punctuation', () => {
		expect(italianTitleCase('  LINGUA   INGLESE (B2)  ')).toBe('Lingua Inglese (B2)');
	});
});

describe('personName', () => {
	it('capitalizes every part of a name, particles and elisions included', () => {
		expect(personName('ALESSANDRO AUTIERO')).toBe('Alessandro Autiero');
		expect(personName("MARIA D'ANGELO-ROSSI")).toBe("Maria D'Angelo-Rossi");
		expect(personName('PAOLO DELLA VALLE')).toBe('Paolo Della Valle');
	});
});

describe('sentenceCase', () => {
	it('lowers a label in capitals but for its roman numerals and accents', () => {
		expect(sentenceCase('II APPELLO')).toBe('II appello');
		expect(sentenceCase('PROVA INTERMEDIA I')).toBe('Prova intermedia I');
		expect(sentenceCase("PROVA DI META'")).toBe('Prova di metà');
	});

	it('only raises the first letter of a label typed in mixed case', () => {
		expect(sentenceCase('compitino del 12/11')).toBe('Compitino del 12/11');
		expect(sentenceCase('VI appello completo')).toBe('VI appello completo');
	});
});
