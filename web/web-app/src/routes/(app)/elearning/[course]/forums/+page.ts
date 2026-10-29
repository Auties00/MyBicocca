import { prefetch } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { courseForumsQuery } from '#lib/features/forum/queries.ts';
import type { PageLoad } from './$types';

/** Hovering the tab already starts loading the course's forums. */
export const load: PageLoad = async ({ parent }) => {
	const { courseId } = await parent();
	const scope = session.current;
	if (scope !== undefined) void prefetch(courseForumsQuery(scope, courseId));
};
