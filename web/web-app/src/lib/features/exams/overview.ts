import { toIsoDate } from '#lib/format/days.ts';

/** Where an exam on the calendar stands: a call by its registrations, or a booking. */
export type DayTone = 'closing' | 'open' | 'booked' | 'upcoming' | 'closed';

/** What a day with several exams shows first: what the student can still act on. */
const PRIORITY: readonly DayTone[] = ['closing', 'open', 'booked', 'upcoming', 'closed'];

/** "2026-11", the prefix of its days' keys. */
export function monthKey(date: Date): string {
	return toIsoDate(date).slice(0, 7);
}

/** Entries by day, each day's in the order given. */
export function byDay<T extends { at: Date }>(entries: readonly T[]): Map<string, T[]> {
	const days = new Map<string, T[]>();
	for (const entry of entries) {
		const key = toIsoDate(entry.at);
		days.set(key, [...(days.get(key) ?? []), entry]);
	}
	return days;
}

export function dayTone(entries: readonly { tone: DayTone }[]): DayTone {
	return PRIORITY.find((tone) => entries.some((entry) => entry.tone === tone)) ?? 'closed';
}

/** The day to show first: the first on which a call can be booked, else the first with any exam. */
export function firstDay(
	days: ReadonlyMap<string, readonly { tone: DayTone }[]>,
	month?: string
): string | undefined {
	const keys = [...days.keys()].filter((key) => !month || key.startsWith(month)).sort();
	const bookable = keys.find((key) =>
		days.get(key)?.some((entry) => entry.tone === 'open' || entry.tone === 'closing')
	);
	return bookable ?? keys[0];
}

const DAY_MS = 86_400_000;

/** Days since the epoch, so that keys a week apart differ by seven whatever the clocks did. */
function dayNumber(key: string): number {
	const [year, month, date] = key.split('-').map(Number);
	return Date.UTC(year, month - 1, date) / DAY_MS;
}

/**
 * The day with exams a week before or after `from` (`direction` -1 or 1), else the one nearest to
 * it on that side, the nearer to `from` on a tie: up and down in a month's grid move by weeks over
 * the days that have exams, and fall back to the next of them when the week has none.
 */
export function weekStep(
	keys: readonly string[],
	from: string,
	direction: 1 | -1
): string | undefined {
	const start = dayNumber(from);
	const target = start + 7 * direction;
	let best: { key: string; gap: number; step: number } | undefined;
	for (const key of keys) {
		const day = dayNumber(key);
		const step = (day - start) * direction;
		if (step <= 0) continue;
		const gap = Math.abs(day - target);
		if (!best || gap < best.gap || (gap === best.gap && step < best.step)) {
			best = { key, gap, step };
		}
	}
	return best?.key;
}

/** The nearest month before or after `month` ("2026-11") with a day in `keys`, as its key. */
export function monthWithDays(
	keys: readonly string[],
	month: string,
	direction: 1 | -1
): string | undefined {
	const months = [...new Set(keys.map((key) => key.slice(0, 7)))].sort();
	return direction > 0
		? months.find((item) => item > month)
		: months.findLast((item) => item < month);
}

/** The first day of every month from `first`'s to `last`'s. */
export function monthsBetween(first: Date, last: Date): Date[] {
	const months: Date[] = [];
	const end = last.getFullYear() * 12 + last.getMonth();
	for (let index = first.getFullYear() * 12 + first.getMonth(); index <= end; index++) {
		months.push(new Date(Math.floor(index / 12), index % 12, 1));
	}
	return months;
}

/**
 * `month` as six weeks from Monday, the days of the months around it left empty: every month
 * takes the same room, so paging through them moves nothing below.
 */
export function monthWeeks(month: Date): (Date | null)[][] {
	const year = month.getFullYear();
	const index = month.getMonth();
	const offset = (new Date(year, index, 1).getDay() + 6) % 7;
	const length = new Date(year, index + 1, 0).getDate();
	return Array.from({ length: 6 }, (_, week) =>
		Array.from({ length: 7 }, (_, weekday) => {
			const day = week * 7 + weekday - offset + 1;
			return day >= 1 && day <= length ? new Date(year, index, day) : null;
		})
	);
}
