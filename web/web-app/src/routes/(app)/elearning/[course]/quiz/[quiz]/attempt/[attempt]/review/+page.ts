import { prefetch } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { reviewQuery } from '#lib/features/quiz/queries.ts';
import type { PageLoad } from './$types';

export const load: PageLoad = async ({ parent }) => {
	const { attemptId } = await parent();
	const scope = session.current;
	if (scope !== undefined) void prefetch(reviewQuery(scope, attemptId));
};
