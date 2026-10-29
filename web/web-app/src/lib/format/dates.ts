import { LocalDate, LocalDateTime, LocalTime } from '@mybicocca/data-api/runtime';
import { cached, intlLocale, type AppLocale } from './locale.ts';

/** Anything the app shows as a date: SDK dates are local, epoch numbers are milliseconds. */
export type DateInput = Date | number | LocalDate | LocalDateTime;

export function toDate(value: DateInput): Date {
	if (value instanceof Date) return value;
	if (typeof value === 'number') return new Date(value);
	if (value instanceof LocalDateTime) {
		const { date } = value;
		return new Date(date.year, date.month - 1, date.day, value.hour, value.minute, value.second);
	}
	return new Date(value.year, value.month - 1, value.day);
}

function dateTimeFormat(locale: AppLocale | undefined, options: Intl.DateTimeFormatOptions) {
	const tag = intlLocale(locale);
	return cached('date', tag, options, () => new Intl.DateTimeFormat(tag, options));
}

const DATE_STYLES = {
	/** 27/10/26 */
	short: { dateStyle: 'short' },
	/** 27 ott 2026 */
	medium: { dateStyle: 'medium' },
	/** 27 ottobre 2026 */
	long: { dateStyle: 'long' },
	/** martedì 27 ottobre 2026 */
	full: { dateStyle: 'full' },
	/** 27 ott */
	dayMonth: { day: 'numeric', month: 'short' },
	/** mar 27 ott */
	weekdayDayMonth: { weekday: 'short', day: 'numeric', month: 'short' },
	/** martedì 27 ottobre */
	weekdayDayMonthLong: { weekday: 'long', day: 'numeric', month: 'long' },
	/** ott */
	month: { month: 'short' },
	/** ottobre 2026 */
	monthYear: { month: 'long', year: 'numeric' }
} satisfies Record<string, Intl.DateTimeFormatOptions>;

export type DateStyle = keyof typeof DATE_STYLES;

export function formatDate(
	value: DateInput,
	style: DateStyle = 'medium',
	locale?: AppLocale
): string {
	return dateTimeFormat(locale, DATE_STYLES[style]).format(toDate(value));
}

/** 14:30 */
export function formatTime(value: DateInput | LocalTime, locale?: AppLocale): string {
	const date =
		value instanceof LocalTime ? new Date(1970, 0, 1, value.hour, value.minute) : toDate(value);
	return dateTimeFormat(locale, { hour: '2-digit', minute: '2-digit' }).format(date);
}

/** 27 ott 2026, 14:30 */
export function formatDateTime(value: DateInput, locale?: AppLocale): string {
	return dateTimeFormat(locale, { dateStyle: 'medium', timeStyle: 'short' }).format(toDate(value));
}

/** 09:30–11:30, in the locale's range form. */
export function formatTimeRange(start: DateInput, end: DateInput, locale?: AppLocale): string {
	return dateTimeFormat(locale, { hour: '2-digit', minute: '2-digit' }).formatRange(
		toDate(start),
		toDate(end)
	);
}

/** lunedì */
export function formatWeekday(
	value: DateInput,
	width: 'long' | 'short' | 'narrow' = 'long',
	locale?: AppLocale
): string {
	return dateTimeFormat(locale, { weekday: width }).format(toDate(value));
}

const MINUTE = 60_000;
const HOUR = 60 * MINUTE;

/**
 * How far `value` is from `now`: "ora", "5 minuti fa", "tra 3 ore" within the same day, then by
 * calendar day ("ieri", "tra 2 giorni"), month and year.
 */
export function formatRelative(
	value: DateInput,
	now: Date = new Date(),
	locale?: AppLocale
): string {
	const tag = intlLocale(locale);
	const format = cached(
		'relative',
		tag,
		{},
		() => new Intl.RelativeTimeFormat(tag, { numeric: 'auto' })
	);
	const date = toDate(value);
	const diff = date.getTime() - now.getTime();
	const distance = Math.abs(diff);
	if (distance < 45_000) return format.format(0, 'second');
	if (distance < 45 * MINUTE) return format.format(Math.round(diff / MINUTE), 'minute');
	const days = calendarDays(now, date);
	if (days === 0) return format.format(Math.round(diff / HOUR), 'hour');
	if (Math.abs(days) < 26) return format.format(days, 'day');
	const months = (date.getFullYear() - now.getFullYear()) * 12 + date.getMonth() - now.getMonth();
	if (Math.abs(months) < 11) return format.format(months, 'month');
	return format.format(Math.round(months / 12), 'year');
}

/** "oggi", "domani", "tra 8 giorni", in lower case as in the middle of a sentence. */
export function formatRelativeDays(days: number, locale?: AppLocale): string {
	const tag = intlLocale(locale);
	return cached(
		'relative',
		tag,
		{},
		() => new Intl.RelativeTimeFormat(tag, { numeric: 'auto' })
	).format(days, 'day');
}

/** Calendar days from `from` to `to`, whatever the hours and daylight saving. */
export function calendarDays(from: Date, to: Date): number {
	const start = Date.UTC(from.getFullYear(), from.getMonth(), from.getDate());
	const end = Date.UTC(to.getFullYear(), to.getMonth(), to.getDate());
	return Math.round((end - start) / (24 * HOUR));
}

const WEEK = 7 * 24 * HOUR;

/**
 * When something recent happened: relative within a week ("ieri", "3 ore fa"), then the day and
 * month, with the year only when it isn't this one.
 */
export function formatRecent(value: DateInput, now: Date = new Date(), locale?: AppLocale): string {
	const date = toDate(value);
	if (Math.abs(now.getTime() - date.getTime()) < WEEK) return formatRelative(date, now, locale);
	const sameYear = date.getFullYear() === now.getFullYear();
	return formatDate(date, sameYear ? 'dayMonth' : 'medium', locale);
}
