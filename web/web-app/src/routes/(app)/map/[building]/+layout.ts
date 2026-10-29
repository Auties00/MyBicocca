import { redirect } from '@sveltejs/kit';
import { prefetch } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { findBuilding } from '#lib/features/map/catalog.ts';
import { buildingHref, parseFilter, roomHref } from '#lib/features/map/links.ts';
import { roomsCatalogQuery } from '#lib/features/map/queries.ts';
import type { LayoutLoad } from './$types';

/**
 * Other names for a building (its old name "U6", any case, a department site "U14-DISCO") lead
 * to its own URL. Hovering a building link already starts loading the room list.
 */
export const load: LayoutLoad = ({ params, url }) => {
	const building = findBuilding(params.building);
	if (building && building.code !== params.building) {
		const filter = parseFilter(url.searchParams);
		redirect(
			308,
			params.room
				? roomHref(building.code, params.room, filter)
				: buildingHref(building.code, filter)
		);
	}
	const scope = session.current;
	if (scope) void prefetch(roomsCatalogQuery(scope));
};
