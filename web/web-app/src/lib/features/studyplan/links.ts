import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';

/** An exam of the plan, opened inside Study plan (`adsceId` of its record-book row). */
export function studyPlanExamHref(id: number): ResolvedPathname {
	return resolve(`services/study-plan/${id}`);
}
