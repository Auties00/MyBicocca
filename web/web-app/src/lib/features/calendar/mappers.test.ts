import type { AffluencesMyReservation } from '@mybicocca/data-api/affluences';
import type { EasyStaffScheduleCell } from '@mybicocca/data-api/easystaff';
import type { Esse3ExamSessionEnrollment } from '@mybicocca/data-api/esse3';
import { LocalDate, LocalTime } from '@mybicocca/data-api/runtime';
import type { CourseDeadline } from '#lib/features/elearning/deadlines.ts';
import { describe, expect, it } from 'vitest';
import { busyLevel } from './busy.ts';
import {
	deadlineFromCourseDeadline,
	examFromBooking,
	lessonActivity,
	lessonFromCell,
	libraryFromReservation
} from './mappers.ts';
import type { CalendarEvent } from './model.ts';

const cell = (overrides: Partial<EasyStaffScheduleCell> = {}) =>
	({
		type: 'Lezione',
		id: '991',
		subjectName: 'BASI DI DATI',
		subjectCode: 'ES107',
		generalCode: null,
		combinedCode: null,
		date: new LocalDate(2026, 6, 18),
		startTime: new LocalTime(9, 30),
		endTime: new LocalTime(11, 30),
		roomCode: 'U24-C2',
		buildingCode: 'U24',
		mapsUrl: null,
		status: '0',
		teacherNames: ['Paolo Ferri'],
		...overrides
	}) as EasyStaffScheduleCell;

describe('lessonFromCell', () => {
	it('maps a lesson with its codes as location', () => {
		expect(lessonFromCell(cell(), 'E3101Q107')).toMatchObject({
			id: 'lesson_991',
			date: '2026-06-18',
			start: 570,
			end: 690,
			shortLabel: 'BD',
			location: { building: 'U24', room: 'U24-C2' },
			cancelled: false,
			activityCode: 'E3101Q107',
			teachers: ['Paolo Ferri']
		});
	});

	it("takes the turno from the cell's section, and from the name only without one", () => {
		const section = (value: string) => ({
			program: 'E3101Q-11',
			curriculumPath: 'GGG',
			activityCode: 'E3101Q114',
			section: value,
			site: 'MILANO'
		});
		const named = 'RICERCA OPERATIVA (T1 FRONTALE TURNO 1)';
		expect(
			lessonFromCell(cell({ subjectName: named, combinedCode: section('T2') }), null)
		).toMatchObject({ turno: 2, group: 'T2' });
		expect(
			lessonFromCell(cell({ subjectName: named, combinedCode: section('TU') }), null)?.turno
		).toBeNull();
		expect(lessonFromCell(cell({ subjectName: named }), null)?.turno).toBe(1);
	});

	it('marks cancelled lessons and skips closures and incomplete cells', () => {
		expect(lessonFromCell(cell({ status: '1' }), null)?.cancelled).toBe(true);
		expect(lessonFromCell(cell({ type: 'chiusura_type' }), null)).toBeNull();
		expect(lessonFromCell(cell({ startTime: null }), null)).toBeNull();
	});
});

describe('lessonActivity', () => {
	const joined = new Map([['ES107', 'E3101Q107']]);
	const career = new Set(['E3101Q107', 'E3101Q114']);

	it("takes the lesson's own code when the career has that activity", () => {
		expect(lessonActivity(cell({ generalCode: 'E3101Q114' }), joined, career)).toBe('E3101Q114');
	});

	it('prefers the career’s code, joined by name, to another degree’s', () => {
		expect(lessonActivity(cell({ generalCode: 'E3102Q031' }), joined, career)).toBe('E3101Q107');
	});

	it("keeps EasyStaff's code where the plan names no activity, and has none without either", () => {
		const unplanned = cell({ subjectCode: 'ES999', generalCode: 'E3101Q120' });
		expect(lessonActivity(unplanned, joined, career)).toBe('E3101Q120');
		expect(lessonActivity(cell({ subjectCode: 'ES999' }), joined, career)).toBeNull();
		expect(lessonActivity(cell(), joined, career)).toBe('E3101Q107');
	});
});

const booking = (overrides: Partial<Esse3ExamSessionEnrollment> = {}) =>
	({
		courseOfStudyId: 10001,
		activityId: 25102,
		callId: 3,
		shiftDateTime: '26/06/2026 09:00:00',
		studentActivityDescription: 'BASI DI DATI',
		examCallDescription: 'Appello estivo',
		classroomDescription: 'Aula U6-08',
		buildingDescription: 'U6',
		examTypeDescription: null,
		graduationTypeCode: 'S',
		callTypeCode: 'PP',
		applicationPosition: 12,
		insertionDate: '02/06/2026',
		enrollmentEndDate: '22/06/2026 23:59:59',
		...overrides
	}) as Esse3ExamSessionEnrollment;

describe('examFromBooking', () => {
	it('maps a booked exam as a nominal two-hour block', () => {
		expect(examFromBooking(booking(), new Map([['BASI DI DATI', 'E3101Q107']]))).toMatchObject({
			id: 'exam_10001_25102_3',
			date: '2026-06-26',
			start: 540,
			end: 660,
			examTypeLabel: 'Prova parziale · Scritto',
			activityCode: 'E3101Q107',
			bookingPosition: 12,
			bookedAt: '2026-06-02',
			cancellableUntil: '2026-06-22'
		});
	});

	it('clamps a late exam to 23:59 and prefers Esse3’s own mode', () => {
		const late = examFromBooking(
			booking({
				shiftDateTime: '26/06/2026 22:30',
				callTypeCode: null,
				examTypeDescription: 'Orale'
			}),
			new Map()
		);
		expect(late).toMatchObject({ end: 23 * 60 + 59, examTypeLabel: 'Orale', activityCode: null });
	});

	it('needs a date and a title', () => {
		expect(examFromBooking(booking({ shiftDateTime: null }), new Map())).toBeNull();
		expect(
			examFromBooking(
				booking({ studentActivityDescription: ' ', examCallDescription: null }),
				new Map()
			)
		).toBeNull();
	});
});

const deadline = (overrides: Partial<CourseDeadline> = {}): CourseDeadline => ({
	eventId: 77,
	courseId: 5105,
	kind: 'assign',
	instanceId: 51050,
	cmId: 510580,
	title: 'Progetto: interprete',
	dueAt: Date.UTC(2026, 10, 11, 22, 59),
	...overrides
});

describe('deadlineFromCourseDeadline', () => {
	it('maps an assignment due at 23:59 in Rome as a point in time', () => {
		expect(deadlineFromCourseDeadline(deadline())).toMatchObject({
			id: 'deadline_77',
			title: 'Progetto: interprete',
			date: '2026-11-11',
			start: 23 * 60 + 59,
			end: 23 * 60 + 59,
			courseId: 5105,
			assignmentId: 51050
		});
	});

	it('strips the due suffix Moodle adds to bare event names, and leaves quizzes out', () => {
		expect(deadlineFromCourseDeadline(deadline({ title: 'Report is due' }))?.title).toBe('Report');
		expect(deadlineFromCourseDeadline(deadline({ title: 'Relazione è in scadenza' }))?.title).toBe(
			'Relazione'
		);
		expect(deadlineFromCourseDeadline(deadline({ kind: 'quiz' }))).toBeNull();
	});
});

describe('libraryFromReservation', () => {
	const reservation = {
		reservationId: 8,
		resourceName: 'Posto 42',
		siteName: 'Biblioteca di Ateneo',
		siteSecondaryName: 'Sede Centrale',
		startDateTime: '2026-06-18T14:00:00Z',
		endDateTime: '2026-06-18T16:30:00Z',
		state: 'UPCOMING'
	} as AffluencesMyReservation;

	it('shows UTC times in Rome and drops cancelled seats', () => {
		expect(libraryFromReservation(reservation)).toMatchObject({
			start: 16 * 60,
			end: 18 * 60 + 30,
			title: 'Biblioteca di Ateneo',
			location: { building: 'Sede Centrale', room: 'Posto 42' },
			seatName: 'Posto 42'
		});
		expect(libraryFromReservation({ ...reservation, state: 'CANCELLED' })).toBeNull();
	});
});

describe('busyLevel', () => {
	const event = (start: number, end: number) => ({ start, end }) as CalendarEvent;

	it('buckets the scheduled hours of a day', () => {
		expect(busyLevel([])).toBe(0);
		expect(busyLevel([event(1439, 1439)])).toBe(1);
		expect(busyLevel([event(540, 660), event(600, 720)])).toBe(2);
		expect(busyLevel([event(480, 840)])).toBe(3);
		expect(busyLevel([event(480, 900)])).toBe(4);
	});
});
