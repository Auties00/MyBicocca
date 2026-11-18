import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';

export function questionnairesHref(): ResolvedPathname {
	return resolve('/(app)/services/questionnaires');
}

export function activityHref(activity: number): ResolvedPathname {
	return resolve('/(app)/services/questionnaires/[activity]', { activity: String(activity) });
}

/** The compilation of one of an activity's units, by its 1-based position. */
export function compileHref(activity: number, unit: number): ResolvedPathname {
	return resolve('/(app)/services/questionnaires/[activity]/[unit]', {
		activity: String(activity),
		unit: String(unit)
	});
}

/** A whole number from a route segment, or undefined for anything else. */
export function routeNumber(segment: string | undefined): number | undefined {
	return segment !== undefined && /^\d{1,12}$/.test(segment) ? Number(segment) : undefined;
}
