import { foldQuery } from '#lib/features/elearning/search/fold.ts';

/** A day named in a query, from today. */
export interface DateQuery {
	date: Date;
	/** 0 today, 1 tomorrow, 2 the day after, when the query said so in words. */
	offset: number | null;
}

const RELATIVE_DAYS: Readonly<Record<string, number>> = {
	oggi: 0,
	domani: 1,
	dopodomani: 2,
	today: 0,
	tomorrow: 1
};

/** Monday is 1, as `Date.getDay()` counts from Sunday 0. */
const WEEKDAYS: Readonly<Record<string, number>> = {
	lunedi: 1,
	martedi: 2,
	mercoledi: 3,
	giovedi: 4,
	venerdi: 5,
	sabato: 6,
	domenica: 0,
	monday: 1,
	tuesday: 2,
	wednesday: 3,
	thursday: 4,
	friday: 5,
	saturday: 6,
	sunday: 0
};

const NUMERIC = /^(\d{1,2})[/.](\d{1,2})(?:[/.](\d{2}|\d{4}))?$/;

function addDays(date: Date, days: number): Date {
	return new Date(date.getFullYear(), date.getMonth(), date.getDate() + days);
}

/**
 * Android's `parseDateQuery`: "domani", "lunedì" (the next one, today included) or "22/6" (a day
 * already past this year means next year's; two-digit years are 2000s). Null for anything else.
 */
export function parseDateQuery(query: string, today: Date): DateQuery | null {
	const text = foldQuery(query);
	const start = new Date(today.getFullYear(), today.getMonth(), today.getDate());
	const offset = RELATIVE_DAYS[text];
	if (offset !== undefined) return { date: addDays(start, offset), offset };
	const weekday = WEEKDAYS[text];
	if (weekday !== undefined) {
		return { date: addDays(start, (weekday - start.getDay() + 7) % 7), offset: null };
	}
	const numeric = NUMERIC.exec(text);
	if (numeric === null) return null;
	const day = Number(numeric[1]);
	const month = Number(numeric[2]);
	const written = numeric[3];
	const year =
		written === undefined
			? start.getFullYear()
			: written.length === 2
				? 2000 + Number(written)
				: Number(written);
	const date = new Date(year, month - 1, day);
	if (date.getMonth() !== month - 1 || date.getDate() !== day) return null;
	if (written === undefined && date < start) date.setFullYear(year + 1);
	return { date, offset: null };
}
