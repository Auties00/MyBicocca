import { addDays, type IsoDate } from './days.ts';

/** One event for a calendar app, written by {@link icsFile}. */
export interface CalendarEvent {
	/** Stable for the same event, so saving it again updates it in the calendar. */
	uid: string;
	title: string;
	/** An instant in epoch milliseconds, or a day when the time is unknown (an all-day event). */
	start: number | IsoDate;
	/**
	 * The end of a timed event, in epoch milliseconds. Without one the event has no length: Esse3
	 * never says how long an exam lasts, so the caller says so in the description rather than guess.
	 */
	end?: number;
	location?: string | null;
	description?: string | null;
	url?: string | null;
}

/** UTC, so the event keeps its instant in whatever zone the calendar opens it. */
const utcStamp = (ms: number) =>
	new Date(ms)
		.toISOString()
		.replace(/[-:]/g, '')
		.replace(/\.\d{3}/, '');

const compactDay = (date: IsoDate) => date.replaceAll('-', '');

/** RFC 5545 text: backslash, semicolon, comma and newlines escaped. */
function escape(value: string): string {
	return value.replace(/[\\;,]/g, (char) => `\\${char}`).replace(/\r?\n/g, '\\n');
}

/** RFC 5545 folds lines longer than 75 octets, continuing them after a space. */
function fold(line: string): string {
	const encoder = new TextEncoder();
	const parts: string[] = [];
	let current = '';
	for (const char of line) {
		const limit = parts.length === 0 ? 75 : 74;
		if (encoder.encode(current + char).length > limit) {
			parts.push(current);
			current = char;
		} else {
			current += char;
		}
	}
	parts.push(current);
	return parts.join('\r\n ');
}

function when(event: CalendarEvent): string[] {
	if (typeof event.start === 'string') {
		return [
			`DTSTART;VALUE=DATE:${compactDay(event.start)}`,
			`DTEND;VALUE=DATE:${compactDay(addDays(event.start, 1))}`
		];
	}
	const start = `DTSTART:${utcStamp(event.start)}`;
	return event.end === undefined
		? [start]
		: [start, `DTEND:${utcStamp(Math.max(event.end, event.start))}`];
}

/** An iCalendar file holding `event`, which calendar apps open as "add this event". */
export function icsFile(event: CalendarEvent, now = Date.now()): string {
	const lines = [
		'BEGIN:VCALENDAR',
		'VERSION:2.0',
		'PRODID:-//MyBicocca//MyBicocca//IT',
		'CALSCALE:GREGORIAN',
		'BEGIN:VEVENT',
		`UID:${event.uid}`,
		`DTSTAMP:${utcStamp(now)}`,
		...when(event),
		`SUMMARY:${escape(event.title)}`,
		...(event.location ? [`LOCATION:${escape(event.location)}`] : []),
		...(event.description ? [`DESCRIPTION:${escape(event.description)}`] : []),
		...(event.url ? [`URL:${event.url}`] : []),
		'END:VEVENT',
		'END:VCALENDAR'
	];
	return `${lines.map(fold).join('\r\n')}\r\n`;
}
