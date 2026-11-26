import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { iseeQuery } from '#lib/features/payments/queries.ts';

/** Hovering the link starts loading what the page opens with. */
export const load = prefetchPage((scope) => [iseeQuery(scope)]);
