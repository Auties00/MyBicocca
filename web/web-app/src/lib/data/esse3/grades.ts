import { decodeInteger, type JsonObject } from '@mybicocca/data-api/runtime';

/** An exam outcome as Esse3 publishes it. */
export type ExamGrade =
	/** Out of thirty, 18 to 30; `cumLaude` only with 30. */
	| { kind: 'numeric'; value: number; cumLaude: boolean }
	/** A pass with no grade (idoneità). */
	| { kind: 'passed' }
	| { kind: 'not_passed' }
	| { kind: 'withdrew' }
	| { kind: 'absent' }
	/** Missing, or not published yet. */
	| { kind: 'unknown' };

const UNKNOWN: ExamGrade = { kind: 'unknown' };

/**
 * Decodes Esse3's untyped `esito` of a booking or a published result. Absence and withdrawal win
 * over the grade, which wins over the bare pass flag. A `votoEsa` of 0 is no grade: Esse3 sends
 * it on failed outcomes, with `superatoFlg=0`. 31 stands for 30 cum laude, as does `lodeFlg`.
 */
export function outcomeGrade(outcome: JsonObject | null | undefined): ExamGrade {
	if (outcome === null || outcome === undefined) return UNKNOWN;
	const grade = decodeInteger(outcome.votoEsa);
	const passed = decodeInteger(outcome.superatoFlg);
	if (decodeInteger(outcome.assenteFlg) === 1) return { kind: 'absent' };
	if (decodeInteger(outcome.ritiratoFlg) === 1) return { kind: 'withdrew' };
	if (grade !== undefined && grade > 0) {
		return {
			kind: 'numeric',
			value: Math.min(grade, 30),
			cumLaude: grade >= 31 || (grade === 30 && decodeInteger(outcome.lodeFlg) === 1)
		};
	}
	if (passed === 1) return { kind: 'passed' };
	if (passed === 0) return { kind: 'not_passed' };
	return UNKNOWN;
}

/**
 * A booking's grade: only once published (`pubblId`), since unpublished bookings carry
 * `superatoFlg=0` and would read as failed.
 */
export function bookingGrade(booking: {
	publicationId: number | null;
	outcome: JsonObject | null;
}): ExamGrade {
	return booking.publicationId === null ? UNKNOWN : outcomeGrade(booking.outcome);
}

/**
 * A record-book row's grade, or null for activities judged without one (idoneità, mode G):
 * Esse3 fills the grade only under evaluation mode `V`.
 */
export function recordGrade(
	outcome: {
		evaluationModeCode: string | null;
		grade: number | null;
		cumLaudeFlag: number | null;
	} | null
): { value: number; cumLaude: boolean } | null {
	if (outcome === null || outcome.evaluationModeCode !== 'V' || outcome.grade === null) return null;
	const value = Math.trunc(outcome.grade);
	return { value, cumLaude: value === 30 && outcome.cumLaudeFlag === 1 };
}

/**
 * The points a grade weighs in averages: 30 cum laude counts as 30, as Esse3's own averages
 * do; charts that plot the laude one step higher pass `laude: 31`.
 */
export function gradePoints(
	grade: { value: number; cumLaude: boolean },
	options: { laude?: number } = {}
): number {
	return grade.cumLaude ? (options.laude ?? 30) : grade.value;
}
