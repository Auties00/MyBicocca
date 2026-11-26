import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { publishedResultsQuery } from '#lib/features/services/sources.ts';

/** Hovering the link starts loading what the page opens with. */
export const load = prefetchPage((scope) => [publishedResultsQuery(scope)]);
