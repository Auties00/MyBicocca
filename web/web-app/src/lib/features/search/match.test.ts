import { describe, expect, it } from 'vitest';
import { highlight, matchScore, prepareQuery, prepareText, type MatchTier } from './match.ts';

function match(query: string, text: string, aliases: string[] = []) {
	const prepared = prepareQuery(query);
	if (prepared === null) return null;
	return matchScore(prepared, prepareText(text), aliases.map(prepareText));
}

const score = (query: string, text: string, aliases: string[] = []) =>
	match(query, text, aliases)?.score ?? null;

describe('matchScore', () => {
	it('ranks the exact text first, ignoring case, accents and spacing', () => {
		expect(score('  probabilità  ', 'Probabilita')).toBe(1);
	});

	it('takes every query word as a prefix of a word of the text', () => {
		expect(score('basi dat', 'Basi di Dati')).toBe(0.85);
		expect(score('dati basi', 'Basi di Dati')).toBe(0.85);
		expect(score('l ingegneria', "L'ingegneria del software")).toBe(0.85);
	});

	it('lets words of six letters or more meet on their Italian stem', () => {
		expect(score('matematiche', 'Analisi Matematica')).toBe(0.85);
		expect(score('mate', 'Analisi Matematica')).toBe(0.85);
	});

	it('reads acronyms of the content words', () => {
		expect(score('asd', 'Algoritmi e Strutture Dati')).toBe(0.8);
		expect(score('asd', 'Analisi')).toBeNull();
	});

	it('scores aliases below the title: exact, then by prefixes, saying which one', () => {
		expect(match('e3101q118', 'Basi di Dati', ['E3101Q118'])).toEqual({
			score: 0.75,
			tier: 'alias',
			alias: 0
		});
		expect(match('esami', 'Libretto', ['esami sostenuti', 'voti', 'esami'])).toEqual({
			score: 0.75,
			tier: 'alias',
			alias: 2
		});
		expect(match('pago', 'Tasse', ['bollettini', 'pagoPA'])).toEqual({
			score: 0.7,
			tier: 'alias',
			alias: 1
		});
	});

	it('maps abbreviations made of word starts onto the 0.55–0.72 band', () => {
		for (const [query, title] of [
			['anmat', 'Analisi Matematica'],
			['sisop', 'Sistemi Operativi'],
			['progr1', 'Programmazione 1']
		]) {
			const found = match(query, title);
			expect(found?.tier).toBe('abbreviation');
			expect(found?.score).toBeGreaterThanOrEqual(0.55);
			expect(found?.score).toBeLessThanOrEqual(0.72);
		}
	});

	it('finds no abbreviation in letters scattered inside words', () => {
		expect(match('aula', 'Average simulator')).toBeNull();
		expect(match('orario', 'Ricerca Operativa e Pianificazione delle Risorse')).toBeNull();
		expect(match('sistemi', 'Probabilità e Statistica per l’Informatica')).toBeNull();
	});

	it('finds no abbreviation in titles longer than five words', () => {
		const long = 'Informativa Privacy e Consenso al Trattamento Dati Personali';
		expect(match('esami', long)).toBeNull();
		expect(match('ipct', long)).toBeNull();
	});

	it('forgives typos within the budget, doubled letters for free', () => {
		const typo = score('algoritni', 'Algoritmi e Strutture Dati');
		expect(typo).toBeGreaterThanOrEqual(0.5);
		expect(typo).toBeLessThan(0.7);
		expect(match('programazione', 'Programmazione 1')).toMatchObject({ tier: 'typo' });
		expect(score('programazione', 'Programmazione 1')).toBeGreaterThan(0.68);
	});

	it('finds nothing for unrelated or too short misspelt words', () => {
		expect(score('fisica', 'Basi di Dati')).toBeNull();
		expect(score('bsd', 'Basi di Dati')).toBeNull();
		expect(prepareQuery('  - / ')).toBeNull();
	});

	it('ignores function words in the typo tier', () => {
		expect(score('analisy di', 'Analisi Matematica')).toBeGreaterThanOrEqual(0.5);
	});

	it('takes no typo on the first letter', () => {
		expect(match('laboratorio', 'Architettura degli Elaboratori')).toBeNull();
		expect(match('laboratori', 'Architettura degli Elaboratori')).toBeNull();
	});

	it('reads a misspelt word as a typo, not as letters picked across the title', () => {
		expect(match('aule', 'Aula 2088 (Aula Tribunale)')).toMatchObject({ tier: 'typo' });
	});

	it('splits words on punctuation', () => {
		expect(score('sala', 'Aula 2151 (Sala Lauree)')).toBe(0.85);
		expect(score('tribunale', 'Aula 2088 (Aula Tribunale)')).toBe(0.85);
	});
});

describe('highlight', () => {
	function ranges(query: string, title: string, tier?: MatchTier) {
		const prepared = prepareQuery(query);
		if (prepared === null) throw new Error('empty query');
		const found = tier ?? matchScore(prepared, prepareText(title))?.tier;
		if (found === undefined) throw new Error(`no match for ${query}`);
		return highlight(prepared, title, found).map(([start, end]) => title.slice(start, end));
	}

	it('marks the typed part of each word', () => {
		expect(ranges('dati basi', 'Basi di Dati')).toEqual(['Basi', 'Dati']);
		expect(ranges('matematiche', 'Analisi Matematica')).toEqual(['Matematica']);
		expect(ranges('probabilita', 'Probabilità')).toEqual(['Probabilità']);
	});

	it('marks the initials of an acronym and the letters of an abbreviation', () => {
		expect(ranges('asd', 'Algoritmi e Strutture Dati')).toEqual(['A', 'S', 'D']);
		expect(ranges('anmat', 'Analisi Matematica')).toEqual(['An', 'Mat']);
	});

	it('marks the whole words a typo stands for', () => {
		expect(ranges('programazione', 'Linguaggi di Programmazione')).toEqual(['Programmazione']);
		expect(ranges('aule', 'Aula 2151 (Sala Lauree)')).toEqual(['Aula']);
	});

	it('marks nothing for an alias', () => {
		expect(ranges('orario', 'Calendario', 'alias')).toEqual([]);
	});
});
