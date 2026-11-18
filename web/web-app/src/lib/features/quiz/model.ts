import type {
	MoodleCourseModule,
	MoodleQuiz,
	MoodleQuizAccessInformation,
	MoodleQuizAttempt,
	MoodleQuizGetAttemptAccessInformationResponse,
	MoodleQuizGetCombinedReviewOptionsResponse
} from '@mybicocca/data-api/elearning';
import { calendarDays } from '#lib/format/dates.ts';

const SECOND = 1000;
const HOUR = 3600 * SECOND;
/** A quiz closing within this is due soon (Android's `DUE_SOON_HOURS`). */
const DUE_SOON_MS = 48 * HOUR;
/** Moodle shows an untimed attempt's countdown only in the last hour before the quiz closes. */
const CLOSE_COUNTDOWN_MS = HOUR;
/** Below this many quizzes the one closing first is in plain sight: no card repeats it. */
const NEXT_FROM = 3;

type Times = Pick<MoodleQuiz, 'timeOpen' | 'timeClose'>;

const instant = (seconds: number | null) =>
	seconds !== null && seconds > 0 ? new Date(seconds * SECOND) : null;

export function opensAt(quiz: Times): Date | null {
	return instant(quiz.timeOpen);
}

export function closesAt(quiz: Times): Date | null {
	return instant(quiz.timeClose);
}

export type Availability = 'upcoming' | 'open' | 'closed';

export function availability(quiz: Times, now: Date): Availability {
	const opens = opensAt(quiz);
	if (opens !== null && now < opens) return 'upcoming';
	const closes = closesAt(quiz);
	if (closes !== null && now > closes) return 'closed';
	return 'open';
}

/** How a closing reads in a list: today or tomorrow with the time, in N days, or on a date. */
export type Closing =
	{ kind: 'today' | 'tomorrow' | 'date'; at: Date } | { kind: 'days'; days: number; at: Date };

export function closing(at: Date, now: Date): Closing {
	const days = calendarDays(now, at);
	if (days <= 0) return { kind: 'today', at };
	if (days === 1) return { kind: 'tomorrow', at };
	if (days <= 14) return { kind: 'days', days, at };
	return { kind: 'date', at };
}

export function closingSoon(quiz: Times, now: Date): boolean {
	const closes = closesAt(quiz);
	if (closes === null) return false;
	const left = closes.getTime() - now.getTime();
	return left > 0 && left <= DUE_SOON_MS;
}

/** Moodle's completion says done (1) or passed (2). */
export function isCompleted(module: MoodleCourseModule | undefined): boolean {
	const state = module?.completionData?.state;
	return state === 1 || state === 2;
}

function isTracked(module: MoodleCourseModule | undefined): boolean {
	return (
		module !== undefined &&
		((module.completion ?? 0) > 0 || module.completionData?.hasCompletion === true)
	);
}

/** A quiz with its activity on the course page, when the page lists it. */
export interface QuizItem {
	quiz: MoodleQuiz;
	module: MoodleCourseModule | undefined;
}

/** The part of a course section the grouping needs: the Content tab's cards give it. */
export interface SectionModules {
	key: string;
	ordinal: number;
	title: string | null;
	modules: readonly MoodleCourseModule[];
}

export interface QuizGroup {
	key: string;
	/**
	 * `section`: a section of the course page; `other`: quizzes no section lists; `all`: the
	 * course's only group, as when the course page isn't known.
	 */
	kind: 'section' | 'other' | 'all';
	/** The section's number on the Content tab. */
	ordinal: number | null;
	title: string | null;
	items: QuizItem[];
}

/**
 * The quizzes grouped by the section they sit in, in course order: real courses have dozens of
 * quizzes with repeated names ("Esercizio 1.2"), which their section tells apart. Quizzes no
 * section lists come last.
 */
export function groupQuizzes(
	quizzes: readonly MoodleQuiz[],
	sections: readonly SectionModules[] | undefined
): QuizGroup[] {
	if (sections === undefined) {
		return [
			{
				key: 'all',
				kind: 'all',
				ordinal: null,
				title: null,
				items: quizzes.map((quiz) => ({ quiz, module: undefined }))
			}
		];
	}
	const byModule = new Map(quizzes.map((quiz) => [quiz.courseModule, quiz]));
	const placed = new Set<number>();
	const groups: QuizGroup[] = [];
	for (const section of sections) {
		const items: QuizItem[] = [];
		for (const module of section.modules) {
			const quiz = module.modName === 'quiz' ? byModule.get(module.id) : undefined;
			if (quiz === undefined || placed.has(quiz.id)) continue;
			placed.add(quiz.id);
			items.push({ quiz, module });
		}
		if (items.length > 0) {
			groups.push({
				key: section.key,
				kind: 'section',
				ordinal: section.ordinal,
				title: section.title,
				items
			});
		}
	}
	const rest = quizzes.filter((quiz) => !placed.has(quiz.id));
	if (rest.length > 0) {
		groups.push({
			key: 'other',
			kind: groups.length === 0 ? 'all' : 'other',
			ordinal: null,
			title: null,
			items: rest.map((quiz) => ({ quiz, module: undefined }))
		});
	}
	return groups;
}

/**
 * The number on a section's badge: its place on the Content tab, the same whatever the filter
 * shows. None when the title already starts with a number of its own ("1. Introduzione").
 */
export function sectionNumber(group: Pick<QuizGroup, 'kind' | 'ordinal' | 'title'>): number | null {
	if (group.kind !== 'section' || group.ordinal === null) return null;
	return /^\s*\d/.test(group.title ?? '') ? null : group.ordinal;
}

export interface Completion {
	done: number;
	tracked: number;
	total: number;
}

export function completionOf(items: readonly QuizItem[]): Completion {
	return {
		done: items.filter((item) => isCompleted(item.module)).length,
		tracked: items.filter((item) => isTracked(item.module)).length,
		total: items.length
	};
}

/** Progress is worth a bar when there are a few quizzes and Moodle tracks most of them. */
export function showsProgress({ tracked, total }: Completion): boolean {
	return total >= 2 && tracked >= total / 2;
}

export type QuizState = 'restricted' | 'upcoming' | 'closed' | 'completed' | 'open';

/** A row's state; the first that applies wins, as on Android. */
export function quizState(item: QuizItem, now: Date): QuizState {
	if (item.module?.userVisible === false) return 'restricted';
	const open = availability(item.quiz, now);
	if (open !== 'open') return open;
	return isCompleted(item.module) ? 'completed' : 'open';
}

export type Filter = 'all' | 'todo' | 'soon';

export function matches(item: QuizItem, filter: Filter, now: Date): boolean {
	const state = quizState(item, now);
	switch (filter) {
		case 'all':
			return true;
		case 'todo':
			return state === 'open';
		case 'soon':
			return state === 'open' && closingSoon(item.quiz, now);
	}
}

/**
 * The quiz to do that closes first, worth a card of its own only when the list is long enough
 * for it to be lost, and when it isn't the row the list starts with anyway.
 */
export function nextToClose(
	groups: readonly QuizGroup[],
	now: Date,
	first: QuizItem | undefined
): QuizItem | undefined {
	const items = groups.flatMap((group) => group.items);
	if (items.length < NEXT_FROM) return undefined;
	const next = items
		.filter((item) => quizState(item, now) === 'open' && closesAt(item.quiz) !== null)
		.toSorted((a, b) => (a.quiz.timeClose ?? 0) - (b.quiz.timeClose ?? 0))[0];
	return next === first ? undefined : next;
}

/** The date the quiz page states, as Android's sheet: an opening still ahead, else the closing. */
export type Schedule = { kind: 'opens' | 'closes' | 'closed'; at: Date } | null;

export function scheduleOf(quiz: Times, now: Date): Schedule {
	const opens = opensAt(quiz);
	if (opens !== null && now < opens) return { kind: 'opens', at: opens };
	const closes = closesAt(quiz);
	if (closes === null) return null;
	return { kind: now > closes ? 'closed' : 'closes', at: closes };
}

/** Previews are teachers' trial runs and never count. */
export function ownAttempts(attempts: readonly MoodleQuizAttempt[]): MoodleQuizAttempt[] {
	return attempts.filter((attempt) => attempt.preview !== 1);
}

/** Moodle lets the student carry on an attempt in progress, or submit an overdue one. */
export function isOpenAttempt(attempt: Pick<MoodleQuizAttempt, 'state'>): boolean {
	return attempt.state === 'inprogress' || attempt.state === 'overdue';
}

/**
 * Time ran out on a quiz with a grace period: Moodle takes no more answers, only the submission,
 * until `timecheckstate`; after that it abandons the attempt.
 */
export function isOverdue(attempt: Pick<MoodleQuizAttempt, 'state'>): boolean {
	return attempt.state === 'overdue';
}

/** Null when the quiz allows any number of attempts. */
export function maxAttempts(quiz: Pick<MoodleQuiz, 'attempts'>): number | null {
	return quiz.attempts !== null && quiz.attempts > 0 ? quiz.attempts : null;
}

/** Moodle's access rules applied to the student's attempt, as the attempt access call reports them. */
export type AttemptAccess = Pick<
	MoodleQuizGetAttemptAccessInformationResponse,
	'endtime' | 'ispreflightcheckrequired' | 'preventnewattemptreasons'
>;

/**
 * Whether Moodle asks a check the app can't give, such as a password, before `attempt` goes on.
 * Moodle answers for the student's open attempt, else for a new one, which a time limit always
 * asks to confirm: the answer is about `attempt` only while it is open.
 */
export function asksCheck(
	attempt: Pick<MoodleQuizAttempt, 'state'> | undefined,
	access: Pick<AttemptAccess, 'ispreflightcheckrequired'> | undefined
): boolean {
	return (
		attempt !== undefined && isOpenAttempt(attempt) && access?.ispreflightcheckrequired === true
	);
}

export type QuizAction =
	| { kind: 'resume'; attempt: MoodleQuizAttempt }
	/** An open attempt that asks for a check the app can't give (a password): only the site goes on. */
	| { kind: 'site'; attempt: MoodleQuizAttempt }
	| { kind: 'start'; number: number }
	| { kind: 'blocked'; reason: 'upcoming' | 'closed' | 'exhausted' | 'empty' | 'denied' };

/**
 * What the quiz page offers: an open attempt to carry on (even once the quiz closed), else a new
 * attempt when the quiz is open, has questions, attempts left and Moodle raises no objection,
 * such as the wait it sets between attempts.
 */
export function quizAction(
	quiz: MoodleQuiz,
	attempts: readonly MoodleQuizAttempt[],
	now: Date,
	access?: Pick<MoodleQuizAccessInformation, 'canAttempt' | 'preventAccessReasons'>,
	attemptAccess?: AttemptAccess
): QuizAction {
	const own = ownAttempts(attempts);
	const open = own.find(isOpenAttempt);
	if (open !== undefined) {
		return asksCheck(open, attemptAccess)
			? { kind: 'site', attempt: open }
			: { kind: 'resume', attempt: open };
	}
	const when = availability(quiz, now);
	if (when !== 'open') return { kind: 'blocked', reason: when };
	const max = maxAttempts(quiz);
	if (max !== null && own.length >= max) return { kind: 'blocked', reason: 'exhausted' };
	if (quiz.hasQuestions === 0) return { kind: 'blocked', reason: 'empty' };
	if (
		(access !== undefined && (!access.canAttempt || access.preventAccessReasons.length > 0)) ||
		(attemptAccess?.preventnewattemptreasons.length ?? 0) > 0
	) {
		return { kind: 'blocked', reason: 'denied' };
	}
	const last = Math.max(0, ...own.map((attempt) => attempt.attempt ?? 0));
	return { kind: 'start', number: last + 1 };
}

/**
 * Raw marks on the quiz's scale: an attempt's `sumgrades` add up question marks, while grades
 * (the best grade, the gradebook) are out of `grade`.
 */
export function scaledGrade(
	sumGrades: number | null,
	quiz: Pick<MoodleQuiz, 'sumGrades' | 'grade'>
): number | null {
	if (sumGrades === null) return null;
	if (quiz.sumGrades === null || quiz.sumGrades <= 0 || quiz.grade === null) return sumGrades;
	return (sumGrades / quiz.sumGrades) * quiz.grade;
}

/** Pages of an attempt: Moodle's layout ends each page with a 0 (`1,2,0,3,0` is two pages). */
export function pageCount(layout: string | null): number | null {
	if (!layout) return null;
	const count = layout.split(',').filter((slot) => slot.trim() === '0').length;
	return count > 0 ? count : null;
}

/**
 * When the attempt must be submitted, in epoch milliseconds: the end time Moodle's access rules
 * give it (the limit, the quiz closing, the student's overrides), or until they answer, the time
 * Moodle set to check the attempt, which it took from the same rules. Null when nothing ends it.
 */
export function attemptDeadline(
	attempt: Pick<MoodleQuizAttempt, 'timeCheckState'>,
	access: Pick<AttemptAccess, 'endtime'> | undefined
): number | null {
	const seconds = access === undefined ? attempt.timeCheckState : access.endtime;
	return seconds !== null && seconds > 0 ? seconds * SECOND : null;
}

/**
 * Whether an attempt still in progress has run out of time, by Moodle's access rules. Asking them
 * lets Moodle close it as the quiz says (submit it, abandon it or hold it overdue), so attempts
 * listed before the answer are out of date.
 */
export function ranOut(
	attempt: Pick<MoodleQuizAttempt, 'state' | 'timeCheckState'>,
	access: Pick<AttemptAccess, 'endtime'> | undefined,
	now: number
): boolean {
	const deadline = attemptDeadline(attempt, access);
	return attempt.state === 'inprogress' && deadline !== null && deadline <= now;
}

/** Moodle's review times, as the bits of a quiz's review settings. */
const REVIEW = {
	during: 0x10000,
	immediatelyAfter: 0x01000,
	laterWhileOpen: 0x00100,
	afterClose: 0x00010
};
/**
 * Moodle shows the overall feedback after every attempt unless the teacher says otherwise, and
 * never during one: its settings have no such time.
 */
const DEFAULT_OVERALL_FEEDBACK =
	REVIEW.immediatelyAfter | REVIEW.laterWhileOpen | REVIEW.afterClose;
/** How long after its submission an attempt counts as "immediately after". */
const IMMEDIATELY_AFTER_MS = 2 * 60 * SECOND;

/** The review time `attempt` is in now, as Moodle tells them apart. */
function reviewTime(
	quiz: Pick<MoodleQuiz, 'timeClose'>,
	attempt: Pick<MoodleQuizAttempt, 'state' | 'timeFinish'>,
	now: number
): number {
	if (attempt.state === 'inprogress') return REVIEW.during;
	if (quiz.timeClose !== null && quiz.timeClose > 0 && now >= quiz.timeClose * SECOND) {
		return REVIEW.afterClose;
	}
	if (now < (attempt.timeFinish ?? 0) * SECOND + IMMEDIATELY_AFTER_MS)
		return REVIEW.immediatelyAfter;
	return REVIEW.laterWhileOpen;
}

/** Moodle's overall-feedback review option across the student's attempts: on for some, for all. */
export interface CombinedOverallFeedback {
	some: boolean;
	all: boolean;
}

/** The overall-feedback option of Moodle's combined review options. */
export function combinedOverallFeedback(
	options: Pick<MoodleQuizGetCombinedReviewOptionsResponse, 'someoptions' | 'alloptions'>
): CombinedOverallFeedback {
	const on = (list: readonly { name: string; value: number }[]) =>
		list.some((option) => option.name === 'overallfeedback' && option.value === 1);
	return { some: on(options.someoptions), all: on(options.alloptions) };
}

/**
 * Whether the quiz's overall feedback (its words for a band of grades) is for the student to see
 * on `attempts`, the student's own: the quiz has some, and its review settings show it at the
 * time each attempt is in, so not while one is in progress. Moodle leaves the settings out of a
 * quiz it no longer lets the student into, such as a closed one; then `combined`, its options
 * worked out over every attempt, decides when it is on for all or for none, and Moodle's default
 * otherwise (or without it).
 */
export function overallFeedbackShown(
	quiz: Pick<MoodleQuiz, 'hasFeedback' | 'reviewOverallFeedback' | 'timeClose'>,
	attempts: readonly Pick<MoodleQuizAttempt, 'state' | 'timeFinish'>[],
	now: number,
	combined?: CombinedOverallFeedback
): boolean {
	if (quiz.hasFeedback !== 1 || attempts.length === 0) return false;
	if (quiz.reviewOverallFeedback === null && combined !== undefined) {
		if (combined.all) return true;
		if (!combined.some) return false;
	}
	const allowed = quiz.reviewOverallFeedback ?? DEFAULT_OVERALL_FEEDBACK;
	return attempts.every((attempt) => (allowed & reviewTime(quiz, attempt, now)) !== 0);
}

/** Timed quizzes always show their countdown; others only in the hour before they close. */
export function showsCountdown(
	quiz: Pick<MoodleQuiz, 'timeLimit'>,
	deadline: number | null,
	now: number
): boolean {
	if (deadline === null) return false;
	return (quiz.timeLimit ?? 0) > 0 || deadline - now <= CLOSE_COUNTDOWN_MS;
}

export type TimerPhase = 'calm' | 'warning' | 'danger' | 'expired';

/** Calm, then a warning from 5 minutes left, danger in the last minute. */
export function timerPhase(leftMs: number): TimerPhase {
	if (leftMs <= 0) return 'expired';
	if (leftMs <= 60 * SECOND) return 'danger';
	if (leftMs <= 5 * 60 * SECOND) return 'warning';
	return 'calm';
}

export type Verdict = 'correct' | 'partial' | 'wrong' | 'unanswered' | 'pending';

/** A reviewed question's outcome, from Moodle's question state; null for descriptions. */
export function verdictOf(state: string | null): Verdict | null {
	switch (state) {
		case 'gradedright':
		case 'mangrright':
			return 'correct';
		case 'gradedpartial':
		case 'mangrpartial':
			return 'partial';
		case 'gradedwrong':
		case 'mangrwrong':
			return 'wrong';
		case 'gaveup':
		case 'mangaveup':
			return 'unanswered';
		case 'needsgrading':
		case 'complete':
		case 'invalid':
			return 'pending';
		default:
			return null;
	}
}

export function verdictCounts(states: readonly (string | null)[]): Record<Verdict, number> {
	const counts: Record<Verdict, number> = {
		correct: 0,
		partial: 0,
		wrong: 0,
		unanswered: 0,
		pending: 0
	};
	for (const state of states) {
		const verdict = verdictOf(state);
		if (verdict !== null) counts[verdict]++;
	}
	return counts;
}
