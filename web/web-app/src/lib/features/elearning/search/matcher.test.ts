import { describe, expect, it } from 'vitest';
import { QueryMatcher, Tier, merged, typoBudget } from './matcher.ts';
import { degeminate, stemItalian } from './italian.ts';
import { damerauLevenshtein, jaroWinkler } from './similarity.ts';
import { subsequenceMatch } from './subsequence.ts';

const match = (query: string, text: string) => new QueryMatcher(query).match(text);

describe('QueryMatcher tiers', () => {
	it('finds the whole query as a substring first, folding case and accents', () => {
		expect(match('probabilita', 'Probabilità e Statistica')).toEqual({
			tier: Tier.substring,
			ranges: [[0, 11]]
		});
		expect(match('  e3101q ', '2627-2-E3101Q106')).toEqual({
			tier: Tier.substring,
			ranges: [[7, 13]]
		});
	});

	it('matches every query word as the prefix of a distinct word', () => {
		expect(match('strut alg', 'Algoritmi e Strutture Dati')).toEqual({
			tier: Tier.prefix,
			ranges: [
				[0, 3],
				[12, 17]
			]
		});
		// Two query words can't share one text word.
		expect(match('alg algo', 'Algoritmi e Strutture Dati')).toBeNull();
	});

	it('matches by light Italian stem from six characters, lighting the whole word', () => {
		expect(match('matematiche avanzate', 'Matematica Avanzata')?.tier).toBe(Tier.prefix);
		expect(match('matematiche avanzate', 'Matematica Avanzata')?.ranges).toEqual([
			[0, 10],
			[11, 19]
		]);
	});

	it('matches the initials of the content words, skipping stop words', () => {
		expect(match('asd', 'Algoritmi e Strutture Dati')).toEqual({
			tier: Tier.acronym,
			ranges: [
				[0, 1],
				[12, 13],
				[22, 23]
			]
		});
		// Spaced letters are word prefixes, a stronger tier.
		expect(match('a s d', 'Algoritmi e Strutture Dati')?.tier).toBe(Tier.prefix);
		// One content word is no acronym.
		expect(match('f', 'della Fisica')?.tier).toBe(Tier.substring);
		expect(match('fx', 'della Fisica')).toBeNull();
	});

	it('matches abbreviations as a subsequence anchored on a word start', () => {
		expect(match('anmat', 'Analisi Matematica')).toEqual({
			tier: Tier.subsequence,
			ranges: [
				[0, 2],
				[8, 11]
			]
		});
		expect(match('fisgen', 'Fisica Generale I')?.tier).toBe(Tier.subsequence);
	});

	it('corrects typos within the budget, ignoring stop words', () => {
		expect(match('algortimi', 'Algoritmi e Strutture Dati')).toEqual({
			tier: Tier.fuzzy,
			ranges: [[0, 9]]
		});
		// Doubled letters cost nothing.
		expect(match('soprattutto', 'Sopratutto')?.tier).toBe(Tier.fuzzy);
		expect(match('stattistica di', 'Statistica')?.tier).toBe(Tier.fuzzy);
		// Every significant word must land on its own text word.
		expect(match('stattistica base', 'Statistica')).toBeNull();
	});

	it('rejects what no tier explains', () => {
		expect(match('chimica', 'Algoritmi e Strutture Dati')).toBeNull();
		// Three letters get no typo budget.
		expect(match('sql', 'Sqi')).toBeNull();
		expect(match('', 'Fisica')).toBeNull();
		expect(match('fisica', '')).toBeNull();
		expect(new QueryMatcher('   ').isEmpty).toBe(true);
	});

	it('keeps highlight indexes on the original text when folding', () => {
		const text = 'Università degli Studi';
		const result = match('studi', text);
		expect(result).not.toBeNull();
		const [start, end] = result?.ranges[0] ?? [0, 0];
		expect(text.slice(start, end)).toBe('Studi');
	});
});

describe('search helpers', () => {
	it('stems only long words, dropping the inflection', () => {
		expect(stemItalian('matematiche')).toBe('matematic');
		expect(stemItalian('matematica')).toBe('matematic');
		expect(stemItalian('analisi')).toBe('analis');
		expect(stemItalian('esercizio')).toBe('eserciz');
		expect(stemItalian('dati')).toBe('dati');
	});

	it('collapses doubled letters', () => {
		expect(degeminate('soprattutto')).toBe('sopratuto');
		expect(degeminate('a')).toBe('a');
	});

	it('caps Damerau-Levenshtein at the budget, counting a swap as one edit', () => {
		expect(damerauLevenshtein('hte', 'the', 1)).toBe(1);
		expect(damerauLevenshtein('kitten', 'sitting', 2)).toBe(-1);
		expect(damerauLevenshtein('kitten', 'sitting', 3)).toBe(3);
		expect(damerauLevenshtein('abc', 'abc', 0)).toBe(0);
	});

	it('weights Jaro-Winkler by the common prefix', () => {
		expect(jaroWinkler('martha', 'marhta')).toBeCloseTo(0.9611, 3);
		expect(jaroWinkler('abc', 'xyz')).toBe(0);
	});

	it('gives the typo budget by length', () => {
		expect([3, 4, 7, 8].map(typoBudget)).toEqual([0, 1, 1, 2]);
	});

	it('refuses short or floating subsequences', () => {
		expect(subsequenceMatch('anm', 'analisi matematica')).toBeNull();
		expect(subsequenceMatch('nali', 'xanalisi')).toBeNull();
	});

	it('merges touching and overlapping ranges', () => {
		expect(
			merged([
				[5, 7],
				[0, 2],
				[2, 3],
				[6, 9]
			])
		).toEqual([
			[0, 3],
			[5, 9]
		]);
	});
});
