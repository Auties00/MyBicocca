import { LocalDate, LocalDateTime } from '@mybicocca/data-api/runtime';
import { describe, expect, it } from 'vitest';
import { calendarDays, formatDate, formatRecent, formatRelative, toDate } from './dates.ts';

const now = new Date(2026, 9, 27, 15, 0);

describe('formatRelative', () => {
	it('says now within 45 seconds, then minutes', () => {
		expect(formatRelative(new Date(2026, 9, 27, 14, 59, 30), now, 'it')).toBe('ora');
		expect(formatRelative(new Date(2026, 9, 27, 14, 55), now, 'en')).toBe('5 minutes ago');
	});

	it('counts hours within the same day, and calendar days across midnight', () => {
		expect(formatRelative(new Date(2026, 9, 27, 9, 0), now, 'en')).toBe('6 hours ago');
		expect(formatRelative(new Date(2026, 9, 26, 23, 0), now, 'it')).toBe('ieri');
		expect(formatRelative(new Date(2026, 9, 29, 8, 0), now, 'en')).toBe('in 2 days');
	});

	it('moves to months and years for distant dates', () => {
		expect(formatRelative(new Date(2026, 6, 1), now, 'en')).toBe('3 months ago');
		expect(formatRelative(new Date(2024, 9, 1), now, 'en')).toBe('2 years ago');
	});
});

describe('calendarDays', () => {
	it('ignores the hours and daylight saving', () => {
		expect(calendarDays(new Date(2026, 9, 24, 23, 0), new Date(2026, 9, 26, 1, 0))).toBe(2);
		expect(calendarDays(new Date(2026, 9, 26), new Date(2026, 9, 24))).toBe(-2);
	});
});

describe('toDate', () => {
	it('reads SDK dates in local time', () => {
		expect(toDate(new LocalDate(2026, 10, 27)).getDate()).toBe(27);
		expect(toDate(new LocalDateTime(new LocalDate(2026, 1, 2), 9, 30)).getHours()).toBe(9);
		expect(formatDate(new LocalDate(2026, 10, 27), 'long', 'it')).toBe('27 ottobre 2026');
	});
});

describe('formatRecent', () => {
	it('is relative within a week, either way', () => {
		expect(formatRecent(new Date(2026, 9, 26, 18, 0), now, 'it')).toBe('ieri');
		expect(formatRecent(new Date(2026, 9, 21, 16, 0), now, 'en')).toBe('6 days ago');
		expect(formatRecent(new Date(2026, 10, 1, 9, 0), now, 'en')).toBe('in 5 days');
	});

	it('gives the day and month past a week, and the year for another year', () => {
		expect(formatRecent(new Date(2026, 9, 19, 10, 0), now, 'it')).toBe('19 ott');
		expect(formatRecent(new Date(2026, 0, 3, 10, 0), now, 'en')).toBe('3 Jan');
		expect(formatRecent(new Date(2025, 11, 30, 10, 0), now, 'it')).toBe('30 dic 2025');
	});
});
