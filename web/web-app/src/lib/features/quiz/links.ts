import type { ResolvedPathname } from '$app/types';
import { coursePath } from '#lib/features/course/links.ts';

const MOODLE = 'https://elearning.unimib.it';

/** The course's Quizzes tab, filtered when a filter is given. */
export function quizzesPath(courseId: number, filter?: string): ResolvedPathname {
	const path = coursePath(courseId, 'quizzes');
	return filter ? (`${path}?filter=${filter}` as ResolvedPathname) : path;
}

/** Where an in-app navigation was headed, to resume it once allowed. */
export function sameAppHref(url: URL): ResolvedPathname {
	return `${url.pathname}${url.search}${url.hash}` as ResolvedPathname;
}

export function quizPath(courseId: number, quizId: number): ResolvedPathname {
	return coursePath(courseId, 'quiz', quizId);
}

/** An attempt at one of its pages (0-based); without a page it opens where it was left. */
export function attemptPath(
	courseId: number,
	quizId: number,
	attemptId: number,
	page?: number
): ResolvedPathname {
	const path = coursePath(courseId, 'quiz', quizId, 'attempt', attemptId);
	return page === undefined ? path : (`${path}?page=${page}` as ResolvedPathname);
}

export function reviewPath(courseId: number, quizId: number, attemptId: number): ResolvedPathname {
	return coursePath(courseId, 'quiz', quizId, 'attempt', attemptId, 'review');
}

/** The attempt on the e-learning site, for the questions this app can't show. */
export function moodleAttemptUrl(attemptId: number, cmId: number, page = 0): `https://${string}` {
	return `${MOODLE}/mod/quiz/attempt.php?attempt=${attemptId}&cmid=${cmId}&page=${page}`;
}

/**
 * The attempt's review on the e-learning site, for what this app can't show, at the question
 * when its fields name it (`q123:4_` is question-123-4 there).
 */
export function moodleReviewUrl(
	attemptId: number,
	cmId: number,
	prefix: string | null = null
): `https://${string}` {
	const question = prefix ? /^q(\d+):(\d+)_$/.exec(prefix) : null;
	const anchor = question ? `#question-${question[1]}-${question[2]}` : '';
	return `${MOODLE}/mod/quiz/review.php?attempt=${attemptId}&cmid=${cmId}&showall=1${anchor}`;
}
