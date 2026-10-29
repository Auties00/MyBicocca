import { toIsoDate } from '#lib/format/days.ts';
import { describe, expect, it } from 'vitest';
import {
	byDay,
	dayTone,
	firstDay,
	monthsBetween,
	monthWeeks,
	monthWithDays,
	weekStep
} from './overview.ts';

describe('monthWeeks', () => {
	it('lays a month out in six weeks from Monday', () => {
		// November 2026 starts on a Sunday and has 30 days.
		const weeks = monthWeeks(new Date(2026, 10, 1));
		expect(weeks).toHaveLength(6);
		expect(weeks.every((week) => week.length === 7)).toBe(true);
		expect(weeks[0].slice(0, 6)).toEqual([null, null, null, null, null, null]);
		expect(weeks[0][6]).toEqual(new Date(2026, 10, 1));
		expect(weeks[5][0]).toEqual(new Date(2026, 10, 30));
		expect(weeks[5].slice(1)).toEqual([null, null, null, null, null, null]);
	});

	it('starts a month that opens on Monday in the first cell', () => {
		// February 2027 starts on a Monday and fills four weeks.
		const weeks = monthWeeks(new Date(2027, 1, 1));
		expect(weeks[0][0]).toEqual(new Date(2027, 1, 1));
		expect(weeks[3][6]).toEqual(new Date(2027, 1, 28));
		expect(weeks[4].every((day) => day === null)).toBe(true);
	});
});

describe('monthsBetween', () => {
	it('lists every month across a new year', () => {
		const months = monthsBetween(new Date(2026, 10, 20), new Date(2027, 1, 3));
		expect(months.map(toIsoDate)).toEqual(['2026-11-01', '2026-12-01', '2027-01-01', '2027-02-01']);
	});
});

describe('days of the overview', () => {
	const entries = [
		{ at: new Date(2026, 10, 7, 10), tone: 'upcoming' as const },
		{ at: new Date(2026, 10, 7, 14), tone: 'booked' as const },
		{ at: new Date(2026, 10, 18, 9), tone: 'closing' as const },
		{ at: new Date(2026, 11, 2, 9), tone: 'open' as const }
	];

	it('groups exams by day', () => {
		const days = byDay(entries);
		expect([...days.keys()]).toEqual(['2026-11-07', '2026-11-18', '2026-12-02']);
		expect(days.get('2026-11-07')).toHaveLength(2);
	});

	it('shows on each day what can still be acted on first', () => {
		expect(dayTone([{ tone: 'upcoming' }, { tone: 'booked' }])).toBe('booked');
		expect(dayTone([{ tone: 'open' }, { tone: 'closing' }])).toBe('closing');
		expect(dayTone([{ tone: 'closed' }, { tone: 'upcoming' }])).toBe('upcoming');
	});

	it('opens on the first day a call can be booked', () => {
		const days = byDay(entries);
		expect(firstDay(days)).toBe('2026-11-18');
		expect(firstDay(days, '2026-12')).toBe('2026-12-02');
		expect(firstDay(byDay(entries.slice(0, 2)))).toBe('2026-11-07');
		expect(firstDay(days, '2027-01')).toBeUndefined();
	});
});

describe('weekStep', () => {
	const keys = ['2026-10-30', '2026-11-07', '2026-11-08', '2026-11-11', '2026-11-14', '2026-11-29'];

	it('moves a week when that day has exams', () => {
		expect(weekStep(keys, '2026-11-07', 1)).toBe('2026-11-14');
		expect(weekStep(keys, '2026-11-14', -1)).toBe('2026-11-07');
	});

	it('takes the exam day nearest to a week away', () => {
		expect(weekStep(keys, '2026-11-08', 1)).toBe('2026-11-14');
		expect(weekStep(keys, '2026-11-14', 1)).toBe('2026-11-29');
		expect(weekStep(keys, '2026-11-11', -1)).toBe('2026-11-07');
	});

	it('crosses months, and prefers the nearer day on a tie', () => {
		expect(weekStep(keys, '2026-11-07', -1)).toBe('2026-10-30');
		expect(weekStep(['2026-11-03', '2026-11-05'], '2026-10-28', 1)).toBe('2026-11-03');
	});

	it('stops at the first and last exam days', () => {
		expect(weekStep(keys, '2026-11-29', 1)).toBeUndefined();
		expect(weekStep(keys, '2026-10-30', -1)).toBeUndefined();
	});
});

describe('monthWithDays', () => {
	const keys = ['2026-11-07', '2026-11-18', '2027-01-12'];

	it('skips the months without exams', () => {
		expect(monthWithDays(keys, '2026-11', 1)).toBe('2027-01');
		expect(monthWithDays(keys, '2027-01', -1)).toBe('2026-11');
		expect(monthWithDays(keys, '2026-12', -1)).toBe('2026-11');
	});

	it('finds nothing past the first and last of them', () => {
		expect(monthWithDays(keys, '2027-01', 1)).toBeUndefined();
		expect(monthWithDays(keys, '2026-11', -1)).toBeUndefined();
	});
});
