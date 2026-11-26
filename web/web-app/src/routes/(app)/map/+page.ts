import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { roomsCatalogQuery } from '#lib/features/map/queries.ts';

/** Hovering the link starts loading what the page opens with. */
export const load = prefetchPage((scope) => [roomsCatalogQuery(scope)]);
