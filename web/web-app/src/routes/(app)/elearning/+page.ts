import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { courseDeadlinesQuery, enrolledCoursesQuery } from '#lib/features/elearning/queries.ts';

/** Hovering the link starts loading what the page opens with. */
export const load = prefetchPage((scope) => [
	enrolledCoursesQuery(scope),
	courseDeadlinesQuery(scope)
]);
