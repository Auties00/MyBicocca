/**
 * Calendar days as ISO `yyyy-MM-dd` strings and months as `yyyy-MM`: plain, comparable, safe to
 * persist and to put in a URL. Arithmetic runs on UTC midnights, so daylight saving never shifts
 * a day.
 */
export type IsoDate = string;
export type YearMonth = string;

const DAY_MS = 86_400_000;
const ISO_DATE = /^(\d{4})-(\d{2})-(\d{2})$/;

const pad = (value: number) => String(value).padStart(2, '0');

function utc(date: IsoDate): number {
	const [year, month, day] = date.split('-').map(Number);
	return Date.UTC(year, month - 1, day);
}

function fromUtc(ms: number): IsoDate {
	const date = new Date(ms);
	return `${date.getUTCFullYear()}-${pad(date.getUTCMonth() + 1)}-${pad(date.getUTCDate())}`;
}

/** Whether `text` is a real calendar day written `yyyy-MM-dd`. */
export function isIsoDate(text: string | null | undefined): text is IsoDate {
	const match = text ? ISO_DATE.exec(text) : null;
	if (!match) return false;
	const [, year, month, day] = match.map(Number);
	return fromUtc(Date.UTC(year, month - 1, day)) === text;
}

export function addDays(date: IsoDate, count: number): IsoDate {
	return fromUtc(utc(date) + count * DAY_MS);
}

/** Days from `from` to `to`, negative when `to` comes first. */
function daysBetween(from: IsoDate, to: IsoDate): number {
	return Math.round((utc(to) - utc(from)) / DAY_MS);
}

/** 0 for Monday through 6 for Sunday. */
export function weekdayIndex(date: IsoDate): number {
	return (new Date(utc(date)).getUTCDay() + 6) % 7;
}

/** The Monday on or before `date`. */
export function startOfWeek(date: IsoDate): IsoDate {
	return addDays(date, -weekdayIndex(date));
}

/** Monday to Sunday of the week holding `date`. */
export function weekDays(date: IsoDate): IsoDate[] {
	const monday = startOfWeek(date);
	return Array.from({ length: 7 }, (_, index) => addDays(monday, index));
}

export function monthOf(date: IsoDate): YearMonth {
	return date.slice(0, 7);
}

export function addMonths(month: YearMonth, count: number): YearMonth {
	const [year, value] = month.split('-').map(Number);
	const index = year * 12 + value - 1 + count;
	return `${Math.floor(index / 12)}-${pad((index % 12) + 1)}`;
}

export function firstOfMonth(month: YearMonth): IsoDate {
	return `${month}-01`;
}

export function lastOfMonth(month: YearMonth): IsoDate {
	return addDays(firstOfMonth(addMonths(month, 1)), -1);
}

/** The same day of the month in `month`, clamped to its last day (31 Jan → 28 Feb). */
export function sameDayIn(date: IsoDate, month: YearMonth): IsoDate {
	const last = lastOfMonth(month);
	const day = Math.min(Number(date.slice(8)), Number(last.slice(8)));
	return `${month}-${pad(day)}`;
}

/** The Mondays of every week that overlaps `month`, spill-over weeks included. */
export function coveringMondays(month: YearMonth): IsoDate[] {
	const last = lastOfMonth(month);
	const mondays: IsoDate[] = [];
	for (let monday = startOfWeek(firstOfMonth(month)); monday <= last; monday = addDays(monday, 7)) {
		mondays.push(monday);
	}
	return mondays;
}

/**
 * The month grid, Monday first: blanks (null) before the 1st, then every day; it stops at the
 * last day, with no trailing blanks.
 */
export function monthGridCells(month: YearMonth): (IsoDate | null)[] {
	const first = firstOfMonth(month);
	const count = daysBetween(first, lastOfMonth(month)) + 1;
	return [
		...Array.from({ length: weekdayIndex(first) }, () => null),
		...Array.from({ length: count }, (_, index) => addDays(first, index))
	];
}

/**
 * The weeks of the month grid, Monday first, adjacent months' days included: every week touching
 * the month, or always six when `fixed`, so a picker keeps its height from month to month.
 */
export function monthWeeks(month: YearMonth, fixed = false): IsoDate[][] {
	const weeks = fixed ? 6 : coveringMondays(month).length;
	const monday = startOfWeek(firstOfMonth(month));
	return Array.from({ length: weeks }, (_, week) =>
		Array.from({ length: 7 }, (_, day) => addDays(monday, week * 7 + day))
	);
}

/** Where a date grid's arrow, Home/End and Page keys move the focused day; null for other keys. */
export function stepByKey(date: IsoDate, key: string): IsoDate | null {
	switch (key) {
		case 'ArrowLeft':
			return addDays(date, -1);
		case 'ArrowRight':
			return addDays(date, 1);
		case 'ArrowUp':
			return addDays(date, -7);
		case 'ArrowDown':
			return addDays(date, 7);
		case 'Home':
			return startOfWeek(date);
		case 'End':
			return addDays(startOfWeek(date), 6);
		case 'PageUp':
			return sameDayIn(date, addMonths(monthOf(date), -1));
		case 'PageDown':
			return sameDayIn(date, addMonths(monthOf(date), 1));
		default:
			return null;
	}
}

/** Local midnight of `date`, or `minutes` past it, for Intl formatting. */
export function toJsDate(date: IsoDate, minutes = 0): Date {
	const [year, month, day] = date.split('-').map(Number);
	return new Date(year, month - 1, day, Math.floor(minutes / 60), minutes % 60);
}

/** The day a local `Date` falls on: the inverse of {@link toJsDate}. */
export function toIsoDate(date: Date): IsoDate {
	return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}
