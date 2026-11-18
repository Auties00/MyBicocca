import { describe, expect, it } from 'vitest';
import { stripMlang } from './mlang.ts';

describe('stripMlang', () => {
	const name = '{mlang it}Basi di Dati{mlang}{mlang en}Databases{mlang}';

	it('keeps the blocks in the wanted language', () => {
		expect(stripMlang(name, 'it')).toBe('Basi di Dati');
		expect(stripMlang(name, 'en')).toBe('Databases');
	});

	it('keeps the text outside blocks', () => {
		expect(stripMlang('E3101Q107 - {mlang it}Basi{mlang}{mlang en}Databases{mlang}', 'en')).toBe(
			'E3101Q107 - Databases'
		);
	});

	it('falls back to other, then English, then the first block language', () => {
		expect(stripMlang('{mlang it}Basi{mlang}{mlang other}Bases{mlang}', 'de')).toBe('Bases');
		expect(stripMlang(name, 'de')).toBe('Databases');
		expect(stripMlang('{mlang fr}Bases{mlang}{mlang it}Basi{mlang}', 'de')).toBe('Bases');
	});

	it('serves a regional variant from its language and reads language lists', () => {
		expect(stripMlang('{mlang en}Databases{mlang}{mlang it}Basi{mlang}', 'en-US')).toBe(
			'Databases'
		);
		expect(stripMlang('{ MLANG it, en_us }Basi{mlang}{mlang de}Banken{mlang}', 'en-us')).toBe(
			'Basi'
		);
	});

	it('leaves names without blocks alone', () => {
		expect(stripMlang('Algoritmi', 'it')).toBe('Algoritmi');
		expect(stripMlang('{mlang it}unterminated', 'it')).toBe('{mlang it}unterminated');
	});
});
