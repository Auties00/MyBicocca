import { createContext } from 'svelte';
import { buildings, densestCity, type Building, type BuildingCategory } from './catalog.ts';
import type { RoomsCatalog } from './rooms.ts';
import { mapPlaces, searchPlaces, type MapPlace } from './search.ts';

export interface Insets {
	top: number;
	right: number;
	bottom: number;
	left: number;
}

export type SearchHit = { place: MapPlace; score: number };

/** How long typing pauses before the camera moves to what the search found. */
const SETTLE_MS = 450;

/** What the parts of the map page share. Create it while a component initializes. */
export class MapState {
	readonly #filter: () => readonly BuildingCategory[];
	readonly #scrollable: () => boolean;
	readonly #rooms: () => RoomsCatalog | undefined;
	#search = $state('');
	#settled = $state('');
	#timer: ReturnType<typeof setTimeout> | undefined;

	/** The building a pin or a list row is pointed at or focused: its pin and its row light up. */
	hovered = $state<string>();
	/** The list row pointed at or focused, by place id: a room row lights alone, not its siblings. */
	hoveredRow = $state<string>();
	/**
	 * While going up a level: the level arrived at and the place left (`building:U18`), whose row
	 * takes focus and comes into view there.
	 */
	returning: { level: string; from: string } | undefined;
	/** The scroll offset of each level the user left, by level key; read back once, never shown. */
	readonly #scrolls: Record<string, number> = {};

	/** The category filter from the URL; empty shows every building. */
	readonly filter = $derived.by(() => this.#filter());
	/** Whether a level's body scrolls: on phones only once the sheet is raised. */
	readonly scrollable = $derived.by(() => this.#scrollable());
	/** Every building, and every room once the room list is in: built once per list. */
	readonly places = $derived.by(() => mapPlaces(this.#rooms()));
	readonly query = $derived(this.#search.trim());
	/** What the search finds among the filtered buildings, best first. */
	readonly hits = $derived.by(() => this.#find(this.query));
	/** The filtered buildings, all of them when no category is picked. */
	readonly shown = $derived.by(() =>
		this.filter.length === 0
			? buildings
			: buildings.filter((entry) => this.filter.includes(entry.category))
	);
	/**
	 * The buildings the camera frames while none is open: what the search found once typing
	 * pauses, otherwise the filtered ones; in either case only the city with most of them.
	 */
	readonly framed = $derived.by((): Building[] => {
		const settled = this.#settled.trim();
		if (!settled) return densestCity(this.shown);
		const found = settled === this.query ? this.hits : this.#find(settled);
		return densestCity(
			buildings.filter((entry) => found.some((hit) => hit.place.building === entry))
		);
	});

	/** `search` is what the directory opens searching for, already settled. */
	constructor(
		filter: () => readonly BuildingCategory[],
		scrollable: () => boolean,
		rooms: () => RoomsCatalog | undefined,
		search = ''
	) {
		this.#filter = filter;
		this.#scrollable = scrollable;
		this.#rooms = rooms;
		this.#search = search;
		this.#settled = search;
		$effect(() => () => clearTimeout(this.#timer));
	}

	rememberScroll(level: string, top: number) {
		this.#scrolls[level] = top;
	}

	/** Where the level's body was scrolled, when the user is coming back up to it. */
	scrollToRestore(level: string): number | undefined {
		return this.returning?.level === level ? this.#scrolls[level] : undefined;
	}

	/** The directory's search, kept while a building is open. */
	get search(): string {
		return this.#search;
	}

	set search(value: string) {
		this.#search = value;
		clearTimeout(this.#timer);
		if (value.trim() === '') this.#settled = '';
		else this.#timer = setTimeout(() => (this.#settled = value), SETTLE_MS);
	}

	#find(query: string): SearchHit[] {
		if (!query) return [];
		return searchPlaces(query, this.places).filter(
			(hit) => this.filter.length === 0 || this.filter.includes(hit.place.building.category)
		);
	}
}

export const [getMapView, setMapView] = createContext<MapState>();
