import type {
	Esse3TranscriptAverage,
	Esse3TranscriptRow,
	Esse3TranscriptStats
} from '@mybicocca/data-api/esse3';
import type { LocalDate } from '@mybicocca/data-api/runtime';
import { esse3Date } from '#lib/data/esse3/dates.ts';
import { recordGrade } from '#lib/data/esse3/grades.ts';

export type RowState = 'passed' | 'attended' | 'planned';

/** A grade out of thirty; `cumLaude` only with 30. */
export interface Grade {
	value: number;
	cumLaude: boolean;
}

/** One activity of the record book (libretto), as the profile shows it. */
export interface LibrettoRow {
	/** `adsceId`: what the per-row calls take. */
	id: number;
	code: string | null;
	name: string;
	/** The degree year it belongs to; 0 for year-less activities (prerequisites). */
	year: number;
	credits: number;
	state: RowState;
	/** Null for activities judged without a grade (idoneità) and for those not passed. */
	grade: Grade | null;
	date: LocalDate | null;
	academicYear: number | null;
	inStudyPlan: boolean;
	bookableCalls: number;
}

export function toLibrettoRow(row: Esse3TranscriptRow): LibrettoRow {
	return {
		id: row.activityChoiceId,
		code: row.activityCode?.trim() || null,
		name: row.activityDescription.trim(),
		year: row.courseYear,
		credits: row.weight,
		state: row.state === 'S' ? 'passed' : row.state === 'F' ? 'attended' : 'planned',
		grade: recordGrade(row.outcome),
		date: esse3Date(row.outcome?.graduationDate),
		academicYear: row.outcome?.academicYearSupervisorId ?? row.academicYearAttendanceId,
		// Year-less rows (prerequisites, OFA) never belong to a plan but are part of the career.
		inStudyPlan: row.planId !== null || row.courseYear <= 0,
		bookableCalls: row.bookableCallsNumber ?? 0
	};
}

/** Oldest exam first, the undated ones last. */
function byDate(a: LibrettoRow, b: LibrettoRow): number {
	const left = a.date?.toString();
	const right = b.date?.toString();
	if (left === right) return 0;
	if (left === undefined) return 1;
	if (right === undefined) return -1;
	return left < right ? -1 : 1;
}

/**
 * The study-plan activities, oldest exam first and the undated ones last: the order the trend
 * chart plots them in and the libretto starts from.
 */
export function studyPlanRows(rows: readonly LibrettoRow[]): LibrettoRow[] {
	return rows.filter((row) => row.inStudyPlan).sort((a, b) => byDate(a, b) || a.id - b.id);
}

/** What the simulator projects from: the graded exams Esse3 averages over. */
export interface GradeRollup {
	gradedExams: number;
	gradeSum: number;
	weightedGradeSum: number;
	gradedCredits: number;
}

/**
 * Sums the passed, graded rows of the whole career, supernumerary ones included, as Esse3's
 * averages count them; 30 cum laude counts as 30.
 */
export function gradeRollup(rows: readonly LibrettoRow[]): GradeRollup {
	const rollup: GradeRollup = {
		gradedExams: 0,
		gradeSum: 0,
		weightedGradeSum: 0,
		gradedCredits: 0
	};
	for (const row of rows) {
		if (row.state !== 'passed' || row.grade === null) continue;
		rollup.gradedExams++;
		rollup.gradeSum += row.grade.value;
		rollup.weightedGradeSum += row.grade.value * row.credits;
		rollup.gradedCredits += row.credits;
	}
	return rollup;
}

/** The career's numbers, all Esse3's own: the app computes no average of its own. */
export interface CareerStats {
	passedCredits: number;
	totalCredits: number;
	arithmeticAverage: number | null;
	weightedAverage: number | null;
	/** The weighted average on the graduation base of 110, when Esse3 sends it. */
	weightedAverage110: number | null;
	passedExams: number;
	plannedExams: number;
}

function averageOf(
	averages: readonly Esse3TranscriptAverage[],
	type: 'A' | 'P',
	base: 30 | 110
): number | null {
	const ofType = averages.filter((average) => average.averageTypeCode === type);
	const exact = ofType.find((average) => average.base === base);
	if (base === 110) return exact?.average ?? null;
	return (exact ?? ofType[0])?.average ?? null;
}

export function careerStats(stats: Esse3TranscriptStats): CareerStats {
	return {
		passedCredits: stats.passedMeasurementUnitWeight ?? 0,
		totalCredits: stats.minMeasurementUnitWeight ?? stats.maxMeasurementUnitWeight ?? 0,
		arithmeticAverage: averageOf(stats.averages, 'A', 30),
		weightedAverage: averageOf(stats.averages, 'P', 30),
		weightedAverage110: averageOf(stats.averages, 'P', 110),
		passedExams: stats.passedTeachingActivityNumber ?? 0,
		// numAdPianificate drops to 0 once everything is passed or attended, so it can't be the total.
		plannedExams: stats.studyPlanTeachingActivityNumber ?? stats.bookletTeachingActivityNumber ?? 0
	};
}

/** `current` of `total`, between 0 and 1. */
export function progress(current: number, total: number): number {
	return total > 0 ? Math.min(1, Math.max(0, current / total)) : 0;
}

/** A passed, graded exam on the trend chart. */
export interface TrendPoint {
	row: LibrettoRow;
	grade: Grade;
	date: LocalDate;
	/** 18 to 31: 30 cum laude plots one step above 30. */
	value: number;
}

/** The graded exams in the order they were passed; ties keep the record book's order. */
export function trendPoints(rows: readonly LibrettoRow[]): TrendPoint[] {
	const points: TrendPoint[] = [];
	for (const row of rows) {
		if (row.state !== 'passed' || row.grade === null || row.date === null) continue;
		points.push({
			row,
			grade: row.grade,
			date: row.date,
			value: row.grade.cumLaude ? 31 : row.grade.value
		});
	}
	return points.sort((a, b) => {
		const left = a.date.toString();
		const right = b.date.toString();
		return left === right ? 0 : left < right ? -1 : 1;
	});
}

/** The rows of one degree year, with what they add up to. */
export interface YearGroup {
	/** 0 for the year-less activities. */
	year: number;
	rows: LibrettoRow[];
	passed: number;
	credits: number;
	passedCredits: number;
}

const collator = new Intl.Collator('it', { sensitivity: 'base', numeric: true });

/**
 * Rows by degree year, in year order; within a year the passed first, then by name. By `date`,
 * the passed come in the order they were passed instead, as on a paper libretto.
 */
export function groupByYear(
	rows: readonly LibrettoRow[],
	order: 'name' | 'date' = 'name'
): YearGroup[] {
	const sorted = [...rows].sort(
		(a, b) =>
			a.year - b.year ||
			Number(b.state === 'passed') - Number(a.state === 'passed') ||
			(order === 'date' && a.state === 'passed' ? byDate(a, b) : 0) ||
			collator.compare(a.name, b.name)
	);
	const groups: YearGroup[] = [];
	for (const row of sorted) {
		let group = groups.at(-1);
		if (group?.year !== row.year) {
			group = { year: row.year, rows: [], passed: 0, credits: 0, passedCredits: 0 };
			groups.push(group);
		}
		group.rows.push(row);
		group.credits += row.credits;
		if (row.state === 'passed') {
			group.passed++;
			group.passedCredits += row.credits;
		}
	}
	return groups;
}

/** What the exam calls page groups calls by: the activity code, else its name. */
export function courseKey(row: Pick<LibrettoRow, 'code' | 'name'>): string {
	return row.code ?? row.name;
}

/** When the oldest of several loads happened, once all of them have. */
export function loadedAt(...times: (number | undefined)[]): Date | undefined {
	const known = times.filter((time) => time !== undefined);
	return known.length === times.length ? new Date(Math.min(...known)) : undefined;
}
