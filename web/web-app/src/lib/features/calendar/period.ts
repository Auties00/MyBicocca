import {
	addDays,
	addMonths,
	firstOfMonth,
	lastOfMonth,
	monthOf,
	weekDays,
	type IsoDate
} from '#lib/format/days.ts';

export type CalendarView = 'day' | 'week' | 'month';

/** The days a view shows around `date`: the day, its Monday-to-Sunday week, or its month. */
export function periodDays(view: CalendarView, date: IsoDate): IsoDate[] {
	if (view === 'day') return [date];
	if (view === 'week') return weekDays(date);
	const first = firstOfMonth(monthOf(date));
	const count = Number(lastOfMonth(monthOf(date)).slice(8));
	return Array.from({ length: count }, (_, index) => addDays(first, index));
}

/**
 * The day selected after paging `delta` periods: the next day, the same weekday of the next
 * week, or in the next month its 1st. Landing on the period holding today selects today, so the
 * agenda and the day strip open on it.
 */
export function stepPeriod(
	view: CalendarView,
	date: IsoDate,
	delta: number,
	today: IsoDate
): IsoDate {
	if (view === 'day') return addDays(date, delta);
	if (view === 'week') {
		const next = addDays(date, delta * 7);
		return weekDays(next).includes(today) ? today : next;
	}
	const month = addMonths(monthOf(date), delta);
	return monthOf(today) === month ? today : firstOfMonth(month);
}

/** Whether the period on screen holds today: the Today button is idle then. */
export function showsToday(view: CalendarView, date: IsoDate, today: IsoDate): boolean {
	if (view === 'day') return date === today;
	if (view === 'week') return weekDays(date).includes(today);
	return monthOf(date) === monthOf(today);
}
