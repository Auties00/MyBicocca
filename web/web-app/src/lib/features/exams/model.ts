import type {
	Esse3ExamSessionEnrollment,
	Esse3ExamSessionTranscript
} from '@mybicocca/data-api/esse3';
import { esse3Date, esse3DateTime } from '#lib/data/esse3/dates.ts';
import { bookingGrade, type ExamGrade } from '#lib/data/esse3/grades.ts';
import { calendarDays, toDate } from '#lib/format/dates.ts';
import { italianTitleCase, personName, sentenceCase } from '#lib/format/names.ts';

/** What addresses an exam call in every `/appelli/{cdsId}/{adId}/{appId}` route. */
export interface CallKey {
	courseOfStudyId: number;
	activityId: number;
	callId: number;
}

export type ExamKind = 'written' | 'oral' | 'written_oral' | 'unknown';

export type CallType = 'final' | 'partial' | 'other';

/** One of the student's bookings (`prenotazioni`), upcoming or past. */
export interface Booking {
	/** The call's key as a URL segment. */
	slug: string;
	key: CallKey;
	applicationListId: number | null;
	/** The booking's `stuId`: cancelling and the receipt take it, never the `matId`. */
	studentId: number | null;
	/** `adsceId`: booking the same call again takes it. */
	activityChoiceId: number | null;
	title: string;
	callDescription: string | null;
	kind: ExamKind;
	callType: CallType;
	/** The student's shift. */
	at: Date | null;
	/** Whether `at` carries a real time rather than Esse3's midnight filler. */
	timed: boolean;
	room: string | null;
	building: string | null;
	credits: number | null;
	/** How the exam is held, as Esse3 words it ("In presenza"). */
	mode: string | null;
	position: number | null;
	bookedAt: Date | null;
	/** The end of the registration window, after which the booking can't be cancelled. */
	cancellableUntil: Date | null;
	note: string | null;
	grade: ExamGrade;
	published: boolean;
	publishedNote: string | null;
}

/** A call the student can book now or later (`appelli`). */
export interface ExamCall {
	slug: string;
	key: CallKey;
	activityChoiceId: number | null;
	/** The course code (`adCod`), never translated. */
	code: string | null;
	title: string;
	degree: string | null;
	description: string | null;
	date: Date | null;
	timed: boolean;
	opensAt: Date | null;
	closesAt: Date | null;
	enrolled: number | null;
	kind: ExamKind;
	callType: CallType;
	notes: string | null;
	president: string | null;
	bookingMode: string | null;
}

/** The calls of one course, as the booking page groups them. */
export interface CourseCalls {
	/** `adCod`, else the name, else `adId`: what `?course=` selects. */
	key: string;
	code: string | null;
	title: string;
	calls: ExamCall[];
}

/** Where a call's registration window stands today. */
export type WindowStatus = 'open' | 'upcoming' | 'closed';

export function callSlug(key: CallKey): string {
	return `${key.courseOfStudyId}-${key.activityId}-${key.callId}`;
}

function keyOf(row: {
	courseOfStudyId: number | null;
	activityId: number | null;
	callId: number | null;
}): CallKey | undefined {
	const { courseOfStudyId, activityId, callId } = row;
	if (courseOfStudyId === null || activityId === null || callId === null) return undefined;
	return { courseOfStudyId, activityId, callId };
}

const text = (value: string | null | undefined) => {
	const trimmed = value?.trim();
	return trimmed ? trimmed : null;
};

/** Esse3's exam mode (`tipoEsaCod`): S, O, or written and oral held jointly (SOC) or apart (SOS). */
export function examKind(code: string | null | undefined): ExamKind {
	switch (code?.trim().toUpperCase()) {
		case 'S':
			return 'written';
		case 'O':
			return 'oral';
		case 'SOC':
		case 'SOS':
			return 'written_oral';
		default:
			return 'unknown';
	}
}

/** `tipoAppCod`: the final exam (PF) or a partial one (PP). */
export function callType(code: string | null | undefined): CallType {
	switch (code?.trim().toUpperCase()) {
		case 'PF':
			return 'final';
		case 'PP':
			return 'partial';
		default:
			return 'other';
	}
}

function dateTime(value: string | null): { at: Date | null; timed: boolean } {
	const parsed = esse3DateTime(value);
	if (parsed) {
		return { at: toDate(parsed), timed: parsed.hour !== 0 || parsed.minute !== 0 };
	}
	const day = esse3Date(value);
	return { at: day ? toDate(day) : null, timed: false };
}

function day(value: string | null): Date | null {
	const parsed = esse3Date(value);
	return parsed ? toDate(parsed) : null;
}

/** A booking row, or undefined when its call key is incomplete and no route could address it. */
export function toBooking(row: Esse3ExamSessionEnrollment): Booking | undefined {
	const key = keyOf(row);
	if (!key) return undefined;
	const shift = dateTime(row.shiftDateTime);
	const name = text(row.studentActivityDescription) ?? text(row.examCallDescription);
	return {
		slug: callSlug(key),
		key,
		applicationListId: row.applicationListId,
		studentId: row.studentId,
		activityChoiceId: row.activityChoiceId,
		title: name ? italianTitleCase(name) : '',
		callDescription: text(row.examCallDescription),
		kind: examKind(row.graduationTypeCode),
		callType: callType(row.callTypeCode),
		at: shift.at,
		timed: shift.timed,
		room: text(row.classroomDescription),
		building: text(row.buildingDescription),
		credits: row.teachingActivityWeight,
		mode: text(row.examTypeDescription),
		position: row.applicationPosition,
		bookedAt: dateTime(row.insertionDate).at,
		cancellableUntil: day(row.enrollmentEndDate),
		note: text(row.studentNote),
		grade: bookingGrade(row),
		published: row.publicationId !== null,
		publishedNote: text(row.publicNote)
	};
}

/** `oraEsa` comes as a full date and time; only its time counts. */
function callDate(date: string | null, time: string | null): { at: Date | null; timed: boolean } {
	const base = day(date);
	const clock = /(\d{1,2}):(\d{2})/.exec(time?.trim().split(/\s+/).pop() ?? '');
	if (!base || !clock) return { at: base, timed: false };
	const hours = Number(clock[1]);
	const minutes = Number(clock[2]);
	if (hours === 0 && minutes === 0) return { at: base, timed: false };
	return {
		at: new Date(base.getFullYear(), base.getMonth(), base.getDate(), hours, minutes),
		timed: true
	};
}

function titleCased(value: string | null): string | null {
	const name = text(value);
	return name ? italianTitleCase(name) : null;
}

function president(row: Esse3ExamSessionTranscript): string | null {
	const name = [row.presidentName, row.presidentSurname].map(text).filter(Boolean).join(' ');
	return name ? personName(name) : null;
}

export function toExamCall(row: Esse3ExamSessionTranscript): ExamCall | undefined {
	const key = keyOf(row);
	if (!key) return undefined;
	const when = callDate(row.callStartDate, row.graduationTime);
	const name = text(row.activityDescription) ?? text(row.studentActivityDescription);
	return {
		slug: callSlug(key),
		key,
		activityChoiceId: row.activityChoiceId,
		code: text(row.activityCode),
		title: name ? italianTitleCase(name) : '',
		degree: titleCased(row.courseOfStudyDescription),
		description: text(row.callDescription),
		date: when.at,
		timed: when.timed,
		opensAt: day(row.enrollmentStartDate),
		closesAt: day(row.enrollmentEndDate),
		enrolled: row.enrolledNumber,
		kind: examKind(row.graduationTypeCode),
		callType: callType(row.callTypeCode),
		notes: text(row.notes),
		president: president(row),
		bookingMode: text(row.bookingManagementTypeDescription)
	};
}

/** Open from the first day to the last day of the window; a missing bound never closes it. */
export function windowStatus(
	call: Pick<ExamCall, 'opensAt' | 'closesAt'>,
	today: Date = new Date()
): WindowStatus {
	if (call.opensAt && calendarDays(today, call.opensAt) > 0) return 'upcoming';
	if (call.closesAt && calendarDays(call.closesAt, today) > 0) return 'closed';
	return 'open';
}

/** Days left to register, 0 on the last day; undefined unless the window is open and bounded. */
export function daysToClose(
	call: Pick<ExamCall, 'opensAt' | 'closesAt'>,
	today: Date = new Date()
): number | undefined {
	if (!call.closesAt || windowStatus(call, today) !== 'open') return undefined;
	return calendarDays(today, call.closesAt);
}

/** Esse3 books a call only with the record-book row and while registrations are open. */
export function canBook(call: ExamCall, today: Date = new Date()): boolean {
	return (call.activityChoiceId ?? 0) > 0 && windowStatus(call, today) === 'open';
}

const byDate = (a: Date | null, b: Date | null) => {
	if (a && b) return a.getTime() - b.getTime();
	return a ? -1 : b ? 1 : 0;
};

export function courseKey(call: Pick<ExamCall, 'code' | 'title' | 'key'>): string {
	return call.code ?? (call.title || String(call.key.activityId));
}

/** Courses by name, their calls by date with undated ones last. */
export function groupCalls(calls: readonly ExamCall[], locale = 'it'): CourseCalls[] {
	const groups = new Map<string, CourseCalls>();
	for (const call of calls) {
		const key = courseKey(call);
		let group = groups.get(key);
		if (!group) {
			group = { key, code: call.code, title: call.title, calls: [] };
			groups.set(key, group);
		}
		group.calls.push(call);
	}
	const collator = new Intl.Collator(locale, { sensitivity: 'base' });
	for (const group of groups.values()) group.calls.sort((a, b) => byDate(a.date, b.date));
	return [...groups.values()].sort((a, b) => collator.compare(a.title, b.title));
}

const folded = (text: string) =>
	text
		.normalize('NFD')
		.replace(/\p{Mn}/gu, '')
		.toLocaleLowerCase('it-IT');

/** Whether a course's name or code contains every word of `search`, accents and case aside. */
export function matchesCourse(group: Pick<CourseCalls, 'title' | 'code'>, search: string): boolean {
	const haystack = folded(`${group.title} ${group.code ?? ''}`);
	return folded(search)
		.split(/\s+/)
		.every((word) => haystack.includes(word));
}

/**
 * The calls of a course that share their day with another one, such as a partial and a full exam
 * at the same hour: only Esse3's description of the call tells them apart.
 */
export function sameDayCalls(calls: readonly ExamCall[]): Set<string> {
	const byDay = new Map<string, ExamCall[]>();
	for (const call of calls) {
		if (!call.date) continue;
		const day = call.date.toDateString();
		byDay.set(day, [...(byDay.get(day) ?? []), call]);
	}
	return new Set(
		[...byDay.values()]
			.filter((group) => group.length > 1)
			.flat()
			.map((call) => call.slug)
	);
}

/** Esse3's description of a call, unless it only repeats the course's name. */
export function ownDescription(description: string | null, courseTitle: string): string | null {
	return description && folded(description) !== folded(courseTitle) ? description : null;
}

/**
 * What tells a call apart within its course. Esse3 often names calls "<course>-<what>"
 * ("ANALISI E PROGETTO DI ALGORITMI-compitin", "ANALISI E PROG ALGO-VI appello completo"), the
 * course sometimes abbreviated: the part after it is the distinctive one.
 */
export function callNickname(description: string, courseTitle: string): string {
	const dash = description.indexOf('-');
	const tail = description.slice(dash + 1).trim();
	if (dash <= 0 || tail === '') return description;
	const course = new Set(
		folded(courseTitle)
			.split(/\s+/)
			.filter((word) => word.length > 2)
	);
	const head = folded(description.slice(0, dash)).split(/\s+/);
	return head.some((word) => course.has(word)) ? tail : description;
}

/**
 * What tells a call apart beyond its course, in sentence case: "II appello" for "PROBABILITÀ E
 * STATISTICA-II APPELLO", "Compitino" for "SISTEMI OPERATIVI-compitino"; null when Esse3's
 * description only repeats the course.
 */
export function callLabel(description: string | null, courseTitle: string): string | null {
	const own = text(description);
	if (!own) return null;
	const label = ownDescription(callNickname(own.replace(/\s+/g, ' '), courseTitle), courseTitle);
	return label && sentenceCase(label);
}

/** A row of a course on the booking page: a call to book, or one already booked. */
export type CourseRow =
	{ type: 'call'; id: string; call: ExamCall } | { type: 'booking'; id: string; booking: Booking };

/** A course's calls and the bookings of the same course, by date: the whole session at a glance. */
export function courseRows(group: CourseCalls, bookings: readonly Booking[]): CourseRow[] {
	const activities = new Set(group.calls.map((call) => call.key.activityId));
	const rows: CourseRow[] = [
		...group.calls.map((call): CourseRow => ({ type: 'call', id: `call:${call.slug}`, call })),
		...bookings
			.filter((booking) => activities.has(booking.key.activityId))
			.map((booking): CourseRow => ({ type: 'booking', id: `booking:${booking.slug}`, booking }))
	];
	const dateOf = (row: CourseRow) => (row.type === 'call' ? row.call.date : row.booking.at);
	return rows.sort((a, b) => byDate(dateOf(a), dateOf(b)));
}

/** Upcoming bookings (today or later, or undated) soonest first; past ones latest first. */
export function splitBookings(
	bookings: readonly Booking[],
	today: Date = new Date()
): { upcoming: Booking[]; past: Booking[] } {
	const upcoming: Booking[] = [];
	const past: Booking[] = [];
	for (const booking of bookings) {
		if (booking.at && calendarDays(today, booking.at) < 0) past.push(booking);
		else upcoming.push(booking);
	}
	upcoming.sort((a, b) => byDate(a.at, b.at));
	past.sort((a, b) => byDate(b.at, a.at));
	return { upcoming, past };
}

export function isUpcoming(booking: Pick<Booking, 'at'>, today: Date = new Date()): boolean {
	return !booking.at || calendarDays(today, booking.at) >= 0;
}

/** Esse3 takes cancellations while registrations are open, for the booking's `stuId`. */
export function canCancel(booking: Booking, today: Date = new Date()): boolean {
	if (booking.studentId === null || !isUpcoming(booking, today)) return false;
	return !booking.cancellableUntil || calendarDays(today, booking.cancellableUntil) >= 0;
}

/** Where an exam is held: the room, and the place around it. */
export interface ExamLocation {
	room: string | null;
	/** The building's address when the room is named after it, else the building's full name. */
	place: string | null;
}

/**
 * Room and building without repeating the building: Esse3 names rooms after their building
 * ("U4-08" in "Edificio U4 - Piazza della Scienza 4"), so that pair is "U4-08" at "Piazza della
 * Scienza 4"; a room of another building keeps the building's full name.
 */
export function examLocation(room: string | null, building: string | null): ExamLocation {
	if (!room || !building) return { room, place: building };
	const { code, address } = buildingParts(building);
	if (code && room.toUpperCase().startsWith(code.toUpperCase())) {
		return { room, place: address || null };
	}
	return { room, place: building };
}

/** Esse3's "Edificio U4 - Piazza della Scienza 4" as its code and its address. */
export function buildingParts(building: string): { code: string; address: string } {
	const name = building.replace(/^edificio\s+/i, '');
	const separator = name.indexOf(' -');
	return {
		code: (separator < 0 ? name : name.slice(0, separator)).trim(),
		address: separator < 0 ? '' : name.slice(separator + 2).trim()
	};
}
