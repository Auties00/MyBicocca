import { prefetch, type QueryOptions } from '#lib/data/query.svelte.ts';
import { session, type SessionScope } from '#lib/data/session.svelte.ts';

/**
 * A page's `load` that starts the queries the page opens with, so hovering (or touching) its link
 * already fetches them: SvelteKit runs `load` on preload, and the page then finds the data on its
 * way or in the cache. Nothing waits on it: the page appears at once, with its skeleton, and a
 * failed prefetch is left for the page to show.
 */
export function prefetchPage(
	queries: (scope: SessionScope) => readonly QueryOptions<unknown>[]
): (event: { parent: () => Promise<unknown> }) => Promise<void> {
	return async ({ parent }) => {
		await parent();
		const scope = session.current;
		if (scope === undefined) return;
		for (const options of queries(scope)) void prefetch(options);
	};
}
