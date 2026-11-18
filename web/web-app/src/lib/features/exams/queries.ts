import type {
	Esse3ExamSessionEnrollment,
	Esse3ExamSessionTranscript
} from '@mybicocca/data-api/esse3';
import { careerKey } from '#lib/data/keys.ts';
import { policies, type QueryKey, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { CallKey } from './model.ts';

/**
 * Fields `/prenotazioni` returns only on request. With `attoreCod=STU` and no date filter the call
 * is about four times faster and brings the whole history, upcoming and past, in one request.
 */
const BOOKING_FIELDS =
	'adStuDes,desAppello,aulaDes,edificioDes,dataInizioApp,dataOraTurno,tipoEsaCod,tipoAppCod,' +
	'dataFineIscr,esito,pubblId,notaPubbl';

/**
 * Fields `/appelli` returns only on request: with them the list carries everything the call page
 * shows, so no per-call request (1.7–4 s each) is ever needed.
 */
const CALL_FIELDS =
	'dataInizioApp,oraEsa,dataInizioIscr,dataFineIscr,tipoEsaCod,note,presidenteNome,' +
	'presidenteCognome,presidenteId,tipoGestPrenDes,numIscritti';

/** Every booking of the career's record book, past ones included. */
export function bookingsQuery(scope: SessionScope): QueryOptions<Esse3ExamSessionEnrollment[]> {
	const matId = scope.career.enrollmentTraitId;
	return {
		key: careerKey(scope, 'esse3.transcript.getBookingsByMatId', matId),
		load: () =>
			scope.api.esse3.transcript.getBookingsByMatId(matId, {
				actorCode: 'STU',
				order: '-dataOraTurno',
				optionalFields: BOOKING_FIELDS
			}),
		policy: policies.live
	};
}

/** The calls the student can book now or later; Esse3 leaves out the ones already booked. */
export function examCallsQuery(scope: SessionScope): QueryOptions<Esse3ExamSessionTranscript[]> {
	const matId = scope.career.enrollmentTraitId;
	return {
		key: careerKey(scope, 'esse3.transcript.getRecordBookExamCalls', matId),
		load: () =>
			scope.api.esse3.transcript.getRecordBookExamCalls(matId, {
				q: 'APPELLI_PRENOTABILI_E_FUTURI',
				order: '+dataInizioApp',
				optionalFields: CALL_FIELDS
			}),
		policy: policies.live
	};
}

/** How many students a call has: a slow request (0.3–5 s), made only where the count shows. */
export function enrolledCountQuery(scope: SessionScope, key: CallKey): QueryOptions<number | null> {
	return {
		key: careerKey(
			scope,
			'esse3.examsCalendar.getExamCall',
			key.courseOfStudyId,
			key.activityId,
			key.callId,
			'numIscritti'
		),
		load: async () =>
			(
				await scope.api.esse3.examsCalendar.getExamCall(
					key.courseOfStudyId,
					key.activityId,
					key.callId,
					{ optionalFields: 'numIscritti' }
				)
			).enrolledNumber,
		policy: policies.live
	};
}

/**
 * What a booking or a cancellation changes: both lists (the published results' key extends the
 * bookings' one) and the call's count. The calendar, the deadlines and Today read the same entries.
 */
export function writeInvalidations(scope: SessionScope, key: CallKey): QueryKey[] {
	return [
		careerKey(scope, 'esse3.transcript.getBookingsByMatId'),
		careerKey(scope, 'esse3.transcript.getRecordBookExamCalls'),
		careerKey(
			scope,
			'esse3.examsCalendar.getExamCall',
			key.courseOfStudyId,
			key.activityId,
			key.callId
		)
	];
}
