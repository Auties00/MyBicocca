import { describe, expect, it } from 'vitest';
import {
	normalizeSubjectName,
	sectionTurno,
	shortLabelFor,
	subjectBase,
	subjectTurno,
	variantLabel
} from './labels.ts';

describe('shortLabelFor', () => {
	it.each([
		['Algoritmi e Strutture Dati', 'ASD'],
		['Calcolo delle Probabilità', 'CP'],
		['Fondamenti di Informatica', 'FI'],
		['Analisi Matematica I', 'AM1'],
		['Programmazione 1', 'Prog1'],
		['Statistica', 'Stat'],
		['Lingua Inglese B2', 'LIB2'],
		['Analisi I (modulo A)', 'Anal1'],
		["Metodi Matematici per l'Ingegneria", 'MMI'],
		['BASI DI DATI', 'BD'],
		['SISTEMI OPERATIVI', 'SO'],
		['PROBABILITÀ E STATISTICA', 'PS'],
		['Sviluppo, Analisi, Progetto e Gestione di Sistemi Informativi Complessi', 'SAPGS']
	])('%s → %s', (name, label) => {
		expect(shortLabelFor(name)).toBe(label);
	});

	it('keeps a lone suffix, and gives up on nothing', () => {
		expect(shortLabelFor('Il 2')).toBe('2');
		expect(shortLabelFor('(solo note)')).toBeNull();
		expect(shortLabelFor('   ')).toBeNull();
	});
});

describe('normalizeSubjectName', () => {
	it('trims, collapses blanks and uppercases', () => {
		expect(normalizeSubjectName('  Basi   di\tdati ')).toBe('BASI DI DATI');
	});
});

describe('EasyStaff variants', () => {
	it('strips the turno and group EasyStaff appends to a course name', () => {
		expect(subjectBase('ANALISI E PROGETTO DI ALGORITMI (T1 LEZ + ESE + LAB)')).toBe(
			'ANALISI E PROGETTO DI ALGORITMI'
		);
		expect(subjectBase('INGEGNERIA DEL SOFTWARE - LAB')).toBe('INGEGNERIA DEL SOFTWARE');
		expect(subjectBase('INTERAZIONE UOMO - MACCHINA')).toBe('INTERAZIONE UOMO - MACCHINA');
		expect(subjectBase('BASI DI DATI')).toBe('BASI DI DATI');
	});

	it('reads the turno of a timetable section', () => {
		expect(sectionTurno('T2')).toBe(2);
		expect(sectionTurno('T1GEA')).toBe(1);
		expect(sectionTurno('TU')).toBeNull();
		expect(sectionTurno('TUGLA')).toBeNull();
		expect(sectionTurno('N0')).toBeNull();
	});

	it('reads the turno of a variant from its name', () => {
		expect(subjectTurno('RICERCA OPERATIVA (T2 FRONTALE TURNO 2)')).toBe(2);
		expect(subjectTurno('RICERCA OPERATIVA (T1GEA ESER TURNO 1 + T1GLA LAB TURNO 1)')).toBe(1);
		expect(subjectTurno('BASI DI DATI (TU FRONTALE TURNO UNICO)')).toBeNull();
		expect(subjectTurno('BASI DI DATI')).toBeNull();
	});

	it('labels a variant briefly, and single-turno lectures not at all', () => {
		expect(variantLabel('X (TU FRONTALE TURNO UNICO)')).toBeNull();
		expect(variantLabel('X (T1 LEZ + ESE + LAB)')).toBe('T1');
		expect(variantLabel('X (T1GEA ESER TURNO 1 + T1GLA LAB TURNO 1)')).toBe('T1 · Ese + Lab');
		expect(variantLabel('X (TUGLA LAB TURNO UNICO GRUPPO A)')).toBe('Lab · Gruppo A');
		expect(variantLabel('X - LAB')).toBe('Lab');
		expect(variantLabel('X (Cognomi A-L)')).toBe('Cognomi A-L');
	});

	it("labels the turno a lesson's section gives over the one its name tells", () => {
		expect(variantLabel('X (T1 LEZ + ESE + LAB)', 2)).toBe('T2');
		expect(variantLabel('X (T1GEA ESER TURNO 1 + T1GLA LAB TURNO 1)', null)).toBe('Ese + Lab');
	});
});
