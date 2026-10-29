import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import type { CourseDeadline } from './deadlines.ts';

/**
 * A resolved path with a query string. The kit types links as paths only, so the one cast that
 * lets a link carry its search parameters lives here; empty parameters are left out.
 */
export function withQuery(
	path: ResolvedPathname,
	params: Record<string, string | undefined>
): ResolvedPathname {
	const search = new URLSearchParams(
		Object.entries(params).filter((entry): entry is [string, string] => !!entry[1])
	);
	return search.size === 0 ? path : (`${path}?${search}` as ResolvedPathname);
}

export function courseHref(courseId: number): ResolvedPathname {
	return resolve('/(app)/elearning/[course]', { course: String(courseId) });
}

/** The course list, filtered by the filter's token (`all`, `year:2`…). */
export function coursesHref(filter?: string): ResolvedPathname {
	return withQuery(resolve('/(app)/elearning'), { filter });
}

/** The assignment's or quiz's page, by the activity's own id. */
export function deadlineHref(deadline: CourseDeadline): ResolvedPathname {
	const course = String(deadline.courseId);
	return deadline.kind === 'assign'
		? resolve('/(app)/elearning/[course]/assignment/[assign]', {
				course,
				assign: String(deadline.instanceId)
			})
		: resolve('/(app)/elearning/[course]/quiz/[quiz]', {
				course,
				quiz: String(deadline.instanceId)
			});
}

/** The catalog at a category (its key), or at its root, searching `query` there when given. */
export function catalogHref(node?: string, query?: string): ResolvedPathname {
	return withQuery(resolve('/(app)/elearning/catalog'), { node, q: query });
}

/** The course's page on the university's Moodle, for a look before enrolling. */
export function moodleCourseUrl(courseId: number): `https://${string}` {
	return `https://elearning.unimib.it/course/view.php?id=${courseId}`;
}
