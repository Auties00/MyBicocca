import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import IconCheck from '~icons/ph/check-bold';
import IconCircleDashed from '~icons/ph/circle-dashed';
import IconClock from '~icons/ph/clock';
import IconWarning from '~icons/ph/warning-bold';
import type { DataError } from '#lib/data/errors.ts';
import { errorMessage } from '#lib/data/errorMessage.ts';
import { formatCredits, formatNumber } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import type { Icon, Tone } from '#lib/ui/types.ts';
import type { PrerequisiteStatus } from '../attempts.ts';
import { progress, type LibrettoRow, type YearGroup } from '../transcript.ts';

/** "Anno 2", or "Prerequisiti" for the year-less activities. */
export function yearTitle(year: number): string {
	return year <= 0 ? m.profile_prerequisites() : m.profile_year({ year });
}

/** The libretto's fragment for a degree year. */
export function yearAnchor(year: number): string {
	return `year-${year}`;
}

/** "67 su 180 CFU". */
export function creditsOf(passed: number, total: number): string {
	return m.profile_credits_of({
		passed: formatNumber(passed, { maximumFractionDigits: 1 }),
		total: formatCredits(total)
	});
}

/** "7 su 7 esami · 51 su 51 CFU"; years worth no credits (prerequisites) leave them out. */
export function yearSummary(group: YearGroup): string {
	const exams = m.profile_year_exams({ passed: group.passed, total: group.rows.length });
	return group.credits > 0 ? `${exams} · ${creditsOf(group.passedCredits, group.credits)}` : exams;
}

/** How far into a year the student is: by credits, or by exams for years worth none. */
export function yearProgress(group: YearGroup): number {
	return group.credits > 0
		? progress(group.passedCredits, group.credits)
		: progress(group.passed, group.rows.length);
}

export interface RowStatus {
	icon: Icon;
	tone: Tone;
	label: string;
}

/** Where a libretto row stands: unmet prerequisites outrank attendance, which outranks the plan. */
export function rowStatus(
	row: LibrettoRow,
	prerequisites: Record<number, PrerequisiteStatus> | undefined
): RowStatus {
	if (row.state === 'passed')
		return { icon: IconCheck, tone: 'success', label: m.profile_passed() };
	if (prerequisites?.[row.id] === 'unsatisfied') {
		return { icon: IconWarning, tone: 'warning', label: m.profile_course_prereq_unmet_short() };
	}
	if (row.state === 'attended') {
		return { icon: IconClock, tone: 'info', label: m.profile_attending() };
	}
	return { icon: IconCircleDashed, tone: 'neutral', label: m.profile_pending() };
}

/** What the libretto's rows show on the right. */
export type LibrettoView = 'grades' | 'credits';

/** A course of the libretto, keeping the list's view for the list beside it. */
export function courseHref(row: Pick<LibrettoRow, 'id'>, view: LibrettoView): ResolvedPathname {
	return view === 'credits'
		? resolve(`profile/exams/${row.id}?view=credits`)
		: resolve(`profile/exams/${row.id}`);
}

/** Why the libretto on screen could not be updated: offline says so, anything else is generic. */
export function staleMessage(error: DataError): string {
	return error.kind === 'offline' ? errorMessage(error) : m.profile_refresh_error();
}
