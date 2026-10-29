import type { MoodleQuiz } from '@mybicocca/data-api/elearning';
import { errorMessage } from '#lib/data/errorMessage.ts';
import type { DataError } from '#lib/data/errors.ts';
import { formatDate, formatTime } from '#lib/format/dates.ts';
import { formatNumber } from '#lib/format/numbers.ts';
import { m } from '#lib/paraglide/messages.js';
import {
	closesAt,
	closing,
	closingSoon,
	isCompleted,
	opensAt,
	quizState,
	type QuizGroup,
	type QuizItem,
	type Verdict
} from './model.ts';

/** A grade or a mark with the quiz's decimals, at most two, and none when whole: 7,5. */
export function formatScore(value: number, quiz?: Pick<MoodleQuiz, 'decimalPoints'>): string {
	const decimals = Math.min(2, Math.max(0, quiz?.decimalPoints ?? 2));
	return formatNumber(value, { maximumFractionDigits: decimals });
}

/** A part of a quiz's status line; `tone` marks the part that needs attention. */
export interface StatusPart {
	text: string;
	tone?: 'warning' | 'danger';
}

/**
 * The line under a quiz's name, in parts, as on Android: state, closing, limit, attempts. A
 * closed quiz never done reads as missed, a closing within two days as urgent.
 */
export function quizStatus(item: QuizItem, now: Date): StatusPart[] {
	const { quiz } = item;
	const state = quizState(item, now);
	const opens = opensAt(quiz);
	const closes = closesAt(quiz);
	if (state === 'restricted') return [{ text: m.quiz_state_restricted() }];
	if (state === 'upcoming' && opens) {
		return [
			{ text: m.quiz_opens_on({ date: formatDate(opens, 'dayMonth'), time: formatTime(opens) }) }
		];
	}
	if (state === 'closed' && closes) {
		return [
			{
				text: m.quiz_closed_on({ date: formatDate(closes, 'dayMonth') }),
				tone: isCompleted(item.module) ? undefined : 'danger'
			}
		];
	}
	const parts: StatusPart[] = [
		{ text: state === 'completed' ? m.quiz_state_completed() : m.quiz_state_available() }
	];
	if (closes) {
		parts.push({
			text: closingLabel(closes, now),
			tone: state === 'open' && closingSoon(quiz, now) ? 'warning' : undefined
		});
	}
	if ((quiz.timeLimit ?? 0) > 0) {
		parts.push({ text: m.quiz_minutes_short({ count: Math.floor((quiz.timeLimit ?? 0) / 60) }) });
	}
	if (quiz.attempts === 1) parts.push({ text: m.quiz_single_attempt() });
	return parts;
}

export function quizStatusLine(item: QuizItem, now: Date): string {
	return quizStatus(item, now)
		.map((part) => part.text)
		.join(' · ');
}

/** A group's heading: its section's name, "Section N" when unnamed, or what the group gathers. */
export function groupTitle(group: QuizGroup): string {
	switch (group.kind) {
		case 'other':
			return m.quiz_group_other();
		case 'all':
			return m.quiz_group_course();
		case 'section':
			return group.title ?? m.quiz_section_fallback({ number: group.ordinal ?? 0 });
	}
}

export function closingLabel(at: Date, now: Date): string {
	const when = closing(at, now);
	switch (when.kind) {
		case 'today':
			return m.quiz_closes_today({ time: formatTime(at) });
		case 'tomorrow':
			return m.quiz_closes_tomorrow({ time: formatTime(at) });
		case 'days':
			return m.quiz_closes_in_days({ count: when.days });
		case 'date':
			return m.quiz_closes_on({ date: formatDate(at, 'dayMonth') });
	}
}

/** "mar 27 ott, 10:30", as Android's sheets write instants. */
export function formatInstant(at: Date): string {
	return `${formatDate(at, 'weekdayDayMonth')}, ${formatTime(at)}`;
}

export function attemptStateLabel(state: string | null): string {
	switch (state) {
		case 'inprogress':
			return m.quiz_attempt_inprogress();
		case 'overdue':
			return m.quiz_attempt_overdue();
		case 'finished':
			return m.quiz_attempt_finished();
		case 'abandoned':
			return m.quiz_attempt_abandoned();
		default:
			return m.quiz_attempt_unknown();
	}
}

export function gradingMethodLabel(method: number | null): string | null {
	switch (method) {
		case 1:
			return m.quiz_grading_highest();
		case 2:
			return m.quiz_grading_average();
		case 3:
			return m.quiz_grading_first();
		case 4:
			return m.quiz_grading_last();
		default:
			return null;
	}
}

export const verdictLabels: Record<Verdict, () => string> = {
	correct: m.quiz_verdict_correct,
	partial: m.quiz_verdict_partial,
	wrong: m.quiz_verdict_wrong,
	unanswered: m.quiz_verdict_unanswered,
	pending: m.quiz_verdict_pending
};

export const verdictCountLabels: Record<Verdict, (inputs: { count: number }) => string> = {
	correct: m.quiz_count_correct,
	partial: m.quiz_count_partial,
	wrong: m.quiz_count_wrong,
	unanswered: m.quiz_count_unanswered,
	pending: m.quiz_count_pending
};

/**
 * What to tell the student about a failed quiz call: Moodle's quiz errors in words of their own,
 * anything else as the data layer says it.
 */
export function quizErrorMessage(error: DataError): string {
	const code = error.kind === 'rejected' || error.kind === 'forbidden' ? error.code : undefined;
	switch (code) {
		case 'submissionoutofsequence':
			return m.quiz_error_out_of_sequence();
		case 'attemptalreadyclosed':
			return m.quiz_error_closed();
		case 'stateoverdue':
			return m.quiz_error_overdue();
		case 'notfinished':
			return m.quiz_error_not_finished();
		case 'attemptstillinprogress':
			return m.quiz_error_in_progress();
		case 'noreview':
		case 'noreviewattempt':
		case 'noreviewuntil':
			return m.quiz_error_no_review();
		case 'preflightdatarequired':
		case 'passworderror':
			return m.quiz_error_access();
		default:
			return errorMessage(error);
	}
}

/** The Moodle code of a failure, for the flows that react to one. */
export function errorCode(error: DataError | undefined): string | undefined {
	return error?.kind === 'rejected' || error?.kind === 'forbidden' ? error.code : undefined;
}
