import { weekdayIndex, type IsoDate } from '#lib/format/days.ts';
import type { CalendarEvent } from './model.ts';

/** The share of a weekday's width a Saturday or Sunday with nothing on it takes. */
export const EMPTY_WEEKEND = 0.5;

/**
 * The widths of Saturday and Sunday, in weekday columns, over `days`: a full column where some day
 * of that weekday has an event, else half of one. A week passes its seven days, a month its weeks.
 */
export function weekendWeights(
	days: Iterable<IsoDate>,
	byDay: ReadonlyMap<IsoDate, readonly CalendarEvent[]>
): readonly [saturday: number, sunday: number] {
	const busy = [false, false];
	for (const day of days) {
		const index = weekdayIndex(day) - 5;
		if (index >= 0 && (byDay.get(day)?.length ?? 0) > 0) busy[index] = true;
	}
	return [busy[0] ? 1 : EMPTY_WEEKEND, busy[1] ? 1 : EMPTY_WEEKEND];
}
