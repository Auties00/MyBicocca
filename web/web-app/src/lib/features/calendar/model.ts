import type { IsoDate } from '#lib/format/days.ts';

/** Where an event comes from; also the prefix of its id (`lesson_123`). */
export type EventSource = 'lesson' | 'exam' | 'deadline' | 'appointment' | 'library';

export const eventSources: readonly EventSource[] = [
	'lesson',
	'exam',
	'deadline',
	'appointment',
	'library'
];

export interface EventLocation {
	room: string | null;
	building: string | null;
	/** EasyStaff's embedded Google Maps address. */
	mapsUrl: string | null;
}

/**
 * One entry of the calendar. Plain data, so the query cache persists it as is. Times are minutes
 * from midnight on `date`, Europe/Rome wall time; `start === end` marks a point in time.
 */
interface EventBase {
	id: string;
	date: IsoDate;
	start: number;
	end: number;
	title: string;
	/** A 3–5 letter label for narrow cells ("ASD"). */
	shortLabel: string | null;
	location: EventLocation | null;
	cancelled: boolean;
	/** The Esse3 activity code, which links the event to its e-learning course editions. */
	activityCode: string | null;
}

export interface LessonEvent extends EventBase {
	source: 'lesson';
	subjectCode: string | null;
	/** The turno the lesson belongs to, when the course is taught in several. */
	turno: number | null;
	/** The turno, group or kind of class, when EasyStaff splits the course: "T1", "Lab · Gruppo A". */
	group: string | null;
	teachers: string[];
}

export interface ExamEvent extends EventBase {
	source: 'exam';
	/** Italian, as Esse3 names it: "Prova parziale · Scritto". */
	examTypeLabel: string | null;
	bookingPosition: number | null;
	bookedAt: IsoDate | null;
	cancellableUntil: IsoDate | null;
}

export interface DeadlineEvent extends EventBase {
	source: 'deadline';
	courseId: number;
	assignmentId: number;
}

export interface AppointmentEvent extends EventBase {
	source: 'appointment';
	serviceGroup: string | null;
}

export interface LibraryEvent extends EventBase {
	source: 'library';
	seatName: string | null;
}

export type CalendarEvent =
	LessonEvent | ExamEvent | DeadlineEvent | AppointmentEvent | LibraryEvent;

export function isPointInTime(event: CalendarEvent): boolean {
	return event.start === event.end;
}

export function durationMinutes(event: CalendarEvent): number {
	return Math.max(0, event.end - event.start);
}

/** "U24 · U24-C2": building and room, whichever exist. */
export function locationLine(event: CalendarEvent): string {
	const location = event.location;
	if (!location) return '';
	return [location.building, location.room].filter((part) => part && part.trim()).join(' · ');
}

/** The short label, or the first four letters of the title. */
export function compactLabel(event: CalendarEvent): string {
	return event.shortLabel?.trim() ? event.shortLabel : event.title.slice(0, 4).toUpperCase();
}

export function isInProgress(event: CalendarEvent, today: IsoDate, minute: number): boolean {
	return event.date === today && event.start <= minute && minute < event.end;
}

/** By day, then start, then end. */
export function compareEvents(a: CalendarEvent, b: CalendarEvent): number {
	if (a.date !== b.date) return a.date < b.date ? -1 : 1;
	return a.start - b.start || a.end - b.end;
}

/**
 * Every event of `lists` once, sorted: the first copy of an id wins. The month slices overlap
 * while the calendar moves to another month, so the same lesson can arrive twice.
 */
export function mergeEvents<T extends CalendarEvent>(...lists: readonly (readonly T[])[]): T[] {
	const byId = new Map<string, T>();
	for (const list of lists) {
		for (const event of list) if (!byId.has(event.id)) byId.set(event.id, event);
	}
	return [...byId.values()].sort(compareEvents);
}

export function groupByDay(events: Iterable<CalendarEvent>): Map<IsoDate, CalendarEvent[]> {
	const days = new Map<IsoDate, CalendarEvent[]>();
	for (const event of events) {
		const list = days.get(event.date);
		if (list) list.push(event);
		else days.set(event.date, [event]);
	}
	for (const list of days.values()) list.sort(compareEvents);
	return days;
}

/** The first event still to start after `minute` of `today`, cancelled lessons skipped. */
export function nextEvent(
	sorted: readonly CalendarEvent[],
	today: IsoDate,
	minute: number
): CalendarEvent | undefined {
	return sorted.find(
		(event) =>
			!event.cancelled && (event.date > today || (event.date === today && event.start > minute))
	);
}

/** The next event of each of `sources` still to start, in the order of `sources`. */
export function nextBySource(
	sorted: readonly CalendarEvent[],
	sources: readonly EventSource[],
	today: IsoDate,
	minute: number
): CalendarEvent[] {
	return sources.flatMap(
		(source) =>
			nextEvent(
				sorted.filter((event) => event.source === source),
				today,
				minute
			) ?? []
	);
}
