import { describe, expect, it } from 'vitest';
import {
	addDays,
	addMonths,
	coveringMondays,
	isIsoDate,
	lastOfMonth,
	monthGridCells,
	monthWeeks,
	sameDayIn,
	startOfWeek,
	stepByKey,
	toIsoDate,
	toJsDate,
	weekdayIndex
} from './days.ts';

describe('calendar days', () => {
	it('steps across months, years and daylight saving', () => {
		expect(addDays('2026-10-24', 2)).toBe('2026-10-26');
		expect(addDays('2026-12-31', 1)).toBe('2027-01-01');
		expect(addDays('2026-03-01', -1)).toBe('2026-02-28');
	});

	it('starts weeks on Monday', () => {
		expect(weekdayIndex('2026-10-26')).toBe(0);
		expect(weekdayIndex('2026-11-01')).toBe(6);
		expect(startOfWeek('2026-11-01')).toBe('2026-10-26');
	});

	it('steps months and clamps days to the shorter month', () => {
		expect(addMonths('2026-12', 1)).toBe('2027-01');
		expect(addMonths('2026-01', -1)).toBe('2025-12');
		expect(lastOfMonth('2028-02')).toBe('2028-02-29');
		expect(sameDayIn('2026-01-31', '2026-02')).toBe('2026-02-28');
	});

	it('lists the Mondays of every week touching a month', () => {
		expect(coveringMondays('2026-06')).toEqual([
			'2026-06-01',
			'2026-06-08',
			'2026-06-15',
			'2026-06-22',
			'2026-06-29'
		]);
		expect(coveringMondays('2026-11')[0]).toBe('2026-10-26');
	});

	it('lays out the month grid from Monday, with no trailing blanks', () => {
		const cells = monthGridCells('2026-10');
		expect(cells.slice(0, 4)).toEqual([null, null, null, '2026-10-01']);
		expect(cells.at(-1)).toBe('2026-10-31');
		expect(cells).toHaveLength(34);
	});

	it('accepts only real dates', () => {
		expect(isIsoDate('2026-02-28')).toBe(true);
		expect(isIsoDate('2026-02-30')).toBe(false);
		expect(isIsoDate('26-02-01')).toBe(false);
		expect(isIsoDate(null)).toBe(false);
	});

	it('lays the month out in whole weeks, six when fixed', () => {
		const october = monthWeeks('2026-10');
		expect(october).toHaveLength(5);
		expect(october[0][0]).toBe('2026-09-28');
		expect(october.at(-1)?.at(-1)).toBe('2026-11-01');
		expect(monthWeeks('2026-10', true)).toHaveLength(6);
		expect(monthWeeks('2027-02')).toHaveLength(4);
	});

	it('moves a grid focus by day, week and month', () => {
		expect(stepByKey('2026-10-27', 'ArrowUp')).toBe('2026-10-20');
		expect(stepByKey('2026-10-27', 'End')).toBe('2026-11-01');
		expect(stepByKey('2026-01-31', 'PageDown')).toBe('2026-02-28');
		expect(stepByKey('2026-10-27', 'Enter')).toBeNull();
	});

	it('converts to and from local dates', () => {
		const date = toJsDate('2026-03-29', 14 * 60 + 30);
		expect([date.getDate(), date.getHours(), date.getMinutes()]).toEqual([29, 14, 30]);
		expect(toIsoDate(date)).toBe('2026-03-29');
	});
});
