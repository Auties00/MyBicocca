import {
	AffluencesReservationState,
	type AffluencesMyReservation
} from '@mybicocca/data-api/affluences';
import {
	EasyStaffScheduleCellType,
	type EasyStaffScheduleCell
} from '@mybicocca/data-api/easystaff';
import type { Esse3ExamSessionEnrollment } from '@mybicocca/data-api/esse3';
import { esse3Date, esse3DateTime } from '#lib/data/esse3/dates.ts';
import type { Reservation } from '#lib/features/appointments/reservation.ts';
import type { CourseDeadline } from '#lib/features/elearning/deadlines.ts';
import { romeWallClock } from '#lib/format/rome.ts';
import {
	normalizeSubjectName,
	sectionTurno,
	shortLabelFor,
	subjectTurno,
	variantLabel
} from './labels.ts';
import type {
	AppointmentEvent,
	DeadlineEvent,
	EventLocation,
	ExamEvent,
	LessonEvent,
	LibraryEvent
} from './model.ts';

function location(
	room: string | null,
	building: string | null,
	mapsUrl: string | null = null
): EventLocation | null {
	const clean = (value: string | null) => (value?.trim() ? value.trim() : null);
	if (!clean(room) && !clean(building) && !clean(mapsUrl)) return null;
	return { room: clean(room), building: clean(building), mapsUrl: clean(mapsUrl) };
}

const minutesOf = (time: { hour: number; minute: number }) => time.hour * 60 + time.minute;

/**
 * The career's activity a lesson belongs to: the Esse3 code EasyStaff gives the lesson when the
 * career has it, else the activity its EasyStaff subject was joined to by name (a course shared
 * between degrees may come with another degree's code), else EasyStaff's code as it is.
 *
 * @param joined EasyStaff subject code → the career's activity code.
 * @param careerCodes The activity codes of the career's plan.
 */
export function lessonActivity(
	cell: Pick<EasyStaffScheduleCell, 'generalCode' | 'combinedCode' | 'subjectCode'>,
	joined: ReadonlyMap<string, string>,
	careerCodes: ReadonlySet<string>
): string | null {
	const code = cell.generalCode ?? cell.combinedCode?.activityCode ?? null;
	if (code !== null && careerCodes.has(code)) return code;
	return (cell.subjectCode === null ? undefined : joined.get(cell.subjectCode)) ?? code;
}

/**
 * A lesson from an EasyStaff timetable cell; null for closures, unknown kinds and lessons
 * missing their id, name, day or times. Only an explicit "0" status counts as confirmed. The
 * turno is the cell's section, or for a cell without one, what its name tells.
 */
export function lessonFromCell(
	cell: EasyStaffScheduleCell,
	activityCode: string | null
): LessonEvent | null {
	if (cell.type !== EasyStaffScheduleCellType.Lesson) return null;
	const { id, subjectName, date, startTime, endTime } = cell;
	if (!id || !subjectName || !date || !startTime || !endTime) return null;
	const turno = cell.combinedCode
		? sectionTurno(cell.combinedCode.section)
		: subjectTurno(subjectName);
	return {
		id: `lesson_${id}`,
		source: 'lesson',
		date: date.toString(),
		start: minutesOf(startTime),
		end: minutesOf(endTime),
		title: subjectName,
		shortLabel: shortLabelFor(subjectName),
		location: location(cell.roomCode, cell.buildingCode, cell.mapsUrl),
		cancelled: cell.status !== '0',
		activityCode,
		subjectCode: cell.subjectCode,
		turno,
		group: variantLabel(subjectName, turno),
		teachers: cell.teacherNames.map((name) => name.trim()).filter(Boolean)
	};
}

/** Esse3 has no exam duration: a nominal two hours, clamped to 23:59. */
const NOMINAL_EXAM_MINUTES = 120;
const LAST_MINUTE = 23 * 60 + 59;

/** Italian, as Esse3 itself words it: the partial-call marker, then the mode. */
function examTypeLabel(booking: Esse3ExamSessionEnrollment): string | null {
	const modes: Record<string, string> = {
		S: 'Scritto',
		O: 'Orale',
		SOC: 'Scritto e orale',
		SOS: 'Scritto e orale'
	};
	const mode =
		booking.examTypeDescription?.trim() ||
		(booking.graduationTypeCode ? modes[booking.graduationTypeCode] : undefined);
	const parts = [booking.callTypeCode === 'PP' ? 'Prova parziale' : null, mode ?? null];
	return parts.filter(Boolean).join(' · ') || null;
}

/**
 * A booked exam; null without a date and time or a title. `activityCodes` joins the course name
 * to the transcript, which links the exam to its e-learning course.
 */
export function examFromBooking(
	booking: Esse3ExamSessionEnrollment,
	activityCodes: ReadonlyMap<string, string>
): ExamEvent | null {
	const when = esse3DateTime(booking.shiftDateTime);
	const title =
		booking.studentActivityDescription?.trim() || booking.examCallDescription?.trim() || '';
	const { courseOfStudyId, activityId, callId } = booking;
	if (!when || !title || courseOfStudyId === null || activityId === null || callId === null) {
		return null;
	}
	const start = when.hour * 60 + when.minute;
	return {
		id: `exam_${courseOfStudyId}_${activityId}_${callId}`,
		source: 'exam',
		date: when.date.toString(),
		start,
		end: Math.min(start + NOMINAL_EXAM_MINUTES, LAST_MINUTE),
		title,
		shortLabel: shortLabelFor(title),
		location: location(booking.classroomDescription, booking.buildingDescription),
		cancelled: false,
		activityCode: activityCodes.get(normalizeSubjectName(title)) ?? null,
		examTypeLabel: examTypeLabel(booking),
		bookingPosition: booking.applicationPosition,
		bookedAt: esse3Date(booking.insertionDate)?.toString() ?? null,
		cancellableUntil: esse3Date(booking.enrollmentEndDate)?.toString() ?? null
	};
}

/** Moodle names action events "<activity> è in scadenza" or "<activity> is due". */
const DUE_SUFFIXES = [' è in scadenza', ' is due'];

/**
 * An assignment's due date, as a point in time in Rome; null for quizzes, which the calendar
 * leaves to the course pages.
 */
export function deadlineFromCourseDeadline(deadline: CourseDeadline): DeadlineEvent | null {
	if (deadline.kind !== 'assign') return null;
	const clock = romeWallClock(deadline.dueAt);
	const trimmed = deadline.title.trim();
	const title =
		DUE_SUFFIXES.reduce(
			(text, suffix) => (text.endsWith(suffix) ? text.slice(0, -suffix.length) : text),
			trimmed
		) || deadline.title;
	return {
		id: `deadline_${deadline.eventId}`,
		source: 'deadline',
		date: clock.date,
		start: clock.minutes,
		end: clock.minutes,
		title,
		shortLabel: shortLabelFor(title),
		location: null,
		cancelled: false,
		activityCode: null,
		courseId: deadline.courseId,
		assignmentId: deadline.instanceId
	};
}

/** A library seat from Affluences, whose times are UTC; null when cancelled or undated. */
export function libraryFromReservation(reservation: AffluencesMyReservation): LibraryEvent | null {
	if (reservation.state === AffluencesReservationState.Cancelled || !reservation.startDateTime)
		return null;
	const startMs = Date.parse(reservation.startDateTime);
	const endMs = reservation.endDateTime ? Date.parse(reservation.endDateTime) : startMs;
	if (Number.isNaN(startMs) || Number.isNaN(endMs)) return null;
	const start = romeWallClock(startMs);
	const end = romeWallClock(endMs);
	const title = reservation.siteName?.trim() || reservation.resourceName?.trim() || '';
	return {
		id: `library_${reservation.reservationId}`,
		source: 'library',
		date: start.date,
		start: start.minutes,
		end: end.date === start.date ? Math.max(start.minutes, end.minutes) : 24 * 60,
		title,
		shortLabel: shortLabelFor(title),
		location: location(reservation.resourceName, reservation.siteSecondaryName),
		cancelled: false,
		activityCode: null,
		seatName: reservation.resourceName
	};
}

/** An information-desk appointment as the appointments feature keeps it on this browser. */
export function appointmentFromStore(appointment: Reservation): AppointmentEvent {
	const start = romeWallClock(appointment.start);
	const end = romeWallClock(appointment.end);
	return {
		id: `appointment_${appointment.code}`,
		source: 'appointment',
		date: start.date,
		start: start.minutes,
		end: end.date === start.date ? Math.max(start.minutes, end.minutes) : 24 * 60,
		title: appointment.serviceName,
		shortLabel: shortLabelFor(appointment.serviceName),
		location: location(appointment.areaAddress, appointment.areaName),
		cancelled: false,
		activityCode: null,
		serviceGroup: appointment.serviceGroup
	};
}

/** Normalized course name → Esse3 activity code, from the transcript rows. */
export function activityCodesByName(
	rows: readonly { activityCode: string | null; activityDescription: string }[]
): Map<string, string> {
	const codes = new Map<string, string>();
	for (const row of rows) {
		if (row.activityCode)
			codes.set(normalizeSubjectName(row.activityDescription), row.activityCode);
	}
	return codes;
}
