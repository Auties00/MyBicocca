import {
	calendarDays,
	formatDate,
	formatTime,
	formatTimeRange,
	formatWeekday
} from '#lib/format/dates.ts';
import { addDays, toJsDate, type IsoDate } from '#lib/format/days.ts';
import { capitalize } from '#lib/format/names.ts';
import { formatPercent } from '#lib/format/numbers.ts';
import { romeDate } from '#lib/format/rome.ts';
import { m } from '#lib/paraglide/messages.js';
import {
	durationParts,
	occupancyLevel,
	type LiveStatus,
	type OccupancyLevel,
	type Reservation
} from './model.ts';

/** Word joiners around the dash, so "14:30–16:30" never breaks across lines. */
const JOINER = String.fromCharCode(0x2060);
const timeRange = (start: Date, end: Date) =>
	formatTimeRange(start, end).replace(/[–-]/g, (dash) => JOINER + dash + JOINER);

/** "1 h", "1 h 30 min", "30 min". */
export function durationLabel(duration: number): string {
	const { hours, minutes } = durationParts(duration);
	if (hours === 0) return m.library_duration_minutes({ minutes });
	return minutes === 0
		? m.library_duration_hours({ hours })
		: m.library_duration_hours_minutes({ hours, minutes });
}

/** "Oggi", "Domani", else the short weekday ("Gio"); `date` is on the library's wall clock. */
export function relativeDay(date: Date, now: number): string {
	const distance = calendarDays(romeDate(now), date);
	if (distance === 0) return m.library_today();
	if (distance === 1) return m.library_tomorrow();
	return capitalize(formatWeekday(date, 'short').replace('.', ''));
}

/** "domani alle 08:50", inside a sentence; `date` is on the library's wall clock. */
export function dayAndTime(date: Date, now: number): string {
	const distance = calendarDays(romeDate(now), date);
	const day =
		distance === 0
			? m.library_today().toLocaleLowerCase()
			: distance === 1
				? m.library_tomorrow().toLocaleLowerCase()
				: formatWeekday(date, 'long');
	return m.library_day_at({ day, time: formatTime(date) });
}

/** "Oggi · 16:00–18:30". */
export function whenShort(reservation: Reservation, now: number): string {
	return `${relativeDay(reservation.start, now)} · ${timeRange(reservation.start, reservation.end)}`;
}

/**
 * "Martedì 28 ottobre · 16:00–18:30"; inside a sentence (`inline`) the weekday keeps the
 * language's own case: "martedì" in Italian, "Tuesday" in English.
 */
export function whenLong(start: Date, end: Date, inline = false): string {
	const day = formatDate(start, 'weekdayDayMonthLong');
	return `${inline ? day : capitalize(day)} · ${timeRange(start, end)}`;
}

/** The wall-clock `Date` of a site day at `minutes`. */
export function siteDate(day: IsoDate, minutes = 0): Date {
	return toJsDate(day, minutes);
}

/** A bookable day in the day strip: "Oggi", "Domani" or "Mer" over "28" and "ott". */
export function dayChip(day: IsoDate, today: IsoDate) {
	const date = siteDate(day);
	const weekday =
		day === today
			? m.library_today()
			: day === addDays(today, 1)
				? m.library_tomorrow()
				: capitalize(formatWeekday(date, 'short').replace('.', ''));
	return {
		weekday,
		date: String(date.getDate()),
		month: formatDate(date, 'month').replace('.', ''),
		label: formatDate(date, 'full')
	};
}

/** "Mar 28 ott · 16:00–18:00". */
export function slotRange(day: IsoDate, start: number, duration: number): string {
	const from = siteDate(day, start);
	const label = capitalize(formatDate(from, 'weekdayDayMonth').replace('.', ''));
	return `${label} · ${timeRange(from, siteDate(day, start + duration))}`;
}

export function occupancyText(percent: number): string {
	return formatPercent(percent / 100);
}

export const occupancyLevelLabel: Record<OccupancyLevel, () => string> = {
	low: m.library_crowded_low,
	mid: m.library_crowded_mid,
	high: m.library_crowded_high
};

export function crowdLabel(percent: number): string {
	return occupancyLevelLabel[occupancyLevel(percent)]();
}

/** "Chiude alle 22:00", "Apre domani alle 8:30", or the site's own sentence. */
export function statusChangeLabel(status: LiveStatus, today: IsoDate): string | null {
	if (status.closes)
		return m.library_closes_at({ time: formatTime(siteDate(today, status.closes.minutes)) });
	const opens = status.opens;
	if (opens) {
		const time = formatTime(siteDate(opens.day ?? today, opens.minutes));
		if (!opens.day || opens.day === today) return m.library_opens_at({ time });
		const day =
			opens.day === addDays(today, 1)
				? m.library_tomorrow().toLocaleLowerCase()
				: formatWeekday(siteDate(opens.day), 'long');
		return m.library_opens_on({ day, time });
	}
	return status.text;
}
