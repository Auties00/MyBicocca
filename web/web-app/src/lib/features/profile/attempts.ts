import type { Esse3Result, Esse3TranscriptTest } from '@mybicocca/data-api/esse3';
import type { LocalDate } from '@mybicocca/data-api/runtime';
import { esse3Date } from '#lib/data/esse3/dates.ts';

/** One outcome of an attempt: the final one, the written one or a partial test's. */
export interface AttemptOutcome {
	passed: boolean;
	/** Partial tests may carry decimals. */
	grade: number | null;
	cumLaude: boolean;
	judgment: string | null;
	date: LocalDate | null;
}

/** One sitting of an exam ("prova") recorded on a libretto row. */
export interface Attempt {
	id: number;
	callDate: LocalDate | null;
	session: string | null;
	status: string | null;
	/** The outcome that tells how it went: the final one, else the written, else the partial. */
	outcome: AttemptOutcome | null;
}

const text = (value: string | null) => value?.trim() || null;

/** An outcome with nothing in it (not passed, no grade, judgment or date) counts as none. */
function toOutcome(result: Esse3Result | null): AttemptOutcome | null {
	if (result === null) return null;
	const outcome: AttemptOutcome = {
		passed: result.supGraduationFlag === 1,
		grade: result.evaluationModeCode === 'V' ? result.grade : null,
		cumLaude: result.cumLaudeFlag === 1,
		judgment: text(result.judgmentTypeDescription),
		date: esse3Date(result.graduationDate)
	};
	const empty =
		!outcome.passed && outcome.grade === null && outcome.judgment === null && outcome.date === null;
	return empty ? null : outcome;
}

export function toAttempt(test: Esse3TranscriptTest): Attempt {
	return {
		id: test.activityRegulationId,
		callDate: esse3Date(test.callDate),
		session: text(test.sessionDescription),
		status: text(test.regulationStatusDescription),
		outcome:
			toOutcome(test.finalOutcome) ??
			toOutcome(test.writingOutcome) ??
			toOutcome(test.partialOutcome)
	};
}

/** The attempts, most recent first; undated ones last. */
export function attempts(tests: readonly Esse3TranscriptTest[]): Attempt[] {
	return tests.map(toAttempt).sort((a, b) => {
		const left = a.callDate?.toString();
		const right = b.callDate?.toString();
		if (left === right) return 0;
		if (left === undefined) return 1;
		if (right === undefined) return -1;
		return left < right ? 1 : -1;
	});
}

export type OutcomeLabel =
	| { kind: 'laude' }
	| { kind: 'grade'; value: number }
	| { kind: 'judgment'; text: string }
	| { kind: 'passed' };

/** What an attempt's trailing label says; the first that applies wins. */
export function outcomeLabel(outcome: AttemptOutcome | null): OutcomeLabel | null {
	if (outcome === null) return null;
	if (outcome.cumLaude) return { kind: 'laude' };
	if (outcome.grade !== null) return { kind: 'grade', value: outcome.grade };
	if (outcome.judgment !== null) return { kind: 'judgment', text: outcome.judgment };
	return outcome.passed ? { kind: 'passed' } : null;
}

export type PrerequisiteStatus = 'satisfied' | 'unsatisfied' | 'unknown';

/** Esse3's prerequisite check: `esito` 1 is satisfied, anything else is not. */
export function prerequisiteStatus(outcome: number | null): PrerequisiteStatus {
	return outcome === 1 ? 'satisfied' : 'unsatisfied';
}
