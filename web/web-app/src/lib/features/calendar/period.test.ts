import { describe, expect, it } from 'vitest';
import { periodDays, showsToday, stepPeriod } from './period.ts';

const today = '2026-10-27';

describe('stepPeriod', () => {
	it('pages days one at a time', () => {
		expect(stepPeriod('day', '2026-10-31', 1, today)).toBe('2026-11-01');
	});

	it('keeps the weekday across weeks, and lands on today in its week', () => {
		expect(stepPeriod('week', '2026-10-28', 1, today)).toBe('2026-11-04');
		expect(stepPeriod('week', '2026-11-05', -1, today)).toBe(today);
	});

	it('moves the selected day into the new month: its 1st, or today', () => {
		expect(stepPeriod('month', '2026-10-27', 1, today)).toBe('2026-11-01');
		expect(stepPeriod('month', '2026-12-15', -2, today)).toBe(today);
		expect(stepPeriod('month', '2026-01-31', -1, today)).toBe('2025-12-01');
	});
});

describe('showsToday', () => {
	it('knows when today is on screen', () => {
		expect(showsToday('day', today, today)).toBe(true);
		expect(showsToday('week', '2026-11-01', today)).toBe(true);
		expect(showsToday('week', '2026-11-02', today)).toBe(false);
		expect(showsToday('month', '2026-10-01', today)).toBe(true);
	});
});

describe('periodDays', () => {
	it('lists the days of each view', () => {
		expect(periodDays('day', today)).toEqual([today]);
		expect(periodDays('week', today)[0]).toBe('2026-10-26');
		expect(periodDays('month', '2026-02-10')).toHaveLength(28);
	});
});
