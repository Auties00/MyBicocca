import { describe, expect, it } from 'vitest';
import { monthWeeks, weekDays, type IsoDate } from '#lib/format/days.ts';
import type { CalendarEvent } from './model.ts';
import { EMPTY_WEEKEND, weekendWeights } from './weekColumns.ts';

const event = {} as CalendarEvent;
const on = (...days: IsoDate[]) => new Map(days.map((day) => [day, [event]] as const));

describe('weekendWeights', () => {
	const week = weekDays('2026-11-18');

	it('narrows a Saturday and a Sunday with nothing on them', () => {
		expect(weekendWeights(week, new Map())).toEqual([EMPTY_WEEKEND, EMPTY_WEEKEND]);
	});

	it('gives a full column to the weekend day that has an event', () => {
		expect(weekendWeights(week, on('2026-11-21'))).toEqual([1, EMPTY_WEEKEND]);
		expect(weekendWeights(week, on('2026-11-22'))).toEqual([EMPTY_WEEKEND, 1]);
	});

	it('ignores events on weekdays', () => {
		expect(weekendWeights(week, on('2026-11-18'))).toEqual([EMPTY_WEEKEND, EMPTY_WEEKEND]);
	});

	it('over a month, one event on any Saturday widens every Saturday', () => {
		const weeks = monthWeeks('2026-11').flat();
		expect(weekendWeights(weeks, on('2026-11-28'))).toEqual([1, EMPTY_WEEKEND]);
	});
});
