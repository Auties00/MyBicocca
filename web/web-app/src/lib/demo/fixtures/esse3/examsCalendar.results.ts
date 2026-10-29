import { esse3Date } from '#lib/data/esse3/dates.ts';
import { calendarDays, toDate } from '#lib/format/dates.ts';
import { DemoStudent } from '../../persona.ts';
import { esse3Error, json, route } from '../../router.ts';
import { demoBookings } from './transcript.exams.ts';

const PRESA_VISIONE =
	'https://s3w.si.unimib.it/e3rest/api/calesa-service-v1/prenotazioni/:matId/:applistaId/presaVisione';

/**
 * Accepting or rejecting a published grade, with the checks Esse3 makes: the booking must be the
 * student's, published, still unanswered and within its deadline. The answer stays for the demo,
 * so the result moves to the archive and the deadlines drop it.
 */
export const routes = [
	route(`PUT ${PRESA_VISIONE}`, async (request) => {
		if (Number(request.params.matId) !== DemoStudent.career.enrollmentTraitId) {
			return esse3Error(404, 'Libretto non trovato');
		}
		const row = demoBookings().find(
			(booking) => String(booking.applistaId) === request.params.applistaId
		);
		if (!row || row.pubblId === null) return esse3Error(404, 'Prenotazione non trovata');
		const answer = (await request.form()).get('presaVisione');
		if (answer !== 'A' && answer !== 'R') {
			return esse3Error(400, 'Valore di presa visione non valido');
		}
		if (row.presaVisione === 'A' || row.presaVisione === 'R') {
			return esse3Error(422, "L'esito è già stato accettato o rifiutato.");
		}
		const deadline = esse3Date(
			typeof row.dataRifEsitoStu === 'string' ? row.dataRifEsitoStu : null
		);
		if (deadline === null || calendarDays(new Date(), toDate(deadline)) < 0) {
			return esse3Error(422, "Il termine per accettare o rifiutare l'esito è scaduto.");
		}
		row.presaVisione = answer;
		return json(row);
	})
];
