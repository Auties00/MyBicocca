import { formatDate, formatTimeRange, formatWeekday } from '#lib/format/dates.ts';
import { addDays, toJsDate, type IsoDate } from '#lib/format/days.ts';
import { cached, intlLocale } from '#lib/format/locale.ts';
import { romeDate, romeDay } from '#lib/format/rome.ts';
import { m } from '#lib/paraglide/messages.js';
import type { SectionId } from './directory.ts';
import type { Reservation } from './reservation.ts';
import type { Slot } from './slots.ts';

const capitalized = (text: string) => text.charAt(0).toLocaleUpperCase() + text.slice(1);

/** "Oggi", "Domani", else the weekday: "Giovedì". */
export function dayLabel(date: IsoDate, now: number): string {
	const today = romeDay(now);
	if (date === today) return m.appointments_today();
	if (date === addDays(today, 1)) return m.appointments_tomorrow();
	return capitalized(formatWeekday(toJsDate(date), 'long'));
}

export function timeRange(start: number, end: number): string {
	return formatTimeRange(romeDate(start), romeDate(Math.max(start, end)));
}

/** "Domani · 14:00–14:20", for cards. */
export function shortWhen(reservation: Pick<Reservation, 'start' | 'end'>, now: number): string {
	return m.appointments_day_time({
		day: dayLabel(romeDay(reservation.start), now),
		time: timeRange(reservation.start, reservation.end)
	});
}

/** "giovedì 28 ottobre 2026, 14:00–14:20". */
export function longWhen(reservation: Pick<Reservation, 'start' | 'end'>): string {
	return m.appointments_when({
		date: formatDate(romeDate(reservation.start), 'full'),
		time: timeRange(reservation.start, reservation.end)
	});
}

function formatDay(date: IsoDate, options: Intl.DateTimeFormatOptions): string {
	const tag = intlLocale();
	const format = cached('date', tag, options, () => new Intl.DateTimeFormat(tag, options));
	return capitalized(format.format(toJsDate(date)));
}

const LONG_DAY = { weekday: 'long', day: 'numeric', month: 'long' } as const;
const SHORT_DAY = { weekday: 'short', day: 'numeric', month: 'short' } as const;

/** A day of the picker, "Giovedì 28 ottobre". */
export function pickerDay(date: IsoDate): string {
	return formatDay(date, LONG_DAY);
}

/** The same day where room is short, "Gio 28 ott". */
export function shortPickerDay(date: IsoDate): string {
	return formatDay(date, SHORT_DAY);
}

/** A slot of `date`, "14:00–14:20". */
export function slotRange(date: IsoDate, slot: Pick<Slot, 'start' | 'end'>): string {
	return formatTimeRange(toJsDate(date, slot.start), toJsDate(date, slot.end));
}

export const sectionLabels: Record<SectionId, () => string> = {
	career: m.appointments_section_career,
	teaching: m.appointments_section_teaching,
	deliveries: m.appointments_section_deliveries,
	other: m.appointments_section_other
};

export const sectionCaptions: Record<SectionId, () => string> = {
	career: m.appointments_section_career_caption,
	teaching: m.appointments_section_teaching_caption,
	deliveries: m.appointments_section_deliveries_caption,
	other: m.appointments_section_other_caption
};

/** Where to go: the area and its address, or nothing when the desk hasn't said. */
export function placeLabel(
	reservation: Pick<Reservation, 'areaName' | 'areaAddress'>
): string | null {
	const parts = [reservation.areaName, reservation.areaAddress].filter(
		(part): part is string => !!part?.trim()
	);
	return parts.length > 0 ? parts.join(' · ') : null;
}
