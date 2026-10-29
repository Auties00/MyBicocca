import { formatTime } from '#lib/format/dates.ts';
import { addDays, monthOf, startOfWeek, toJsDate, type IsoDate } from '#lib/format/days.ts';
import { cached, intlLocale } from '#lib/format/locale.ts';
import { capitalize, italianTitleCase } from '#lib/format/names.ts';
import { m } from '#lib/paraglide/messages.js';
import { subjectBase } from './labels.ts';
import { isPointInTime, type CalendarEvent } from './model.ts';

function intl(options: Intl.DateTimeFormatOptions): Intl.DateTimeFormat {
	const tag = intlLocale();
	return cached('calendar', tag, options, () => new Intl.DateTimeFormat(tag, options));
}

/** "Gio", "Thu": the short weekday, capitalized, without the abbreviation dot. */
export function weekdayShort(date: IsoDate): string {
	return capitalize(intl({ weekday: 'short' }).format(toJsDate(date)).replace(/\.$/, ''));
}

/** "G", "T". */
export function weekdayNarrow(date: IsoDate): string {
	return intl({ weekday: 'narrow' }).format(toJsDate(date)).toLocaleUpperCase(intlLocale());
}

/** "Giugno": the month's own name, as a title. */
export function monthName(date: IsoDate): string {
	return capitalize(intl({ month: 'long' }).format(toJsDate(date)));
}

/** "Ottobre 2026". */
export function monthYear(date: IsoDate): string {
	return capitalize(intl({ month: 'long', year: 'numeric' }).format(toJsDate(date)));
}

/** "18 giugno". */
export function dayMonth(date: IsoDate): string {
	return intl({ day: 'numeric', month: 'long' }).format(toJsDate(date));
}

/** "Gio 18". */
export function weekdayDay(date: IsoDate): string {
	return `${weekdayShort(date)} ${toJsDate(date).getDate()}`;
}

/** "Martedì 27 ottobre 2026". */
export function fullDate(date: IsoDate): string {
	return capitalize(
		intl({ weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }).format(toJsDate(date))
	);
}

/** "Mar 3 nov". */
export function shortDate(date: IsoDate): string {
	return capitalize(
		intl({ weekday: 'short', day: 'numeric', month: 'short' }).format(toJsDate(date))
	);
}

/** "15–21": the day numbers of the week holding `date`. */
export function weekSpan(date: IsoDate): string {
	const monday = startOfWeek(date);
	return `${toJsDate(monday).getDate()}–${toJsDate(addDays(monday, 6)).getDate()}`;
}

/**
 * "26 ottobre – 1 novembre 2026", "15–21 giugno 2026", "29 dicembre 2026 – 4 gennaio 2027";
 * with `short` months, "26 ott – 1 nov 2026" for a toolbar. Built from parts: Intl's own range
 * format pads Italian day numbers ("01 novembre").
 */
export function weekTitle(date: IsoDate, month: 'long' | 'short' = 'long'): string {
	const monday = startOfWeek(date);
	const sunday = addDays(monday, 6);
	const year = (day: IsoDate) => day.slice(0, 4);
	const withYear = (day: IsoDate) =>
		intl({ day: 'numeric', month, year: 'numeric' }).format(toJsDate(day));
	if (year(monday) !== year(sunday)) return `${withYear(monday)} – ${withYear(sunday)}`;
	if (monthOf(monday) !== monthOf(sunday)) {
		const start = intl({ day: 'numeric', month }).format(toJsDate(monday));
		return `${start} – ${withYear(sunday)}`;
	}
	return `${toJsDate(monday).getDate()}–${dayMonthYear(sunday)}`;
}

/** "1 novembre 2026". */
function dayMonthYear(date: IsoDate): string {
	return intl({ day: 'numeric', month: 'long', year: 'numeric' }).format(toJsDate(date));
}

/** "09:30". */
export function clockTime(minutes: number): string {
	return formatTime(new Date(2000, 0, 1, Math.floor(minutes / 60), minutes % 60));
}

/** "09:30–11:30", or "23:59" for a point in time. */
export function timeRange(event: Pick<CalendarEvent, 'start' | 'end'>): string {
	if (event.start === event.end) return clockTime(event.start);
	return `${clockTime(event.start)}–${clockTime(event.end)}`;
}

/** "2 h", "1 h 30 min", "45 min". */
export function duration(minutes: number): string {
	const hours = Math.floor(minutes / 60);
	const rest = minutes % 60;
	if (hours === 0) return m.calendar_duration_minutes({ minutes: rest });
	if (rest === 0) return m.calendar_duration_hours({ hours });
	return m.calendar_duration_hours_minutes({ hours, minutes: rest });
}

/** The detail's time line: "Entro le 23:59", or "09:30–11:30 · 2 h". */
export function detailTime(event: CalendarEvent): string {
	if (isPointInTime(event)) return m.calendar_detail_by_time({ time: clockTime(event.start) });
	return m.calendar_detail_time_duration({
		range: timeRange(event),
		duration: duration(event.end - event.start)
	});
}

/** "Lezione", "Esame · Scritto": the kind, as a card says it. */
export function kindLabel(event: CalendarEvent): string {
	switch (event.source) {
		case 'lesson':
			return event.group
				? m.calendar_type_lesson_with_group({ group: event.group })
				: m.calendar_type_lesson();
		case 'exam':
			return event.examTypeLabel
				? m.calendar_type_exam_with_type({ type: event.examTypeLabel })
				: m.calendar_type_exam();
		case 'deadline':
			return m.calendar_type_deadline();
		case 'appointment':
			return m.calendar_type_appointment();
		case 'library':
			return m.calendar_type_library();
	}
}

/** The kind, as the detail's subtitle says it. */
export function detailKind(event: CalendarEvent): string {
	switch (event.source) {
		case 'lesson':
			return event.group
				? m.calendar_type_lesson_with_group({ group: event.group })
				: m.calendar_detail_type_lesson();
		case 'exam':
			return event.examTypeLabel
				? m.calendar_detail_type_exam_with_type({ type: event.examTypeLabel })
				: m.calendar_detail_type_exam();
		case 'deadline':
			return m.calendar_detail_type_deadline();
		case 'appointment':
			return event.serviceGroup
				? m.calendar_detail_type_appointment_with_group({ group: event.serviceGroup })
				: m.calendar_detail_type_appointment();
		case 'library':
			return m.calendar_detail_type_library();
	}
}

/** "Lezioni", "Esami": a source, as the legend names it. */
export function sourceName(source: CalendarEvent['source']): string {
	switch (source) {
		case 'lesson':
			return m.calendar_source_lesson();
		case 'exam':
			return m.calendar_source_exam();
		case 'deadline':
			return m.calendar_source_deadline();
		case 'appointment':
			return m.calendar_source_appointment();
		case 'library':
			return m.calendar_source_library();
	}
}

/**
 * The title as the calendar shows it: EasyStaff and Esse3 write course names in capitals, which
 * read as shouting next to each other, so those get Italian title case; others stay as written.
 * A lesson's turno or group goes to its kind line instead.
 */
export function displayTitle(event: Pick<CalendarEvent, 'title' | 'source'>): string {
	const title = event.source === 'lesson' ? subjectBase(event.title) : event.title.trim();
	return title === title.toLocaleUpperCase('it-IT') ? italianTitleCase(title) : title;
}

/** The detail's heading: the title (the kind when blank) over the kind, flagged when cancelled. */
export function detailHeading(event: CalendarEvent): {
	title: string;
	kind: string;
	cancelled: boolean;
} {
	const kind = detailKind(event);
	return { title: displayTitle(event) || kind, kind, cancelled: event.cancelled };
}

/** The detail's subtitle as one line, for headers that take plain text. */
export function detailSubtitle(event: CalendarEvent): string {
	const kind = detailKind(event);
	return event.cancelled ? `${m.calendar_detail_cancelled()} · ${kind}` : kind;
}

/** "3 edizioni disponibili su e-learning". */
export function editionsLine(count: number): string {
	return m.calendar_course_picker_subtitle({ count });
}

/** A person's name as EasyStaff writes it ("FERRI PAOLO"), in title case; others as written. */
export function personName(name: string): string {
	const trimmed = name.trim();
	return trimmed === trimmed.toLocaleUpperCase('it-IT') ? italianTitleCase(trimmed) : trimmed;
}
