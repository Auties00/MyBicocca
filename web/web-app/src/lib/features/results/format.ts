import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import type { ExamGrade } from '#lib/data/esse3/grades.ts';
import { capitalize } from '#lib/format/names.ts';
import { formatDate } from '#lib/format/dates.ts';
import { formatGrade } from '#lib/format/grades.ts';
import { cached, intlLocale } from '#lib/format/locale.ts';
import { formatCredits } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import type { Fact } from '#lib/ui/Facts.svelte';
import type { Status } from '#lib/ui/types.ts';
import { visibleChoice, type ExamResult } from './model.ts';

export function resultHref(id: string): ResolvedPathname {
	return resolve('/(app)/services/results/[id]', { id });
}

export function resultTitle(result: Pick<ExamResult, 'title'>): string {
	return result.title || m.results_exam_fallback();
}

const SENTENCE_STYLES = {
	dayMonth: { day: 'numeric', month: 'short' },
	medium: { dateStyle: 'medium' },
	long: { dateStyle: 'long' }
} satisfies Record<string, Intl.DateTimeFormatOptions>;

type SentenceStyle = keyof typeof SENTENCE_STYLES;

/**
 * A date as the messages need it after an article: whole, its day of the month (Italian elides
 * the article before 8 and 11), and without the day, since Italian writes "il 1° novembre".
 */
export function sentenceDate(
	date: Date,
	style: SentenceStyle
): { date: string; day: number; month: string } {
	const tag = intlLocale();
	const options = SENTENCE_STYLES[style];
	const format = cached('date', tag, options, () => new Intl.DateTimeFormat(tag, options));
	const parts = format.formatToParts(date);
	const at = parts.findIndex((part) => part.type === 'day');
	/* The day leaves with the separator next to it: "1 novembre", "Nov 1". */
	const gap = parts[at + 1]?.type === 'literal' ? at + 1 : at - 1;
	const month = parts
		.filter((_, index) => index !== at && index !== gap)
		.map((part) => part.value)
		.join('');
	return { date: format.format(date), day: date.getDate(), month };
}

/** "Sostenuto l’11 giu 2026", "Sostenuto il 1° giu 2026". */
export function takenOnLine(date: Date): string {
	return m.results_taken_on(sentenceDate(date, 'medium'));
}

/** "Decidi entro il 1° nov", or with the month in full and the year. */
export function decideByLine(date: Date, style: 'dayMonth' | 'long' = 'dayMonth'): string {
	return m.results_decide_by(sentenceDate(date, style));
}

/** "Non rifiutato entro l’8 ottobre 2026: il voto resta valido." */
export function lapsedLine(deadline: Date): string {
	return m.results_lapsed_on(sentenceDate(deadline, 'long'));
}

/** A grade in words for sentences: "27 su 30", "30 e lode", "Idoneo". */
export function gradeInWords(grade: ExamGrade): string {
	return grade.kind === 'numeric' && !grade.cumLaude
		? m.results_out_of_thirty({ grade: grade.value })
		: formatGrade(grade, { full: true });
}

/** "Voto: 30 e lode", for a grade shown as a numeral or a glyph. */
export function spokenGrade(grade: ExamGrade): string {
	return m.results_grade({ grade: formatGrade(grade, { full: true }) });
}

/** The line under the result's title: what to do by when, else when it was taken. */
export function detailSubtitle(result: ExamResult): string {
	if (result.pending && result.deadline) return decideByLine(result.deadline);
	if (result.takenOn) return takenOnLine(result.takenOn);
	return capitalize(gradeInWords(result.grade));
}

/** What the result's page tells beside the grade, leaving out what its subtitle already says. */
export function resultFacts(result: ExamResult): Fact[] {
	const facts: Fact[] = [];
	if (result.pending && result.takenOn) {
		facts.push({
			label: m.results_fact_taken_on(),
			value: capitalize(formatDate(result.takenOn, 'full'))
		});
	}
	if (result.callDescription) {
		facts.push({ label: m.results_fact_call(), value: result.callDescription });
	}
	if (result.credits !== null) {
		facts.push({ label: m.results_fact_credits(), value: formatCredits(result.credits) });
	}
	if (result.bookedOn) {
		facts.push({ label: m.results_fact_booked_on(), value: formatDate(result.bookedOn, 'long') });
	}
	return facts;
}

/** The archive row's second line: the answer, or the outcome, with the day of the exam. */
export function archiveLine(result: ExamResult): string {
	const { takenOn } = result;
	const date = takenOn ? formatDate(takenOn, 'medium') : null;
	const choice = visibleChoice(result);
	const lead =
		choice === 'accepted'
			? m.results_choice_accepted()
			: choice === 'rejected'
				? m.results_choice_rejected()
				: result.grade.kind === 'numeric' || result.grade.kind === 'unknown'
					? null
					: formatGrade(result.grade, { full: true });
	if (lead === null) return takenOn ? takenOnLine(takenOn) : '';
	return date ? `${lead} · ${date}` : lead;
}

/** Where the result stands, as a status chip says it; undefined for a plain published grade. */
export function resultStatus(result: ExamResult): { status: Status; label: string } | undefined {
	if (result.pending) return { status: 'pending', label: m.results_status_pending() };
	const choice = visibleChoice(result);
	if (choice === 'accepted') return { status: 'success', label: m.results_choice_accepted() };
	if (choice === 'rejected') return { status: 'danger', label: m.results_choice_rejected() };
	if (result.lapsed) return { status: 'neutral', label: m.results_lapsed() };
	switch (result.grade.kind) {
		case 'passed':
			return { status: 'success', label: formatGrade(result.grade, { full: true }) };
		case 'not_passed':
			return { status: 'danger', label: formatGrade(result.grade, { full: true }) };
		case 'withdrew':
		case 'absent':
			return { status: 'neutral', label: formatGrade(result.grade, { full: true }) };
		default:
			return undefined;
	}
}

/** An attempt beside the open result, named by its day: "6 luglio 2025", else the call's name. */
export function attemptTitle(result: ExamResult): string {
	if (result.takenOn) return formatDate(result.takenOn, 'long');
	return result.callDescription ?? resultTitle(result);
}
