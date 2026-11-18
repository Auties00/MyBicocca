import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { librariesQuery } from '#lib/features/library/queries.ts';

/** Hovering the link starts loading what the page opens with. */
export const load = prefetchPage((scope) => [librariesQuery(scope)]);
