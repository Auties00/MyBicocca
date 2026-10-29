import type { EasyStaffRoomOccupationEvent } from '@mybicocca/data-api/easystaff';
import { LocalDate } from '@mybicocca/data-api/runtime';
import { publicKey } from '#lib/data/keys.ts';
import { policies, type QueryOptions } from '#lib/data/query.svelte.ts';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import type { RoomShowcase, RoomsCatalog } from './rooms.ts';

/**
 * Every room of every site in one script (`combo.php`): the map, the search and any page naming
 * a room read this one entry.
 */
export function roomsCatalogQuery(scope: SessionScope): QueryOptions<RoomsCatalog> {
	return {
		key: publicKey('easystaff.buildings.getRooms'),
		load: () => scope.api.easyStaff.buildings.getRooms(),
		policy: policies.shared(10)
	};
}

/**
 * The showcase of a building's sites: floors, seats, equipment, accessibility of every room. One
 * page per building, so opening a room needs no further request.
 */
export function roomShowcaseQuery(
	scope: SessionScope,
	sites: readonly string[]
): QueryOptions<RoomShowcase> {
	return {
		key: publicKey('easystaff.buildings.getRoomDetails', sites.join(',')),
		load: () => scope.api.easyStaff.buildings.getRoomDetails([...sites]),
		policy: policies.shared(60)
	};
}

/** What is booked in a building's rooms on `day` (`yyyy-MM-dd`): live, so kept briefly. */
export function occupationQuery(
	scope: SessionScope,
	sites: readonly string[],
	day: string
): QueryOptions<EasyStaffRoomOccupationEvent[]> {
	return {
		key: publicKey('easystaff.buildings.getBuildingOccupation', sites.join(','), day),
		load: () => {
			const date = LocalDate.parse(day);
			if (!date) throw new RangeError(`Not a date: ${day}`);
			return scope.api.easyStaff.buildings.getBuildingOccupation([...sites], date);
		},
		policy: policies.memory(2)
	};
}
