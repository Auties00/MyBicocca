import type { EasyStaffPlanningService } from '@mybicocca/data-api/easystaff';
import { query, type Query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { servicesQuery } from './queries.ts';

/** The office directory, for a component. */
export function useServices(): Query<EasyStaffPlanningService[]> {
	return query(() => session.current && servicesQuery(session.current));
}
