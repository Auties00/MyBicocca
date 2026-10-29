import { days, esse3Date, esse3DateTime } from '../../dates.ts';
import { DemoCareer, DemoCourses, DemoStudent, type DemoCourse } from '../../persona.ts';
import { esse3Error, json, route } from '../../router.ts';
import { demoState } from '../../state.ts';

const API = 'https://s3w.si.unimib.it/e3rest/api';

/** One exam call of a course being attended, as Android's demo schedules them. */
export interface DemoCall {
	course: DemoCourse;
	number: number;
	date: Date;
	opensAt: Date;
	closesAt: Date;
	room: string;
	building: string;
	enrolled: number;
	/** Esse3's `tipoEsaCod`. */
	kind: 'S' | 'O' | 'SOC';
	/** Esse3's `tipoAppCod`: the final exam or a partial one. */
	type: 'PF' | 'PP';
	/** `desApp`, which Esse3 cuts at 40 characters. */
	description: string;
	/** Why Esse3 refuses to book it, if it does. */
	refusal?: string;
}

const BUILDINGS = {
	U1: 'U1 - Piazza della Scienza, 1',
	U4: 'U4 - Piazza della Scienza, 4',
	U6: "U6 - Piazza dell'Ateneo Nuovo, 1",
	U14: 'U14 - Viale Sarca, 336',
	U24: 'U24 - Viale Sarca'
} as const;

const QUESTIONNAIRE_REFUSAL =
	"Per prenotarsi all'appello è necessario compilare il questionario di valutazione della " +
	"didattica dell'insegnamento.";

function call(
	course: DemoCourse,
	number: number,
	inDays: number,
	hour: number,
	minute: number,
	room: string,
	building: keyof typeof BUILDINGS,
	enrolled: number,
	options: {
		kind?: DemoCall['kind'];
		closesIn?: number;
		refusal?: string;
		partial?: string;
	} = {}
): DemoCall {
	const description =
		options.partial ??
		(number === 1 ? course.name : `${course.name}-${'I'.repeat(number)} appello`);
	return {
		course,
		number,
		date: days(inDays, hour, minute),
		opensAt: days(inDays - 30),
		closesAt: days(options.closesIn ?? inDays - 3),
		room,
		building: BUILDINGS[building],
		enrolled,
		kind: options.kind ?? 'S',
		type: options.partial === undefined ? 'PF' : 'PP',
		description: description.slice(0, 40),
		...(options.refusal === undefined ? {} : { refusal: options.refusal })
	};
}

/**
 * Two summer calls for each course being attended, as on Android; the first statistics and
 * algorithms calls are booked. Operating systems closes in two days and holds a partial exam the
 * day of its second call, at the same hour, as real courses do; languages is held written and
 * oral and refuses bookings until its questionnaire is filled in. The databases mid-term, in three
 * days, closed its registrations yesterday: Esse3 still lists such a call among the future ones.
 */
export const demoCalls = () =>
	demoState('esse3.examCalls', (): DemoCall[] => {
		const { statistics, algorithms, databases, operatingSystems, languages } = DemoCourses;
		const languagesOptions = { kind: 'SOC', refusal: QUESTIONNAIRE_REFUSAL } as const;
		return [
			call(statistics, 1, 8, 9, 30, 'U4-08', 'U4', 84),
			call(statistics, 2, 29, 9, 30, 'U4-08', 'U4', 31),
			call(algorithms, 1, 13, 14, 0, 'U6-06', 'U6', 112),
			call(algorithms, 2, 34, 14, 0, 'U6-06', 'U6', 47),
			call(databases, 1, 11, 10, 0, 'U24-C2', 'U24', 96),
			call(databases, 2, 32, 10, 0, 'U24-C2', 'U24', 22),
			call(operatingSystems, 1, 19, 9, 0, 'U14-T024', 'U14', 73, { closesIn: 2 }),
			call(operatingSystems, 2, 40, 9, 0, 'U14-T024', 'U14', 15),
			call(operatingSystems, 3, 40, 9, 0, 'U14-T024', 'U14', 41, {
				partial: 'SISTEMI OPERATIVI-compitino'
			}),
			call(languages, 1, 22, 14, 30, 'U1-09', 'U1', 58, languagesOptions),
			call(languages, 2, 43, 14, 30, 'U1-09', 'U1', 9, languagesOptions),
			call(databases, 3, 3, 11, 0, 'U24-C2', 'U24', 64, {
				closesIn: -1,
				partial: 'BASI DI DATI-prova intermedia'
			})
		];
	});

export function callKeyOf(of: DemoCall) {
	return DemoCareer.callKey(of.course, of.number);
}

/** A `/prenotazioni` row, as Esse3 sends it with the app's optional fields. */
export type DemoBookingRow = Record<string, unknown> & {
	cdsId: number;
	adId: number;
	appId: number;
	pubblId: number | null;
};

const { career } = DemoStudent;

/** The booking row for `of`, made `bookedOn` at `position`. */
export function bookingRow(
	of: DemoCall,
	position: number,
	bookedOn: Date,
	note: string | null = null
): DemoBookingRow {
	const key = callKeyOf(of);
	return {
		applistaId: 900000 + key.activityId * 10 + of.number,
		cdsId: key.courseOfStudyId,
		adId: key.activityId,
		appId: key.callId,
		appLogId: 1,
		stuId: career.id,
		matId: career.enrollmentTraitId,
		adsceId: key.activityId,
		adStuCod: of.course.code,
		adStuDes: of.course.name,
		desAppello: of.description,
		aulaDes: of.room,
		edificioDes: of.building,
		dataInizioApp: esse3Date(of.date),
		dataOraTurno: esse3DateTime(of.date),
		tipoEsaCod: of.kind,
		tipoAppCod: of.type,
		posizApp: position,
		posiz: position,
		dataIns: esse3DateTime(bookedOn),
		dataInizioIscr: esse3Date(of.opensAt),
		dataFineIscr: esse3DateTime(of.closesAt),
		pesoAd: of.course.cfu,
		tipoSvolgimentoEsameDes: 'Esame in Presenza',
		notaStudente: note,
		esito: { superatoFlg: 0, assenteFlg: 0, ritiratoFlg: 0, votoEsa: null },
		pubblId: null,
		notaPubbl: null,
		presaVisione: null
	};
}

/** What the examiners recorded: a grade, a pass or a fail, or that the student didn't sit it. */
interface DemoOutcome {
	grade: number | null;
	passed: boolean;
	cumLaude?: boolean;
	absent?: boolean;
	withdrew?: boolean;
}

/**
 * A past booking with its published outcome. `ack` is `presaVisione`: accepted unless given, null
 * for an outcome recorded without asking the student (an oral exam, an older record).
 */
function pastRow(
	activity: { id: number; code: string; name: string; credits: number },
	on: Date,
	outcome: DemoOutcome,
	options: {
		ack?: 'A' | 'N' | 'R' | 'V' | null;
		note?: string;
		decideBy?: Date;
		callId?: number;
	} = {}
): DemoBookingRow {
	const callId = options.callId ?? 1;
	return {
		applistaId: 800000 + activity.id * 10 + callId,
		cdsId: DemoCareer.courseOfStudyId,
		adId: activity.id,
		appId: callId,
		stuId: career.id,
		matId: career.enrollmentTraitId,
		adsceId: activity.id,
		adStuCod: activity.code,
		adStuDes: activity.name,
		desAppello: activity.name,
		aulaDes: null,
		edificioDes: null,
		dataInizioApp: esse3Date(on),
		dataOraTurno: esse3DateTime(new Date(on.getFullYear(), on.getMonth(), on.getDate(), 9, 30)),
		tipoEsaCod: 'S',
		tipoAppCod: 'PF',
		posizApp: 20 + (activity.id % 40),
		dataIns: esse3DateTime(new Date(on.getFullYear(), on.getMonth(), on.getDate() - 12, 11, 4)),
		dataFineIscr: esse3DateTime(new Date(on.getFullYear(), on.getMonth(), on.getDate() - 3)),
		pesoAd: activity.credits,
		tipoSvolgimentoEsameDes: 'Esame in Presenza',
		notaStudente: null,
		esito: {
			superatoFlg: outcome.passed ? 1 : 0,
			assenteFlg: outcome.absent ? 1 : 0,
			ritiratoFlg: outcome.withdrew ? 1 : 0,
			votoEsa: outcome.grade ?? 0,
			lodeFlg: outcome.cumLaude ? 1 : 0
		},
		pubblId: 910000 + activity.id * 10 + callId,
		notaPubbl: options.note ?? null,
		presaVisione: options.ack === undefined ? 'A' : options.ack,
		dataRifEsitoStu: options.decideBy ? esse3DateTime(options.decideBy) : null
	};
}

/** `days` days before `date`, at midnight. */
const before = (date: Date, days: number) =>
	new Date(date.getFullYear(), date.getMonth(), date.getDate() - days);

/** The first-year exams, recorded before Bicocca asked students to accept their grades. */
const RECORDED = new Set(['E3101Q101', 'E3101Q102']);

/**
 * Every booking of the record book: the two upcoming ones, the exams already passed (the oldest
 * recorded without a decision, Ricerca Operativa left unanswered past its deadline, so it stands),
 * a failed first attempt, an absence and a withdrawal, and two results still waiting for the
 * student's decision: the web lab's, and a 30 cum laude whose deadline is tomorrow.
 */
export const demoBookings = () =>
	demoState('esse3.bookings', (): DemoBookingRow[] => {
		const calls = demoCalls();
		const activities = DemoCareer.activities();
		const byCode = (code: string) => activities.find((activity) => activity.code === code);
		const analysis = byCode('E3101Q103');
		const algebra = byCode('E3101Q111');
		const physics = byCode('E3101Q113');
		const security = byCode('E3101Q117');
		const lab = activities.find((activity) => activity.id === DemoCareer.webLabId);
		const passed = activities
			.filter((activity) => activity.passedOn !== null)
			.map((activity) => {
				const on = activity.passedOn ?? days(0);
				const outcome = { grade: activity.grade, passed: true, cumLaude: activity.cumLaude };
				if (RECORDED.has(activity.code)) return pastRow(activity, on, outcome, { ack: null });
				if (activity.code !== 'E3101Q114') return pastRow(activity, on, outcome);
				return pastRow(activity, on, outcome, { ack: 'V', decideBy: before(on, -10) });
			});
		return [
			bookingRow(calls[0], 37, days(-9, 18, 47)),
			bookingRow(calls[2], 12, days(-9, 18, 52)),
			...(security
				? [
						pastRow(
							security,
							days(-8),
							{ grade: 30, passed: true, cumLaude: true },
							{ ack: 'V', decideBy: days(1, 23, 59) }
						)
					]
				: []),
			...(algebra?.passedOn
				? [
						pastRow(
							algebra,
							before(algebra.passedOn, 35),
							{ grade: null, passed: false, absent: true },
							{ ack: null, callId: 2 }
						)
					]
				: []),
			...(physics?.passedOn
				? [
						pastRow(
							physics,
							before(physics.passedOn, 42),
							{ grade: null, passed: false, withdrew: true },
							{ ack: null, callId: 2 }
						)
					]
				: []),
			...(lab
				? [
						pastRow(
							lab,
							days(-6),
							{ grade: 27, passed: true },
							{
								ack: 'N',
								note: 'Visione dei compiti venerdì alle 14:00 in U14, studio 2041.',
								decideBy: days(5, 23, 59)
							}
						)
					]
				: []),
			...passed,
			...(analysis?.passedOn
				? [
						pastRow(
							analysis,
							before(analysis.passedOn, 28),
							{ grade: null, passed: false },
							{
								ack: 'R',
								callId: 2
							}
						)
					]
				: [])
		];
	});

const sameKey = (row: DemoBookingRow, key: ReturnType<typeof callKeyOf>) =>
	row.cdsId === key.courseOfStudyId && row.adId === key.activityId && row.appId === key.callId;

/** The calls Esse3 lists as bookable now or later: future ones the student hasn't booked. */
function bookableCalls() {
	const bookings = demoBookings();
	return demoCalls()
		.filter((of) => of.date.getTime() > Date.now())
		.filter((of) => !bookings.some((row) => sameKey(row, callKeyOf(of))))
		.sort((a, b) => a.date.getTime() - b.date.getTime());
}

const callStatus = (of: DemoCall) => {
	const now = Date.now();
	if (of.opensAt.getTime() > now) return { code: 'P', description: 'Iscrizioni non ancora aperte' };
	if (of.closesAt.getTime() + 24 * 60 * 60_000 <= now) {
		return { code: 'C', description: 'Iscrizioni chiuse' };
	}
	return { code: 'I', description: 'Iscrizioni aperte' };
};

/** An `/appelli` row with the app's optional fields. */
export function callRow(of: DemoCall) {
	const key = callKeyOf(of);
	const [name, surname] = of.course.teacher.split(' ');
	const status = callStatus(of);
	return {
		cdsId: key.courseOfStudyId,
		adId: key.activityId,
		appId: key.callId,
		appelloId: key.activityId * 100 + of.number,
		adsceId: key.activityId,
		matId: career.enrollmentTraitId,
		adCod: of.course.code,
		adDes: of.course.name,
		cdsCod: career.easyStaffProgramCode,
		cdsDes: DemoCareer.degree,
		desApp: of.description,
		dataInizioApp: esse3Date(of.date),
		oraEsa: esse3DateTime(of.date),
		dataInizioIscr: esse3Date(of.opensAt),
		dataFineIscr: esse3Date(of.closesAt),
		numIscritti: of.enrolled,
		stato: status.code,
		statoDes: status.description,
		tipoAppCod: of.type,
		tipoEsaCod: of.kind,
		riservatoFlg: 0,
		note:
			`Presentarsi con il badge universitario e un documento d'identità.\n` +
			`${of.building.split(' - ')[0]}, aula ${of.room}. Durata della prova: 2 ore.`,
		presidenteId: 40000 + of.course.moodleCourseId,
		presidenteNome: name,
		presidenteCognome: surname,
		tipoGestPrenDes: 'Prenotazione online'
	};
}

export const routes = [
	route(
		`GET ${API}/libretto-service-v2/libretti/:matId/prenotazioni`,
		(request) => {
			if (Number(request.params.matId) !== career.enrollmentTraitId) {
				return esse3Error(404, 'Libretto non trovato');
			}
			const published = request.url.searchParams.get('filter')?.includes('pubblId=gt=0');
			const rows = demoBookings().filter((row) => !published || row.pubblId !== null);
			const dated = (row: DemoBookingRow) => String(row.dataOraTurno ?? '');
			const time = (row: DemoBookingRow) => {
				const [d, m, y] = dated(row).slice(0, 10).split('/');
				return `${y}${m}${d}${dated(row).slice(11)}`;
			};
			return json([...rows].sort((a, b) => time(b).localeCompare(time(a))));
		},
		{ empty: () => esse3Error(404, 'Nessuna prenotazione trovata') }
	),
	route(
		`GET ${API}/libretto-service-v2/libretti/:matId/appelli`,
		(request) => {
			if (Number(request.params.matId) !== career.enrollmentTraitId) {
				return esse3Error(404, 'Libretto non trovato');
			}
			return json(bookableCalls().map(callRow));
		},
		{ empty: () => esse3Error(404, 'Nessun appello trovato') }
	)
];
