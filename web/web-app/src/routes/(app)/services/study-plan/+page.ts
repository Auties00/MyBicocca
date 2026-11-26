import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { recordBookRowsQuery } from '#lib/features/profile/queries.ts';
import { planHeadersQuery } from '#lib/features/services/sources.ts';
import { studyPathQuery } from '#lib/features/studyplan/queries.ts';

/** Hovering the link starts loading what the page opens with. */
export const load = prefetchPage((scope) => [
	planHeadersQuery(scope),
	studyPathQuery(scope),
	recordBookRowsQuery(scope)
]);
