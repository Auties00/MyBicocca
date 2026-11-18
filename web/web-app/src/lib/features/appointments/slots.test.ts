import {
	decodeEasyStaffPlanningArea,
	decodeEasyStaffPlanningDaySchedule,
	decodeEasyStaffPlanningMonthSchedule
} from '@mybicocca/data-api/easystaff';
import { describe, expect, it } from 'vitest';
import {
	availableDays,
	bookingMonths,
	daySlots,
	firstMonthWithDays,
	offeringsOf,
	splitByHalfDay
} from './slots.ts';

const decoded = <T>(value: T | undefined): T => {
	if (value === undefined) throw new Error('Undecodable fixture');
	return value;
};

describe('offeringsOf', () => {
	const area = (id: number, hidden: boolean, services: unknown[]) =>
		decoded(
			decodeEasyStaffPlanningArea({
				id,
				cliente: 2,
				area_code: `A${id}`,
				area_name: `Area ${id}`,
				address: '',
				public_hidden: hidden,
				servizi: services
			})
		);
	const offered = (id: number, durata: number, max_range?: number) => ({
		id,
		cliente: 2,
		type: 'Segreteria',
		durata,
		max_range,
		raggruppamento: 'Carriere Studenti'
	});

	it('takes the service as each visible area offers it, with that area’s duration', () => {
		const offerings = offeringsOf(
			[
				area(1, true, [offered(7, 600)]),
				area(2, false, [offered(8, 900)]),
				area(3, false, [offered(7, 1800, 45)])
			],
			7
		);
		expect(offerings).toEqual([
			{
				serviceName: 'Segreteria',
				serviceGroup: 'Carriere Studenti',
				areaId: 3,
				areaName: 'Area 3',
				areaAddress: null,
				durationSeconds: 1800,
				virtual: false,
				description: null,
				horizonDays: 45
			}
		]);
	});

	it('books 60 days ahead when the portal sets no range', () => {
		expect(offeringsOf([area(2, false, [offered(7, 600)])], 7)[0].horizonDays).toBe(60);
	});
});

describe('availableDays', () => {
	it('keeps the days with an open window, in order', () => {
		const schedule = decoded(
			decodeEasyStaffPlanningMonthSchedule({
				prima_disp: '2026-11-03 09:00',
				schedule: {
					'2026-11-05': ['09:00-12:00'],
					'2026-11-04': [],
					'2026-11-03': ['09:00-12:00', '14:00-15:30']
				}
			})
		);
		expect(availableDays(schedule)).toEqual(['2026-11-03', '2026-11-05']);
	});
});

describe('daySlots', () => {
	it('reads the day’s slots by start, taken ones marked', () => {
		const schedule = decoded(
			decodeEasyStaffPlanningDaySchedule({
				schedule: {
					'2026-11-03': {
						'14:00-14:20': { disponibili: 1, su: 1 },
						'09:20-09:40': { disponibili: 0, su: 1 },
						'09:00-09:20': { disponibili: 2, su: 2 }
					}
				}
			})
		);
		const slots = daySlots(schedule);
		expect(slots).toEqual([
			{ start: 540, end: 560, available: true },
			{ start: 560, end: 580, available: false },
			{ start: 840, end: 860, available: true }
		]);
		expect(splitByHalfDay(slots).afternoon).toEqual([{ start: 840, end: 860, available: true }]);
	});

	it('is empty for a day the portal lists nothing for', () => {
		expect(daySlots(decoded(decodeEasyStaffPlanningDaySchedule({ schedule: {} })))).toEqual([]);
	});
});

describe('firstMonthWithDays', () => {
	const months: Record<string, string[]> = {
		'2026-10': [],
		'2026-11': [],
		'2026-12': ['2026-12-02']
	};

	it('looks one month after the other until one has a free day', async () => {
		const asked: string[] = [];
		const found = await firstMonthWithDays('2026-10', '2027-01', async (month) => {
			asked.push(month);
			return months[month] ?? [];
		});
		expect(found).toEqual({ month: '2026-12', days: ['2026-12-02'] });
		expect(asked).toEqual(['2026-10', '2026-11', '2026-12']);
	});

	it('stops at the horizon with no days when nothing is free', async () => {
		const found = await firstMonthWithDays('2026-10', '2026-11', async (month) => months[month]);
		expect(found).toEqual({ month: '2026-11', days: [] });
	});

	it('stops at the first failure', async () => {
		await expect(
			firstMonthWithDays('2026-10', '2026-12', async () => {
				throw new Error('offline');
			})
		).rejects.toThrow('offline');
	});
});

describe('bookingMonths', () => {
	it('spans from this month to the horizon’s, on the Rome calendar', () => {
		// 31 October 23:30 UTC is already 1 November in Rome.
		expect(bookingMonths(Date.UTC(2026, 9, 31, 23, 30), 30)).toEqual({
			first: '2026-11',
			last: '2026-12'
		});
	});
});
