import { resolve } from '$app/paths';
import type { ResolvedPathname } from '$app/types';
import type { ExternalHref } from '#lib/ui/types.ts';
import { categories, type Building, type BuildingCategory } from './catalog.ts';

/** The category filter in a map URL (`?filter=teaching,sport`); empty means every category. */
export function parseFilter(search: { get(name: string): string | null }): BuildingCategory[] {
	const wanted = new Set(search.get('filter')?.split(',') ?? []);
	return categories.filter((category) => wanted.has(category));
}

function withFilter(path: string, filter: readonly BuildingCategory[]): ResolvedPathname {
	return (filter.length ? `${path}?filter=${filter.join(',')}` : path) as ResolvedPathname;
}

export function mapHref(filter: readonly BuildingCategory[] = []): ResolvedPathname {
	return withFilter(resolve('map'), filter);
}

/** The map with its directory searching for `query` (`?q=aula`), as global search hands it on. */
export function mapSearchHref(query: string): ResolvedPathname {
	return `${resolve('map')}?q=${encodeURIComponent(query)}` as ResolvedPathname;
}

/** The search a map URL starts with. */
export function parseQuery(search: { get(name: string): string | null }): string {
	return search.get('q') ?? '';
}

export function buildingHref(
	building: string,
	filter: readonly BuildingCategory[] = []
): ResolvedPathname {
	return withFilter(
		resolve('/(app)/map/[building]', { building: encodeURIComponent(building) }),
		filter
	);
}

export function roomHref(
	building: string,
	room: string,
	filter: readonly BuildingCategory[] = []
): ResolvedPathname {
	return withFilter(
		resolve('/(app)/map/[building]/[room]', {
			building: encodeURIComponent(building),
			room: encodeURIComponent(room)
		}),
		filter
	);
}

/** Directions to the building in Google Maps, which opens the app where there is one. */
export function directionsHref(building: Building): ExternalHref {
	return `https://www.google.com/maps/dir/?api=1&destination=${building.latitude},${building.longitude}`;
}
