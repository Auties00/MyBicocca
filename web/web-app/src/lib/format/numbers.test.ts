import { describe, expect, it } from 'vitest';
import { formatBytes, formatCredits, formatCurrency } from './numbers.ts';

describe('formatCredits', () => {
	it('shows a decimal only when there is one, with the plural', () => {
		expect(formatCredits(8, 'it')).toBe('8 CFU');
		expect(formatCredits(7.5, 'it')).toBe('7,5 CFU');
		expect(formatCredits(1, 'en')).toBe('1 credit');
		expect(formatCredits(12, 'en')).toBe('12 credits');
	});
});

describe('formatCurrency', () => {
	it('writes euros in the locale', () => {
		expect(formatCurrency(156, 'EUR', 'it')).toBe('156,00 €');
		expect(formatCurrency(156, 'EUR', 'en')).toBe('€156.00');
	});
});

describe('formatBytes', () => {
	it('picks the largest unit at least 1', () => {
		expect(formatBytes(256 * 1024 * 1024, 'en')).toBe('256 MB');
		expect(formatBytes(1.5 * 1024 * 1024 * 1024, 'en')).toBe('1.5 GB');
	});

	it('moves to the next unit from 1000, never showing four digits', () => {
		expect(formatBytes(1021 * 1024, 'it')).toBe('1 MB');
		expect(formatBytes(999 * 1024, 'en')).toBe('999 kB');
		expect(formatBytes(1000, 'en')).toBe('1 kB');
	});

	it('spells bytes out, plural in English', () => {
		expect(formatBytes(900, 'en')).toBe('900 bytes');
		expect(formatBytes(1, 'en')).toBe('1 byte');
		expect(formatBytes(900, 'it')).toBe('900 byte');
	});
});
