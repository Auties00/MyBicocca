import { describe, expect, it } from 'vitest';
import { buildings, densestCity, findBuilding, type Building } from './catalog.ts';

const pick = (...codes: string[]): Building[] =>
	codes.map((code) => findBuilding(code)).filter((entry) => entry !== undefined);

describe('densestCity', () => {
	it('keeps the city with the most buildings', () => {
		expect(densestCity(buildings).every((entry) => entry.city === 'Milano')).toBe(true);
		const codes = densestCity(pick('U01', 'U08', 'U18', 'U-BG')).map((entry) => entry.code);
		expect(codes).toEqual(['U08', 'U18']);
	});

	it('breaks a tie with the city listed first, and is empty for no buildings', () => {
		expect(densestCity(pick('U-BG', 'U26')).map((entry) => entry.code)).toEqual(['U-BG']);
		expect(densestCity([])).toEqual([]);
	});
});
