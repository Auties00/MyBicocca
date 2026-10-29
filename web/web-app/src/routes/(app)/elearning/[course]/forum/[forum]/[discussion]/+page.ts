import { error } from '@sveltejs/kit';
import { prefetch } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { discussionPostsQuery } from '#lib/features/forum/queries.ts';
import type { PageLoad } from './$types';

/** Hovering a discussion already starts loading its posts. */
export const load: PageLoad = ({ params }) => {
	const discussionId = Number(params.discussion);
	if (!Number.isSafeInteger(discussionId) || discussionId <= 0) {
		error(404, 'Discussion not found');
	}
	const scope = session.current;
	if (scope !== undefined) void prefetch(discussionPostsQuery(scope, discussionId));
	return { discussionId };
};
