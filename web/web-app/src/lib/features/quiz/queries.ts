import type {
	MoodleAttemptReviewResponse,
	MoodleQuizGetCombinedReviewOptionsResponse,
	MoodleQuizAccessInformation,
	MoodleQuizGetAttemptAccessInformationResponse,
	MoodleQuizGetQuizFeedbackForGradeResponse,
	MoodleQuizzesResponse,
	MoodleUserAttemptsResponse,
	MoodleUserBestGradeResponse
} from '@mybicocca/data-api/elearning';
import { accountKey } from '#lib/data/keys.ts';
import { invalidate, policies, type QueryKey, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';

/*
 * Named after the SDK call each one wraps, so another feature making the same call shares the
 * entry: the e-learning deadlines read the same quiz lists.
 */

/** The course's quizzes (`mod_quiz_get_quizzes_by_courses`), fresh for 10 minutes as on Android. */
export function courseQuizzesQuery(
	scope: SessionScope,
	courseId: number
): QueryOptions<MoodleQuizzesResponse> {
	return {
		key: accountKey(scope.account, 'moodle.quiz.getQuizzesByCourses', courseId),
		load: () =>
			scope.api.moodle((moodle, auth) =>
				moodle.quiz.getQuizzesByCourses(auth, { courseIds: [courseId] })
			),
		policy: policies.fresh(10)
	};
}

/** Every attempt of the student at the quiz, previews left out by Moodle; always revalidated. */
export function attemptsQuery(
	scope: SessionScope,
	quizId: number
): QueryOptions<MoodleUserAttemptsResponse> {
	return {
		key: accountKey(scope.account, 'moodle.quiz.getUserAttempts', quizId),
		load: () =>
			scope.api.moodle((moodle, auth) =>
				moodle.quiz.getUserAttempts(quizId, auth, { status: 'all', includePreviews: false })
			)
	};
}

/** The student's grade for the quiz, on the quiz's scale, by its grading method. */
export function bestGradeQuery(
	scope: SessionScope,
	quizId: number
): QueryOptions<MoodleUserBestGradeResponse> {
	return {
		key: accountKey(scope.account, 'moodle.quiz.getUserBestGrade', quizId),
		load: () => scope.api.moodle((moodle, auth) => moodle.quiz.getUserBestGrade(quizId, auth))
	};
}

/** Whether Moodle lets the student attempt the quiz now, and the rules it applies. */
export function accessQuery(
	scope: SessionScope,
	quizId: number
): QueryOptions<MoodleQuizAccessInformation> {
	return {
		key: accountKey(scope.account, 'moodle.quiz.getQuizAccessInformation', quizId),
		load: () =>
			scope.api.moodle((moodle, auth) => moodle.quiz.getQuizAccessInformation(quizId, auth))
	};
}

/**
 * Moodle's rules applied to an attempt (the given one, else the student's open or last one): when
 * it must be submitted, whether it asks a check before going on, and why no new attempt is
 * allowed. Asking also lets Moodle close an attempt whose time has run out, as its pages do.
 */
export function attemptAccessQuery(
	scope: SessionScope,
	quizId: number,
	attemptId?: number
): QueryOptions<MoodleQuizGetAttemptAccessInformationResponse> {
	return {
		key: accountKey(
			scope.account,
			'moodle.quiz.getAttemptAccessInformation',
			quizId,
			attemptId ?? 0
		),
		load: () =>
			scope.api.moodle((moodle, auth) =>
				moodle.quiz.getAttemptAccessInformation(quizId, auth, { attemptId })
			)
	};
}

/**
 * What Moodle lets the student review across their attempts: each option on for some of them and
 * on for all. The answer for a quiz the list gives no review settings for, such as a closed one.
 */
export function combinedReviewQuery(
	scope: SessionScope,
	quizId: number
): QueryOptions<MoodleQuizGetCombinedReviewOptionsResponse> {
	return {
		key: accountKey(scope.account, 'moodle.quiz.getCombinedReviewOptions', quizId),
		load: () =>
			scope.api.moodle((moodle, auth) => moodle.quiz.getCombinedReviewOptions(quizId, auth))
	};
}

/** The quiz's overall feedback for a grade on its scale: '' for a band the teacher left blank. */
export function gradeFeedbackQuery(
	scope: SessionScope,
	quizId: number,
	grade: number
): QueryOptions<MoodleQuizGetQuizFeedbackForGradeResponse> {
	return {
		key: accountKey(scope.account, 'moodle.quiz.getQuizFeedbackForGrade', quizId, grade),
		load: () =>
			scope.api.moodle((moodle, auth) => moodle.quiz.getQuizFeedbackForGrade(quizId, grade, auth)),
		policy: policies.fresh(60)
	};
}

/**
 * A finished attempt with every question, the student's answers, marks and feedback. Teachers
 * grade essays later, so it is revalidated on every visit.
 */
export function reviewQuery(
	scope: SessionScope,
	attemptId: number
): QueryOptions<MoodleAttemptReviewResponse> {
	return {
		key: accountKey(scope.account, 'moodle.quiz.getAttemptReview', attemptId),
		load: () => scope.api.moodle((moodle, auth) => moodle.quiz.getAttemptReview(attemptId, auth))
	};
}

/** What starting or finishing an attempt changes: the quiz's attempts, grade, access, completion. */
export function attemptChanges(scope: SessionScope, courseId: number, quizId: number): QueryKey[] {
	return [
		accountKey(scope.account, 'moodle.quiz.getUserAttempts', quizId),
		accountKey(scope.account, 'moodle.quiz.getUserBestGrade', quizId),
		accountKey(scope.account, 'moodle.quiz.getQuizAccessInformation', quizId),
		accountKey(scope.account, 'moodle.quiz.getAttemptAccessInformation', quizId),
		accountKey(scope.account, 'moodle.course.getContents', courseId)
	];
}

export function invalidateAttempts(scope: SessionScope, courseId: number, quizId: number): void {
	invalidate(...attemptChanges(scope, courseId, quizId));
}
