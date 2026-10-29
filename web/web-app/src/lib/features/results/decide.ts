import type { Esse3ExamSessionEnrollment } from '@mybicocca/data-api/esse3';
import { errorMessage } from '#lib/data/errorMessage.ts';
import { DataFailure, toDataError } from '#lib/data/errors.ts';
import { careerKey } from '#lib/data/keys.ts';
import { mutate, setQueryData } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { refusalReason } from '#lib/features/exams/writes.ts';
import { publishedResultsQuery } from '#lib/features/services/sources.ts';
import { m } from '#lib/paraglide/messages.js';
import type { Choice, ExamResult } from './model.ts';

const ANSWERS = { accepted: 'A', rejected: 'R' } as const;

/**
 * Accepts or rejects a published grade, which can't be undone. Once Esse3 has it, the result
 * leaves the pending ones at once, answered, and every list reading the bookings reloads.
 *
 * @throws DataFailure When Esse3 refuses or can't be reached; nothing changed then.
 */
export async function decide(
	scope: SessionScope,
	result: ExamResult,
	choice: Choice
): Promise<void> {
	const id = result.applicationListId;
	if (id === null) throw new DataFailure({ kind: 'unknown', detail: 'No applistaId' });
	const answer = ANSWERS[choice];
	/* The bookings' key prefixes the published results'; an accepted grade heads for the record book. */
	const invalidate = [
		careerKey(scope, 'esse3.transcript.getBookingsByMatId'),
		...(choice === 'accepted' ? [careerKey(scope, 'esse3.transcript.getRecordBookRows')] : [])
	];
	try {
		await mutate(
			() =>
				scope.api.esse3.examsCalendar.putAcknowledgmentOfReceiptApplicationList(
					scope.career.enrollmentTraitId,
					id,
					answer
				),
			{ invalidate }
		);
	} catch (error) {
		// A success whose body doesn't read as a booking still recorded the answer: telling the
		// student it failed would invite a second, refused attempt. The reload shows what Esse3 holds.
		if (!(error instanceof DataFailure) || error.error.kind !== 'decode') throw error;
	}
	setQueryData<Esse3ExamSessionEnrollment[]>(publishedResultsQuery(scope).key, (rows = []) =>
		rows.map((row) =>
			row.applicationListId === id ? { ...row, acknowledgmentOfReceipt: answer } : row
		)
	);
}

/** What to tell the student about a decision Esse3 didn't take. */
export function decisionFailure(error: unknown): string {
	const reason = refusalReason(error);
	return reason ? m.results_refused({ reason }) : errorMessage(toDataError(error));
}
