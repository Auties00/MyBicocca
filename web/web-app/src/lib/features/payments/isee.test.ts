import { decodeEsse3EnrollmentForTuition } from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import { declareYear, iseeMargin, iseeYears } from './isee.ts';

const row = (fields: Record<string, unknown>) => {
	const value = decodeEsse3EnrollmentForTuition({
		aaIscrId: 2025,
		cdsDes: 'INFORMATICA',
		isee: 24_380,
		sogliaIsee: 30_000,
		des: 'Esonero parziale per reddito',
		...fields
	});
	if (value === undefined) throw new Error('not an object');
	return value;
};

const NOT_PRESENTED = 99_999_999;

describe('iseeYears', () => {
	it('keeps the years with an ISEE on file, newest first', () => {
		const years = iseeYears([
			row({ aaIscrId: 2024, isee: 18_000 }),
			row({ aaIscrId: 2026, isee: NOT_PRESENTED }),
			row({ aaIscrId: 2025 }),
			row({ aaIscrId: null }),
			row({ aaIscrId: 2023, isee: null })
		]);
		expect(years.map((year) => [year.year, year.isee])).toEqual([
			[2025, 24_380],
			[2024, 18_000]
		]);
	});

	it('maps the threshold, course and exemption, blanks as missing', () => {
		expect(iseeYears([row({ des: ' ', tipoIscrCodDes: 'In corso', annoCorso: 2 })])[0]).toEqual({
			year: 2025,
			isee: 24_380,
			threshold: 30_000,
			course: 'INFORMATICA',
			exemption: null,
			enrollmentType: 'In corso',
			courseYear: 2
		});
	});

	it('keeps one row per year', () => {
		expect(iseeYears([row({}), row({ isee: 1 })]).map((year) => year.isee)).toEqual([24_380]);
	});
});

describe('declareYear', () => {
	const october = new Date(2026, 9, 27);

	it('offers this year while its enrolment has no ISEE yet', () => {
		expect(declareYear([row({}), row({ aaIscrId: 2026, isee: NOT_PRESENTED })], october)).toBe(
			2026
		);
		expect(declareYear([row({}), row({ aaIscrId: 2026, isee: null })], october)).toBe(2026);
	});

	it('offers nothing once this year has an ISEE', () => {
		expect(declareYear([row({ aaIscrId: 2026 })], october)).toBeNull();
	});

	it('offers nothing when the latest enrolment is not this calendar year', () => {
		expect(declareYear([row({ aaIscrId: 2025, isee: NOT_PRESENTED })], october)).toBeNull();
		expect(
			declareYear([row({ aaIscrId: 2026, isee: NOT_PRESENTED })], new Date(2027, 0, 5))
		).toBeNull();
		expect(declareYear([], october)).toBeNull();
	});

	it('counts the calendar year in Italy', () => {
		const newYearInRome = new Date(Date.UTC(2026, 11, 31, 23, 30));
		expect(declareYear([row({ aaIscrId: 2027, isee: NOT_PRESENTED })], newYearInRome)).toBe(2027);
	});
});

describe('iseeMargin', () => {
	it('measures the distance below the threshold, negative above it', () => {
		expect(iseeMargin({ isee: 24_380, threshold: 30_000 })).toBe(5_620);
		expect(iseeMargin({ isee: 31_000, threshold: 30_000 })).toBe(-1_000);
		expect(iseeMargin({ isee: 1, threshold: null })).toBeNull();
		expect(iseeMargin({ isee: 1, threshold: 0 })).toBeNull();
	});
});
