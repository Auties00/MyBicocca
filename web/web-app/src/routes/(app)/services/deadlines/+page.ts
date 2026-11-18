import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { bookingsQuery, examCallsQuery } from '#lib/features/exams/queries.ts';
import {
	chargesQuery,
	invoicesQuery,
	planHeadersQuery,
	publishedResultsQuery
} from '#lib/features/services/sources.ts';

/** Hovering the link starts loading what the page opens with. */
export const load = prefetchPage((scope) => [
	publishedResultsQuery(scope),
	invoicesQuery(scope),
	chargesQuery(scope),
	bookingsQuery(scope),
	examCallsQuery(scope),
	planHeadersQuery(scope)
]);
