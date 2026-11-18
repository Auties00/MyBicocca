import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import { findBuilding } from '#lib/features/map/catalog.ts';
import { buildingHref, roomHref } from '#lib/features/map/links.ts';
import { buildingParts } from './model.ts';

/** How the booking page shows the calls when it has one pane: by course, or on a calendar. */
export type BookView = 'list' | 'calendar';

/**
 * The booking page, narrowed to a course (`?course=`), with a call open (`?call=`), on the
 * calendar (`?view=calendar`).
 */
export function bookHref(
	options: { course?: string | null; call?: string | null; view?: BookView } = {}
): ResolvedPathname {
	const params = new URLSearchParams();
	if (options.course) params.set('course', options.course);
	if (options.call) params.set('call', options.call);
	if (options.view === 'calendar') params.set('view', 'calendar');
	const query = params.toString();
	const path = resolve('services/exams/book');
	return (query ? `${path}?${query}` : path) as ResolvedPathname;
}

/** One booking: its day's confirmation, or its outcome once sat. */
export function bookingHref(slug: string): ResolvedPathname {
	return resolve('/(app)/services/exams/[id]', { id: slug });
}

/**
 * Where the exam is held on the campus map: the room, else its building; null where the map
 * doesn't know the building. Esse3 names rooms after their building ("U4-08").
 */
export function placeHref(room: string | null, building: string | null): ResolvedPathname | null {
	const site =
		(room ? findBuilding(room) : undefined) ??
		(building ? findBuilding(buildingParts(building).code) : undefined);
	if (!site) return null;
	return room ? roomHref(site.code, room) : buildingHref(site.code);
}
