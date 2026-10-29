import type {
	EasyStaffRoomOccupationEvent,
	EasyStaffRoomShowcaseRoom
} from '@mybicocca/data-api/easystaff';
import { now } from '#lib/data/clock.svelte.ts';
import { query, type Query } from '#lib/data/query.svelte.ts';
import { session } from '#lib/data/session.svelte.ts';
import { romeDay } from '#lib/format/rome.ts';
import type { Building } from './catalog.ts';
import { occupationQuery, roomShowcaseQuery } from './queries.ts';
import {
	buildingRooms,
	buildingSites,
	showcaseRooms,
	withShowcase,
	type MapRoom,
	type RoomShowcase,
	type RoomsCatalog
} from './rooms.ts';
import { roomStatus, scheduleByRoom, type RoomStatus, type ScheduleEntry } from './status.ts';

/**
 * A building's rooms with their floors, and today's bookings: what the building and room pages
 * show. Create it while a component initializes.
 */
export class BuildingData {
	/** The EasyStaff sites the building's rooms are listed under; undefined until the list loads. */
	readonly sites: string[] | undefined;
	readonly showcase: Query<RoomShowcase>;
	readonly occupation: Query<EasyStaffRoomOccupationEvent[]>;
	/** The rooms by name, with floors once the showcase is in. */
	readonly rooms: MapRoom[] | undefined;
	/** Today's bookings by room; undefined until they load or when they failed. */
	readonly schedule: Map<string, ScheduleEntry[]> | undefined;
	readonly #details: Map<string, EasyStaffRoomShowcaseRoom>;

	constructor(building: () => Building | undefined, catalog: () => RoomsCatalog | undefined) {
		this.sites = $derived.by(() => {
			const entry = building();
			const rooms = catalog();
			return entry && rooms ? buildingSites(entry, rooms) : undefined;
		});
		const day = $derived(romeDay(now.current.getTime()));
		this.showcase = query(() => {
			const scope = session.current;
			const sites = this.sites;
			return scope && sites && sites.length > 0 && roomShowcaseQuery(scope, sites);
		});
		this.occupation = query(() => {
			const scope = session.current;
			const sites = this.sites;
			return scope && sites && sites.length > 0 && occupationQuery(scope, sites, day);
		});
		this.#details = $derived(showcaseRooms(this.showcase.data ?? {}));
		this.rooms = $derived.by(() => {
			const entry = building();
			const rooms = catalog();
			return entry && rooms ? withShowcase(buildingRooms(entry, rooms), this.#details) : undefined;
		});
		this.schedule = $derived(
			this.occupation.data ? scheduleByRoom(this.occupation.data) : undefined
		);
	}

	/** The showcase's entry for a room, with its description, equipment and accessibility. */
	details(code: string): EasyStaffRoomShowcaseRoom | undefined {
		return this.#details.get(code);
	}

	/** Whether the room is free now; undefined while the bookings load. */
	status(code: string): RoomStatus | undefined {
		if (this.schedule) return roomStatus(this.schedule.get(code) ?? [], now.current.getTime());
		return this.occupation.error ? { kind: 'unknown' } : undefined;
	}
}
