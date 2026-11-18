import type {
	MoodleAssignmentsResponse,
	MoodleCalendarEvent,
	MoodleEnrolledCourse,
	MoodleQuizzesResponse
} from '@mybicocca/data-api/elearning';
import { accountKey } from '#lib/data/keys.ts';
import { cache, invalidate, policies, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import {
	ACTION_EVENTS_PAGE,
	collectActionEvents,
	coursesToResolve,
	resolveDeadlines,
	type CourseDeadline
} from './deadlines.ts';

/*
 * Queries named after one SDK call hold that call's raw answer, so every feature making the same
 * call shares one cache entry (Calendar reads the same action events and assignments). The
 * `elearning` ones derive from several calls.
 */

const FIFTEEN_MINUTES = policies.fresh(15);

/**
 * The student's Moodle courses, as Moodle answers them (`core_enrol_get_users_courses`), fresh
 * for 15 minutes as on Android. Course detail, Today and search share this entry; turn the answer
 * into the list's courses with `toEnrolledCourses(data, getLocale())`:
 *
 * ```ts
 * const courses = query(() => session.current && enrolledCoursesQuery(session.current));
 * ```
 */
export function enrolledCoursesQuery(scope: SessionScope): QueryOptions<MoodleEnrolledCourse[]> {
	return {
		key: accountKey(scope.account, 'moodle.course.getUsersCourses'),
		load: () =>
			scope.api.moodle((moodle, auth, user) => moodle.course.getUsersCourses(user.userId, auth)),
		policy: FIFTEEN_MINUTES
	};
}

/** Every action event of the account (`core_calendar_get_action_events_by_timesort`), paged. */
export function actionEventsQuery(scope: SessionScope): QueryOptions<MoodleCalendarEvent[]> {
	return {
		key: accountKey(scope.account, 'moodle.calendar.getActionEventsByTimesort'),
		load: () =>
			scope.api.moodle((moodle, auth) =>
				collectActionEvents((timeSortFrom, afterEventId) =>
					moodle.calendar.getActionEventsByTimesort(auth, {
						timeSortFrom,
						afterEventId,
						limitNum: ACTION_EVENTS_PAGE
					})
				)
			),
		policy: FIFTEEN_MINUTES
	};
}

function assignmentsQuery(
	scope: SessionScope,
	courseIds: number[]
): QueryOptions<MoodleAssignmentsResponse> {
	return {
		key: accountKey(scope.account, 'moodle.assign.getAssignments', ...courseIds),
		load: () =>
			scope.api.moodle((moodle, auth) => moodle.assign.getAssignments(auth, { courseIds })),
		policy: FIFTEEN_MINUTES
	};
}

function quizzesQuery(
	scope: SessionScope,
	courseIds: number[]
): QueryOptions<MoodleQuizzesResponse> {
	return {
		key: accountKey(scope.account, 'moodle.quiz.getQuizzesByCourses', ...courseIds),
		load: () =>
			scope.api.moodle((moodle, auth) => moodle.quiz.getQuizzesByCourses(auth, { courseIds })),
		policy: FIFTEEN_MINUTES
	};
}

function fetchQuery<T>(options: QueryOptions<T>): Promise<T> {
	return cache.fetch(options.key, options.load, options.policy ?? policies.live);
}

/**
 * Assignment and quiz deadlines of every enrolled course, with their activities' ids, ordered by
 * due date. The events give course-module ids; the assignment and quiz lists of the courses
 * involved translate them.
 */
export function courseDeadlinesQuery(scope: SessionScope): QueryOptions<CourseDeadline[]> {
	return {
		key: accountKey(scope.account, 'elearning', 'deadlines'),
		load: async () => {
			const events = await fetchQuery(actionEventsQuery(scope));
			const assignCourses = coursesToResolve(events, 'assign');
			const quizCourses = coursesToResolve(events, 'quiz');
			const [assignments, quizzes] = await Promise.all([
				assignCourses.length === 0 ? undefined : fetchQuery(assignmentsQuery(scope, assignCourses)),
				quizCourses.length === 0 ? undefined : fetchQuery(quizzesQuery(scope, quizCourses))
			]);
			return resolveDeadlines(
				events,
				new Map(
					(assignments?.courses ?? []).flatMap((course) =>
						course.assignments.map((assignment) => [assignment.cmId, assignment.id] as const)
					)
				),
				new Map((quizzes?.quizzes ?? []).map((quiz) => [quiz.courseModule, quiz.id] as const))
			);
		},
		policy: FIFTEEN_MINUTES
	};
}

/**
 * Reloads the course list and its deadlines now: the calls they derive from first, so the
 * derived deadlines reload from fresh answers rather than cached ones.
 */
export function refreshCourses(scope: SessionScope): void {
	invalidate(
		accountKey(scope.account, 'moodle.calendar.getActionEventsByTimesort'),
		accountKey(scope.account, 'moodle.assign.getAssignments'),
		accountKey(scope.account, 'moodle.quiz.getQuizzesByCourses'),
		accountKey(scope.account, 'moodle.course.getUsersCourses'),
		accountKey(scope.account, 'elearning')
	);
}
