import type { EasyStaffRoomOccupationEvent } from '@mybicocca/data-api/easystaff';
import { LocalDate, LocalDateTime } from '@mybicocca/data-api/runtime';
import { describe, expect, it } from 'vitest';
import { romeInstant } from '#lib/format/rome.ts';
import { roomStatus, scheduleByRoom, type ScheduleEntry } from './status.ts';

/** 27 October 2026 at `hour`:`minute` in Rome, wherever the tests run. */
const at = (hour: number, minute = 0) =>
	romeInstant(new LocalDateTime(new LocalDate(2026, 10, 27), hour, minute));

const entry = (start: number, end: number, closure = false): ScheduleEntry => ({
	id: `${start}`,
	title: 'Lezione',
	kind: 'Lezione',
	teachers: [],
	start,
	end,
	closure
});

describe('roomStatus', () => {
	it('is unknown without an occupation', () => {
		expect(roomStatus(undefined, at(10))).toEqual({ kind: 'unknown' });
	});

	it('is free until the next booking, or for the rest of the day', () => {
		const entries = [entry(at(8, 30), at(10, 30)), entry(at(14, 30), at(16, 30))];
		expect(roomStatus(entries, at(11))).toEqual({ kind: 'free', until: at(14, 30) });
		expect(roomStatus(entries, at(17))).toEqual({ kind: 'free', until: null });
		expect(roomStatus([], at(11))).toEqual({ kind: 'free', until: null });
	});

	it('runs back-to-back and overlapping bookings into one stretch', () => {
		const entries = [
			entry(at(8, 30), at(10, 30)),
			entry(at(10, 30), at(12, 30)),
			entry(at(12, 0), at(13, 30)),
			entry(at(15), at(16))
		];
		const status = roomStatus(entries, at(9));
		expect(status).toMatchObject({ kind: 'busy', until: at(13, 30) });
	});

	it('ends a booking at its end time', () => {
		expect(roomStatus([entry(at(8, 30), at(10, 30))], at(10, 30))).toEqual({
			kind: 'free',
			until: null
		});
	});

	it('reports a closure that covers now, until its end or for the day', () => {
		const allDay = [entry(at(0), at(23, 59), true), entry(at(9), at(11))];
		expect(roomStatus(allDay, at(10))).toEqual({ kind: 'closed', until: null });

		const lunch = [
			entry(at(9), at(12)),
			entry(at(12), at(13), true),
			entry(at(13), at(14), true),
			entry(at(14), at(16))
		];
		expect(roomStatus(lunch, at(12, 30))).toEqual({ kind: 'closed', until: at(14) });
	});
});

const event = (overrides: Partial<EasyStaffRoomOccupationEvent>): EasyStaffRoomOccupationEvent => ({
	id: '1',
	title: 'FISICA I',
	date: new LocalDate(2026, 10, 27),
	startDateTime: new LocalDateTime(new LocalDate(2026, 10, 27), 8, 30),
	endDateTime: new LocalDateTime(new LocalDate(2026, 10, 27), 10, 30),
	roomName: 'U1-03 con Podio',
	roomCode: 'U1-03',
	buildingName: 'ATLAS ex U1',
	buildingCode: 'U01',
	facultyId: '20',
	status: '0',
	eventType: 'Lezione',
	isUniversityEvent: false,
	teachers: [],
	...overrides
});

describe('scheduleByRoom', () => {
	it('groups bookings by room, sorted, and drops cancelled ones', () => {
		const rooms = scheduleByRoom([
			event({
				id: '2',
				startDateTime: new LocalDateTime(new LocalDate(2026, 10, 27), 10, 30),
				endDateTime: new LocalDateTime(new LocalDate(2026, 10, 27), 12, 30)
			}),
			event({ id: '1' }),
			event({ id: '3', status: '1' }),
			event({ id: '4', roomCode: 'U1-04' })
		]);
		expect(rooms.get('U1-03')?.map((item) => item.id)).toEqual(['1', '2']);
		expect(rooms.get('U1-04')?.map((item) => item.id)).toEqual(['4']);
	});

	it('marks closures and reads teachers and kinds as people write them', () => {
		const [closure] =
			scheduleByRoom([event({ eventType: 'chiusura_type', status: null })]).get('U1-03') ?? [];
		expect(closure).toMatchObject({ closure: true, kind: null });

		const [lesson] =
			scheduleByRoom([
				event({
					eventType: 'Attivit\u00c3\u00a0 istituzionali',
					teachers: [
						{ name: 'MASSIMILIANO', surname: "D'ARIENZO", email: null, code: null },
						{ name: 'Laura ', surname: "D'angelo", email: null, code: null }
					]
				})
			]).get('U1-03') ?? [];
		expect(lesson.kind).toBe('Attività istituzionali');
		expect(lesson.teachers).toEqual(["Massimiliano D'Arienzo", "Laura D'Angelo"]);
	});
});
