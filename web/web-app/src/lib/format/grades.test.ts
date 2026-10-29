import { describe, expect, it } from 'vitest';
import { formatAverage, formatGrade, formatGradeValue } from './grades.ts';

describe('formatGrade', () => {
	it('writes 30 cum laude as 30L, spelled out on request', () => {
		const laude = { kind: 'numeric', value: 30, cumLaude: true } as const;
		expect(formatGrade(laude, { locale: 'it' })).toBe('30L');
		expect(formatGrade(laude, { full: true, locale: 'it' })).toBe('30 e lode');
		expect(formatGrade(laude, { full: true, locale: 'en' })).toBe('30 with honours');
	});

	it('writes plain grades as numbers', () => {
		expect(formatGrade({ kind: 'numeric', value: 28, cumLaude: false }, { locale: 'en' })).toBe(
			'28'
		);
	});

	it('abbreviates the outcomes without a grade, per language', () => {
		expect(formatGrade({ kind: 'passed' }, { locale: 'it' })).toBe('Id.');
		expect(formatGrade({ kind: 'not_passed' }, { locale: 'en' })).toBe('Fail');
		expect(formatGrade({ kind: 'withdrew' }, { locale: 'it' })).toBe('Rit.');
		expect(formatGrade({ kind: 'absent' }, { full: true, locale: 'it' })).toBe('Assente');
		expect(formatGrade({ kind: 'unknown' })).toBe('—');
	});
});

describe('formatGradeValue', () => {
	it('shows the laude only on a 30', () => {
		expect(formatGradeValue({ value: 30, cumLaude: true })).toBe('30L');
		expect(formatGradeValue({ value: 27, cumLaude: false })).toBe('27');
	});
});

describe('formatAverage', () => {
	it('always shows two decimals in the locale', () => {
		expect(formatAverage(27.456, 'it')).toBe('27,46');
		expect(formatAverage(28, 'en')).toBe('28.00');
	});
});
