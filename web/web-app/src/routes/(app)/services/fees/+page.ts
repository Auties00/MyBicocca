import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { chargesQuery, invoicesQuery } from '#lib/features/payments/queries.ts';

/** Hovering the link starts loading what the page opens with. */
export const load = prefetchPage((scope) => [invoicesQuery(scope), chargesQuery(scope)]);
