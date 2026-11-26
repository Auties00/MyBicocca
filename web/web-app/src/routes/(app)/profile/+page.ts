import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { careerStatsQuery, recordBookRowsQuery } from '#lib/features/profile/queries.ts';

/** Hovering the link starts loading what the page opens with. */
export const load = prefetchPage((scope) => [recordBookRowsQuery(scope), careerStatsQuery(scope)]);
