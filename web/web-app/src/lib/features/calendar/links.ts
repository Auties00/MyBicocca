import type { ResolvedPathname } from '$app/types';
import { resolve } from '$app/paths';
import { bookingHref } from '#lib/features/exams/links.ts';
import { findBuilding } from '#lib/features/map/catalog.ts';
import { buildingHref, roomHref } from '#lib/features/map/links.ts';
import type { ExternalHref } from '#lib/ui/types.ts';
import type { CalendarEvent, EventLocation } from './model.ts';

/** EasyStaff's embedded map writes the place as `!2d<longitude>!3d<latitude>`. */
const EMBED_COORDINATES = /!2d(-?\d+(?:\.\d+)?)!3d(-?\d+(?:\.\d+)?)/;

/**
 * Google Maps on the event's place: its coordinates when EasyStaff's embedded map gives them, the
 * embedded map itself otherwise, or a search for the building and room. Null without a place.
 */
export function mapsLink(location: EventLocation | null): ExternalHref | null {
	if (!location) return null;
	const coordinates = location.mapsUrl ? EMBED_COORDINATES.exec(location.mapsUrl) : null;
	if (coordinates) {
		return `https://www.google.com/maps/search/?api=1&query=${coordinates[2]},${coordinates[1]}`;
	}
	if (location.mapsUrl?.startsWith('https://')) return location.mapsUrl as ExternalHref;
	const label = [location.building, location.room].filter(Boolean).join(' ');
	if (!label) return null;
	return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(`${label} Università di Milano-Bicocca`)}`;
}

/**
 * The place on the campus map: the room when there is one, else the building. Null when the
 * building isn't one the map knows (a library seat's site, an appointment's office address).
 */
export function campusMapLink(location: EventLocation | null): ResolvedPathname | null {
	const building = location?.building ? findBuilding(location.building) : undefined;
	if (!building) return null;
	return location?.room ? roomHref(building.code, location.room) : buildingHref(building.code);
}

export const links = {
	course: (courseId: number) => resolve('/(app)/elearning/[course]', { course: String(courseId) }),
	assignment: (courseId: number, assignmentId: number) =>
		resolve('/(app)/elearning/[course]/assignment/[assign]', {
			course: String(courseId),
			assign: String(assignmentId)
		}),
	/** The booked exam's page: its slug is the call key, as `exam_<cds>_<ad>_<app>` holds it. */
	exam: (event: CalendarEvent) => bookingHref(event.id.split('_').slice(1).join('-')),
	library: () => resolve('/(app)/services/library'),
	appointments: () => resolve('/(app)/services/appointments')
};
