import { prefetchPage } from '#lib/data/prefetchPage.ts';
import { recordBookQuestionnairesQuery } from '#lib/features/questionnaires/queries.ts';

/** Hovering the link starts loading what the page opens with. */
export const load = prefetchPage((scope) => [recordBookQuestionnairesQuery(scope)]);
