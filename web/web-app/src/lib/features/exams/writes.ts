import type {
	Esse3ExamSessionEnrollment,
	Esse3ExamSessionTranscript
} from '@mybicocca/data-api/esse3';
import { errorMessage } from '#lib/data/errorMessage.ts';
import { DataFailure, toDataError } from '#lib/data/errors.ts';
import { mutate, setQueryData } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { m } from '#lib/paraglide/messages.js';
import type { Booking, CallKey, ExamCall } from './model.ts';
import { outcomes } from './outcomes.svelte.ts';
import { bookingsQuery, examCallsQuery, writeInvalidations } from './queries.ts';

const sameCall = (
	row: Pick<Esse3ExamSessionEnrollment, 'courseOfStudyId' | 'activityId' | 'callId'>,
	key: CallKey
) =>
	row.courseOfStudyId === key.courseOfStudyId &&
	row.activityId === key.activityId &&
	row.callId === key.callId;

/**
 * Books `call` for its record-book row. Esse3 answers with no body: the call is recorded as booked
 * for the page to report, and leaves the bookable ones at once; the booking itself shows up when
 * the lists reload.
 *
 * @throws DataFailure When Esse3 refuses or can't be reached.
 */
export async function bookCall(scope: SessionScope, call: ExamCall, note: string): Promise<void> {
	const { key, activityChoiceId } = call;
	if (activityChoiceId === null) throw new DataFailure({ kind: 'unknown', detail: 'No adsceId' });
	await mutate(
		() =>
			scope.api.esse3.examsCalendar.postExamCallEnrolledList(
				key.courseOfStudyId,
				key.activityId,
				key.callId,
				{
					activityChoiceId,
					studentNote: note.trim() || null,
					studentEnrollmentType: null,
					callLogId: null,
					tagCode: null,
					actorCode: null,
					examType: null,
					compensatoryMeasures: []
				}
			),
		{ invalidate: writeInvalidations(scope, key) }
	);
	outcomes.markBooked(scope, call);
	setQueryData<Esse3ExamSessionTranscript[]>(examCallsQuery(scope).key, (rows = []) =>
		rows.filter((row) => !sameCall(row, key))
	);
}

/**
 * Cancels `booking`: it is recorded as cancelled for the page to report, and leaves the bookings
 * at once.
 *
 * @throws DataFailure When Esse3 refuses or can't be reached.
 */
export async function cancelBooking(scope: SessionScope, booking: Booking): Promise<void> {
	const { key, studentId } = booking;
	if (studentId === null) throw new DataFailure({ kind: 'unknown', detail: 'No stuId' });
	await mutate(
		() =>
			scope.api.esse3.examsCalendar.deleteExamCallEnrolledList(
				key.courseOfStudyId,
				key.activityId,
				key.callId,
				studentId
			),
		{ invalidate: writeInvalidations(scope, key) }
	);
	outcomes.markCancelled(scope, booking);
	setQueryData<Esse3ExamSessionEnrollment[]>(bookingsQuery(scope).key, (rows = []) =>
		rows.filter((row) => !sameCall(row, key))
	);
}

/**
 * The reason Esse3 gave for refusing a write ("È necessario compilare il questionario…"), in its
 * own Italian words: business refusals come as 4xx with a `retErrMsg`.
 */
export function refusalReason(error: unknown): string | undefined {
	const failure = toDataError(error);
	if (failure.kind !== 'rejected' && failure.kind !== 'forbidden') return undefined;
	return failure.service === 'esse3' && failure.message ? failure.message : undefined;
}

/** What to tell the user about a failed booking or cancellation. */
export function writeFailure(error: unknown): string {
	const reason = refusalReason(error);
	return reason ? m.exams_refused({ reason }) : errorMessage(toDataError(error));
}

/** Esse3 gates some bookings on the teaching questionnaire, and says so in its refusal. */
export function mentionsQuestionnaire(error: unknown): boolean {
	return /questionari/i.test(refusalReason(error) ?? '');
}
