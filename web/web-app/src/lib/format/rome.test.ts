import { LocalDate, LocalDateTime } from '@mybicocca/data-api/runtime';
import { describe, expect, it } from 'vitest';
import { romeClock, romeDate, romeDay, romeHour, romeInstant, romeWallClock } from './rome.ts';

const wall = (year: number, month: number, day: number, hour: number, minute = 0) =>
	new LocalDateTime(new LocalDate(year, month, day), hour, minute);

describe('romeWallClock', () => {
	it('reads the wall clock in Rome, whatever the device zone', () => {
		expect(romeWallClock(Date.UTC(2026, 9, 27, 9, 30))).toEqual({
			date: '2026-10-27',
			minutes: 10 * 60 + 30
		});
		expect(romeWallClock(Date.UTC(2026, 6, 1, 22, 15))).toEqual({
			date: '2026-07-02',
			minutes: 15
		});
	});
});

describe('romeInstant', () => {
	it('reads a wall-clock time as Rome, in winter and in summer', () => {
		expect(romeInstant(wall(2026, 10, 27, 8, 30))).toBe(Date.UTC(2026, 9, 27, 7, 30));
		expect(romeInstant(wall(2026, 6, 15, 8, 30))).toBe(Date.UTC(2026, 5, 15, 6, 30));
		expect(romeInstant({ date: '2026-07-15', minutes: 9 * 60 })).toBe(Date.UTC(2026, 6, 15, 7));
		expect(romeInstant({ date: '2026-11-03', minutes: 14 * 60 + 20 })).toBe(
			Date.UTC(2026, 10, 3, 13, 20)
		);
	});

	it('holds on the days the clocks change', () => {
		expect(romeInstant(wall(2026, 3, 29, 1, 30))).toBe(Date.UTC(2026, 2, 29, 0, 30));
		expect(romeInstant(wall(2026, 3, 29, 3, 30))).toBe(Date.UTC(2026, 2, 29, 1, 30));
		expect(romeInstant(wall(2026, 10, 25, 1, 30))).toBe(Date.UTC(2026, 9, 24, 23, 30));
		expect(romeInstant(wall(2026, 10, 25, 3, 30))).toBe(Date.UTC(2026, 9, 25, 2, 30));
		expect(romeInstant({ date: '2026-03-29', minutes: 9 * 60 })).toBe(Date.UTC(2026, 2, 29, 7));
		expect(romeInstant({ date: '2026-10-25', minutes: 9 * 60 })).toBe(Date.UTC(2026, 9, 25, 8));
	});

	it('round-trips through each form of the Rome clock', () => {
		const instant = Date.UTC(2026, 9, 27, 13, 45);
		expect(romeClock(instant).toString()).toBe('2026-10-27T14:45:00');
		expect(romeInstant(romeClock(instant))).toBe(instant);
		for (const at of [
			'2026-10-28T15:00:00Z',
			'2026-06-18T14:00:00Z',
			'2026-03-29T01:30:00Z',
			'2026-10-25T09:00:00Z'
		]) {
			const epoch = Date.parse(at);
			expect(romeInstant(romeDate(epoch))).toBe(epoch);
			expect(romeInstant(romeWallClock(epoch))).toBe(epoch);
		}
	});
});

describe('romeHour', () => {
	it('is the hour in Rome on the day it is there, midnight closing the day', () => {
		const lateEvening = Date.UTC(2026, 9, 26, 23, 30);
		expect(romeHour(lateEvening, 8)).toBe(Date.UTC(2026, 9, 27, 7));
		expect(romeHour(lateEvening, 24)).toBe(Date.UTC(2026, 9, 27, 23));
	});
});

describe('romeDay', () => {
	it('is the date in Rome, not on the device', () => {
		expect(romeDay(Date.UTC(2026, 9, 26, 23, 30))).toBe('2026-10-27');
		expect(romeDay(Date.UTC(2026, 9, 27, 12, 0))).toBe('2026-10-27');
	});
});
