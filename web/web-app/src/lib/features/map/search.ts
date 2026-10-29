import type { ResolvedPathname } from '$app/types';
import { matchScore } from '#lib/shell/match.ts';
import { buildings, type Building } from './catalog.ts';
import { buildingHref, roomHref } from './links.ts';
import { buildingAlias, buildingName, buildingTitle } from './names.ts';
import { buildingRooms, type MapRoom, type RoomsCatalog } from './rooms.ts';

/**
 * A building or a room as search lists it. The global search builds its index from
 * `mapPlaces(rooms)`, with the rooms of `roomsCatalogQuery` (none until they load).
 */
export type MapPlace =
	| {
			kind: 'building';
			/** `building:U01`, stable across sessions. */
			id: string;
			title: string;
			subtitle: string;
			keywords: string[];
			href: ResolvedPathname;
			building: Building;
	  }
	| {
			kind: 'room';
			/** `room:U01/U1-03`. */
			id: string;
			title: string;
			subtitle: string;
			keywords: string[];
			href: ResolvedPathname;
			building: Building;
			room: MapRoom;
	  };

export const buildingPlaceId = (code: string) => `building:${code}`;

export const roomPlaceId = (room: MapRoom) => `room:${room.building}/${room.code}`;

export function mapPlaces(rooms?: RoomsCatalog): MapPlace[] {
	const places: MapPlace[] = [];
	for (const building of buildings) {
		const alias = buildingAlias(building);
		places.push({
			kind: 'building',
			id: buildingPlaceId(building.code),
			title: buildingTitle(building),
			subtitle: `${building.address}, ${building.city}`,
			keywords: [
				building.code,
				buildingName(building),
				building.name,
				building.city,
				...(alias ? [alias] : [])
			],
			href: buildingHref(building.code),
			building
		});
	}
	if (!rooms) return places;
	for (const building of buildings) {
		const title = buildingTitle(building);
		for (const room of buildingRooms(building, rooms)) {
			places.push({
				kind: 'room',
				id: roomPlaceId(room),
				title: room.name,
				subtitle: title,
				keywords: [room.code, `${room.name} ${title}`],
				href: roomHref(building.code, room.code),
				building,
				room
			});
		}
	}
	return places;
}

/** The places matching `query`, best first; on ties, buildings (listed first) before rooms. */
export function searchPlaces(
	query: string,
	places: readonly MapPlace[]
): Array<{ place: MapPlace; score: number }> {
	return places
		.map((place) => ({ place, score: matchScore(query, place) }))
		.filter((hit) => hit.score > 0)
		.sort((a, b) => b.score - a.score);
}
