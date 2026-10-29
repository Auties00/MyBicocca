import { describe, expect, it } from 'vitest';
import { joinParts, sentenceCase } from './text.ts';

describe('sentenceCase', () => {
	it('lowers a capitalised name but its first letter', () => {
		expect(sentenceCase('SCIENZE E TECNOLOGIE INFORMATICHE')).toBe(
			'Scienze e tecnologie informatiche'
		);
		expect(sentenceCase('MANAGEMENT AND BUSINESS DATA ANALYTICS')).toBe(
			'Management and business data analytics'
		);
	});

	it('keeps acronyms and class codes in capitals', () => {
		expect(sentenceCase('ICT FOR SMART SOCIETIES')).toBe('ICT for smart societies');
		expect(sentenceCase('L-31 SCIENZE')).toBe('L-31 scienze');
	});

	it('lowers elided articles and the words they join', () => {
		expect(sentenceCase("L'ARTE DEL NOVECENTO")).toBe("L'arte del novecento");
	});

	it('keeps the capital of places and institutions', () => {
		expect(sentenceCase("DIRITTO DELL'UNIONE EUROPEA")).toBe("Diritto dell'Unione europea");
		expect(sentenceCase('STORIA DI MILANO E DELLA LOMBARDIA')).toBe(
			'Storia di Milano e della Lombardia'
		);
	});

	it('leaves names already in mixed case as written', () => {
		expect(sentenceCase('Corso di Laurea')).toBe('Corso di Laurea');
	});
});

describe('joinParts', () => {
	it('skips missing parts and keeps each dot on the line of the part before it', () => {
		expect(joinParts(['Milano', null, '', '2025'])).toBe('Milano\u00a0· 2025');
	});
});
