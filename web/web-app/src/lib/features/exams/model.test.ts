import { decodeEsse3ExamSessionEnrollment } from '@mybicocca/data-api/esse3';
import { decodeEsse3ExamSessionTranscript } from '@mybicocca/data-api/esse3';
import { describe, expect, it } from 'vitest';
import {
	canBook,
	callLabel,
	callNickname,
	canCancel,
	courseRows,
	daysToClose,
	examLocation,
	groupCalls,
	matchesCourse,
	sameDayCalls,
	splitBookings,
	toBooking,
	toExamCall,
	windowStatus,
	type Booking,
	type ExamCall
} from './model.ts';

const today = new Date(2026, 5, 18, 10, 30);
const on = (month: number, day: number, hour = 0, minute = 0) =>
	new Date(2026, month - 1, day, hour, minute);

function call(overrides: Partial<ExamCall> = {}): ExamCall {
	return {
		slug: '1-2-3',
		key: { courseOfStudyId: 1, activityId: 2, callId: 3 },
		activityChoiceId: 99,
		code: 'E3101Q106',
		title: 'Algoritmi e Strutture Dati',
		degree: null,
		description: null,
		date: on(7, 1, 14),
		timed: true,
		opensAt: on(6, 1),
		closesAt: on(6, 28),
		enrolled: null,
		kind: 'written',
		callType: 'final',
		notes: null,
		president: null,
		bookingMode: null,
		...overrides
	};
}

function booking(at: Date | null, overrides: Partial<Booking> = {}): Booking {
	return {
		slug: '1-2-3',
		key: { courseOfStudyId: 1, activityId: 2, callId: 3 },
		applicationListId: 1,
		studentId: 7,
		activityChoiceId: 99,
		title: 'Basi di Dati',
		callDescription: null,
		kind: 'written',
		callType: 'final',
		at,
		timed: true,
		room: null,
		building: null,
		credits: 8,
		mode: null,
		position: 3,
		bookedAt: null,
		cancellableUntil: null,
		note: null,
		grade: { kind: 'unknown' },
		published: false,
		publishedNote: null,
		...overrides
	};
}

describe('windowStatus', () => {
	it('is open from the first to the last day', () => {
		expect(windowStatus(call({ opensAt: on(6, 18), closesAt: on(6, 18) }), today)).toBe('open');
		expect(windowStatus(call(), today)).toBe('open');
	});

	it('is upcoming before it opens and closed after the last day', () => {
		expect(windowStatus(call({ opensAt: on(6, 19) }), today)).toBe('upcoming');
		expect(windowStatus(call({ closesAt: on(6, 17) }), today)).toBe('closed');
	});

	it('stays open without bounds', () => {
		expect(windowStatus(call({ opensAt: null, closesAt: null }), today)).toBe('open');
	});

	it('counts the days left to register', () => {
		expect(daysToClose(call(), today)).toBe(10);
		expect(daysToClose(call({ closesAt: on(6, 18) }), today)).toBe(0);
		expect(daysToClose(call({ opensAt: on(6, 20) }), today)).toBeUndefined();
	});

	it('books only open calls with a record-book row', () => {
		expect(canBook(call(), today)).toBe(true);
		expect(canBook(call({ activityChoiceId: null }), today)).toBe(false);
		expect(canBook(call({ closesAt: on(6, 10) }), today)).toBe(false);
	});
});

describe('groupCalls', () => {
	it('sorts courses by name and their calls by date, undated last', () => {
		const groups = groupCalls([
			call({ slug: 'b2', code: 'B', title: 'Basi di Dati', date: on(7, 20) }),
			call({ slug: 'a1', code: 'A', title: 'algoritmi', date: null }),
			call({ slug: 'b1', code: 'B', title: 'Basi di Dati', date: on(6, 29) }),
			call({ slug: 'a2', code: 'A', title: 'algoritmi', date: on(7, 1) })
		]);
		expect(groups.map((group) => group.key)).toEqual(['A', 'B']);
		expect(groups[0].calls.map((c) => c.slug)).toEqual(['a2', 'a1']);
		expect(groups[1].calls.map((c) => c.slug)).toEqual(['b1', 'b2']);
	});

	it('falls back to the name when the code is missing', () => {
		const [group] = groupCalls([call({ code: null, title: 'Fisica' })]);
		expect(group.key).toBe('Fisica');
	});
});

describe('courseRows', () => {
	it('puts the course’s bookings among its calls, by date', () => {
		const [group] = groupCalls([
			call({
				slug: '1-2-4',
				key: { courseOfStudyId: 1, activityId: 2, callId: 4 },
				date: on(7, 22)
			})
		]);
		const rows = courseRows(group, [
			booking(on(7, 1, 14)),
			booking(on(6, 26), { slug: '1-5-1', key: { courseOfStudyId: 1, activityId: 5, callId: 1 } })
		]);
		expect(rows.map((row) => row.id)).toEqual(['booking:1-2-3', 'call:1-2-4']);
	});
});

describe('calls on the same day', () => {
	it('finds the calls that share a day', () => {
		const twins = sameDayCalls([
			call({ slug: 'a', date: on(11, 10, 16, 30) }),
			call({ slug: 'b', date: on(11, 10, 9) }),
			call({ slug: 'c', date: on(11, 24, 16, 30) }),
			call({ slug: 'd', date: null })
		]);
		expect([...twins].sort()).toEqual(['a', 'b']);
	});

	it('names them after what follows the course in Esse3’s description', () => {
		const title = 'Analisi e Progetto di Algoritmi';
		expect(callNickname('ANALISI E PROGETTO DI ALGORITMI-compitin', title)).toBe('compitin');
		expect(callNickname('ANALISI E PROG ALGO-VI appello completo', title)).toBe(
			'VI appello completo'
		);
		expect(callNickname('Appello 1 sessione estiva', title)).toBe('Appello 1 sessione estiva');
		expect(callNickname('PROVA SCRITTA - MODULO B', title)).toBe('PROVA SCRITTA - MODULO B');
	});
});

describe('callLabel', () => {
	it('words a call the same wherever it shows, in sentence case', () => {
		expect(callLabel('PROBABILITÀ E STATISTICA-II APPELLO', 'Probabilità e Statistica')).toBe(
			'II appello'
		);
		expect(callLabel('SISTEMI OPERATIVI-compitino', 'Sistemi Operativi')).toBe('Compitino');
		expect(
			callLabel('ANALISI E PROG ALGO-VI appello completo', 'Analisi e Progetto di Algoritmi')
		).toBe('VI appello completo');
		expect(callLabel('APPELLO  STRAORDINARIO', 'Fisica')).toBe('Appello straordinario');
	});

	it('has nothing to add when Esse3 only repeats the course', () => {
		expect(callLabel('FISICA', 'Fisica')).toBeNull();
		expect(callLabel('  ', 'Fisica')).toBeNull();
		expect(callLabel(null, 'Fisica')).toBeNull();
	});
});

describe('matchesCourse', () => {
	const course = { title: 'Probabilità e Statistica', code: 'E3101Q109' };

	it('matches every word, ignoring accents and case', () => {
		expect(matchesCourse(course, 'probabilita STAT')).toBe(true);
		expect(matchesCourse(course, 'q109')).toBe(true);
		expect(matchesCourse(course, '  ')).toBe(true);
	});

	it('rejects a word the course lacks', () => {
		expect(matchesCourse(course, 'statistica analisi')).toBe(false);
	});
});

describe('bookings', () => {
	it('splits upcoming (today included) from past', () => {
		const { upcoming, past } = splitBookings(
			[
				booking(on(6, 26, 9, 30), { slug: 'later' }),
				booking(on(6, 18, 9), { slug: 'today' }),
				booking(on(2, 5), { slug: 'feb' }),
				booking(null, { slug: 'undated' }),
				booking(on(6, 17), { slug: 'yesterday' })
			],
			today
		);
		expect(upcoming.map((b) => b.slug)).toEqual(['today', 'later', 'undated']);
		expect(past.map((b) => b.slug)).toEqual(['yesterday', 'feb']);
	});

	it('cancels upcoming bookings until the window closes', () => {
		expect(canCancel(booking(on(6, 26), { cancellableUntil: on(6, 23) }), today)).toBe(true);
		expect(canCancel(booking(on(6, 26), { cancellableUntil: on(6, 18) }), today)).toBe(true);
		expect(canCancel(booking(on(6, 26), { cancellableUntil: on(6, 17) }), today)).toBe(false);
		expect(canCancel(booking(on(6, 26), { studentId: null }), today)).toBe(false);
		expect(canCancel(booking(on(6, 1)), today)).toBe(false);
	});
});

describe('wire mapping', () => {
	it('maps a booking row', () => {
		const row = decodeEsse3ExamSessionEnrollment({
			applistaId: 900001,
			cdsId: 10001,
			adId: 25104,
			appId: 1,
			stuId: 700001,
			adsceId: 25104,
			adStuDes: "PROBABILITA' E STATISTICA",
			desAppello: 'Appello 1 sessione estiva',
			tipoEsaCod: 'S',
			tipoAppCod: 'PF',
			dataOraTurno: '26/06/2026 09:30:00',
			aulaDes: 'U4-08',
			edificioDes: ' ',
			posizApp: 37,
			dataIns: '09/06/2026 18:47:00',
			dataFineIscr: '23/06/2026 00:00:00',
			esito: { superatoFlg: 0 },
			pubblId: null
		});
		const mapped = row && toBooking(row);
		expect(mapped).toMatchObject({
			slug: '10001-25104-1',
			title: 'Probabilità e Statistica',
			kind: 'written',
			callType: 'final',
			at: on(6, 26, 9, 30),
			timed: true,
			room: 'U4-08',
			building: null,
			position: 37,
			cancellableUntil: on(6, 23),
			grade: { kind: 'unknown' },
			published: false
		});
	});

	it('drops a row whose call key is incomplete', () => {
		const row = decodeEsse3ExamSessionEnrollment({ cdsId: 1, adId: 2 });
		expect(row && toBooking(row)).toBeUndefined();
	});

	it('maps a call with its time from oraEsa', () => {
		const row = decodeEsse3ExamSessionTranscript({
			cdsId: 10001,
			adId: 25101,
			appId: 2,
			adsceId: 25101,
			adCod: 'E3101Q106',
			adDes: 'ALGORITMI E STRUTTURE DATI',
			dataInizioApp: '22/07/2026',
			oraEsa: '22/07/2026 14:00:00',
			dataInizioIscr: '22/06/2026',
			dataFineIscr: '19/07/2026',
			tipoEsaCod: 'SOC',
			presidenteNome: 'LAURA',
			presidenteCognome: 'CONTI'
		});
		const mapped = row && toExamCall(row);
		expect(mapped).toMatchObject({
			slug: '10001-25101-2',
			title: 'Algoritmi e Strutture Dati',
			date: on(7, 22, 14),
			timed: true,
			opensAt: on(6, 22),
			closesAt: on(7, 19),
			kind: 'written_oral',
			president: 'Laura Conti'
		});
	});

	it('leaves a midnight call untimed', () => {
		const row = decodeEsse3ExamSessionTranscript({
			cdsId: 1,
			adId: 2,
			appId: 3,
			dataInizioApp: '22/07/2026',
			oraEsa: '22/07/2026 00:00:00'
		});
		expect(row && toExamCall(row)).toMatchObject({ date: on(7, 22), timed: false });
	});
});

describe('examLocation', () => {
	it('drops the building a room is named after', () => {
		expect(examLocation('U4-08', 'Edificio U4 - Piazza della Scienza 4')).toEqual({
			room: 'U4-08',
			place: 'Piazza della Scienza 4'
		});
		expect(examLocation('U6-06', 'U6')).toEqual({ room: 'U6-06', place: null });
	});

	it('keeps a building the room is not named after', () => {
		expect(examLocation('Aula Magna', 'Edificio U6 - Piazza dell’Ateneo Nuovo 1')).toEqual({
			room: 'Aula Magna',
			place: 'Edificio U6 - Piazza dell’Ateneo Nuovo 1'
		});
	});

	it('uses whichever is known', () => {
		expect(examLocation(null, 'U4')).toEqual({ room: null, place: 'U4' });
		expect(examLocation('U4-08', null)).toEqual({ room: 'U4-08', place: null });
	});
});
