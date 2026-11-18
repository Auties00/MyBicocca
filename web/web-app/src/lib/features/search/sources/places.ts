import IconBuildings from '~icons/ph/buildings';
import IconDoor from '~icons/ph/door';
import IconMapTrifold from '~icons/ph/map-trifold';
import { mapSearchHref } from '#lib/features/map/links.ts';
import type { RoomsCatalog } from '#lib/features/map/rooms.ts';
import { mapPlaces, type MapPlace } from '#lib/features/map/search.ts';
import { foldQuery } from '#lib/features/elearning/search/fold.ts';
import { m } from '#lib/paraglide/messages.js';
import type { PaletteItem } from '#lib/shell/palette.svelte.ts';
import { category } from '../categories.ts';
import { searchDoc, type SearchDoc } from '../rank.ts';

const titles = new Intl.Collator(undefined, { numeric: true, sensitivity: 'base' });

/**
 * Among places a query matches as well: the buildings in the campus order, then the rooms with
 * the plainest names ("Aula 23" before "Aula 2151 (Sala Lauree)").
 */
function plainestFirst(a: MapPlace, b: MapPlace): number {
	if (a.kind === 'building' || b.kind === 'building') {
		return a.kind === b.kind ? 0 : a.kind === 'building' ? -1 : 1;
	}
	return a.title.length - b.title.length || titles.compare(a.title, b.title);
}

/** The campus buildings, and their rooms once EasyStaff's list is in. */
export function placeDocs(rooms: RoomsCatalog | undefined): SearchDoc[] {
	const places = category('places');
	return mapPlaces(rooms)
		.sort(plainestFirst)
		.map((place) =>
			searchDoc(
				{
					id: place.id,
					title: place.title,
					subtitle: place.subtitle,
					icon: place.kind === 'building' ? IconBuildings : IconDoor,
					href: place.href,
					...places
				},
				place.keywords
			)
		);
}

/** The same words in the map's directory, for a query that names more places than a group shows. */
export function mapRow(query: string): PaletteItem {
	return {
		id: `map:${foldQuery(query)}`,
		title: m.search_map_title({ query }),
		subtitle: m.search_map_subtitle(),
		icon: IconMapTrifold,
		href: mapSearchHref(query.trim()),
		fallback: true,
		...category('elsewhere')
	};
}
