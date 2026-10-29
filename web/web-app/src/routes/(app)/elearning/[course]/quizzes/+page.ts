import { prefetch } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { courseQuizzesQuery } from '#lib/features/quiz/queries.ts';
import type { PageLoad } from './$types';

/** Hovering the tab starts loading the course's quizzes. */
export const load: PageLoad = async ({ parent }) => {
	const { courseId } = await parent();
	const scope = session.current;
	if (scope !== undefined) void prefetch(courseQuizzesQuery(scope, courseId));
};
