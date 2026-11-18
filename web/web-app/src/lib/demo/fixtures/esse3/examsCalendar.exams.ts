import { demoPdf } from '../../pdf.ts';
import { DemoStudent } from '../../persona.ts';
import { bytes, empty, esse3Error, json, route, type DemoRequest } from '../../router.ts';
import { evaluated } from './questionnaires.ts';
import {
	bookingRow,
	callKeyOf,
	callRow,
	demoBookings,
	demoCalls,
	type DemoBookingRow,
	type DemoCall
} from './transcript.exams.ts';

const CALLS = 'https://s3w.si.unimib.it/e3rest/api/calesa-service-v1/appelli/:cdsId/:adId/:appId';

/** The count is a slow query on the real server: the page loads it lazily, so it shows here too. */
const COUNT_LATENCY_MS = 900;

function keyOf(request: DemoRequest) {
	return {
		courseOfStudyId: Number(request.params.cdsId),
		activityId: Number(request.params.adId),
		callId: Number(request.params.appId)
	};
}

function findCall(request: DemoRequest): DemoCall | undefined {
	const key = keyOf(request);
	return demoCalls().find((of) => {
		const candidate = callKeyOf(of);
		return (
			candidate.courseOfStudyId === key.courseOfStudyId &&
			candidate.activityId === key.activityId &&
			candidate.callId === key.callId
		);
	});
}

function bookingIndex(request: DemoRequest): number {
	const key = keyOf(request);
	return demoBookings().findIndex(
		(row) =>
			row.cdsId === key.courseOfStudyId && row.adId === key.activityId && row.appId === key.callId
	);
}

function student(request: DemoRequest): boolean {
	return Number(request.params.stuId) === DemoStudent.career.id;
}

function slipLines(row: DemoBookingRow): string[] {
	const { fullName, studentNumber } = DemoStudent;
	return [
		`Studente: ${fullName} - matricola ${studentNumber}`,
		`Insegnamento: ${String(row.adStuDes)} (${String(row.adStuCod)})`,
		`Appello: ${String(row.desAppello)}`,
		`Data e ora: ${String(row.dataOraTurno).slice(0, 16)}`,
		`Aula: ${String(row.aulaDes ?? 'da definire')}`,
		`Posizione in lista: ${String(row.posizApp)}`,
		`Prenotazione effettuata il ${String(row.dataIns).slice(0, 16)}`
	];
}

const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export const routes = [
	route(`GET ${CALLS}`, async (request) => {
		const of = findCall(request);
		if (!of) return esse3Error(404, 'Appello non trovato');
		await delay(COUNT_LATENCY_MS);
		return json({ ...callRow(of), turni: [], sessioni: [], commissione: [] });
	}),
	route(`POST ${CALLS}/iscritti`, async (request) => {
		const of = findCall(request);
		if (!of) return esse3Error(404, 'Appello non trovato');
		if (bookingIndex(request) >= 0) {
			return esse3Error(422, "Lo studente risulta già iscritto all'appello.");
		}
		const now = Date.now();
		if (now < of.opensAt.getTime() || now >= of.closesAt.getTime() + 24 * 60 * 60_000) {
			return esse3Error(422, "Le iscrizioni all'appello non sono aperte.");
		}
		// The questionnaire's refusal lasts until the course's questionnaire is filled in.
		if (of.refusal && !evaluated(callKeyOf(of).activityId)) return esse3Error(422, of.refusal);
		const body = await request.json<{ notaStu?: string | null }>();
		of.enrolled += 1;
		demoBookings().push(bookingRow(of, of.enrolled, new Date(), body.notaStu ?? null));
		return empty(201);
	}),
	route(`DELETE ${CALLS}/iscritti/:stuId`, (request) => {
		const index = bookingIndex(request);
		if (!student(request) || index < 0) return esse3Error(404, 'Prenotazione non trovata');
		const of = findCall(request);
		if (of && Date.now() >= of.closesAt.getTime() + 24 * 60 * 60_000) {
			return esse3Error(422, 'Il periodo di cancellazione della prenotazione è terminato.');
		}
		demoBookings().splice(index, 1);
		if (of) of.enrolled = Math.max(0, of.enrolled - 1);
		return empty(204);
	}),
	route(`GET ${CALLS}/iscritti/:stuId/statino-prenotazione`, (request) => {
		const row = demoBookings()[bookingIndex(request)];
		if (!student(request) || !row) return esse3Error(404, 'Prenotazione non trovata');
		return bytes(demoPdf('Promemoria di prenotazione', slipLines(row)), 'application/pdf');
	}),
	route(`GET ${CALLS}/iscritti/:stuId/attestato-di-presenza`, (request) => {
		const row = demoBookings()[bookingIndex(request)];
		if (!student(request) || !row) return esse3Error(404, 'Prenotazione non trovata');
		if (row.pubblId === null) {
			return esse3Error(422, "L'attestato è disponibile solo dopo la pubblicazione dell'esito.");
		}
		return bytes(
			demoPdf("Attestato di presenza all'esame", slipLines(row).slice(0, 4)),
			'application/pdf'
		);
	})
];
