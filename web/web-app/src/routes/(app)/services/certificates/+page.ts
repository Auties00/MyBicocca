import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { certificatesQuery } from '#lib/features/documents/queries.ts';

/** Hovering the link starts loading what the page opens with. */
export const load = prefetchPage((scope) => [certificatesQuery(scope)]);
