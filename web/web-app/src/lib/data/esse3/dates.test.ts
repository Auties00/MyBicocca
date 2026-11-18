import { describe, expect, it } from 'vitest';
import { esse3Date, esse3DateTime } from './dates.ts';

describe('esse3Date', () => {
	it('reads dd/MM/yyyy with or without a time, and ISO dates', () => {
		expect(esse3Date('22/01/2025')?.toString()).toBe('2025-01-22');
		expect(esse3Date('22/01/2025 23:59:59')?.toString()).toBe('2025-01-22');
		expect(esse3Date('2025-01-22T00:00:00')?.toString()).toBe('2025-01-22');
	});

	it('is null for blank or unreadable input', () => {
		expect(esse3Date('')).toBeNull();
		expect(esse3Date(null)).toBeNull();
		expect(esse3Date('soon')).toBeNull();
	});
});

describe('esse3DateTime', () => {
	it('reads seconds when present', () => {
		expect(esse3DateTime('03/07/2025 09:30:00')?.toString()).toBe('2025-07-03T09:30:00');
		expect(esse3DateTime('03/07/2025 09:30')?.toString()).toBe('2025-07-03T09:30:00');
		expect(esse3DateTime('03/07/2025')).toBeNull();
	});
});
