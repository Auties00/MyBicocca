import { error } from '@sveltejs/kit';
import { prefetch } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { courseForumsQuery, forumDiscussionsQuery } from '#lib/features/forum/queries.ts';
import type { LayoutLoad } from './$types';

/** Hovering a forum's link already starts loading its discussions. */
export const load: LayoutLoad = async ({ params, parent }) => {
	const forumId = Number(params.forum);
	if (!Number.isSafeInteger(forumId) || forumId <= 0) error(404, 'Forum not found');
	const { courseId } = await parent();
	const scope = session.current;
	if (scope !== undefined) {
		void prefetch(courseForumsQuery(scope, courseId));
		void prefetch(forumDiscussionsQuery(scope, forumId));
	}
	return { forumId };
};
