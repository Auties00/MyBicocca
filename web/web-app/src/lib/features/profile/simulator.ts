import type { GradeRollup } from './transcript.ts';

export type AverageKind = 'arithmetic' | 'weighted';

/** Below it an exam is not passed. */
export const MIN_GRADE = 18;
/** 30 cum laude, which the simulator counts as 31 (the trend chart plots it there too). */
export const LAUDE_GRADE = 31;

export type GradeInput =
	{ kind: 'empty' } | { kind: 'low' } | { kind: 'high' } | { kind: 'valid'; value: number };

/** The grade field as typed: digits only, 18 to 31 where 31 is 30 cum laude. */
export function parseGrade(text: string): GradeInput {
	const value = parseDigits(text);
	if (value === null) return { kind: 'empty' };
	if (value < MIN_GRADE) return { kind: 'low' };
	if (value > LAUDE_GRADE) return { kind: 'high' };
	return { kind: 'valid', value };
}

export type CreditsInput = { kind: 'empty' } | { kind: 'zero' } | { kind: 'valid'; value: number };

export function parseCredits(text: string): CreditsInput {
	const value = parseDigits(text);
	if (value === null) return { kind: 'empty' };
	return value === 0 ? { kind: 'zero' } : { kind: 'valid', value };
}

function parseDigits(text: string): number | null {
	const trimmed = text.trim();
	return /^\d{1,2}$/.test(trimmed) ? Number(trimmed) : null;
}

/** What the projection starts from: Esse3's average and the local counts behind it. */
export interface SimulationBase {
	kind: AverageKind;
	/** Esse3's average of that kind, out of thirty. */
	current: number | null;
	rollup: GradeRollup | null;
}

/**
 * The average after one more exam with `grade` (and `credits`, weighted only). Anchored on
 * Esse3's average, with the local rollup giving only how many exams (n) or credits (C) it spans,
 * so a grade above the average always raises it, as the tile shows it:
 * arithmetic (current·n + g) / (n + 1), weighted (current·C + g·cfu) / (C + cfu).
 */
export function projectAverage(
	base: SimulationBase,
	grade: number,
	credits?: number
): number | null {
	const { current, rollup } = base;
	if (current === null || rollup === null) return null;
	if (base.kind === 'arithmetic') {
		const n = rollup.gradedExams;
		return (current * n + grade) / (n + 1);
	}
	if (credits === undefined || credits <= 0) return null;
	const total = rollup.gradedCredits;
	return (current * total + grade * credits) / (total + credits);
}

export type RequiredGrade =
	/** Even the lowest passing grade gets there. */
	| { kind: 'any' }
	/** The lowest grade that gets there, 18 to 30; 31 means 30 cum laude. */
	| { kind: 'grade'; value: number }
	/** Not with one exam, even with 30 cum laude. */
	| { kind: 'unreachable' };

/**
 * The reverse question: the lowest grade of the next exam that brings the average to `target`,
 * inverting `projectAverage`: g = target·(n + 1) − current·n, or (target·(C + cfu) − current·C) / cfu.
 */
export function requiredGrade(
	base: SimulationBase,
	target: number,
	credits?: number
): RequiredGrade | null {
	const { current, rollup } = base;
	if (current === null || rollup === null) return null;
	let exact: number;
	if (base.kind === 'arithmetic') {
		const n = rollup.gradedExams;
		exact = target * (n + 1) - current * n;
	} else {
		if (credits === undefined || credits <= 0) return null;
		const total = rollup.gradedCredits;
		exact = (target * (total + credits) - current * total) / credits;
	}
	// Averages carry two decimals: forgive the float noise of the inversion.
	const needed = Math.ceil(exact - 1e-9);
	if (needed <= MIN_GRADE) return { kind: 'any' };
	if (needed > LAUDE_GRADE) return { kind: 'unreachable' };
	return { kind: 'grade', value: needed };
}

/** A target average as typed: "28", "27,5" or "27.50", within the passing range. */
export function parseTarget(text: string): number | null {
	const normalized = text.trim().replace(',', '.');
	if (!/^\d{1,2}(\.\d{0,2})?$/.test(normalized)) return null;
	const value = Number(normalized);
	return value >= MIN_GRADE && value <= 30 ? value : null;
}
