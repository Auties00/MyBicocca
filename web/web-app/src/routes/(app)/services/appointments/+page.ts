import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { reservationsQuery } from '#lib/features/appointments/reservations.ts';

/** Hovering the link starts loading what the page opens with. */
export const load = prefetchPage((scope) => [reservationsQuery(scope)]);
