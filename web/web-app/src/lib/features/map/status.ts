import type { EasyStaffRoomOccupationEvent } from '@mybicocca/data-api/easystaff';
import { personName } from '#lib/format/names.ts';
import { romeHour, romeInstant } from '#lib/format/rome.ts';
import { repairMojibake } from './text.ts';

/** One booking of a room today. Times are epoch milliseconds, read as Rome's wall clock. */
export interface ScheduleEntry {
	id: string;
	title: string;
	/** EasyStaff's kind of event, in Italian ("Lezione"); null when blank. */
	kind: string | null;
	teachers: string[];
	start: number;
	end: number;
	/** The room is closed for the span, rather than in use. */
	closure: boolean;
}

/** The kind EasyStaff gives a room's closing (a holiday, works). */
const CLOSURE = 'chiusura_type';
const CANCELLED = '1';

/**
 * A building's occupation by room code, each room's bookings by start. Cancelled bookings are
 * left out; closures are kept and marked.
 */
export function scheduleByRoom(
	events: readonly EasyStaffRoomOccupationEvent[]
): Map<string, ScheduleEntry[]> {
	const rooms = new Map<string, ScheduleEntry[]>();
	for (const event of events) {
		if (event.status === CANCELLED || event.roomCode === '') continue;
		const kind = repairMojibake(event.eventType.trim());
		const entry: ScheduleEntry = {
			id: event.id,
			title: event.title.trim(),
			kind: kind === '' || kind === CLOSURE ? null : kind,
			teachers: event.teachers
				.map((teacher) => personName(`${teacher.name} ${teacher.surname}`.replace(/\s+/g, ' ')))
				.filter((name) => name !== ''),
			start: romeInstant(event.startDateTime),
			end: romeInstant(event.endDateTime),
			closure: kind === CLOSURE
		};
		const list = rooms.get(event.roomCode);
		if (list) list.push(entry);
		else rooms.set(event.roomCode, [entry]);
	}
	for (const list of rooms.values()) list.sort((a, b) => a.start - b.start || a.end - b.end);
	return rooms;
}

export type RoomStatus =
	/** `until` is when the next booking starts; null when nothing else is booked today. */
	| { kind: 'free'; until: number | null }
	/** `until` is when the bookings running into one another end. */
	| { kind: 'busy'; until: number; entry: ScheduleEntry }
	/** `until` is when the closures running into one another end; null when that is the day's end. */
	| { kind: 'closed'; until: number | null }
	/** The occupation couldn't be read. */
	| { kind: 'unknown' };

/** The end of the entries matching `chained` that run on, back to back, from `end`. */
function stretch(
	entries: readonly ScheduleEntry[],
	end: number,
	chained: (entry: ScheduleEntry) => boolean
): number {
	for (;;) {
		const next = entries.find((entry) => chained(entry) && entry.start <= end && entry.end > end);
		if (!next) return end;
		end = next.end;
	}
}

/**
 * Whether a room is in use at `now`. Back-to-back bookings count as one stretch, so a room
 * booked 10:30–12:30 and 12:30–14:30 is busy until 14:30; closures chain the same way.
 */
export function roomStatus(entries: readonly ScheduleEntry[] | undefined, now: number): RoomStatus {
	if (entries === undefined) return { kind: 'unknown' };
	const current = entries.find((entry) => entry.start <= now && now < entry.end);
	if (current?.closure) {
		const until = stretch(entries, current.end, (entry) => entry.closure);
		/* A closure written up to 23:59 still closes the room for the day. */
		return { kind: 'closed', until: until >= romeHour(now, 24) - 60_000 ? null : until };
	}
	if (!current) {
		const next = entries.find((entry) => entry.start > now);
		return { kind: 'free', until: next ? next.start : null };
	}
	return { kind: 'busy', until: stretch(entries, current.end, () => true), entry: current };
}
