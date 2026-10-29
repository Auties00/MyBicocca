import type { Esse3ExamSessionEnrollment } from '@mybicocca/data-api/esse3';
import { academicYear } from '#lib/data/academicYear.ts';
import { esse3Date, esse3DateTime } from '#lib/data/esse3/dates.ts';
import { bookingGrade, type ExamGrade } from '#lib/data/esse3/grades.ts';
import { callLabel, callSlug } from '#lib/features/exams/model.ts';
import { italianTitleCase } from '#lib/format/names.ts';
import { requiresDecision } from '#lib/features/services/deadlines.ts';
import { calendarDays, toDate } from '#lib/format/dates.ts';

/** What the student answered to a published grade (`presaVisione` A or R). */
export type Choice = 'accepted' | 'rejected';

export interface ExamResult {
	/** The booking id (`applistaId`), else the booking's slug: the detail's URL segment. */
	id: string;
	/** The booking's slug on the exams pages (`cdsId-adId-appId`). */
	booking: string;
	/** What accepting and rejecting address; without it the outcome can't be acted on. */
	applicationListId: number | null;
	/** Title-cased; empty when Esse3 names neither the activity nor the call. */
	title: string;
	/** The activity's code (`adStuCod`), never translated. */
	code: string | null;
	/** The call's own name, when it says more than the activity's. */
	callDescription: string | null;
	/** The activity's weight in credits; null when Esse3 doesn't say or says 0. */
	credits: number | null;
	takenOn: Date | null;
	bookedOn: Date | null;
	grade: ExamGrade;
	choice: Choice | null;
	note: string | null;
	/** The last day to reject (`dataRifEsitoStu`); a grade not rejected by then stands. */
	deadline: Date | null;
	/** Waiting for the student's decision, by the rule the deadlines share. */
	pending: boolean;
	/** Days left to decide, 0 on the last day; null unless pending. */
	daysLeft: number | null;
	/** Never answered, and the last day to reject it has passed: the grade stands. */
	lapsed: boolean;
}

function text(value: string | null | undefined): string | null {
	const trimmed = value?.trim();
	return trimmed ? trimmed : null;
}

function day(value: string | null): Date | null {
	const date = esse3Date(value);
	return date === null ? null : toDate(date);
}

function choiceOf(ack: string | null): Choice | null {
	if (ack === 'A') return 'accepted';
	if (ack === 'R') return 'rejected';
	return null;
}

/** A published outcome, or undefined for a row no call key addresses. */
export function toResult(row: Esse3ExamSessionEnrollment, today: Date): ExamResult | undefined {
	const { courseOfStudyId, activityId, callId, applicationListId } = row;
	if (courseOfStudyId === null || activityId === null || callId === null) return undefined;
	const activity = text(row.studentActivityDescription);
	const call = text(row.examCallDescription);
	const name = activity ?? call;
	const shift = esse3DateTime(row.shiftDateTime) ?? esse3Date(row.shiftDateTime);
	const deadline = day(row.studentOutcomeReferenceDate);
	const grade = bookingGrade(row);
	const pending = requiresDecision(row, today);
	const unanswered = row.acknowledgmentOfReceipt === 'N' || row.acknowledgmentOfReceipt === 'V';
	const booking = callSlug({ courseOfStudyId, activityId, callId });
	return {
		id: applicationListId === null ? booking : String(applicationListId),
		booking,
		applicationListId,
		title: name === null ? '' : italianTitleCase(name),
		code: text(row.studentActivityCode),
		callDescription: activity === null ? null : callLabel(call, activity),
		credits: (row.teachingActivityWeight ?? 0) > 0 ? row.teachingActivityWeight : null,
		takenOn: shift === null ? null : toDate(shift),
		bookedOn: day(row.insertionDate),
		grade,
		choice: choiceOf(row.acknowledgmentOfReceipt),
		note: text(row.publicNote),
		deadline,
		pending,
		daysLeft: pending && deadline !== null ? calendarDays(today, deadline) : null,
		lapsed:
			unanswered &&
			applicationListId !== null &&
			decidable(grade) &&
			deadline !== null &&
			calendarDays(today, deadline) < 0
	};
}

export interface ResultsSplit {
	/** The soonest deadline first. */
	pending: ExamResult[];
	/** In Esse3's order: the latest sitting first. */
	archived: ExamResult[];
}

export function splitResults(
	rows: readonly Esse3ExamSessionEnrollment[],
	today: Date
): ResultsSplit {
	const pending: ExamResult[] = [];
	const archived: ExamResult[] = [];
	for (const row of rows) {
		const result = toResult(row, today);
		if (result) (result.pending ? pending : archived).push(result);
	}
	pending.sort((a, b) => (a.daysLeft ?? 0) - (b.daysLeft ?? 0));
	return { pending, archived };
}

/** Whether accepting or rejecting means anything: a grade or a pass, not a failure or an absence. */
export function decidable(grade: ExamGrade): boolean {
	return grade.kind === 'numeric' || grade.kind === 'passed';
}

/** An outcome with no exam behind it: the student didn't show up, or walked out. */
export function absence(grade: ExamGrade): boolean {
	return grade.kind === 'absent' || grade.kind === 'withdrew';
}

/** The student's answer, where the outcome was one to answer. */
export function visibleChoice(result: Pick<ExamResult, 'choice' | 'grade'>): Choice | null {
	return decidable(result.grade) ? result.choice : null;
}

export interface ArchiveYear {
	/** The academic year by its starting year; null for outcomes without a date. */
	year: number | null;
	results: ExamResult[];
}

/** The other sittings of the same activity, by its code, else its name: the latest first. */
export function attempts(result: ExamResult, all: readonly ExamResult[]): ExamResult[] {
	if (result.code === null && result.title === '') return [];
	const same = (other: ExamResult) =>
		result.code !== null ? other.code === result.code : other.title === result.title;
	return all
		.filter((other) => other.id !== result.id && same(other))
		.sort((a, b) => (b.takenOn?.getTime() ?? -Infinity) - (a.takenOn?.getTime() ?? -Infinity));
}

/** The archive by academic year, the latest first; undated outcomes last. */
export function archiveByYear(archived: readonly ExamResult[]): ArchiveYear[] {
	const years = new Map<number | null, ExamResult[]>();
	for (const result of archived) {
		const year = result.takenOn === null ? null : academicYear(result.takenOn);
		const group = years.get(year);
		if (group) group.push(result);
		else years.set(year, [result]);
	}
	return [...years]
		.map(([year, results]) => ({ year, results }))
		.sort((a, b) => (b.year ?? -Infinity) - (a.year ?? -Infinity));
}

export interface ArchiveSummary {
	/** Grades and passes that stand: neither rejected nor failed. */
	passed: number;
	/** Outcomes the examiners failed, and grades the student rejected. */
	notPassed: number;
	absences: number;
}

/** What the archive holds, for the pane beside the list when nothing waits for a decision. */
export function summarizeArchive(archived: readonly ExamResult[]): ArchiveSummary {
	let passed = 0;
	let notPassed = 0;
	let absences = 0;
	for (const result of archived) {
		if (absence(result.grade)) absences++;
		else if (visibleChoice(result) === 'rejected' || result.grade.kind === 'not_passed')
			notPassed++;
		else if (decidable(result.grade)) passed++;
	}
	return { passed, notPassed, absences };
}
