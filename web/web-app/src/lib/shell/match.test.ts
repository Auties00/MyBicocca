import { describe, expect, it } from 'vitest';
import { matchScore, normalize } from './match.ts';

describe('normalize', () => {
	it('drops accents, case and extra spaces', () => {
		expect(normalize('  Università   degli STUDI ')).toBe('universita degli studi');
	});
});

describe('matchScore', () => {
	const fees = { title: 'Tasse', subtitle: 'Servizi', keywords: ['pagamenti', 'pagoPA'] };
	const plan = { title: 'Piano di studi', subtitle: 'Servizi · Didattica' };

	it('ranks exact over prefix over word prefixes', () => {
		expect(matchScore('tasse', fees)).toBe(1);
		expect(matchScore('tas', fees)).toBe(0.9);
		expect(matchScore('pia stu', plan)).toBe(0.8);
	});

	it('falls back to keywords, then to the subtitle', () => {
		expect(matchScore('pagopa', fees)).toBe(0.65);
		expect(matchScore('didattica', plan)).toBe(0.4);
	});

	it('ignores accents and returns 0 without a match or a query', () => {
		expect(matchScore('piàno', plan)).toBe(0.9);
		expect(matchScore('mappa', fees)).toBe(0);
		expect(matchScore('   ', fees)).toBe(0);
	});
});
