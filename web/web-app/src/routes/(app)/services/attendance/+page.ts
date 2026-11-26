import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { attendanceQuery } from '#lib/features/attendance/queries.ts';

/** Hovering the link starts loading what the page opens with. */
export const load = prefetchPage((scope) => [attendanceQuery(scope)]);
