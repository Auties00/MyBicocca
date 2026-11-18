import type {
	EasyStaffRoom,
	EasyStaffRoomShowcaseBuilding,
	EasyStaffRoomShowcaseRoom
} from '@mybicocca/data-api/easystaff';
import { buildings, type Building } from './catalog.ts';
import { roomName } from './names.ts';
import { parseRoomFloor } from './text.ts';

/** Every room EasyStaff knows, by the code of the site it lists it under. */
export type RoomsCatalog = Record<string, EasyStaffRoom[]>;

/** The showcase of a building's sites, by site code. */
export type RoomShowcase = Record<string, EasyStaffRoomShowcaseBuilding>;

export interface MapRoom {
	code: string;
	name: string;
	/** Seats; null when unknown. */
	capacity: number | null;
	/** The catalogue building it stands in. */
	building: string;
	/** Null until the showcase tells it, or when the staff left it blank. */
	floor: number | null;
}

const catalogCodes = new Set(buildings.map((entry) => entry.code));

/**
 * The EasyStaff sites a building's rooms are listed under: its own code plus the departments'
 * sites inside it ("U06-GIURISPRUDENZA" is in U06), sorted so the list is a stable cache key.
 */
export function buildingSites(building: Building, catalog: RoomsCatalog): string[] {
	return Object.keys(catalog)
		.filter(
			(site) =>
				site === building.code || (site.startsWith(`${building.code}-`) && !catalogCodes.has(site))
		)
		.sort();
}

const collator = new Intl.Collator('it', { numeric: true, ignorePunctuation: true });

/** The building's rooms, by name in natural order ("U1-2" before "U1-10"). */
export function buildingRooms(building: Building, catalog: RoomsCatalog): MapRoom[] {
	const seen = new Set<string>();
	const rooms: MapRoom[] = [];
	for (const site of buildingSites(building, catalog)) {
		for (const room of catalog[site]) {
			if (room.code === 'all' || seen.has(room.code)) continue;
			seen.add(room.code);
			rooms.push({
				code: room.code,
				name: roomName(room.name),
				capacity: room.capacity !== null && room.capacity > 0 ? room.capacity : null,
				building: building.code,
				floor: null
			});
		}
	}
	return rooms.sort((a, b) => collator.compare(a.name, b.name));
}

export function showcaseRooms(showcase: RoomShowcase): Map<string, EasyStaffRoomShowcaseRoom> {
	const rooms = new Map<string, EasyStaffRoomShowcaseRoom>();
	for (const site of Object.values(showcase)) {
		for (const room of site.rooms) if (room.code) rooms.set(room.code, room);
	}
	return rooms;
}

/** The rooms with the floor (and the seats, where the listing has none) the showcase gives. */
export function withShowcase(
	rooms: readonly MapRoom[],
	showcase: ReadonlyMap<string, EasyStaffRoomShowcaseRoom>
): MapRoom[] {
	return rooms.map((room) => {
		const entry = showcase.get(room.code);
		if (!entry) return room;
		const capacity = entry.capacity !== null && entry.capacity > 0 ? entry.capacity : null;
		return { ...room, floor: parseRoomFloor(entry.floor), capacity: room.capacity ?? capacity };
	});
}

export interface FloorGroup {
	/** Null for the rooms whose floor isn't known. */
	floor: number | null;
	rooms: MapRoom[];
}

/** Rooms by floor, lowest first, the ones without a floor last. */
export function groupByFloor(rooms: readonly MapRoom[]): FloorGroup[] {
	const groups = new Map<number | null, MapRoom[]>();
	for (const room of rooms) {
		const list = groups.get(room.floor);
		if (list) list.push(room);
		else groups.set(room.floor, [room]);
	}
	return [...groups]
		.sort(([a], [b]) => (a === null ? 1 : b === null ? -1 : a - b))
		.map(([floor, list]) => ({ floor, rooms: list }));
}
