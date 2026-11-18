import { describe, expect, it } from 'vitest';
import { calendarWeeks } from './calendar.ts';
import type { Deadline } from './deadlines.ts';

const deadline = (today: Date, days: number): Deadline => ({
	source: 'fee',
	id: `fee:${days}`,
	date: new Date(today.getFullYear(), today.getMonth(), today.getDate() + days),
	days,
	urgent: days <= 7,
	title: null,
	invoiceId: days,
	amount: 0,
	expired: false,
	installment: null,
	academicYear: null
});

describe('calendarWeeks', () => {
	it('pads the window from a week back to a month ahead into Monday-to-Sunday weeks', () => {
		const today = new Date(2026, 9, 27);
		const weeks = calendarWeeks([], today);
		const days = weeks.flat();
		expect(weeks.every((week) => week.length === 7)).toBe(true);
		expect(days[0].iso).toBe('2026-10-19');
		expect(days.at(-1)?.iso).toBe('2026-11-29');
		expect(days.filter((day) => day.inWindow).map((day) => day.iso)).toHaveLength(38);
		expect(days.find((day) => day.isToday)?.iso).toBe('2026-10-27');
		expect(days.find((day) => day.iso === '2026-10-19')?.inWindow).toBe(false);
		expect(days.find((day) => day.iso === '2026-10-20')?.inWindow).toBe(true);
	});

	it('needs no padding when the window starts on a Monday', () => {
		const days = calendarWeeks([], new Date(2026, 10, 9)).flat();
		expect(days[0]).toMatchObject({ iso: '2026-11-02', inWindow: true });
	});

	it('marks each day with its deadlines, overdue, urgent or later', () => {
		const today = new Date(2026, 9, 27);
		const items = [
			deadline(today, -2),
			deadline(today, 3),
			deadline(today, 3),
			deadline(today, 12)
		];
		const tones = Object.fromEntries(
			calendarWeeks(items, today)
				.flat()
				.filter((day) => day.tones.length > 0)
				.map((day) => [day.iso, day.tones])
		);
		expect(tones).toEqual({
			'2026-10-25': ['overdue'],
			'2026-10-30': ['urgent', 'urgent'],
			'2026-11-08': ['calm']
		});
	});

	it('tints each day by its strongest deadline', () => {
		const today = new Date(2026, 9, 27);
		const late = { ...deadline(today, 3), id: 'late', days: -1 };
		const days = calendarWeeks([deadline(today, 12), late, deadline(today, 3)], today).flat();
		const tone = (iso: string) => days.find((day) => day.iso === iso)?.tone;
		expect(tone('2026-10-30')).toBe('overdue');
		expect(tone('2026-11-08')).toBe('calm');
		expect(tone('2026-10-28')).toBeNull();
	});
});
